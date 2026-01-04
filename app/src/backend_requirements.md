# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Smart Parking Backend is a Spring Boot REST API that manages real-time parking spot availability data from IoT sensors via MQTT. The system is designed to integrate with an Android application that displays live parking availability on Google Maps (similar to Velivert in France).

**Tech Stack:**
- Spring Boot 3.5.0 (Java 21)
- Gradle (Kotlin DSL)
- H2 In-Memory Database
- HiveMQ Cloud MQTT Broker (MQTT 5)
- JSON message processing

## Build & Run Commands

### Build
```bash
./gradlew build
```

### Run Application
```bash
./gradlew bootRun
```

### Run Tests
```bash
./gradlew test
```

### Clean Build
```bash
./gradlew clean build
```

### Run Single Test
```bash
./gradlew test --tests "ParkingBackendApplicationTests"
```

### Access H2 Console
Once running, navigate to: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

## Architecture Overview

### Real-time Data Flow (MQTT → Database → REST API)

```
IoT Sensors → MQTT Broker (HiveMQ Cloud) → MqttService → MqttMessageListener → ParkingService → Database
                                                                                                    ↓
Android App ← REST API ← ParkingController ← ParkingService ← Database
```

**Key Components:**

1. **MQTT Integration Layer** (`com.smartpark.parking_backend.mqtt` + `service.MqttService`)
   - `MqttService.java`: Establishes SSL connection to HiveMQ Cloud broker on startup (`@PostConstruct`), subscribes to `parking/#` topic pattern, and spawns daemon thread for continuous message listening
   - `MqttMessageListener.java`: Parses incoming JSON payloads `{"spot": <slotId>, "status": <boolean>}` and updates database via `ParkingService`
   - `DummyMqttPublisher.java`: Development-only service (`@ConditionalOnProperty`) that publishes simulated sensor data every 5 seconds for testing without physical sensors

2. **Domain Model** (`com.smartpark.parking_backend.model`)
   - `ParkingLot`: Represents a parking station (e.g., "CPS2 Smart Garage")
   - `ParkingSlot`: Represents individual parking spots with `slotNumber`, `isOccupied`, and optional `sensorId`
   - Relationship: One ParkingLot has many ParkingSlots (bidirectional `@OneToMany`)

3. **Service Layer** (`com.smartpark.parking_backend.service`)
   - `ParkingService.java`: Core business logic for CRUD operations on lots and slots
   - Critical method: `updateSlotStatus(Long slotId, boolean isOccupied)` - called by MQTT listener to update real-time occupancy

4. **REST API** (`com.smartpark.parking_backend.controller.ParkingController`)
   - `@CrossOrigin(origins = "*")` enabled for Android client
   - Base path: `/api/parking`

### Database Schema

```sql
PARKING_LOT (id, name, address)
PARKING_SLOT (id, slot_number, is_occupied, sensor_id, parking_lot_id)
```

**Seeded Data** (via `DataLoader.java`):
- 1 Parking Lot: "CPS2 Smart Garage" at "University Campus"
- 3 Parking Slots: slots 1-2 have sensors (sensor-01, sensor-02), slot 3 has no sensor

## REST API Endpoints for Android Integration

### Get All Parking Lots
```
GET /api/parking/lots
Response: [
  {
    "id": 1,
    "name": "CPS2 Smart Garage",
    "address": "University Campus",
    "parkingSlots": [...]
  }
]
```

### Get All Parking Slots (Live Availability)
```
GET /api/parking/slots
Response: [
  {
    "id": 1,
    "slotNumber": 1,
    "occupied": false,
    "sensorId": "sensor-01",
    "parkingLot": {...}
  },
  ...
]
```

**Android Implementation Notes:**
- Call `/api/parking/lots` to get parking station locations and metadata
- Call `/api/parking/slots` to get real-time occupancy status
- Filter slots by `parkingLot.id` to calculate "X free out of Y total" for each station
- Use `parkingLot.address` for geocoding or store lat/lng coordinates in future schema updates
- Poll these endpoints periodically (e.g., every 10-30 seconds) or implement WebSocket for push updates

### Admin Operations
```
POST /api/parking/lots                    # Create new parking lot
POST /api/parking/lots/{lotId}/slots      # Add slot to lot
PUT /api/parking/slots/{id}/status?occupied=true  # Manual status update
PUT /api/parking/slots/{id}               # Update slot details
DELETE /api/parking/slots/{id}            # Remove slot
```

