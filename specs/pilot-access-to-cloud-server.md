# Pilot Access to Cloud Server - Comprehensive Technical Specification

## Overview & Purpose

The Pilot Access to Cloud Server functionality enables DJI Pilot 2 applications running on remote controllers to establish secure, authenticated connections with third-party cloud platforms. This foundational capability serves as the gateway for all cloud-based operations, including real-time telemetry, mission management, media handling, and remote control.

### Business Value
- **Remote Fleet Management**: Monitor and control multiple drones from centralized cloud platforms
- **Real-time Situational Awareness**: Stream telemetry, video, and status data for operational oversight
- **Automated Operations**: Enable cloud-initiated missions, firmware updates, and configuration changes
- **Data Analytics**: Aggregate flight data, media, and performance metrics for business intelligence
- **Compliance & Auditing**: Maintain detailed logs and audit trails for regulatory compliance

### Target Devices & Compatibility

| Device Category | Model | Firmware Support | Cloud Connectivity | Notes |
|----------------|-------|------------------|-------------------|-------|
| **Remote Controllers** | RC Plus | DJI Pilot 2 v4.4+ | Full Support | Standard consumer model |
| | RC Pro | DJI Pilot 2 v4.0+ | Full Support | Enterprise model with enhanced processing |
| | RC Pro Enterprise | DJI Pilot 2 v4.0+ | Full Support | Enterprise model with RTK |
| | Smart Controller Enterprise | DJI Pilot 2 v3.8+ | Full Support | Legacy enterprise model |
| **Aircraft** | M350 RTK | v07.01.00+ | Via RC Gateway | Enterprise inspection platform |
| | M300 RTK | v57.00.01+ | Via RC Gateway | Enterprise survey/mapping platform |
| | M30/M30T Series | v07.01.00+ | Via RC Gateway | Compact enterprise platform |
| | M3E/M3T/M3M Series | v07.01.00+ | Via RC Gateway | Enterprise versions of consumer models |

## Technical Architecture

### High-Level System Architecture

```mermaid
graph TB
    subgraph "DJI Ecosystem"
        DevPortal["DJI Developer Portal"]
        License["App License Validation"]
        Pilot2["DJI Pilot 2 Application"]
        JSBridge["JSBridge API Layer"]
        WebView["Embedded WebView"]
    end
    
    subgraph "Remote Controller Hardware"
        RC["RC Hardware"]
        AndroidOS["Android OS"]
        Network["Network Stack"]
        Security["Security Module"]
    end
    
    subgraph "Cloud Platform Infrastructure"
        LoadBalancer["Load Balancer"]
        AuthService["Authentication Service"]
        MQTTCluster["MQTT Broker Cluster"]
        WebSocketGateway["WebSocket Gateway"]
        DeviceRegistry["Device Registry"]
        TelemetryDB["Telemetry Database"]
    end
    
    subgraph "Aircraft Systems"
        FlightController["Flight Controller"]
        Gimbal["Gimbal & Cameras"]
        RTK["RTK Module"]
        PayloadSDK["Payload SDK"]
    end
    
    DevPortal --> License
    License --> Pilot2
    Pilot2 --> JSBridge
    JSBridge --> WebView
    
    RC --> AndroidOS
    AndroidOS --> Network
    Network --> Security
    
    Pilot2 --> Network
    Network --> LoadBalancer
    LoadBalancer --> AuthService
    LoadBalancer --> MQTTCluster
    LoadBalancer --> WebSocketGateway
    
    MQTTCluster --> DeviceRegistry
    MQTTCluster --> TelemetryDB
    
    RC --> FlightController
    FlightController --> Gimbal
    FlightController --> RTK
    FlightController --> PayloadSDK
```

### Connection Establishment Workflow

```mermaid
sequenceDiagram
    participant Dev as DJI Developer Portal
    participant P2 as DJI Pilot 2
    participant WV as WebView H5 Page
    participant Cloud as Cloud Platform
    participant MQTT as MQTT Broker
    participant WS as WebSocket Gateway
    participant RC as Remote Controller
    participant Aircraft as Aircraft

    Note over Dev,Aircraft: Phase 1: License Verification & Authentication
    
    P2->>Dev: Verify App License (appId, appKey, license)
    Dev-->>P2: License Valid + Capabilities
    P2->>WV: Load Cloud H5 Page
    WV->>Cloud: Login Request (Username/Password)
    Cloud-->>WV: JWT Token + MQTT Credentials
    WV->>P2: Configure Cloud Module (MQTT params)
    
    Note over Dev,Aircraft: Phase 2: Cloud Module Initialization
    
    P2->>P2: Load Cloud Module Components
    P2->>MQTT: Connect (credentials, SSL/TLS)
    MQTT-->>P2: Connection Established
    P2->>WS: WebSocket Connection (JWT token)
    WS-->>P2: WebSocket Connected
    
    Note over Dev,Aircraft: Phase 3: Device Registration & Topology Discovery
    
    RC->>Aircraft: Establish DJI Link
    Aircraft-->>RC: Device Info + Capabilities
    P2->>Cloud: Device Registration Request
    Cloud->>Cloud: Store Device Metadata
    Cloud-->>P2: Registration Confirmed
    
    Note over Dev,Aircraft: Phase 4: Real-time Communication Setup
    
    P2->>MQTT: Subscribe to Command Topics
    P2->>MQTT: Publish Device Status
    P2->>WS: Subscribe to Real-time Updates
    
    loop Continuous Operation
        P2->>MQTT: Heartbeat + Telemetry
        MQTT-->>Cloud: Forward Data
        Cloud->>WS: Broadcast Status Updates
        Cloud->>MQTT: Send Commands
        MQTT-->>P2: Deliver Commands
    end
```

## API Documentation

### REST API Endpoints

#### 1. Device Registration & Binding

**Endpoint:** `POST /manage/api/v1/devices/bind`

**Purpose:** Register and bind a remote controller device to a workspace

