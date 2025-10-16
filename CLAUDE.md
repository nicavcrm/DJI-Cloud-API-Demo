# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the DJI Cloud API Demo project (v1.10.0) - a Spring Boot application that demonstrates integration with DJI's cloud platform for drone fleet management. Note: The project has a deprecation notice as of April 10, 2025, but continues to serve as a reference implementation.

## Essential Commands

### Build Commands
```bash
# Clean and build entire project
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Build specific module
mvn clean install -pl cloud-sdk
mvn clean install -pl sample
```

### Running the Application
```bash
# Run Spring Boot application
mvn spring-boot:run -pl sample

# Run from JAR
java -jar sample/target/sample-1.10.0.jar

# Debug mode (port 5005)
mvn spring-boot:run -pl sample -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

### Database Setup
```bash
# Initialize MySQL database
mysql -u root -p < sql/cloud_sample.sql
mysql -u root -p < sql/manage_device.sql
mysql -u root -p < sql/manage_device_dictionary.sql
```

### Testing
```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl sample
```

## High-Level Architecture

### Module Structure
- **cloud-sdk**: Core SDK module containing domain models and business logic
  - Organized by domain: device, control, wayline, livestream, media, firmware, map, debug, hms
  - Follows Domain-Driven Design patterns
- **sample**: Sample implementation demonstrating SDK usage
  - Contains controllers, services, and Spring Boot configuration
  - Default port: 6789

### Communication Architecture
1. **MQTT** (Eclipse Paho): Primary protocol for bi-directional device communication
   - Device telemetry, commands, and status updates
   - Topic-based pub/sub pattern

2. **WebSocket**: Real-time updates to web clients
   - Device status changes
   - Live telemetry data

3. **REST API**: HTTP endpoints for client applications
   - Swagger documentation at `/swagger-ui/index.html`
   - Postman collections in `api/` directory

### Key Integration Points
1. **Device Management Flow**:
   - Devices connect via MQTT → SDK processes messages → Updates stored in MySQL → WebSocket broadcasts changes

2. **Storage Abstraction**:
   - Supports multiple providers: MinIO, Aliyun OSS, AWS S3
   - Configured in `application.yml` under `oss` section

3. **Authentication**:
   - JWT tokens for API authentication
   - DJI Cloud API credentials (App ID/Key/License) required

### Configuration Requirements
Before running, configure in `sample/src/main/resources/application.yml`:
- MySQL connection (default port 3306)
- Redis connection (default port 6379)
- MQTT broker settings
- DJI Cloud API credentials
- Storage provider (if using media features)

### SDK Architecture and Extension Points

The cloud-sdk module provides abstract base classes for extending functionality:
- `AbstractDeviceService`: Device lifecycle management (online/offline events)
- `AbstractControlService`: Drone control commands
- `AbstractWaylineService`: Flight mission management
- `AbstractLivestreamService`: Live video streaming
- `AbstractMediaService`: Media file operations
- `AbstractFirmwareService`: Firmware update operations

To extend functionality:
1. Create a class extending the appropriate abstract service
2. Override required methods to implement business logic
3. Register the class as a Spring bean using `@Service` annotation

### Technology Stack

**Backend Framework**: Spring Boot 2.7.12 with Java 11
**Database**: MySQL 8.0+ with MyBatis Plus for ORM
**Message Broker**: MQTT (Eclipse Paho) for device communication
**Cache**: Redis for session and data caching
**Storage**: Pluggable OSS providers (MinIO, Aliyun OSS, AWS S3)
**Authentication**: JWT with Auth0 library
**Documentation**: Swagger/OpenAPI 3 at `/swagger-ui/index.html`

### Key Enums and Constants

Device types are managed through:
- `DeviceEnum`: Supported device models (M30, M300, M3D, M3T, M3M, M400)
- `DeviceTypeEnum`: Device categories (DOCK, AIRCRAFT, RC)
- `PayloadModelEnum`: Camera and payload types
- State management through `RcStateDataKeyEnum`

### Recent Development Focus
Current work involves adding support for new drone models (M400, H30, H30T) and enhancing remote control cloud authentication features. Device information is managed through SQL dictionary tables.