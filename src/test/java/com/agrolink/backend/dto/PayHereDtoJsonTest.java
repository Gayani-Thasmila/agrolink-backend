package com.agrolink.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PayHereDtoJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void checkoutResponseIncludesPayHereSnakeCaseFields() throws Exception {
        PayHereCheckoutResponse response = PayHereCheckoutResponse.builder()
                .sandbox(true)
                .checkoutUrl("https://sandbox.payhere.lk/pay/checkout")
                .merchantId("123")
                .returnUrl("https://example.com/success")
                .cancelUrl("https://example.com/cancel")
                .notifyUrl("https://example.com/notify")
                .orderId("55")
                .items("Agrolink Order #55")
                .currency("LKR")
                .amount("100.00")
                .firstName("Saman")
                .lastName("Perera")
                .email("saman@example.com")
                .phone("0712345678")
                .address("No 1")
                .city("Colombo")
                .country("Sri Lanka")
                .hash("ABC123")
                .build();

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertThat(json.get("checkoutUrl").asText()).isEqualTo("https://sandbox.payhere.lk/pay/checkout");
        assertThat(json.get("checkout_url").asText()).isEqualTo("https://sandbox.payhere.lk/pay/checkout");
        assertThat(json.get("merchantId").asText()).isEqualTo("123");
        assertThat(json.get("merchant_id").asText()).isEqualTo("123");
        assertThat(json.get("returnUrl").asText()).isEqualTo("https://example.com/success");
        assertThat(json.get("return_url").asText()).isEqualTo("https://example.com/success");
        assertThat(json.get("firstName").asText()).isEqualTo("Saman");
        assertThat(json.get("first_name").asText()).isEqualTo("Saman");
        assertThat(json.get("lastName").asText()).isEqualTo("Perera");
        assertThat(json.get("last_name").asText()).isEqualTo("Perera");
        assertThat(json.get("orderId").asText()).isEqualTo("55");
        assertThat(json.get("order_id").asText()).isEqualTo("55");
    }

    @Test
    void requestAcceptsSnakeCaseAliases() throws Exception {
        String payload = """
                {
                  "order_id": 55,
                  "first_name": "Saman",
                  "last_name": "Perera",
                  "currency": "LKR"
                }
                """;

        PayHereRequest request = objectMapper.readValue(payload, PayHereRequest.class);

        assertThat(request.getOrderId()).isEqualTo(55);
        assertThat(request.getFirstName()).isEqualTo("Saman");
        assertThat(request.getLastName()).isEqualTo("Perera");
        assertThat(request.getCurrency()).isEqualTo("LKR");
    }
}