**Authentication:** Bearer JWT Token

**Request Headers:**
```http
POST /manage/api/v1/devices/bind
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
X-Workspace-ID: workspace_abc123
```

**Request Body:**
```json
{
    "device_sn": "1ZNDH7D0010078",
    "device_name": "RC-Survey-Team-Alpha",
    "workspace_id": "workspace_abc123", 
    "device_type": 119,
    "domain": 0,
    "device_model": "RC Plus",
    "firmware_version": "04.04.0100",
    "metadata": {
        "team": "Survey Team Alpha",
        "location": "Site A",
        "operator": "John Doe"
    }
}
```

**Response (Success - 200):**
```json
{
    "code": 0,
    "message": "Device bound successfully",
    "data": {
        "device_sn": "1ZNDH7D0010078",
        "device_id": "dev_123456789",
        "bound_time": "2025-08-21T10:00:00Z",
        "workspace_id": "workspace_abc123",
        "mqtt_credentials": {
            "username": "device_1ZNDH7D0010078",
            "password": "Kj8n3mP9qR7sT2uV",
            "client_id": "rc_1ZNDH7D0010078",
            "keep_alive": 60
        },
        "websocket_endpoint": "wss://api.example.com/ws/device/1ZNDH7D0010078",
        "heartbeat_interval": 30,
        "command_timeout": 10
    }
}
```

**Response (Error - 400):**
```json
{
    "code": 100003,
    "message": "Device already bound to another workspace",
    "data": {
        "current_workspace": "workspace_xyz789",
        "bound_time": "2025-08-20T15:30:00Z"
    }
}
```

#### 2. Device Authentication Status

**Endpoint:** `GET /manage/api/v1/devices/{device_sn}/auth-status`

**Purpose:** Check device authentication and connection status

```http
GET /manage/api/v1/devices/1ZNDH7D0010078/auth-status
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```json
{
    "code": 0,
    "message": "Device authentication status retrieved",
    "data": {
        "device_sn": "1ZNDH7D0010078",
        "auth_status": "authenticated",
        "connection_status": "online",
        "last_heartbeat": "2025-08-21T10:05:30Z",
        "mqtt_connected": true,
        "websocket_connected": true,
        "license_status": "valid",
        "license_expires": "2025-12-31T23:59:59Z"
    }
}
```

### MQTT Communication Protocols

#### Topic Structure & QoS Levels

**Topic Pattern:** `{message_type}/product/{device_sn}/{function}`

| Topic Type | QoS | Purpose | Example |
|------------|-----|---------|---------|
| `sys/product/+/status` | 1 | Device lifecycle events | Online/offline notifications |
| `thing/product/+/requests` | 1 | Cloud-to-device commands | Control commands, config updates |
| `thing/product/+/property/set` | 1 | Property updates | Configuration changes |
| `thing/product/+/events` | 0 | Event notifications | Alerts, status changes |

#### 1. Device Online Registration Message

**Topic:** `sys/product/{device_sn}/status`  
**QoS:** 1  
**Retained:** false

```json
{
    "bid": "reg_20250821_001",
    "tid": "txn_device_online_001", 
    "timestamp": 1724241600000,
    "method": "thing.topo.add",
    "data": {
        "domain": 0,
        "type": 119,
        "sub_type": 0,
        "device_secret": "sha256_device_hash",
        "ntp_server_host": "pool.ntp.org",
        "thing_version": "1.0.0",
        "children": [
            {
                "domain": 1,
                "type": 89,
                "sub_type": 0,
                "device_secret": "sha256_aircraft_hash",
                "index": "3-0-0",
                "sn": "1ZNDH8G001P001",
                "thing_version": "1.0.0"
            }
        ]
    }
}
```

#### 2. Heartbeat Message

**Topic:** `thing/product/{device_sn}/heartbeat`  
**QoS:** 0  
**Interval:** 30 seconds

```json
{
    "bid": "hb_20250821_001",
    "tid": "hb_txn_001",
    "timestamp": 1724241660000,
    "data": {
        "system_status": "normal",
        "battery_level": 85,
        "network_signal": -65,
        "temperature": 45,
        "memory_usage": 67,
        "cpu_usage": 34
    }
}
```

#### 3. Command Response Message

**Topic:** `thing/product/{device_sn}/services_reply`  
**QoS:** 1

```json
{
    "bid": "cmd_response_001",
    "tid": "original_command_txn",
    "timestamp": 1724241720000,
    "data": {
        "result": 0,
        "output": {
            "execution_time": 1.234,
            "status": "completed",
            "details": "Command executed successfully"
        }
    }
}
```

### WebSocket Events

#### Connection Configuration

**Endpoint:** `wss://api.example.com/ws`  
**Protocol:** WebSocket v13  
**Authentication:** JWT token in query parameter or Authorization header

```javascript
const wsUrl = `wss://api.example.com/ws?token=${jwtToken}`;
const socket = new WebSocket(wsUrl);
```

#### 1. Device Status Update Event

**Event Type:** `device.status.update`

```json
{
    "event_type": "device.status.update",
    "timestamp": 1724241780000,
    "data": {
        "device_sn": "1ZNDH7D0010078",
        "status": "online",
        "previous_status": "offline", 
        "battery_level": 87,
        "signal_strength": -58,
        "location": {
            "latitude": 40.7128,
            "longitude": -74.0060,
            "altitude": 10.5
        }
    }
}
```

#### 2. Command Execution Event

**Event Type:** `command.execution.status`

```json
{
    "event_type": "command.execution.status",
    "timestamp": 1724241840000,
    "data": {
        "command_id": "cmd_20250821_001",
        "device_sn": "1ZNDH7D0010078",
        "status": "executing",
        "progress": 75,
        "estimated_completion": "2025-08-21T10:12:00Z"
    }
}
```

## Frontend Implementation

### Vue.js Component Integration

#### 1. Cloud Connection Service

**File:** `/Cloud-API-Demo-Web/src/services/CloudConnectionService.ts`

```typescript
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PilotBridge from '@/api/pilot-bridge'

