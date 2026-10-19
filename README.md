# SIGIP-Sales-Service

Gestión de pedidos y pagos para SIGIP.

- Java 25
- Spring Boot 3.5.6
- Spring Cloud 2025.0.0
- Configuración: application.properties
- Puerto local: 8095
- Base de datos: sigip_sales

Configura `DB_USERNAME` y `DB_PASSWORD` en el entorno local si no utilizas las credenciales por defecto. Requiere MySQL y Eureka (puerto 8761).

Endpoint de salud: `GET /api/sales/health` (consulta la ruta del controlador para el path exacto).