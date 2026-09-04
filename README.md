# FX Order Processor

Spring Boot service that processes multi-currency orders, applies live FX exchange rates with fallback caching, and calculates tiered processing fees.

## Features

- **Multi-currency Order Processing**: Converts line items from source currency to target currency.
- **FX Rates with Fallback**: Live rate fetching from Frankfurter v2 with in-memory caching (Caffeine), local PostgreSQL database fallback snapshots, and static baseline fallbacks.
- **Tiered Processing Fee**:
  - Orders under $1,000 USD equivalent: 1.5% fee
  - Orders $1,000 USD equivalent and above: 0.5% fee
- **Customer Reporting**: Summary endpoint aggregating total orders, lifetime spend, and cumulative fees paid.

## Tech Stack

- Java 17
- Spring Boot 3.5
- Spring Data JPA / Hibernate
- PostgreSQL
- Caffeine Cache
- Spring Web (RestClient)

## Database Setup

1. Make sure PostgreSQL is running and create the database:
   ```sql
   CREATE DATABASE fx_orders_db;
   ```

2. Run the user provisioning script:
   ```bash
   psql -U postgres -d fx_orders_db -f src/main/resources/setup_app_user.sql
   ```

3. Initialize the schema and baseline snapshot rates:
   ```bash
   psql -U app_user -d fx_orders_db -f src/main/resources/schema.sql
   ```

## Configuration

Database credentials and service properties are defined in `src/main/resources/application.properties`. You can override them using environment variables:

- `DB_URL` (default: `jdbc:postgresql://localhost:5432/fx_orders_db`)
- `DB_USERNAME` (default: `app_user`)
- `DB_PASSWORD` (default: `AppUserSecure2026!`)

## Build & Run

Run the application:
```bash
./mvnw spring-boot:run
```

The application starts on `http://localhost:8080`.

## API Endpoints

### 1. Create Order
`POST /api/v1/orders`

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-1001",
    "sourceCurrency": "EUR",
    "targetCurrency": "USD",
    "items": [
      {
        "description": "Enterprise Cloud Subscription",
        "quantity": 2,
        "unitPrice": 450.00
      },
      {
        "description": "Onboarding Consultation",
        "quantity": 1,
        "unitPrice": 200.00
      }
    ]
  }'
```

### 2. Get Order by ID
`GET /api/v1/orders/{orderId}`

```bash
curl http://localhost:8080/api/v1/orders/d08d80e3-ec42-4767-8326-ef83bacdd76b
```

### 3. Customer Summary
`GET /api/v1/orders/summary?customerId={customerId}`

```bash
curl "http://localhost:8080/api/v1/orders/summary?customerId=CUST-1001"
```