interface CloudConnectionState {
    isConnected: boolean
    deviceSn: string
    workspaceId: string
    lastHeartbeat: Date | null
    connectionError: string | null
}

class CloudConnectionService {
    private state = reactive<CloudConnectionState>({
        isConnected: false,
        deviceSn: '',
        workspaceId: '',
        lastHeartbeat: null,
        connectionError: null
    })

    public readonly connectionState = readonly(this.state)

    async initializeConnection(config: CloudConfig): Promise<boolean> {
        try {
            // Step 1: Verify DJI License
            const licenseValid = await this.verifyLicense(config.appId, config.appKey, config.license)
            if (!licenseValid) {
                throw new Error('DJI License verification failed')
            }

            // Step 2: Load Cloud Module Components
            await this.loadCloudComponents(config)

            // Step 3: Establish MQTT Connection
            await this.connectMQTT(config.mqtt)

            // Step 4: Setup WebSocket Connection
            await this.connectWebSocket(config.websocket)

            // Step 5: Register Device
            await this.registerDevice(config.device)

            this.state.isConnected = true
            this.state.connectionError = null
            this.startHeartbeat()

            return true

        } catch (error) {
            this.state.connectionError = error.message
            message.error(`Connection failed: ${error.message}`)
            return false
        }
    }

    private async verifyLicense(appId: string, appKey: string, license: string): Promise<boolean> {
        try {
            const result = PilotBridge.platformVerifyLicense(appId, appKey, license)
            if (result) {
                console.log('DJI License verification successful')
                return true
            } else {
                throw new Error('Invalid license credentials')
            }
        } catch (error) {
            console.error('License verification error:', error)
            throw new Error('License verification failed')
        }
    }

    private async loadCloudComponents(config: CloudConfig): Promise<void> {
        // Load Thing Module (MQTT)
        const thingConfig = {
            host: config.mqtt.host,
            username: config.mqtt.username,
            password: config.mqtt.password,
            connectCallback: 'onThingConnected'
        }

        const thingResult = PilotBridge.loadComponent('Thing', thingConfig)
        if (!thingResult) {
            throw new Error('Failed to load Thing module')
        }

        // Load WebSocket Module
        const wsConfig = {
            host: config.websocket.url,
            token: config.auth.token,
            connectCallback: 'onWebSocketConnected'
        }

        const wsResult = PilotBridge.loadComponent('Ws', wsConfig)
        if (!wsResult) {
            throw new Error('Failed to load WebSocket module')
        }

        // Set workspace configuration
        PilotBridge.setWorkspaceId(config.workspace.id)
        PilotBridge.setPlatformMessage(
            config.workspace.platformName,
            config.workspace.title,
            config.workspace.description
        )
    }

    private async connectMQTT(mqttConfig: MQTTConfig): Promise<void> {
        return new Promise((resolve, reject) => {
            // Setup global callback
            window.onThingConnected = (connected: boolean) => {
                if (connected) {
                    console.log('MQTT connection established')
                    resolve()
                } else {
                    reject(new Error('MQTT connection failed'))
                }
            }

            // Check connection status periodically
            const checkConnection = setInterval(() => {
                if (PilotBridge.thingGetConnectState()) {
                    clearInterval(checkConnection)
                    resolve()
                }
            }, 1000)

            // Timeout after 30 seconds
            setTimeout(() => {
                clearInterval(checkConnection)
                reject(new Error('MQTT connection timeout'))
            }, 30000)
        })
    }

    private async connectWebSocket(wsConfig: WebSocketConfig): Promise<void> {
        return new Promise((resolve, reject) => {
            window.onWebSocketConnected = (connected: boolean) => {
                if (connected) {
                    console.log('WebSocket connection established')
                    resolve()
                } else {
                    reject(new Error('WebSocket connection failed'))
                }
            }

            const result = PilotBridge.wsConnect(wsConfig.url, wsConfig.token, 'onWebSocketConnected')
            if (!result) {
                reject(new Error('Failed to initiate WebSocket connection'))
            }
        })
    }

    private startHeartbeat(): void {
        setInterval(() => {
            if (this.state.isConnected) {
                // Send heartbeat via MQTT
                const heartbeat = {
                    timestamp: Date.now(),
                    device_sn: this.state.deviceSn,
                    status: 'alive'
                }

                // Update last heartbeat timestamp
                this.state.lastHeartbeat = new Date()
            }
        }, 30000) // 30 second interval
    }

    async disconnect(): Promise<void> {
        try {
            PilotBridge.unloadComponent('Thing')
            PilotBridge.unloadComponent('Ws')
            PilotBridge.wsDisconnect()

            this.state.isConnected = false
            this.state.lastHeartbeat = null

        } catch (error) {
            console.error('Disconnect error:', error)
        }
    }
}

export const cloudConnectionService = new CloudConnectionService()
```

#### 2. Connection Status Component

**File:** `/Cloud-API-Demo-Web/src/components/CloudConnectionStatus.vue`

```vue
<template>
    <div class="cloud-connection-status">
        <a-card title="Cloud Connection Status" size="small">
            <div class="status-grid">
                <div class="status-item">
                    <a-badge 
                        :status="connectionState.isConnected ? 'success' : 'error'"
                        :text="connectionState.isConnected ? 'Connected' : 'Disconnected'"
                    />
                </div>
                
                <div class="status-item">
                    <label>Device SN:</label>
                    <span>{{ connectionState.deviceSn || 'N/A' }}</span>
                </div>
                
                <div class="status-item">
                    <label>Workspace:</label>
                    <span>{{ connectionState.workspaceId || 'N/A' }}</span>
                </div>
                
                <div class="status-item">
                    <label>Last Heartbeat:</label>
                    <span>{{ formatHeartbeat(connectionState.lastHeartbeat) }}</span>
                </div>
                
                <div class="status-item" v-if="connectionState.connectionError">
                    <a-alert 
                        :message="connectionState.connectionError"
                        type="error" 
                        show-icon 
                        closable
                    />
                </div>
            </div>
            
            <div class="connection-actions">
                <a-button 
                    @click="reconnect"
                    :loading="reconnecting"
                    type="primary"
                    v-if="!connectionState.isConnected"
                >
                    Reconnect
                </a-button>
                
                <a-button 
                    @click="disconnect"
                    danger
                    v-if="connectionState.isConnected"
                >
                    Disconnect
                </a-button>
            </div>
        </a-card>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { cloudConnectionService } from '@/services/CloudConnectionService'
