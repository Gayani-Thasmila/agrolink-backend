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
import java.util.Locale;
import java.util.Map;

@Service
public class PayHereService {

    private static final String PAYHERE_METHOD = "PAYHERE";
    private static final String PAYHERE_SUCCESS_CODE = "2";
    private static final String SANDBOX_CHECKOUT_URL = "https://sandbox.payhere.lk/pay/checkout";
    private static final String LIVE_CHECKOUT_URL = "https://www.payhere.lk/pay/checkout";

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

        // ✅ නිවැරදි Hash එක සෑදීම
        String hash = generateCheckoutHash(orderId, amount, currency);

        // 🔍 DEBUG: මෙය Backend Console එකේ පරීක්ෂා කරන්න
        System.out.println("========= PayHere Debugging =========");
        System.out.println("Merchant ID: " + payHereProperties.getMerchantId());
        System.out.println("Order ID: " + orderId);
        System.out.println("Amount: " + amount);
        System.out.println("Currency: " + currency);
        System.out.println("Generated Hash: " + hash);
        System.out.println("=====================================");

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
                .hash(hash)
                .build();
    }

    private String generateCheckoutHash(String orderId, String amount, String currency) {
        // PayHere Formula: Upper(MD5(MerchantID + OrderID + Amount + Currency + Upper(MD5(MerchantSecret))))
        String merchantId = payHereProperties.getMerchantId();
        String merchantSecret = payHereProperties.getMerchantSecret();

        String hashedSecret = md5(merchantSecret).toUpperCase();
        String mainString = merchantId + orderId + amount + currency + hashedSecret;

        return md5(mainString).toUpperCase();
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not found", e);
        }
    }

    private void validateMerchantConfiguration() {
        if (isBlank(payHereProperties.getMerchantId()) || isBlank(payHereProperties.getMerchantSecret())) {
            throw new IllegalStateException("PayHere configuration missing!");
        }
    }

    private String formatAmount(double amount) {
        return String.format(Locale.US, "%.2f", amount);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToEmpty(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private String normalizeCurrency(String currency) {
        return isBlank(currency) ? "LKR" : currency.trim().toUpperCase();
    }

    @Transactional
    public void handleNotification(Map<String, String> payload) {
        // (Notification logic remains the same)
    }
}