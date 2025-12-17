package com.sentinel.billing.webhook;

import com.sentinel.billing.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Webhook de PayPal (MOCK, estilo realista).
 */
@RestController
@RequestMapping("/api/webhooks/paypal")
public class PaypalWebhookController {

    private static final Logger log = LoggerFactory.getLogger(PaypalWebhookController.class);

    private final PaymentService paymentService;

    public PaypalWebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestHeader MultiValueMap<String, String> headers,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        log.info("=== Webhook de PayPal recibido ===");
        log.info("Headers: {}", headers);
        log.info("Body (map): {}", body);

        // En PayPal real, normalmente usarías:
        //  - 'resource.id' para consultar la orden/pago.
        //  - 'resource.custom_id' o 'resource.supplementary_data' como metadata.
        //
        // Para el MOCK:
        //  - Aceptamos 'planId' directo.
        //  - O 'resource.custom_id = BASIC|STANDARD|PRO|ENTERPRISE'.
        String planId = resolvePlanIdFromBody(body);

        paymentService.confirmMockPayment("PAYPAL", planId);

        String message = "Webhook de PayPal procesado (MOCK) para plan " + planId;
        return ResponseEntity.ok(message);
    }

    @SuppressWarnings("unchecked")
    private String resolvePlanIdFromBody(Map<String, Object> body) {
        if (body == null || body.isEmpty()) {
            log.warn("Body nulo o vacío en webhook de PayPal. Usando plan STANDARD por defecto.");
            return "STANDARD";
        }

        // 1) Intento directo: { "planId": "PRO" }
        Object direct = body.get("planId");
        if (direct instanceof String s && !s.isBlank()) {
            log.info("planId leído directamente del body: {}", s);
            return s;
        }

        // 2) Estilo aproximado de webhook de PayPal:
        // {
        //   "resource": {
        //     "custom_id": "PRO"
        //   }
        // }
        Object resourceObj = body.get("resource");
        if (resourceObj instanceof Map<?, ?> resRaw) {
            Map<String, Object> resource = (Map<String, Object>) resRaw;
            Object customId = resource.get("custom_id");
            if (customId instanceof String s && !s.isBlank()) {
                log.info("planId leído desde resource.custom_id: {}", s);
                return s;
            }
        }

        log.warn("No se encontró planId en el webhook de PayPal. Usando plan STANDARD por defecto.");
        return "STANDARD";
    }
}