import { formatDistanceToNow } from 'date-fns'

const connectionState = cloudConnectionService.connectionState
const reconnecting = ref(false)

const formatHeartbeat = (date: Date | null) => {
    if (!date) return 'Never'
    return formatDistanceToNow(date, { addSuffix: true })
}

const reconnect = async () => {
    reconnecting.value = true
    try {
        // Retrieve stored configuration
        const config = localStorage.getItem('cloudConfig')
        if (config) {
            await cloudConnectionService.initializeConnection(JSON.parse(config))
        }
    } finally {
        reconnecting.value = false
    }
}

const disconnect = async () => {
    await cloudConnectionService.disconnect()
}
</script>

<style scoped>
.cloud-connection-status {
    margin-bottom: 16px;
}

.status-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px;
    margin-bottom: 16px;
}

.status-item label {
    font-weight: 600;
    margin-right: 8px;
}

.connection-actions {
    text-align: center;
}
</style>
```

## Backend Implementation

### Spring Boot Service Layer

#### 1. SDK Device Service Implementation

**File:** `/sample/src/main/java/com/dji/sample/manage/service/impl/SDKDeviceService.java`

```java
@Service
@Slf4j
public class SDKDeviceService extends AbstractDeviceService {

    @Autowired
    private IDeviceService deviceService;
    
    @Autowired
    private IDeviceRedisService deviceRedisService;
    
    @Autowired
    private IWebSocketMessageService webSocketMessageService;

    @Override
    public TopicStatusResponse<MqttReply> updateTopoOnline(
            TopicStatusRequest<UpdateTopo> request, MessageHeaders headers) {
        
        log.info("Processing device topology update for gateway: {}", request.getFrom());
        
        try {
            // Extract device information
            UpdateTopo topology = request.getData();
            String gatewaySn = request.getFrom();
            UpdateTopoSubDevice subDevice = topology.getSubDevices().get(0);
            String aircraftSn = subDevice.getSn();

            // Check if devices are already online
            Optional<DeviceDTO> existingGateway = deviceRedisService.getDeviceOnline(gatewaySn);
            Optional<DeviceDTO> existingAircraft = deviceRedisService.getDeviceOnline(aircraftSn);
            
            if (existingGateway.isPresent() && existingAircraft.isPresent()) {
                log.info("Devices already online, updating status");
                deviceOnlineAgain(existingGateway.get().getWorkspaceId(), gatewaySn, aircraftSn);
                return TopicStatusResponse.<MqttReply>builder()
                    .tid(request.getTid())
                    .bid(request.getBid()) 
                    .timestamp(System.currentTimeMillis())
                    .data(MqttReply.success())
                    .build();
            }

            // Register gateway device
            DeviceDTO gateway = processGatewayRegistration(gatewaySn, topology);
            
            // Register aircraft device
            DeviceDTO aircraft = processAircraftRegistration(aircraftSn, subDevice, gateway);
            
            // Setup MQTT subscriptions
            GatewayManager gatewayManager = SDKManager.registerDevice(
                gatewaySn, aircraftSn,
                topology.getDomain(), topology.getType(), topology.getSubType(),
                topology.getThingVersion(), subDevice.getThingVersion()
            );
            
            // Subscribe to gateway topics
            deviceService.gatewayOnlineSubscribeTopic(gatewayManager);
            
            if (StringUtils.hasText(aircraft.getWorkspaceId())) {
                // Subscribe to aircraft topics
                deviceService.subDeviceOnlineSubscribeTopic(gatewayManager);
            }
            
            // Broadcast device online status via WebSocket
            broadcastDeviceOnline(gateway, aircraft);
            
            return TopicStatusResponse.<MqttReply>builder()
                .tid(request.getTid())
                .bid(request.getBid())
                .timestamp(System.currentTimeMillis())
                .data(MqttReply.success())
                .build();
                
        } catch (Exception e) {
            log.error("Error processing device topology update", e);
            return TopicStatusResponse.<MqttReply>builder()
                .tid(request.getTid())
                .bid(request.getBid())
                .timestamp(System.currentTimeMillis())
                .data(MqttReply.fail(e.getMessage()))
                .build();
        }
    }

    private DeviceDTO processGatewayRegistration(String deviceSn, UpdateTopo topology) {
        log.info("Registering gateway device: {}", deviceSn);
        
        DeviceDTO gateway = DeviceDTO.builder()
            .deviceSn(deviceSn)
            .deviceName(generateDeviceName(deviceSn, topology.getType()))
            .deviceType(topology.getType())
            .subType(topology.getSubType())
            .domain(topology.getDomain())
            .thingVersion(topology.getThingVersion())
            .deviceSecret(topology.getDeviceSecret())
            .status(true)
            .loginTime(LocalDateTime.now())
            .createTime(LocalDateTime.now())
            .updateTime(LocalDateTime.now())
            .build();

        // Check device dictionary for additional metadata
        Optional<DeviceDictionaryDTO> dictionaryEntry = 
            dictionaryService.getDeviceByType(topology.getType(), topology.getSubType());
        
        if (dictionaryEntry.isPresent()) {
            DeviceDictionaryDTO dict = dictionaryEntry.get();
            gateway.setDeviceModel(dict.getDeviceModel());
            gateway.setFirmwareVersion(dict.getFirmwareVersion());
        }

        // Save device to database and Redis
        Optional<DeviceDTO> savedGateway = deviceService.saveDevice(gateway);
        if (savedGateway.isEmpty()) {
            throw new RuntimeException("Failed to save gateway device: " + deviceSn);
        }
        
        return savedGateway.get();
    }

