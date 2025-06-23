# Virtual Power Plant Application

The **Virtual Power Plant Application** is a Spring Boot-based project designed to manage and analyze battery data. It provides RESTful APIs for battery registration, querying, and statistical analysis. The application is built with Java, Spring Boot, and Maven, and supports PostgreSQL as the primary database.

## Features

### 1. **Battery Management**
- **Register Batteries**: Add batteries in bulk with details like name, postcode, and watt capacity.
- **Update Battery Status**: Modify the status of a battery (e.g., active, inactive).
- **Retrieve Battery by ID**: Fetch detailed information about a specific battery.

### 2. **Query and Analysis**
- **Postcode Range Query**: Retrieve batteries within a specific postcode range, with optional capacity filters.
- **High Capacity Batteries**: Fetch batteries with a watt capacity above a specified threshold.
- **Aggregated Statistics by Postcode**: Calculate total, average, minimum, and maximum capacities for batteries grouped by postcode.

### 3. **Sorting and Filtering**
- **Sorted Results**: Battery names and statistics are sorted alphabetically or numerically for better readability.
- **Capacity Filters**: Apply minimum and maximum watt capacity filters to refine queries.

### 4. **Advanced Java Streams Usage**
- Utilizes Java Streams for parallel processing, filtering, sorting, and aggregation of battery data.

### 5. **Database Support**
- **Primary Database**: PostgreSQL for production.
- **Test Database**: H2 in-memory database for testing purposes.

### 6. **API Documentation**
- Integrated with **Springdoc OpenAPI** for API documentation.
- Swagger UI available at `/swagger-ui.html`.
- http://localhost:8080/swagger-ui/index.html

### 7. **Error Handling**
- Custom exceptions for handling scenarios like battery not found or invalid input.

### 8. **Logging and Debugging**
- Added logback-spring.xml for logging in Console and File Appender for app.log

### 9. **Builder Pattern**
- Implemented Builder pattern to create Test data for reusability and reduce redundency.

### 9. **Database Indexing**
- Implemented Database indexing to find the data faster without scanning the whole table and creating a seperate column and data are arranged in sorted order using Binary Searching Algorithm.

## Upcoming Features
- Adding Testcontainers and Integration testing to test in Test Environment in Realtime.
- Working on Redis to save the data in a cache for faster retrieval and API optimization.

## Technologies Used
- **Java**: Core programming language.
- **Spring Boot**: Framework for building the application.
- **Maven**: Dependency management and build tool.
- **PostgreSQL**: Primary database.
- **H2 Database**: In-memory database for testing.
- **Swagger/OpenAPI**: API documentation and testing.

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- PostgreSQL database

### Running the Application
1. Clone the repository.
2. Configure the database connection in `application-local.yml` or `application-test.yml`.
3. Build the project:
   ```bash
   mvn clean install
   ```
4. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### Testing
Run the test cases using:
```bash
mvn test
```

## API Endpoints
- **POST /batteries**: Register batteries in bulk.
- **POST /batteries/batch**: Register multiple batteries concurrently in batch.
- **PUT /batteries/{id}/status**: Update the operational status of a battery.
- **GET /batteries/{id}**: Retrieve a battery by ID.
- **GET /batteries/postcode-range**: Query batteries within a postcode range.
- **GET /batteries/high-capacity**: Retrieve batteries with high capacity.
- **GET /batteries/statistics**: Get aggregated statistics by postcode.
- **GET /batteries/statistics/by-postcode**: Retrieve aggregated battery statistics grouped by postcode.
- **GET /batteries/count**: Get total battery count.
- **GET /batteries/search**: Search batteries by name.
- **GET /batteries/postcodes**: Get unique postcodes.
- **GET /batteries/health**: Check if the battery service is operational.

## Configuration
- **Profiles**:
    - `dev`: For development with postgres database.
    - `local`: For local development.
    - `test`: For testing with H2 database.
- **Database Configuration**:
    - Update `spring.datasource.url`, `username`, and `password` in `application.yml`.

