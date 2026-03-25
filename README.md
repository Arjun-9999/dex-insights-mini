# Dex Insights Mini

A full-stack application providing insights into retail store data, including store statuses, transactions, and incidents. Built with Spring Boot backend and Angular frontend.

## Prerequisites

- Java 21
- Maven 3.8+
- Node.js 20.19+
- Angular CLI 18+

## Run Steps

### Backend (Spring Boot)

1. Navigate to the project root directory.
2. Run `mvn spring-boot:run` to start the backend server on port 8080.

### Frontend (Angular)

1. Navigate to the `ui/dex-ui` directory.
2. Run `npm install` to install dependencies.
3. Run `ng serve --proxy-config proxy.conf.json` to start the development server on port 4200.

The frontend will proxy API requests to the backend.

## Test Steps

### Backend APIs

Use Postman or curl to test the following endpoints:

- **GET /v1/stores**: Retrieve list of stores with optional filters (brand, status, sortByOfflinePumps, page, size for pagination).
- **GET /v1/stores/{storeId}**: Get details of a specific store.
- **GET /v1/insights/overview**: Get overview insights.
- **POST /v1/chat**: Send a question in JSON body {"question": "your question", "storeId": "optional"}.

Example curl for chat:
```bash
curl -X POST http://localhost:8080/v1/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "How many stores are online?"}'
```

### Frontend

Open http://localhost:4200 in browser and interact with the UI to view stores, insights, and chat.

## Design Decisions / Tradeoffs

- **Backend**: Used Spring Boot for rapid development. Data loaded from JSON files in resources for simplicity (tradeoff: not scalable for large datasets; in production, use database).
- **Chat**: Basic keyword matching for grounding; citations provided where possible. Tradeoff: Not using advanced NLP; could be improved with embeddings or LLMs.
- **Pagination**: Implemented for stores list using Spring Data Pageable.
- **Validation**: Added basic input validation for chat requests.
- **Logging**: Standard Spring logging; could add structured logging.
- **UI**: Angular for component-based UI. Simple UI without advanced features to focus on functionality.

## AWS Productionization Notes

- **Backend**: Deploy to Elastic Beanstalk or ECS. Use RDS for data instead of JSON files. Enable CloudWatch for logging.
- **Frontend**: Build with `ng build --prod` and host on S3 + CloudFront.
- **Docker**: Use provided Dockerfile for containerization. Run with docker-compose for local dev.
- **Security**: Add authentication (e.g., Cognito). Use HTTPS. Validate inputs to prevent injection.
- **Scaling**: For high load, use API Gateway + Lambda for backend, or scale ECS tasks. Cache with ElastiCache if needed.