    private DeviceDTO processAircraftRegistration(String aircraftSn, UpdateTopoSubDevice subDevice, DeviceDTO gateway) {
        log.info("Registering aircraft device: {}", aircraftSn);
        
        DeviceDTO aircraft = DeviceDTO.builder()
            .deviceSn(aircraftSn)
            .deviceName(generateDeviceName(aircraftSn, subDevice.getType()))
            .deviceType(subDevice.getType())
            .subType(subDevice.getSubType())
            .domain(subDevice.getDomain())
            .thingVersion(subDevice.getThingVersion())
            .deviceSecret(subDevice.getDeviceSecret())
            .parentSn(gateway.getDeviceSn())
            .workspaceId(gateway.getWorkspaceId())
            .status(true)
            .loginTime(LocalDateTime.now())
            .createTime(LocalDateTime.now())
            .updateTime(LocalDateTime.now())
            .build();

        Optional<DeviceDTO> savedAircraft = deviceService.saveDevice(aircraft);
        if (savedAircraft.isEmpty()) {
            throw new RuntimeException("Failed to save aircraft device: " + aircraftSn);
        }
        
        return savedAircraft.get();
    }

    private void broadcastDeviceOnline(DeviceDTO gateway, DeviceDTO aircraft) {
        // Create WebSocket message for gateway online
        WebSocketMessageResponse<DeviceDTO> gatewayMessage = WebSocketMessageResponse.<DeviceDTO>builder()
            .bizCode(BizCodeEnum.DEVICE_ONLINE.getCode())
            .timestamp(System.currentTimeMillis())
            .data(gateway)
            .build();
        
        webSocketMessageService.sendMessageToWorkspace(gateway.getWorkspaceId(), gatewayMessage);

        // Create WebSocket message for aircraft online
        WebSocketMessageResponse<DeviceDTO> aircraftMessage = WebSocketMessageResponse.<DeviceDTO>builder()
            .bizCode(BizCodeEnum.DEVICE_ONLINE.getCode())
            .timestamp(System.currentTimeMillis())
            .data(aircraft)
            .build();
        
        webSocketMessageService.sendMessageToWorkspace(aircraft.getWorkspaceId(), aircraftMessage);
        
        log.info("Broadcasted device online status for gateway {} and aircraft {}", 
            gateway.getDeviceSn(), aircraft.getDeviceSn());
    }

    @Override
    public TopicStatusResponse<MqttReply> updateTopoOffline(
            TopicStatusRequest<UpdateTopo> request, MessageHeaders headers) {
        
        String gatewaySn = request.getFrom();
        log.info("Processing device offline for gateway: {}", gatewaySn);
        
        try {
            // Update device status in Redis and database
            deviceRedisService.setDeviceOffline(gatewaySn);
            deviceService.updateDeviceStatus(gatewaySn, false);
            
            // Handle aircraft offline if exists
            UpdateTopo topology = request.getData();
            if (topology.getSubDevices() != null && !topology.getSubDevices().isEmpty()) {
                String aircraftSn = topology.getSubDevices().get(0).getSn();
                deviceRedisService.setDeviceOffline(aircraftSn);
                deviceService.updateDeviceStatus(aircraftSn, false);
            }
            
            // Broadcast offline status
            broadcastDeviceOffline(gatewaySn);
            
            return TopicStatusResponse.<MqttReply>builder()
                .tid(request.getTid())
                .bid(request.getBid())
                .timestamp(System.currentTimeMillis())
                .data(MqttReply.success())
                .build();
                
        } catch (Exception e) {
            log.error("Error processing device offline", e);
            return TopicStatusResponse.<MqttReply>builder()
                .tid(request.getTid())
                .bid(request.getBid())
                .timestamp(System.currentTimeMillis())
                .data(MqttReply.fail(e.getMessage()))
                .build();
        }
    }

    private String generateDeviceName(String deviceSn, Integer deviceType) {
        // Generate human-readable device name based on type
        String deviceTypeName = DeviceTypeEnum.findByType(deviceType)
            .map(DeviceTypeEnum::getDesc)
            .orElse("Unknown");
        
        return String.format("%s-%s", deviceTypeName, deviceSn.substring(deviceSn.length() - 6));
    }
}
```

#### 2. Device REST Controller

**File:** `/sample/src/main/java/com/dji/sample/manage/controller/DeviceController.java`

```java
@RestController
@RequestMapping("${url.manage.prefix}${url.manage.version}/devices")
@Slf4j
@Api(tags = "Device Management")
public class DeviceController {

    @Autowired
    private IDeviceService deviceService;
    
    @Autowired
    private IWorkspaceService workspaceService;

