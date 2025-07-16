# Expense Service

## Overview
The Expense Service is a microservice designed to manage user expenses. It provides functionality for creating, updating, and retrieving expense records. The service receives expense data through both REST API endpoints and Kafka messages from other services.

## Architecture
The Expense Service follows a microservice architecture pattern and integrates with other services through Kafka messaging. It uses a MySQL database for persistent storage of expense data.

### Workflow
1. **Data Ingestion**: The service receives expense data through:
   - REST API endpoints for direct client interactions
   - Kafka messages from other services (like Data Service)

2. **Data Processing**:
   - Validates and processes expense data
   - Converts string amount to BigDecimal for calculations
   - Sets default currency (INR) if not provided
   - Ensures idempotency by checking external IDs

3. **Data Storage**:
   - Stores processed expense data in MySQL database
   - Generates UUIDs for expenses if not provided
   - Timestamps expense creation

4. **Data Retrieval**:
   - Provides API endpoints to retrieve expenses by user ID

### Component Diagram
```
┌─────────────┐     REST API     ┌─────────────────────┐
│  Client     │─────────────────▶│                     │
└─────────────┘                  │                     │
                                 │   Expense Service   │
┌─────────────┐     Kafka        │                     │
│ Data Service│─────────────────▶│                     │
└─────────────┘                  └─────────────────────┘
                                          │
                                          │
                                          ▼
                                 ┌─────────────────────┐
                                 │    MySQL Database   │
                                 └─────────────────────┘
```

## API Endpoints

### 1. Get User Expenses
- **Endpoint**: `GET /expense/v1/getExpense`
- **Headers**: `X-User-Id` (required)
- **Response**: List of expense DTOs for the specified user
- **Status Codes**:
  - 200 OK: Successfully retrieved expenses
  - 404 Not Found: User or expenses not found

### 2. Add Expense
- **Endpoint**: `POST /expense/v1/addExpense`
- **Headers**: `X-User-Id` (required)
- **Body**: Expense DTO (JSON)
- **Response**: Boolean indicating success
- **Status Codes**:
  - 201 Created: Successfully created expense
  - 400 Bad Request: Invalid request or data

### 3. Update Expense
- **Endpoint**: `PUT /expense/v1/updateExpense`
- **Headers**: `X-User-Id` (required)
- **Body**: Expense DTO (JSON)
- **Response**: Boolean indicating success
- **Status Codes**:
  - 200 OK: Successfully updated expense
  - 400 Bad Request: Invalid request or expense not found

## Kafka Integration

The service consumes expense data from a Kafka topic:

- **Topic**: `expense_service` (configurable in application.properties)
- **Consumer Group**: `expense-info-consumer-group`
- **Message Format**: JSON serialized ExpenseDto objects
- **Deserializer**: Custom ExpenseDeserialiser class

When a message is received from Kafka, the service:
1. Deserializes the message into an ExpenseDto object
2. Processes the expense data
3. Stores it in the database

This allows the Expense Service to receive expense data from other services (like Data Service) asynchronously.

## Setup and Deployment

### Prerequisites
- Java 21
- MySQL 8.0
- Kafka

### Configuration
The service can be configured through environment variables:

- `KAFKA_HOST`: Kafka host (default: localhost)
- `KAFKA_PORT`: Kafka port (default: 9092)
- `MYSQL_HOST`: MySQL host (default: localhost)
- `MYSQL_PORT`: MySQL port (default: 3306)
- `MYSQL_DB`: MySQL database name (default: expenseservice)
- `MYSQL_USER`: MySQL username (default: root)
- `MYSQL_PASSWORD`: MySQL password (default: password)

### Building the Service
```bash
./gradlew build
```

### Running Locally
```bash
./gradlew bootRun
```

### Docker Deployment
The service can be containerized using Docker:

```bash
# Build the JAR
./gradlew build

# Build the Docker image
docker build -t expense-service .

# Run the container
docker run -p 9820:9820 \
  -e KAFKA_HOST=kafka \
  -e MYSQL_HOST=mysql \
  -e MYSQL_USER=root \
  -e MYSQL_PASSWORD=password \
  expense-service
```

## Dependencies
- Spring Boot 3.4.6
- Spring Web (REST API)
- Spring Data JPA (Database access)
- Spring Kafka (Kafka integration)
- MySQL Connector
- Lombok
- Java 21

## Data Model

### Expense Entity
- `id`: Auto-generated primary key
- `externalId`: Unique identifier for the expense (UUID)
- `userId`: Identifier for the user who owns the expense
- `amount`: BigDecimal representation of the expense amount
- `currency`: Currency of the expense (default: INR)
- `createdAt`: Timestamp when the expense was created
- `merchant`: Where the expense occurred

## Error Handling
The service includes error handling for:
- Invalid expense data
- Database connection issues
- Kafka connectivity problems
- Duplicate expense records (idempotency check)