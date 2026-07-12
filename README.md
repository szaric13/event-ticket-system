# Event Ticket Booking System

Mikroservisna aplikacija za rezervaciju karata sa atomskom Redis rezervacijom i asinhronim notifikacijama preko RabbitMQ-ja.

## Live Demo (Render)

| Servis | Swagger UI |
|--------|------------|
| Inventory | https://event-inventory-service.onrender.com/swagger-ui/index.html |
| Booking | https://booking-service-4f5b.onrender.com/swagger-ui/index.html |
| Notification | https://notification-service-2t3h.onrender.com/swagger-ui/index.html |

> Besplatni Render servisi spavaju posle 15 minuta neaktivnosti. Prvi zahtev može potrajati 30-60 sekundi.

## Lokalno pokretanje

```bash
git clone https://github.com/szaric13/event-ticket-system.git
cd event-ticket-system
docker compose up --build