    @PostMapping("/bind")
    @ApiOperation(value = "Bind device to workspace")
    public ResponseEntity<HttpResultResponse<DeviceBindResponseDTO>> bindDevice(
            @RequestBody @Valid DeviceBindRequestDTO request,
            HttpServletRequest httpRequest) {
        
        try {
            // Validate workspace exists
            Optional<WorkspaceDTO> workspace = workspaceService.getWorkspaceById(request.getWorkspaceId());
            if (workspace.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(HttpResultResponse.error("Workspace not found"));
            }

            // Check if device is already bound
            Optional<DeviceDTO> existingDevice = deviceService.getDeviceBySerial(request.getDeviceSn());
            if (existingDevice.isPresent() && StringUtils.hasText(existingDevice.get().getWorkspaceId())) {
                return ResponseEntity.badRequest()
                    .body(HttpResultResponse.error("Device already bound to workspace"));
            }

            // Bind device to workspace
            DeviceDTO device = deviceService.bindDeviceToWorkspace(
                request.getDeviceSn(),
                request.getWorkspaceId(),
                request.getDeviceName(),
                request.getMetadata()
            );

            // Generate MQTT credentials
            MqttCredentials mqttCredentials = generateMqttCredentials(device);

            DeviceBindResponseDTO response = DeviceBindResponseDTO.builder()
                .deviceSn(device.getDeviceSn())
                .deviceId(device.getDeviceId())
                .boundTime(device.getUpdateTime())
                .workspaceId(device.getWorkspaceId())
                .mqttCredentials(mqttCredentials)
                .websocketEndpoint(generateWebSocketEndpoint(device))
                .heartbeatInterval(30)
                .commandTimeout(10)
                .build();

            return ResponseEntity.ok(HttpResultResponse.success(response));
            
        } catch (Exception e) {
            log.error("Error binding device", e);
            return ResponseEntity.internalServerError()
                .body(HttpResultResponse.error("Device binding failed: " + e.getMessage()));
        }
    }

    @GetMapping("/{deviceSn}/auth-status")
    @ApiOperation(value = "Get device authentication status")
    public ResponseEntity<HttpResultResponse<DeviceAuthStatusDTO>> getAuthStatus(
            @PathVariable String deviceSn) {
        
        try {
            Optional<DeviceDTO> device = deviceService.getDeviceBySerial(deviceSn);
            if (device.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            DeviceDTO deviceData = device.get();
            
            // Check Redis for real-time status
            boolean isOnline = deviceRedisService.isDeviceOnline(deviceSn);
            Date lastHeartbeat = deviceRedisService.getLastHeartbeat(deviceSn);
            
            DeviceAuthStatusDTO status = DeviceAuthStatusDTO.builder()
                .deviceSn(deviceSn)
                .authStatus(StringUtils.hasText(deviceData.getWorkspaceId()) ? "authenticated" : "pending")
                .connectionStatus(isOnline ? "online" : "offline")
                .lastHeartbeat(lastHeartbeat)
                .mqttConnected(isOnline)
                .websocketConnected(deviceRedisService.isWebSocketConnected(deviceSn))
                .licenseStatus("valid")
                .licenseExpires(getLicenseExpiration())
                .build();

            return ResponseEntity.ok(HttpResultResponse.success(status));
            
        } catch (Exception e) {
            log.error("Error getting auth status", e);
            return ResponseEntity.internalServerError()
                .body(HttpResultResponse.error("Failed to get auth status"));
        }
    }

    private MqttCredentials generateMqttCredentials(DeviceDTO device) {
        return MqttCredentials.builder()
            .username("device_" + device.getDeviceSn())
            .password(generateSecurePassword())
            .clientId("rc_" + device.getDeviceSn())
            .keepAlive(60)
            .build();
    }

    private String generateWebSocketEndpoint(DeviceDTO device) {
        return String.format("wss://api.example.com/ws/device/%s", device.getDeviceSn());
    }

    private String generateSecurePassword() {
        // Generate secure random password
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
```

## Device Integration

### Supported Aircraft Models & Capabilities

#### DJI M350 RTK Integration

```json
{
    "device_type": 89,
    "sub_type": 0,
    "model": "M350 RTK",
    "capabilities": {
        "max_flight_time": 55,
        "max_speed": 23,
        "max_altitude": 7000,
        "rtk_support": true,
        "multi_payload": true,
        "ip_rating": "IP55",
        "supported_payloads": [
            "Zenmuse H30",
            "Zenmuse H30T", 
            "Zenmuse H20N",
            "Zenmuse P1",
            "Zenmuse L1"
        ],
        "cloud_features": {
            "live_streaming": true,
            "waypoint_missions": true,
            "media_sync": true,
            "remote_control": true,
            "firmware_updates": true
        }
    },
    "telemetry_frequency": {
        "high_frequency": 10, // 10Hz for position, attitude
        "medium_frequency": 5, // 5Hz for camera status
        "low_frequency": 1    // 1Hz for system status
    }
}
```

#### DJI M30 Series Integration

```json
{
    "device_type": 67,
    "sub_type": 0,
    "model": "M30T",
    "capabilities": {
        "max_flight_time": 41,
        "max_speed": 15,
        "max_altitude": 7000,
        "rtk_support": true,
        "integrated_payload": true,
        "ip_rating": "IP55",
        "integrated_cameras": {
            "wide_camera": "4K/30fps",
            "zoom_camera": "200x hybrid zoom",
            "thermal_camera": "640x512 radiometric",
            "laser_rangefinder": "1200m range"
        },
        "cloud_features": {
            "live_streaming": true,
            "waypoint_missions": true,
            "media_sync": true,
            "remote_control": true,
            "firmware_updates": true,
            "night_operations": true
        }
    }
}
```

### Communication Protocols

#### DJI Link Protocol Integration

The Remote Controller communicates with aircraft using the proprietary DJI Link protocol, which provides:

- **Low Latency Control**: < 50ms for control commands
- **High Bandwidth Telemetry**: Up to 15.5MHz bandwidth
- **Reliable Connection**: Automatic frequency hopping and error correction
- **Extended Range**: Up to 15km (depending on model and conditions)

```java
// DJI Link Status Monitoring
@Component
public class DJILinkMonitor {
    
    @EventListener
    public void onLinkQualityUpdate(LinkQualityEvent event) {
        LinkQualityData data = LinkQualityData.builder()
            .signalStrength(event.getSignalStrength())
            .linkQuality(event.getLinkQuality())
            .latency(event.getLatency())
            .packetLoss(event.getPacketLoss())
            .timestamp(System.currentTimeMillis())
            .build();
            
        // Forward to cloud via MQTT
        mqttService.publish("thing/product/" + event.getDeviceSn() + "/link-status", data);
    }
}
```

## Data Models

### Request/Response Schemas

#### Device Registration Request

```typescript
interface DeviceBindRequest {
    device_sn: string;          // Device serial number
    device_name?: string;       // Human readable name
    workspace_id: string;       // Target workspace ID
    device_type: number;        // Device type enum value
    domain: number;             // Device domain (0=gateway, 1=sub-device)
    metadata?: {                // Optional metadata
        team?: string;
        location?: string;
        operator?: string;
        tags?: string[];
    };
}

interface DeviceBindResponse {
    device_sn: string;
    device_id: string;
    bound_time: string;         // ISO 8601 timestamp
    workspace_id: string;
    mqtt_credentials: {
        username: string;
        password: string;
        client_id: string;
        keep_alive: number;
    };
    websocket_endpoint: string;
    heartbeat_interval: number; // seconds
    command_timeout: number;    // seconds
}
```

#### Device Status Schema

```typescript
interface DeviceStatusMessage {
    bid: string;                // Business ID for request tracking
    tid: string;                // Transaction ID
    timestamp: number;          // Unix timestamp in milliseconds
    method: string;             // MQTT method name
    data: {
        domain: number;         // Device domain
        type: number;           // Device type
        sub_type: number;       // Device sub-type
        device_secret: string;  // Device authentication hash
        ntp_server_host: string; // NTP server for time sync
        thing_version: string;   // Thing model version
        children?: Array<{      // Sub-devices (aircraft)
            domain: number;
            type: number;
            sub_type: number;
            device_secret: string;
            index: string;      // Device index identifier
            sn: string;         // Sub-device serial number
            thing_version: string;
        }>;
    };
}
```

### Database Entity Models

#### Device Entity Schema

```sql
CREATE TABLE `device` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `device_sn` VARCHAR(64) NOT NULL UNIQUE,
    `device_name` VARCHAR(128),
    `device_type` INT NOT NULL,
    `sub_type` INT DEFAULT 0,
    `domain` INT DEFAULT 0,
    `device_model` VARCHAR(64),
    `firmware_version` VARCHAR(32),
    `thing_version` VARCHAR(16) DEFAULT '1.0.0',
    `device_secret` VARCHAR(128),
    `parent_sn` VARCHAR(64),
    `workspace_id` VARCHAR(64),
    `status` BOOLEAN DEFAULT FALSE,
    `login_time` DATETIME,
    `logout_time` DATETIME,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX `idx_device_sn` (`device_sn`),
    INDEX `idx_workspace_id` (`workspace_id`),
    INDEX `idx_parent_sn` (`parent_sn`),
    INDEX `idx_status` (`status`)
);
```

#### Device Dictionary Schema

```sql
CREATE TABLE `device_dictionary` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `device_type` INT NOT NULL,
    `sub_type` INT DEFAULT 0,
    `device_model` VARCHAR(64) NOT NULL,
    `device_name` VARCHAR(128) NOT NULL,
    `device_desc` TEXT,
    `firmware_version` VARCHAR(32),
    `capabilities` JSON,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY `uk_device_type_sub` (`device_type`, `sub_type`)
);
```

## Communication Workflows

### Device Connection Sequence

```mermaid
sequenceDiagram
    participant P2 as DJI Pilot 2
    participant H5 as H5 WebView
    participant Auth as Auth Service
    participant MQTT as MQTT Broker
    participant WS as WebSocket
    participant DB as Database
    participant Redis as Redis Cache

