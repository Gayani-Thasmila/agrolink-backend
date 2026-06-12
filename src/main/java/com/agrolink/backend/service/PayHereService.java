package com.agrolink.backend.service;

import com.agrolink.backend.config.PayHereProperties;
import com.agrolink.backend.dto.PayHereCheckoutResponse;
import com.agrolink.backend.dto.PayHereRequest;
import com.agrolink.backend.model.Order;
import com.agrolink.backend.model.Payment;
import com.agrolink.backend.repository.OrderRepository;
import com.agrolink.backend.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class PayHereService {

    private static final String PAYHERE_METHOD = "PAYHERE";
    private static final String PAYHERE_SUCCESS_CODE = "2";
    private static final String SANDBOX_CHECKOUT_URL = "https://sandbox.payhere.lk/pay/checkout";
    private static final String LIVE_CHECKOUT_URL = "https://www.payhere.lk/pay/checkout";
    private static final Pattern MD5_HEX_PATTERN = Pattern.compile("^[A-Fa-f0-9]{32}$");

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PayHereProperties payHereProperties;

    public PayHereService(OrderRepository orderRepository,
                          PaymentRepository paymentRepository,
                          PayHereProperties payHereProperties) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.payHereProperties = payHereProperties;
    }

    @Transactional
    public PayHereCheckoutResponse createCheckoutSession(PayHereRequest request) {
        validateMerchantConfiguration();

        if (request.getOrderId() == null) {
            throw new IllegalArgumentException("orderId is required");
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found for id: " + request.getOrderId()));

        String orderId = String.valueOf(order.getId());
        String currency = normalizeCurrency(request.getCurrency());
        String amount = formatAmount(order.getTotalPrice());
        String items = isBlank(request.getItems()) ? "Agrolink Order #" + orderId : request.getItems().trim();

        Payment payment = paymentRepository.findTopByOrderIdOrderByIdDesc(order.getId()).orElseGet(Payment::new);
        payment.setOrder(order);
        payment.setMethod(PAYHERE_METHOD);
        payment.setStatus("PENDING");
        payment.setPaymentDate(new Date());
        payment.setAmount(BigDecimal.valueOf(order.getTotalPrice()).setScale(2, RoundingMode.HALF_UP));
        payment.setCurrency(currency);
        paymentRepository.save(payment);

        return PayHereCheckoutResponse.builder()
                .sandbox(payHereProperties.isSandbox())
                .checkoutUrl(payHereProperties.isSandbox() ? SANDBOX_CHECKOUT_URL : LIVE_CHECKOUT_URL)
                .merchantId(payHereProperties.getMerchantId())
                .returnUrl(payHereProperties.getReturnUrl())
                .cancelUrl(payHereProperties.getCancelUrl())
                .notifyUrl(payHereProperties.getNotifyUrl())
                .orderId(orderId)
                .items(items)
                .currency(currency)
                .amount(amount)
                .firstName(trimToEmpty(request.getFirstName()))
                .lastName(trimToEmpty(request.getLastName()))
                .email(trimToEmpty(request.getEmail()))
                .phone(trimToEmpty(request.getPhone()))
                .address(trimToEmpty(request.getAddress()))
                .city(trimToEmpty(request.getCity()))
                .country(trimToEmpty(request.getCountry(), "Sri Lanka"))
                .hash(generateCheckoutHash(orderId, amount, currency))
                .build();
    }

    @Transactional
    public void handleNotification(Map<String, String> payload) {
        validateMerchantConfiguration();

        String orderId = payload.get("order_id");
        if (isBlank(orderId)) {
            throw new IllegalArgumentException("order_id is required");
        }

        Order order = orderRepository.findById(Integer.parseInt(orderId))
                .orElseThrow(() -> new IllegalArgumentException("Order not found for id: " + orderId));

        if (!isValidNotification(payload)) {
            throw new IllegalArgumentException("Invalid PayHere notification signature");
        }

        Payment payment = paymentRepository.findTopByOrderIdOrderByIdDesc(order.getId()).orElseGet(Payment::new);
        payment.setOrder(order);
        payment.setMethod(PAYHERE_METHOD);
        payment.setPaymentDate(new Date());
        payment.setAmount(parseAmount(payload.get("payhere_amount")));
        payment.setCurrency(normalizeCurrency(payload.get("payhere_currency")));
        payment.setGatewayPaymentId(trimToNull(payload.get("payment_id")));
        payment.setReference(trimToNull(payload.get("md5sig")));

        String statusCode = payload.get("status_code");
        if (PAYHERE_SUCCESS_CODE.equals(statusCode)) {
            payment.setStatus("SUCCESS");
            order.setStatus("PAID");
        } else {
            payment.setStatus("FAILED");
            order.setStatus("PAYMENT_FAILED");
        }

        orderRepository.save(order);
        paymentRepository.save(payment);
    }

    private boolean isValidNotification(Map<String, String> payload) {
        String expected = generateNotificationHash(
                payload.get("merchant_id"),
                payload.get("order_id"),
                payload.get("payhere_amount"),
                payload.get("payhere_currency"),
                payload.get("status_code")
        );
        return expected.equalsIgnoreCase(trimToEmpty(payload.get("md5sig")));
    }

    private String generateCheckoutHash(String orderId, String amount, String currency) {
        return md5HexUpper(
                payHereProperties.getMerchantId()
                        + orderId
                        + amount
                        + currency
                        + getMerchantSecretMd5()
        );
    }

    private String generateNotificationHash(String merchantId,
                                            String orderId,
                                            String amount,
                                            String currency,
                                            String statusCode) {
        return md5HexUpper(
                trimToEmpty(merchantId)
                        + trimToEmpty(orderId)
                        + trimToEmpty(amount)
                        + trimToEmpty(currency)
                        + trimToEmpty(statusCode)
                        + getMerchantSecretMd5()
        );
    }

    private void validateMerchantConfiguration() {
        if (isBlank(payHereProperties.getMerchantId()) || isBlank(payHereProperties.getMerchantSecret())) {
            throw new IllegalStateException("PayHere merchant configuration is missing. Set PAYHERE_MERCHANT_ID and PAYHERE_MERCHANT_SECRET.");
        }
    }

    private String getMerchantSecretMd5() {
        String secret = trimToEmpty(payHereProperties.getMerchantSecret());
        if (MD5_HEX_PATTERN.matcher(secret).matches()) {
            return secret.toUpperCase(Locale.ROOT);
        }
        return md5HexUpper(secret);
    }

    private String formatAmount(double amount) {
        return String.format(Locale.US, "%.2f", amount);
    }

    private BigDecimal parseAmount(String amount) {
        if (isBlank(amount)) {
            return null;
        }
        return new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeCurrency(String currency) {
        return isBlank(currency) ? "LKR" : currency.trim().toUpperCase(Locale.ROOT);
    }

    private String md5HexUpper(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(trimToEmpty(value).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).toUpperCase(Locale.ROOT);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available", e);
        }
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToEmpty(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
