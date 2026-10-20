# sales-service

Ventas: pedidos, flujo de estados y pagos.

## Funcionalidad

- `GET|POST|PUT|DELETE /api/v1/pedidos` (el cliente ve y edita solo los suyos; editar/eliminar solo en REGISTRADO).
- El precio se obtiene del catálogo con OpenFeign (circuit breaker + fallback).
- Estados: REGISTRADO → PAGO_EN_REVISION → CONFIRMADO → EN_DISENO → EN_PRODUCCION → TERMINADO → ENTREGADO (o CANCELADO).
- `PATCH /api/v1/pedidos/{id}/estado` (ADMIN), `POST /{id}/cancelacion`, `POST|GET /{id}/pagos`, `POST /api/v1/pagos/{id}/verificacion` (ADMIN), `GET /pedidos/resumen` (ADMIN).
- Cada cambio de estado publica un evento en RabbitMQ después del commit.

## Pruebas

- `PedidoRepositoryTest`, `EstadoPedidoTest`, `CatalogServiceFallbackFactoryTest`
- `PedidoFlujoIntegrationTest` (flujo completo, propiedad, pagos)

## Ejecución

Puerto `8095`. Base local `sigip_sales` (MySQL); en la nube, esquema propio en PostgreSQL.

Variables: `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (o `DB_URL` completa).

```bash
mvn -pl backend/sales-service -am spring-boot:run
```