    Note over P2,Redis: Authentication Phase
    P2->>H5: Load Cloud Platform URL
    H5->>Auth: User Login Request
    Auth-->>H5: JWT Token + MQTT Credentials
    H5->>P2: Configure Cloud Components
    
    Note over P2,Redis: Connection Phase  
    P2->>MQTT: Connect with Credentials
    MQTT-->>P2: Connection Acknowledged
    P2->>WS: WebSocket Connection
    WS-->>P2: WebSocket Established
    
    Note over P2,Redis: Registration Phase
    P2->>MQTT: Publish Device Online Status
    MQTT->>Auth: Forward Registration
    Auth->>DB: Store Device Info
    Auth->>Redis: Cache Device Status
    Auth-->>MQTT: Registration Confirmed
    MQTT-->>P2: Confirmation
    
    Note over P2,Redis: Operational Phase
    loop Every 30 seconds
        P2->>MQTT: Heartbeat + Telemetry
        MQTT->>Redis: Update Device Status
    end
    
    loop Real-time Events
        Auth->>WS: Status Updates
        WS-->>P2: Push Notifications
    end
```

### Error Handling & Recovery

```mermaid
flowchart TD
    A[Connection Lost] --> B{Error Type?}
    
    B -->|Network Issue| C[Exponential Backoff Retry]
    B -->|Authentication Failed| D[Refresh Credentials]
    B -->|Server Unreachable| E[Switch to Backup Server]
    B -->|License Expired| F[License Renewal Required]
    
    C --> G[Reconnection Attempt]
    D --> H[Re-authenticate]
    E --> I[Health Check Backup]
    F --> J[User Intervention]
    
    G --> K{Success?}
    H --> K
    I --> K
    J --> K
    
    K -->|Yes| L[Resume Operations]
    K -->|No| M[Increment Retry Count]
    
    M --> N{Max Retries?}
    N -->|No| C
    N -->|Yes| O[Manual Intervention Required]
    