## MQTT Configuration

Configuration is in `src/main/resources/application.properties`:

```properties
# HiveMQ Cloud Broker
mqtt.broker.host=bf05acb5ee194085a7731e5ca603fe6c.s1.eu.hivemq.cloud
mqtt.broker.port=8883
mqtt.client.username=<username>
mqtt.client.password=<password>
mqtt.topic.subscribe=parking/#
mqtt.enabled=true

# Development: Dummy sensor simulator
mqtt.dummy.enabled=true  # Set to false when using real sensors
```

**MQTT Message Format from Sensors:**
```json
{
  "spot": 1,        // Must match database slot ID
  "status": true    // true = occupied, false = free
}
```

**Topic Pattern:**
- Subscription: `parking/#` (wildcard for all parking topics)
- Example sensor topics: `parking/sensor/1`, `parking/sensor/2`

## Development Workflow

### Testing MQTT Integration Without Physical Sensors

1. Set `mqtt.dummy.enabled=true` in `application.properties`
2. Run application: `./gradlew bootRun`
3. Watch console logs for simulated sensor data every 5 seconds
4. Verify database updates via H2 console or REST API

### Adding New Parking Lot for Android Map

1. Start application
2. POST to `/api/parking/lots` with JSON:
   ```json
   {
     "name": "Downtown Parking",
     "address": "123 Main St"
   }
   ```
3. Note the returned `id`
4. POST slots to `/api/parking/lots/{id}/slots`:
   ```json
   {
     "slotNumber": 101,
     "sensorId": "sensor-downtown-01",
     "occupied": false
   }
   ```

### Integrating New IoT Sensors

1. Configure sensor to publish to `parking/sensor/<slot-id>` topic
2. Ensure JSON payload matches: `{"spot": <slotId>, "status": <boolean>}`
3. Add corresponding database entries via REST API
4. Set `mqtt.dummy.enabled=false` to disable simulator

## Important Implementation Notes

**MQTT Connection Lifecycle:**
- Connection established on application startup via `@PostConstruct`
- Listener thread is daemon thread (won't block JVM shutdown)
- Graceful disconnection on shutdown via `@PreDestroy`

**Error Handling:**
- MQTT message parsing errors are logged but don't crash the listener
- Invalid slot IDs in MQTT messages throw `RuntimeException` in `ParkingService.updateSlotStatus()`
- Missing JSON fields are caught and logged in `MqttMessageListener`

**Scheduled Tasks:**
- Application uses `@EnableScheduling` (see `ParkingBackendApplication.java:8`)
- `DummyMqttPublisher` publishes every 5 seconds (`@Scheduled(fixedRate = 5000)`)

## Future Android App Features to Support

### Recommended Backend Enhancements:

1. **Add GPS Coordinates to ParkingLot Model**
   ```java
   private Double latitude;
   private Double longitude;
   ```
   This enables direct Google Maps marker placement without geocoding

2. **Implement Aggregation Endpoint**
   ```
   GET /api/parking/lots/{id}/availability
   Response: { "totalSlots": 10, "freeSlots": 7, "occupancyRate": 0.3 }
   ```

3. **Add WebSocket Support** (instead of polling)
   - Broadcast slot status changes to connected Android clients
   - Use Spring WebSocket with STOMP protocol

4. **Implement Authentication**
   - Add Spring Security for admin operations
   - Public read access for Android app (GET endpoints)
   - JWT tokens for admin operations (POST/PUT/DELETE)

5. **Historical Data Tracking**
   - Add `ParkingEvent` entity to track occupancy changes with timestamps
   - Enables analytics like "peak hours" or "average availability"

## Git Workflow

Current branch: `main-mqtt-connection`
Main branch: `main`

This is a feature branch for MQTT integration. Merge to `main` when MQTT functionality is stable.

## Configuration Properties Reference

All MQTT settings can be overridden via environment variables or `application.properties`:

- `mqtt.broker.host`: HiveMQ Cloud broker hostname
- `mqtt.broker.port`: Default 8883 (SSL/TLS)
- `mqtt.client.username`: MQTT authentication username
- `mqtt.client.password`: MQTT authentication password
- `mqtt.topic.subscribe`: Topic pattern (wildcards supported)
- `mqtt.enabled`: Toggle MQTT connection (true/false)
- `mqtt.dummy.enabled`: Enable dummy sensor simulator (true/false)
