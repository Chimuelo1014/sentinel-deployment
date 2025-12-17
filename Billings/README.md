# Billings

# Sentinel Billing Service

Servicio encargado de gestionar pagos, planes, suscripciones y facturación dentro del ecosistema Sentinel.

##  Funcionalidad principal

###  Pasarelas de Pago
- **MercadoPago**
  - Preferencias de pago
  - Captura
  - Webhooks
  - Validación de firma (HMAC)

- **PayPal**
  - Orders API
  - Captura de pago
  - Webhooks
  - Validación JWT + firma

- **Crypto**
  - USDT/USDC (ERC20 o POS)
  - BTC Lightning (via BTCPayServer)
  - Webhooks para registros on-chain

---

## Suscripciones y Facturación
- Creación de suscripciones
- Renovación automática
- Reintentos de cobro
- Cancelación
- Historial de facturación
- Facturas digitales
- Registro en blockchain (opcional)

---