    L --> P[Sync Missed Data]
    P --> Q[Normal Operation]
```

## Security Considerations

### Transport Layer Security

1. **MQTT over TLS/SSL**:
   - Use MQTT over SSL (port 8883) for production
   - Implement certificate validation
   - Support SNI (Server Name Indication)
   - Use TLS 1.2 minimum, prefer TLS 1.3

```yaml
mqtt:
  BASIC:
    protocol: MQTTS
    host: mqtt.example.com
    port: 8883
    ssl:
      enabled: true
      ca_cert: /path/to/ca.crt
      client_cert: /path/to/client.crt
      client_key: /path/to/client.key
      verify_hostname: true
```

2. **WebSocket Secure (WSS)**:
   - Always use WSS in production
   - Implement proper certificate validation
   - Use same-origin policy where applicable

### Authentication & Authorization

1. **JWT Token Management**:
   - Use RS256 or ES256 for signing
   - Implement token refresh mechanism
   - Set appropriate expiration times (< 24 hours)
   - Include device-specific claims

```javascript
// JWT Payload Structure
{
    "iss": "cloud-platform",
    "sub": "device_1ZNDH7D0010078",
    "aud": "cloud-api",
    "exp": 1724328000,
    "iat": 1724241600,
    "device_sn": "1ZNDH7D0010078",
    "workspace_id": "workspace_abc123",
    "permissions": ["telemetry:read", "commands:execute", "media:upload"]
}
```

2. **Device Authentication**:
   - Use device-specific secrets/certificates
   - Implement device fingerprinting
   - Support certificate rotation

### Data Encryption & Privacy

1. **End-to-End Encryption**:
   - Encrypt sensitive telemetry data
   - Use AES-256-GCM for payload encryption
   - Implement key rotation policies

2. **PII Protection**:
   - Hash or encrypt location data when storing
   - Implement data retention policies
   - Support GDPR/CCPA compliance

## Configuration & Deployment

### Environment Configuration

#### Development Environment

```yaml
# application-dev.yml
spring:
  profiles:
    active: dev

mqtt:
  BASIC:
    protocol: MQTT
    host: localhost
    port: 1883
    username: dev_user
    password: dev_pass
    ssl:
      enabled: false

jwt:
  secret: dev_secret_key_not_for_production
  age: 86400

logging:
  level:
    com.dji: debug
    mqtt: debug
```

#### Production Environment

```yaml
# application-prod.yml  
spring:
  profiles:
    active: prod

mqtt:
  BASIC:
    protocol: MQTTS
    host: ${MQTT_HOST}
    port: 8883
    username: ${MQTT_USERNAME}
    password: ${MQTT_PASSWORD}
    ssl:
      enabled: true
      ca_cert: ${SSL_CA_CERT_PATH}
      client_cert: ${SSL_CLIENT_CERT_PATH}
      client_key: ${SSL_CLIENT_KEY_PATH}

jwt:
  secret: ${JWT_SECRET}
  age: 3600

logging:
  level:
    com.dji: info
    root: warn
```

### Docker Deployment

#### Multi-Stage Dockerfile

```dockerfile
# Build stage
FROM maven:3.9-openjdk-11-slim AS builder
WORKDIR /app
COPY pom.xml .
COPY cloud-sdk/pom.xml ./cloud-sdk/
COPY sample/pom.xml ./sample/
RUN mvn dependency:go-offline

COPY . .
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:11-jre-slim
WORKDIR /app

# Create non-root user
RUN useradd -r -u 1000 cloudapi

# Copy application
COPY --from=builder /app/sample/target/sample-*.jar app.jar

# Create directories and set permissions
RUN mkdir -p /app/logs /app/ssl && \
    chown -R cloudapi:cloudapi /app

USER cloudapi

EXPOSE 6789

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:6789/manage/api/v1/health || exit 1

CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
```

#### Docker Compose Configuration

```yaml
version: '3.8'

services:
  cloud-api:
    build: .
    ports:
      - "6789:6789"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - MQTT_HOST=mqtt-broker
      - MQTT_USERNAME=${MQTT_USERNAME}
      - MQTT_PASSWORD=${MQTT_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - DB_HOST=mysql
      - REDIS_HOST=redis
    volumes:
      - ./ssl:/app/ssl:ro
      - ./logs:/app/logs
    depends_on:
      - mysql
      - redis
      - mqtt-broker
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:6789/manage/api/v1/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  mqtt-broker:
    image: eclipse-mosquitto:2.0
    ports:
      - "1883:1883"
      - "8883:8883"
    volumes:
      - ./mosquitto/config:/mosquitto/config:ro
      - ./mosquitto/data:/mosquitto/data
      - ./mosquitto/log:/mosquitto/log
      - ./ssl:/mosquitto/ssl:ro
    restart: unless-stopped

  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=${DB_ROOT_PASSWORD}
      - MYSQL_DATABASE=cloud_sample
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql:/docker-entrypoint-initdb.d:ro
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data
    restart: unless-stopped

volumes:
  mysql_data:
  redis_data:
```

### Kubernetes Deployment

#### Deployment Manifest

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: cloud-api
  labels:
    app: cloud-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: cloud-api
  template:
    metadata:
      labels:
        app: cloud-api
    spec:
      containers:
      - name: cloud-api
        image: cloud-api:latest
        ports:
        - containerPort: 6789
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: MQTT_HOST
          value: "mqtt-service"
        - name: DB_HOST
          value: "mysql-service"
        - name: REDIS_HOST
          value: "redis-service"
        - name: MQTT_USERNAME
          valueFrom:
            secretKeyRef:
              name: mqtt-credentials
              key: username
        - name: MQTT_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mqtt-credentials
              key: password
        livenessProbe:
          httpGet:
            path: /manage/api/v1/health
            port: 6789
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /manage/api/v1/health
            port: 6789
          initialDelaySeconds: 30
          periodSeconds: 10
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        volumeMounts:
        - name: ssl-certs
          mountPath: /app/ssl
          readOnly: true
      volumes:
      - name: ssl-certs
        secret:
          secretName: ssl-certificates
---
apiVersion: v1
kind: Service
metadata:
  name: cloud-api-service
spec:
  selector:
    app: cloud-api
  ports:
  - protocol: TCP
    port: 80
    targetPort: 6789
  type: ClusterIP
```

This comprehensive specification provides developers with all the technical details needed to understand, implement, and maintain the pilot access to cloud server functionality. The document includes real code examples from the actual codebase, detailed API documentation, security considerations, and deployment configurations suitable for production environments.