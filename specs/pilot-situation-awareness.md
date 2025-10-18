# Pilot Situation Awareness - Comprehensive Technical Specification

## Overview & Purpose

The Pilot Situation Awareness functionality, also known as Team Situational Awareness (TSA), provides real-time visualization and monitoring of all DJI devices, personnel, and assets within a shared workspace. This system enables collaborative drone operations by displaying the position, status, and telemetry of multiple aircraft, remote controllers, docks, and ground assets on a unified map interface within DJI Pilot 2.

### Business Value
- **Multi-Team Coordination**: Enable multiple pilots and operators to coordinate operations across shared airspace
- **Operational Safety**: Prevent conflicts and maintain safe separation between aircraft and ground assets
- **Fleet Monitoring**: Real-time oversight of multiple aircraft operations from a centralized view
- **Situational Context**: Enhanced awareness of environmental conditions, obstacles, and operational constraints
- **Emergency Response**: Rapid identification and response to emergency situations across the fleet

### Use Cases
1. **Search and Rescue Operations**: Coordinate multiple aircraft across large search areas
2. **Construction Monitoring**: Track progress across multiple work sites with various teams
3. **Security and Surveillance**: Maintain awareness of all assets during security operations
4. **Pipeline/Infrastructure Inspection**: Coordinate inspection teams across extensive linear assets
5. **Agricultural Operations**: Manage multiple aircraft for crop monitoring and treatment

### Target Devices & Compatibility

| Device Category | Model | TSA Role | Data Provided | Icon Support |
|----------------|-------|----------|---------------|--------------|
| **Remote Controllers** | RC Plus, RC Pro | Position Source | Location, status, operator info | Built-in + Custom |
| **Aircraft** | M350, M300, M30/30T, M3E/3T/3M | Mobile Asset | Full telemetry, position, mission status | Built-in + Custom |
| **Docks** | DJI Dock, Dock 2 | Fixed Asset | Status, weather, aircraft state | Built-in + Custom |
| **Ground Assets** | Vehicles, Personnel, Equipment | Context Object | Position, status, custom metadata | Custom Required |

## Technical Architecture

### System Architecture Overview

```mermaid
graph TB
    subgraph "Multi-Pilot Environment"
        P2A["DJI Pilot 2 (Team A)"]
        P2B["DJI Pilot 2 (Team B)"]
        P2C["DJI Pilot 2 (Team C)"]
        WebA["WebView A"]
        WebB["WebView B"]
        WebC["WebView C"]
    end
    
    subgraph "Cloud Platform Infrastructure"
        LB["Load Balancer"]
        Auth["Authentication Service"]
        TSA["TSA Service"]
        WebSocketCluster["WebSocket Cluster"]
        DeviceRegistry["Device Registry"]
        TopologyService["Topology Service"]
        TelemetryAggregator["Telemetry Aggregator"]
        IconService["Icon Service"]
    end
    
    subgraph "Data Sources"
        MQTT["MQTT Broker"]
        RDB["Relational Database"]
        RedisCache["Redis Cache"]
        FileStorage["File Storage"]
    end
    
    subgraph "External Assets"
        GroundVehicle["Ground Vehicles"]
        Personnel["Personnel Trackers"]
        WeatherStation["Weather Stations"]
        ThirdPartyAPI["Third-party APIs"]
    end
    
    P2A --> WebA
    P2B --> WebB  
    P2C --> WebC
    
    WebA --> LB
    WebB --> LB
    WebC --> LB
    
    LB --> Auth
    LB --> TSA
    LB --> WebSocketCluster
    
    TSA --> DeviceRegistry
    TSA --> TopologyService
    TSA --> TelemetryAggregator
    TSA --> IconService
    
    DeviceRegistry --> RDB
    TopologyService --> RDB
    TelemetryAggregator --> MQTT
    TelemetryAggregator --> RedisCache
    IconService --> FileStorage
    
    MQTT --> RDB
    
    GroundVehicle --> LB
    Personnel --> LB
    WeatherStation --> LB
    ThirdPartyAPI --> LB
```

### Data Flow Architecture

```mermaid
sequenceDiagram
    participant P2 as DJI Pilot 2
    participant WV as WebView
    participant WSGateway as WebSocket Gateway
    participant TSA as TSA Service
    participant Topology as Topology Service
    participant Telemetry as Telemetry Service
    participant MQTT as MQTT Broker
    participant Other as Other Devices

    Note over P2,Other: Initial Connection & Setup
    P2->>WV: Load TSA Module
    WV->>TSA: Load TSA Component
    TSA->>P2: TSA Module Loaded
    P2->>WSGateway: WebSocket Connection
    WSGateway-->>P2: Connection Established

    Note over P2,Other: Initial Topology Request
    P2->>Topology: GET /manage/api/v1/devices/topology?workspace_id=xxx
    Topology->>Topology: Query All Devices in Workspace
    Topology-->>P2: Device Topology Response
    P2->>P2: Display Devices on Map

    Note over P2,Other: Real-time Telemetry Updates
    loop Continuous Telemetry
        Other->>MQTT: Publish Telemetry Data
        MQTT->>Telemetry: Process Telemetry
        Telemetry->>WSGateway: Broadcast Updates
        WSGateway->>P2: Push Telemetry (WebSocket)
        P2->>P2: Update Device Positions/Status
    end

    Note over P2,Other: Device Lifecycle Events
    alt Device Online
        Other->>MQTT: Device Online Message
        MQTT->>TSA: Process Device Online
        TSA->>WSGateway: Broadcast Device Online
        WSGateway->>P2: Device Online Event
        P2->>Topology: Request Updated Topology
        Topology-->>P2: Updated Topology
    else Device Offline
        Other->>MQTT: Device Offline Message
        MQTT->>TSA: Process Device Offline
        TSA->>WSGateway: Broadcast Device Offline
        WSGateway->>P2: Device Offline Event
    end
```

## API Documentation

### REST API Endpoints

#### 1. Device Topology Retrieval

**Endpoint:** `GET /manage/api/v1/devices/topology`

**Purpose:** Retrieve the complete device topology for a workspace, including all devices, their relationships, and current status

**Authentication:** Bearer JWT Token

**Request:**
```http
GET /manage/api/v1/devices/topology?workspace_id=workspace_abc123
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Response (Success - 200):**
```json
{
    "code": 0,
    "message": "Topology retrieved successfully",
    "timestamp": 1724241600000,
    "data": {
        "list": [
            {
                "parents": [
                    {
                        "device_sn": "1ZNDH7D0010078",
                        "device_name": "Survey Team Alpha RC",
                        "device_model": {
                            "domain": 2,
                            "type": 119,
                            "sub_type": 0
                        },
                        "device_desc": "RC Plus Remote Controller",
                        "icon_urls": {
                            "normal_icon_url": "resource://Pilot2/drawable/tsa_rc_normal",
                            "selected_icon_url": "resource://Pilot2/drawable/tsa_rc_selected"
                        },
                        "location": {
                            "latitude": 40.712776,
                            "longitude": -74.005974,
                            "altitude": 10.5,
                            "accuracy": 1.2
                        },
                        "operator_info": {
                            "name": "John Doe",
                            "team": "Survey Team Alpha",
                            "role": "Lead Pilot"
                        }
                    }
                ],
                "hosts": [
                    {
                        "device_sn": "1ZNDH8G001P001",
                        "device_name": "M350-Alpha-001",
                        "device_model": {
                            "domain": 1,
                            "type": 89,
                            "sub_type": 0
                        },
                        "device_desc": "M350 RTK Enterprise Aircraft",
                        "icon_urls": {
                            "normal_icon_url": "resource://Pilot2/drawable/tsa_aircraft_m350_normal",
                            "selected_icon_url": "resource://Pilot2/drawable/tsa_aircraft_m350_selected"
                        },
                        "location": {
                            "latitude": 40.713001,
                            "longitude": -74.005823,
                            "altitude": 45.8,
                            "accuracy": 0.3
                        },
                        "telemetry": {
                            "battery_level": 87,
                            "flight_mode": "P-GPS",
                            "signal_strength": -45,
                            "home_distance": 23.5,
                            "flight_time": 485,
                            "velocity": {
                                "horizontal": 2.1,
                                "vertical": 0.0
                            },
                            "attitude": {
                                "pitch": -2.1,
                                "roll": 1.3,
                                "yaw": 245.7
                            }
                        },
                        "mission_status": {
                            "active": true,
                            "mission_type": "waypoint",
                            "progress": 65,
                            "estimated_completion": "2025-08-21T11:45:00Z"
                        }
                    }
                ]
            }
        ]
    }
}
```

#### 2. Device Status Update

**Endpoint:** `POST /manage/api/v1/devices/{device_sn}/status`

**Purpose:** Update device status and position from external sources (vehicles, personnel, etc.)

```http
POST /manage/api/v1/devices/1ZNDH7D0010078/status
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
    "location": {
        "latitude": 40.712776,
        "longitude": -74.005974,
        "altitude": 10.5,
        "accuracy": 1.2,
        "timestamp": 1724241660000
    },
    "status": {
        "online": true,
        "battery_level": 92,
        "signal_strength": -58
    },
    "metadata": {
        "operator": "John Doe",
        "team": "Survey Team Alpha",
        "activity": "Aerial Survey - Grid Pattern"
    }
}
```

### WebSocket Events

#### 1. Device Position Update

**Event Type:** `device_osd`

```json
{
    "biz_code": "device_osd",
    "version": "1.0.0",
    "timestamp": 1724241720000,
    "data": {
        "device_sn": "1ZNDH8G001P001",
        "location": {
            "latitude": 40.713125,
            "longitude": -74.005701,
            "altitude": 52.3,
            "accuracy": 0.2,
            "timestamp": 1724241720000
        },
        "telemetry": {
            "attitude_head": 245.7,
            "attitude_pitch": -1.8,
            "attitude_roll": 0.9,
            "elevation": 52.3,
            "battery": {
                "capacity": 87,
                "voltage": 23.8,
                "temperature": 28.5,
                "remaining_flight_time": 1245
            },
            "firmware_version": "07.01.10.03",
            "gear": 1,
            "height": 42.1,
            "home_distance": 28.7,
            "horizontal_speed": 3.2,
            "mode_code": 6,
            "total_flight_distance": 2.847,
            "total_flight_time": 543,
            "vertical_speed": 0.1,
            "wind_direction": 2,
            "wind_speed": 2.3,
            "position_state": {
                "gps_number": 18,
                "rtk_number": 12,
                "position_fixed": true,
                "quality": "RTK_FIXED"
            }
        },
        "mission_info": {
            "wayline_id": "wl_20250821_001",
            "current_waypoint": 8,
            "total_waypoints": 24,
            "progress": 33,
            "estimated_completion": 1247
        }
    }
}
```

#### 2. Device Online/Offline Status

**Event Type:** `device_online` / `device_offline`

```json
{
    "biz_code": "device_online",
    "version": "1.0.0", 
    "timestamp": 1724241780000,
    "data": {
        "device_sn": "1ZNDH7D0010078",
        "device_name": "Survey Team Beta RC",
        "device_type": 119,
        "workspace_id": "workspace_abc123",
        "location": {
            "latitude": 40.714523,
            "longitude": -74.003412,
            "altitude": 12.1
        },
        "operator_info": {
            "name": "Jane Smith",
            "team": "Survey Team Beta",
            "contact": "jane.smith@company.com"
        },
        "connection_time": 1724241780000
    }
}
```

#### 3. Device Topology Update

**Event Type:** `device_update_topo`

```json
{
    "biz_code": "device_update_topo",
    "version": "1.0.0",
    "timestamp": 1724241840000,
    "data": {
        "workspace_id": "workspace_abc123",
        "change_type": "AIRCRAFT_CONNECTED",
        "gateway_sn": "1ZNDH7D0010078",
        "aircraft_sn": "1ZNDH8G001P002",
        "topology": {
            "parents": [...],
            "hosts": [...]
        }
    }
}
```

## Frontend Implementation

### Vue.js TSA Integration

#### 1. TSA Service Implementation

**File:** `/Cloud-API-Demo-Web/src/services/TSAService.ts`

```typescript
import { reactive, ref, Ref } from 'vue'
import { message } from 'ant-design-vue'
import PilotBridge from '@/api/pilot-bridge'
import { cloudConnectionService } from './CloudConnectionService'

export interface DevicePosition {
    device_sn: string
    device_name: string
    device_type: number
    location: {
        latitude: number
        longitude: number
        altitude: number
        accuracy: number
        timestamp: number
    }
    telemetry?: DeviceTelemetry
    icon_urls: {
        normal_icon_url: string
        selected_icon_url: string
    }
    metadata?: Record<string, any>
}

export interface DeviceTelemetry {
    battery_level: number
    signal_strength: number
    flight_mode?: string
    velocity?: {
        horizontal: number
        vertical: number
    }
    attitude?: {
        pitch: number
        roll: number
        yaw: number
    }
}

export interface TSAState {
    devices: Map<string, DevicePosition>
    isLoaded: boolean
    selectedDevice: string | null
    error: string | null
}

class TSAService {
    private state = reactive<TSAState>({
        devices: new Map(),
        isLoaded: false,
        selectedDevice: null,
        error: null
    })

    public readonly tsaState = readonly(this.state)

    async initializeTSA(workspaceId: string): Promise<boolean> {
        try {
            // Load TSA module in DJI Pilot 2
            const tsaConfig = {
                workspaceId: workspaceId,
                mapType: 'google', // or 'mapbox', 'amap'
                enableClustering: true,
                refreshInterval: 1000 // 1 second
            }

            const loadResult = PilotBridge.loadComponent('Tsa', tsaConfig)
            if (!loadResult) {
                throw new Error('Failed to load TSA module')
            }

            // Set up WebSocket event handlers
            this.setupWebSocketHandlers()

            // Load initial device topology
            await this.loadDeviceTopology(workspaceId)

            this.state.isLoaded = true
            this.state.error = null

            console.log('TSA service initialized successfully')
            return true

        } catch (error) {
            this.state.error = error.message
            message.error(`TSA initialization failed: ${error.message}`)
            return false
        }
    }

    private setupWebSocketHandlers(): void {
        // Global WebSocket message handlers for TSA events
        window.tsaWebSocketHandlers = {
            device_osd: (data: any) => {
                this.handleDeviceOSDUpdate(data)
            },
            device_online: (data: any) => {
                this.handleDeviceOnline(data)
            },
            device_offline: (data: any) => {
                this.handleDeviceOffline(data)
            },
            device_update_topo: (data: any) => {
                this.handleTopologyUpdate(data)
            }
        }
    }

    private async loadDeviceTopology(workspaceId: string): Promise<void> {
        try {
            const response = await fetch(`/manage/api/v1/devices/topology?workspace_id=${workspaceId}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${cloudConnectionService.getToken()}`,
                    'Content-Type': 'application/json'
                }
            })

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`)
            }

            const result = await response.json()
            if (result.code !== 0) {
                throw new Error(result.message)
            }

            // Process topology data
            result.data.list.forEach(topology => {
                // Add parent devices (RCs, Docks)
                topology.parents?.forEach(parent => {
                    this.addOrUpdateDevice(parent)
                })

                // Add host devices (Aircraft, Payloads)
                topology.hosts?.forEach(host => {
                    this.addOrUpdateDevice(host)
                })
            })

        } catch (error) {
            console.error('Error loading device topology:', error)
            throw error
        }
    }

    private addOrUpdateDevice(deviceData: any): void {
        const device: DevicePosition = {
            device_sn: deviceData.device_sn,
            device_name: deviceData.device_name || deviceData.device_sn,
            device_type: deviceData.device_model?.type || 0,
            location: deviceData.location || {
                latitude: 0,
                longitude: 0,
                altitude: 0,
                accuracy: 0,
                timestamp: Date.now()
            },
            telemetry: deviceData.telemetry,
            icon_urls: deviceData.icon_urls || this.getDefaultIcons(deviceData.device_model?.type),
            metadata: deviceData.metadata
        }

        this.state.devices.set(device.device_sn, device)
    }

    private handleDeviceOSDUpdate(data: any): void {
        const deviceSn = data.device_sn
        if (!deviceSn) return

        const existingDevice = this.state.devices.get(deviceSn)
        if (!existingDevice) {
            console.warn(`Received OSD update for unknown device: ${deviceSn}`)
            return
        }

        // Update device position and telemetry
        if (data.location) {
            existingDevice.location = {
                ...existingDevice.location,
                ...data.location
            }
        }

        if (data.telemetry) {
            existingDevice.telemetry = {
                ...existingDevice.telemetry,
                ...data.telemetry
            }
        }

        // Trigger reactivity
        this.state.devices.set(deviceSn, { ...existingDevice })
    }

    private handleDeviceOnline(data: any): void {
        console.log(`Device online: ${data.device_sn}`)
        
        // Add device to map if not exists
        if (!this.state.devices.has(data.device_sn)) {
            this.addOrUpdateDevice(data)
        }

        // Show notification
        message.success(`Device ${data.device_name || data.device_sn} is now online`)
    }

    private handleDeviceOffline(data: any): void {
        console.log(`Device offline: ${data.device_sn}`)
        
        const device = this.state.devices.get(data.device_sn)
        if (device) {
            // Mark device as offline but keep it on map
            device.metadata = {
                ...device.metadata,
                status: 'offline',
                offline_time: Date.now()
            }
            this.state.devices.set(data.device_sn, device)
        }

        message.warning(`Device ${data.device_name || data.device_sn} went offline`)
    }

    private handleTopologyUpdate(data: any): void {
        console.log('Topology update received:', data)
        
        // Reload topology for the affected workspace
        if (data.workspace_id) {
            this.loadDeviceTopology(data.workspace_id).catch(error => {
                console.error('Error reloading topology:', error)
            })
        }
    }

    private getDefaultIcons(deviceType: number): { normal_icon_url: string, selected_icon_url: string } {
        const iconMap = {
            119: { // RC Plus
                normal_icon_url: 'resource://Pilot2/drawable/tsa_rc_normal',
                selected_icon_url: 'resource://Pilot2/drawable/tsa_rc_selected'
            },
            89: { // M350
                normal_icon_url: 'resource://Pilot2/drawable/tsa_aircraft_m350_normal',
                selected_icon_url: 'resource://Pilot2/drawable/tsa_aircraft_m350_selected'
            },
            67: { // M30
                normal_icon_url: 'resource://Pilot2/drawable/tsa_aircraft_m30_normal',
                selected_icon_url: 'resource://Pilot2/drawable/tsa_aircraft_m30_selected'
            },
            1: { // Dock
                normal_icon_url: 'resource://Pilot2/drawable/tsa_dock_normal',
                selected_icon_url: 'resource://Pilot2/drawable/tsa_dock_selected'
            }
        }

        return iconMap[deviceType] || {
            normal_icon_url: 'resource://Pilot2/drawable/tsa_equipment_normal',
            selected_icon_url: 'resource://Pilot2/drawable/tsa_equipment_selected'
        }
    }

    selectDevice(deviceSn: string): void {
        this.state.selectedDevice = deviceSn
    }

    getSelectedDevice(): DevicePosition | null {
        if (!this.state.selectedDevice) return null
        return this.state.devices.get(this.state.selectedDevice) || null
    }

    async unloadTSA(): Promise<void> {
        try {
            PilotBridge.unloadComponent('Tsa')
            this.state.devices.clear()
            this.state.isLoaded = false
            this.state.selectedDevice = null
            this.state.error = null

        } catch (error) {
            console.error('Error unloading TSA:', error)
        }
    }

    // Update device position from external source
    async updateDevicePosition(deviceSn: string, position: {
        latitude: number
        longitude: number
        altitude?: number
        accuracy?: number
    }): Promise<boolean> {
        try {
            const response = await fetch(`/manage/api/v1/devices/${deviceSn}/status`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${cloudConnectionService.getToken()}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    location: {
                        ...position,
                        timestamp: Date.now()
                    }
                })
            })

            return response.ok

        } catch (error) {
            console.error('Error updating device position:', error)
            return false
        }
    }
}

export const tsaService = new TSAService()
```

#### 2. TSA Map Component

**File:** `/Cloud-API-Demo-Web/src/components/TSAMapView.vue`

```vue
<template>
    <div class="tsa-map-container">
        <div class="tsa-toolbar">
            <a-space>
                <a-button @click="refreshTopology" :loading="refreshing">
                    <template #icon><ReloadOutlined /></template>
                    Refresh
                </a-button>
                
                <a-select v-model:value="filterType" placeholder="Filter Device Type">
                    <a-select-option value="">All Devices</a-select-option>
                    <a-select-option value="119">Remote Controllers</a-select-option>
                    <a-select-option value="89">M350 Aircraft</a-select-option>
                    <a-select-option value="67">M30 Aircraft</a-select-option>
                    <a-select-option value="1">Docks</a-select-option>
                </a-select>

                <a-switch 
                    v-model:checked="showTelemetry" 
                    checkedChildren="Telemetry" 
                    unCheckedChildren="Basic"
                />
                
                <a-badge :count="tsaState.devices.size" show-zero>
                    <span>Active Devices</span>
                </a-badge>
            </a-space>
        </div>

        <div class="device-list">
            <a-list 
                :data-source="filteredDevices" 
                size="small"
                :pagination="{ pageSize: 10, size: 'small' }"
            >
                <template #renderItem="{ item }">
                    <a-list-item 
                        :class="{ 'device-selected': item.device_sn === tsaState.selectedDevice }"
                        @click="selectDevice(item.device_sn)"
                    >
                        <a-list-item-meta>
                            <template #avatar>
                                <a-avatar :src="getDeviceIcon(item)" size="small" />
                            </template>
                            
                            <template #title>
                                <div class="device-title">
                                    <span>{{ item.device_name }}</span>
                                    <a-tag 
                                        :color="getDeviceStatusColor(item)"
                                        size="small"
                                    >
                                        {{ getDeviceStatus(item) }}
                                    </a-tag>
                                </div>
                            </template>
                            
                            <template #description>
                                <div class="device-info">
                                    <div v-if="item.location">
                                        <GlobalOutlined /> 
                                        {{ item.location.latitude.toFixed(6) }}, 
                                        {{ item.location.longitude.toFixed(6) }}
                                    </div>
                                    
                                    <div v-if="item.telemetry && showTelemetry">
                                        <BatteryOutlined /> 
                                        {{ item.telemetry.battery_level }}%
                                        
                                        <SignalOutlined />
                                        {{ item.telemetry.signal_strength }}dBm
                                    </div>
                                    
                                    <div v-if="item.metadata?.operator">
                                        <UserOutlined />
                                        {{ item.metadata.operator }}
                                    </div>
                                </div>
                            </template>
                        </a-list-item-meta>
                    </a-list-item>
                </template>
            </a-list>
        </div>

        <div v-if="selectedDeviceInfo" class="device-details">
            <a-card title="Device Details" size="small">
                <a-descriptions size="small" :column="2">
                    <a-descriptions-item label="Device SN">
                        {{ selectedDeviceInfo.device_sn }}
                    </a-descriptions-item>
                    <a-descriptions-item label="Device Name">
                        {{ selectedDeviceInfo.device_name }}
                    </a-descriptions-item>
                    <a-descriptions-item label="Location">
                        {{ selectedDeviceInfo.location?.latitude?.toFixed(6) }}, 
                        {{ selectedDeviceInfo.location?.longitude?.toFixed(6) }}
                    </a-descriptions-item>
                    <a-descriptions-item label="Altitude">
                        {{ selectedDeviceInfo.location?.altitude?.toFixed(1) }}m
                    </a-descriptions-item>
                    
                    <template v-if="selectedDeviceInfo.telemetry">
                        <a-descriptions-item label="Battery">
                            {{ selectedDeviceInfo.telemetry.battery_level }}%
                        </a-descriptions-item>
                        <a-descriptions-item label="Signal">
                            {{ selectedDeviceInfo.telemetry.signal_strength }}dBm
                        </a-descriptions-item>
                        <a-descriptions-item label="Speed">
                            {{ selectedDeviceInfo.telemetry.velocity?.horizontal?.toFixed(1) || 0 }}m/s
                        </a-descriptions-item>
                        <a-descriptions-item label="Flight Mode">
                            {{ selectedDeviceInfo.telemetry.flight_mode || 'N/A' }}
                        </a-descriptions-item>
                    </template>
                </a-descriptions>
            </a-card>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { tsaService, DevicePosition } from '@/services/TSAService'
import { 
    ReloadOutlined, 
    GlobalOutlined, 
    BatteryOutlined,
    SignalOutlined,
    UserOutlined
} from '@ant-design/icons-vue'

const tsaState = tsaService.tsaState
const refreshing = ref(false)
const filterType = ref('')
const showTelemetry = ref(true)

const filteredDevices = computed(() => {
    const devices = Array.from(tsaState.devices.values())
    if (!filterType.value) return devices
    
    return devices.filter(device => device.device_type.toString() === filterType.value)
})

const selectedDeviceInfo = computed(() => {
    return tsaService.getSelectedDevice()
})

const selectDevice = (deviceSn: string) => {
    tsaService.selectDevice(deviceSn)
}

const getDeviceIcon = (device: DevicePosition) => {
    return device.icon_urls?.normal_icon_url || '/icons/device-default.png'
}

const getDeviceStatus = (device: DevicePosition) => {
    if (device.metadata?.status === 'offline') return 'Offline'
    if (device.telemetry?.battery_level && device.telemetry.battery_level < 20) return 'Low Battery'
    return 'Online'
}

const getDeviceStatusColor = (device: DevicePosition) => {
    if (device.metadata?.status === 'offline') return 'red'
    if (device.telemetry?.battery_level && device.telemetry.battery_level < 20) return 'orange'
    return 'green'
}

const refreshTopology = async () => {
    refreshing.value = true
    try {
        // Get workspace ID from current context
        const workspaceId = localStorage.getItem('currentWorkspaceId')
        if (workspaceId) {
            await tsaService.loadDeviceTopology(workspaceId)
        }
    } finally {
        refreshing.value = false
    }
}

onMounted(() => {
    // TSA service should already be initialized by parent component
})

onUnmounted(() => {
    // Keep TSA service running for other components
})
</script>

<style scoped>
.tsa-map-container {
    display: flex;
    flex-direction: column;
    height: 100%;
}

.tsa-toolbar {
    padding: 16px;
    border-bottom: 1px solid #f0f0f0;
}

.device-list {
    flex: 1;
    overflow-y: auto;
    padding: 8px;
}

.device-selected {
    background-color: #e6f7ff;
    border-color: #1890ff;
}

.device-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.device-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
    font-size: 12px;
}

.device-info > div {
    display: flex;
    align-items: center;
    gap: 4px;
}

.device-details {
    border-top: 1px solid #f0f0f0;
    padding: 8px;
    max-height: 300px;
    overflow-y: auto;
}
</style>
```

## Backend Implementation

### Spring Boot Service Layer

#### 1. Topology Service Implementation

**File:** `/sample/src/main/java/com/dji/sample/manage/service/impl/TopologyServiceImpl.java`

```java
@Service
@Slf4j
public class TopologyServiceImpl implements ITopologyService {

    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private IDeviceRedisService deviceRedisService;

    @Autowired
    private IWebSocketMessageService webSocketMessageService;

    @Override
    public List<TopologyList> getDeviceTopology(String workspaceId) {
        log.info("Retrieving device topology for workspace: {}", workspaceId);

        try {
            // Query all gateway devices (RCs and Docks) in the workspace
            List<DeviceDTO> gatewayDevices = deviceService.getDevicesByParams(
                DeviceQueryParam.builder()
                    .workspaceId(workspaceId)
                    .domains(Arrays.asList(
                        DeviceDomainEnum.REMOTE_CONTROL.getDomain(),
                        DeviceDomainEnum.DOCK.getDomain()
                    ))
                    .status(true) // Only online devices
                    .build()
            );

            List<TopologyList> topologyList = new ArrayList<>();

            // Build topology for each gateway device
            for (DeviceDTO gateway : gatewayDevices) {
                Optional<TopologyList> topology = buildDeviceTopology(gateway);
                topology.ifPresent(topologyList::add);
            }

            log.info("Retrieved {} topology entries for workspace {}", topologyList.size(), workspaceId);
            return topologyList;

        } catch (Exception e) {
            log.error("Error retrieving device topology for workspace {}", workspaceId, e);
            throw new RuntimeException("Failed to retrieve device topology", e);
        }
    }

    private Optional<TopologyList> buildDeviceTopology(DeviceDTO gateway) {
        try {
            // Build parent device (gateway) information
            DeviceTopology gatewayTopology = convertToDeviceTopology(gateway);
            
            // Get real-time position and telemetry from Redis
            Optional<DevicePositionDTO> gatewayPosition = deviceRedisService.getDevicePosition(gateway.getDeviceSn());
            gatewayPosition.ifPresent(pos -> {
                gatewayTopology.setLocation(convertToLocationData(pos));
            });

            List<DeviceTopology> parents = Arrays.asList(gatewayTopology);

            // Build host devices (aircraft, payloads) connected to this gateway
            List<DeviceTopology> hosts = new ArrayList<>();
            
            if (gateway.getDomain().equals(DeviceDomainEnum.REMOTE_CONTROL.getDomain())) {
                // For RC gateways, find connected aircraft
                List<DeviceDTO> connectedAircraft = deviceService.getDevicesByParams(
                    DeviceQueryParam.builder()
                        .parentSn(gateway.getDeviceSn())
                        .domains(Arrays.asList(DeviceDomainEnum.AIRCRAFT.getDomain()))
                        .status(true)
                        .build()
                );

                for (DeviceDTO aircraft : connectedAircraft) {
                    DeviceTopology aircraftTopology = convertToDeviceTopology(aircraft);
                    
                    // Add real-time telemetry
                    Optional<DeviceTelemetryDTO> telemetry = deviceRedisService.getDeviceTelemetry(aircraft.getDeviceSn());
                    telemetry.ifPresent(telem -> {
                        aircraftTopology.setTelemetry(telem);
                    });

                    // Add mission information if available
                    Optional<MissionStatusDTO> missionStatus = getMissionStatus(aircraft.getDeviceSn());
                    missionStatus.ifPresent(mission -> {
                        aircraftTopology.setMissionStatus(mission);
                    });

                    hosts.add(aircraftTopology);
                }
            }

            return Optional.of(TopologyList.builder()
                .parents(parents)
                .hosts(hosts)
                .build());

        } catch (Exception e) {
            log.error("Error building topology for gateway {}", gateway.getDeviceSn(), e);
            return Optional.empty();
        }
    }

    private DeviceTopology convertToDeviceTopology(DeviceDTO device) {
        DeviceTopologyModel deviceModel = DeviceTopologyModel.builder()
            .domain(device.getDomain())
            .type(device.getDeviceType())
            .subType(device.getSubType())
            .build();

        // Get device icon URLs based on device type
        DeviceIconUrl iconUrls = getDeviceIconUrls(device.getDeviceType(), device.getSubType());

        return DeviceTopology.builder()
            .deviceSn(device.getDeviceSn())
            .deviceName(device.getDeviceName())
            .deviceModel(deviceModel)
            .deviceDesc(getDeviceDescription(device))
            .iconUrls(iconUrls)
            .build();
    }

    private DeviceIconUrl getDeviceIconUrls(Integer deviceType, Integer subType) {
        // Map device types to appropriate icons
        String normalIcon;
        String selectedIcon;

        switch (deviceType) {
            case 119: // RC Plus
                normalIcon = "resource://Pilot2/drawable/tsa_rc_normal";
                selectedIcon = "resource://Pilot2/drawable/tsa_rc_selected";
                break;
            case 89: // M350
                normalIcon = "resource://Pilot2/drawable/tsa_aircraft_m350_normal";
                selectedIcon = "resource://Pilot2/drawable/tsa_aircraft_m350_selected";
                break;
            case 67: // M30/M30T
                if (subType == 1) { // M30T
                    normalIcon = "resource://Pilot2/drawable/tsa_aircraft_m30t_normal";
                    selectedIcon = "resource://Pilot2/drawable/tsa_aircraft_m30t_selected";
                } else {
                    normalIcon = "resource://Pilot2/drawable/tsa_aircraft_m30_normal";
                    selectedIcon = "resource://Pilot2/drawable/tsa_aircraft_m30_selected";
                }
                break;
            case 1: // Dock
                normalIcon = "resource://Pilot2/drawable/tsa_dock_normal";
                selectedIcon = "resource://Pilot2/drawable/tsa_dock_selected";
                break;
            default:
                normalIcon = "resource://Pilot2/drawable/tsa_equipment_normal";
                selectedIcon = "resource://Pilot2/drawable/tsa_equipment_selected";
        }

        return DeviceIconUrl.builder()
            .normalIconUrl(normalIcon)
            .selectedIconUrl(selectedIcon)
            .build();
    }

    private LocationData convertToLocationData(DevicePositionDTO position) {
        return LocationData.builder()
            .latitude(position.getLatitude())
            .longitude(position.getLongitude())
            .altitude(position.getAltitude())
            .accuracy(position.getAccuracy())
            .timestamp(position.getTimestamp())
            .build();
    }

    private Optional<MissionStatusDTO> getMissionStatus(String deviceSn) {
        // Check if device is currently executing a mission
        try {
            // Query mission from wayline service
            Optional<WaylineJobDTO> activeJob = waylineJobService.getActiveJobByDeviceSn(deviceSn);
            
            if (activeJob.isPresent()) {
                WaylineJobDTO job = activeJob.get();
                return Optional.of(MissionStatusDTO.builder()
                    .active(true)
                    .missionType("waypoint")
                    .missionId(job.getJobId())
                    .progress(job.getProgress())
                    .estimatedCompletion(calculateEstimatedCompletion(job))
                    .build());
            }

        } catch (Exception e) {
            log.warn("Error retrieving mission status for device {}", deviceSn, e);
        }

        return Optional.empty();
    }

    @Override
    public void broadcastTopologyUpdate(String workspaceId, TopologyChangeType changeType, 
                                      String gatewaySn, String deviceSn) {
        try {
            // Create topology update message
            TopologyUpdateMessage updateMessage = TopologyUpdateMessage.builder()
                .workspaceId(workspaceId)
                .changeType(changeType.name())
                .gatewaySn(gatewaySn)
                .deviceSn(deviceSn)
                .timestamp(System.currentTimeMillis())
                .build();

            // Send via WebSocket to all clients in workspace
            WebSocketMessageResponse<TopologyUpdateMessage> wsMessage = 
                WebSocketMessageResponse.<TopologyUpdateMessage>builder()
                    .bizCode(BizCodeEnum.DEVICE_UPDATE_TOPO.getCode())
                    .timestamp(System.currentTimeMillis())
                    .data(updateMessage)
                    .build();

            webSocketMessageService.sendMessageToWorkspace(workspaceId, wsMessage);

            log.info("Broadcasted topology update for workspace {} - {} device {}", 
                workspaceId, changeType, deviceSn);

        } catch (Exception e) {
            log.error("Error broadcasting topology update", e);
        }
    }
}
```

#### 2. Real-time Telemetry Handler

**File:** `/sample/src/main/java/com/dji/sample/manage/service/impl/DeviceTelemetryService.java`

```java
@Service
@Slf4j
public class DeviceTelemetryService {

    @Autowired
    private IDeviceRedisService deviceRedisService;

    @Autowired
    private IWebSocketMessageService webSocketMessageService;

    @EventListener
    public void handleDeviceOSDUpdate(DeviceOSDEvent event) {
        try {
            String deviceSn = event.getDeviceSn();
            OsdData osdData = event.getOsdData();

            // Store telemetry in Redis for fast access
            DeviceTelemetryDTO telemetry = convertOSDToTelemetry(osdData);
            deviceRedisService.setDeviceTelemetry(deviceSn, telemetry);

            // Update device position if available
            if (osdData.getLatitude() != null && osdData.getLongitude() != null) {
                DevicePositionDTO position = DevicePositionDTO.builder()
                    .deviceSn(deviceSn)
                    .latitude(osdData.getLatitude().doubleValue())
                    .longitude(osdData.getLongitude().doubleValue())
                    .altitude(osdData.getHeight() != null ? osdData.getHeight().doubleValue() : 0.0)
                    .accuracy(1.0) // GPS accuracy
                    .timestamp(System.currentTimeMillis())
                    .build();

                deviceRedisService.setDevicePosition(deviceSn, position);
            }

            // Broadcast OSD data to connected clients
            broadcastOSDUpdate(deviceSn, osdData, telemetry);

        } catch (Exception e) {
            log.error("Error handling device OSD update for device {}", event.getDeviceSn(), e);
        }
    }

    private DeviceTelemetryDTO convertOSDToTelemetry(OsdData osdData) {
        return DeviceTelemetryDTO.builder()
            .batteryLevel(osdData.getBattery() != null ? osdData.getBattery().getCapacity() : null)
            .signalStrength(calculateSignalStrength(osdData))
            .flightMode(getFlightModeString(osdData.getModeCode()))
            .velocity(VelocityDTO.builder()
                .horizontal(osdData.getHorizontalSpeed())
                .vertical(osdData.getVerticalSpeed())
                .build())
            .attitude(AttitudeDTO.builder()
                .pitch(osdData.getAttitudePitch())
                .roll(osdData.getAttitudeRoll())
                .yaw(osdData.getAttitudeHead())
                .build())
            .homeDistance(osdData.getHomeDistance())
            .flightTime(osdData.getTotalFlightTime() != null ? osdData.getTotalFlightTime().longValue() : null)
            .windSpeed(osdData.getWindSpeed())
            .windDirection(osdData.getWindDirection() != null ? osdData.getWindDirection().name() : null)
            .gpsInfo(GPSInfoDTO.builder()
                .satelliteCount(osdData.getPositionState() != null ? osdData.getPositionState().getGpsNumber() : null)
                .quality(osdData.getPositionState() != null ? osdData.getPositionState().getQuality() : null)
                .build())
            .timestamp(System.currentTimeMillis())
            .build();
    }

    private void broadcastOSDUpdate(String deviceSn, OsdData osdData, DeviceTelemetryDTO telemetry) {
        try {
            // Get device information
            Optional<DeviceDTO> deviceOpt = deviceService.getDeviceBySn(deviceSn);
            if (deviceOpt.isEmpty()) {
                return;
            }

            DeviceDTO device = deviceOpt.get();

            // Create WebSocket message
            DeviceOSDWebSocketMessage wsMessage = DeviceOSDWebSocketMessage.builder()
                .deviceSn(deviceSn)
                .location(LocationDTO.builder()
                    .latitude(osdData.getLatitude() != null ? osdData.getLatitude().doubleValue() : null)
                    .longitude(osdData.getLongitude() != null ? osdData.getLongitude().doubleValue() : null)
                    .altitude(osdData.getHeight() != null ? osdData.getHeight().doubleValue() : null)
                    .timestamp(System.currentTimeMillis())
                    .build())
                .telemetry(telemetry)
                .build();

            WebSocketMessageResponse<DeviceOSDWebSocketMessage> response = 
                WebSocketMessageResponse.<DeviceOSDWebSocketMessage>builder()
                    .bizCode(BizCodeEnum.DEVICE_OSD.getCode())
                    .version("1.0.0")
                    .timestamp(System.currentTimeMillis())
                    .data(wsMessage)
                    .build();

            // Send to all clients in the device's workspace
            webSocketMessageService.sendMessageToWorkspace(device.getWorkspaceId(), response);

        } catch (Exception e) {
            log.error("Error broadcasting OSD update for device {}", deviceSn, e);
        }
    }

    private Integer calculateSignalStrength(OsdData osdData) {
        // Calculate signal strength based on available data
        // This is a simplified implementation
        return -50; // Example value
    }

    private String getFlightModeString(DroneModeCodeEnum modeCode) {
        if (modeCode == null) return "Unknown";
        
        switch (modeCode) {
            case MANUAL: return "Manual";
            case ATTI: return "Attitude";
            case P_GPS: return "P-GPS";
            case WAYPOINT: return "Waypoint";
            case GO_HOME: return "RTH";
            case FORCE_LANDING: return "Landing";
            default: return "Unknown";
        }
    }
}
```

## Device Integration

### Supported Device Categories

#### 1. Remote Controller Integration

```java
// RC Device Telemetry Structure
@Data
@Builder
public class RCTelemetryData {
    private String deviceSn;
    private LocationData location;
    private Integer batteryLevel;
    private Integer signalStrength;
    private String operatorName;
    private String teamAssignment;
    private Long lastHeartbeat;
    
    // RC-specific data
    private Boolean aircraftConnected;
    private String connectedAircraftSn;
    private RCStatusEnum status;
    private Integer temperature;
    private String firmwareVersion;
}
```

#### 2. Aircraft Integration

```java
// Aircraft Real-time Telemetry
@Data
@Builder
public class AircraftTelemetryData {
    private String deviceSn;
    private LocationData location;
    private AttitudeData attitude;
    private VelocityData velocity;
    private BatteryData battery;
    private FlightStatusData flightStatus;
    private MissionData missionData;
    private SensorData sensors;
    private Long timestamp;
    
    @Data
    @Builder
    public static class FlightStatusData {
        private String flightMode;
        private Boolean isFlying;
        private Float homeDistance;
        private Integer flightTime;
        private Float totalDistance;
        private Boolean rtkFixed;
        private Integer satelliteCount;
    }
    
    @Data
    @Builder
    public static class MissionData {
        private Boolean missionActive;
        private String missionType;
        private String missionId;
        private Integer currentWaypoint;
        private Integer totalWaypoints;
        private Integer progress;
        private Long estimatedCompletion;
    }
}
```

#### 3. Dock Integration

```java
// Dock Status and Environmental Data
@Data
@Builder
public class DockTelemetryData {
    private String deviceSn;
    private LocationData location;
    private EnvironmentalData environmental;
    private DockStatusData status;
    private AircraftData dockedAircraft;
    private MaintenanceData maintenance;
    
    @Data
    @Builder
    public static class EnvironmentalData {
        private Float temperature;
        private Float humidity;
        private Float windSpeed;
        private String windDirection;
        private Float rainfall;
        private String weatherCondition;
    }
    
    @Data
    @Builder
    public static class DockStatusData {
        private Boolean operational;
        private String coverState; // OPEN, CLOSED, OPENING, CLOSING
        private String chargeState; // CHARGING, IDLE, ERROR
        private Boolean remoteAccessible;
        private Integer batteryLevel;
        private Boolean maintenanceMode;
    }
}
```

### Communication Protocols

#### MQTT Topic Structure for TSA

```java
// TSA-specific MQTT topics
public class TSAMqttTopics {
    
    // Device position updates
    public static final String DEVICE_POSITION = "thing/product/{device_sn}/position";
    
    // Real-time telemetry
    public static final String DEVICE_OSD = "thing/product/{device_sn}/osd";
    
    // Status updates
    public static final String DEVICE_STATUS = "thing/product/{device_sn}/status";
    
    // Mission updates
    public static final String MISSION_STATUS = "thing/product/{device_sn}/mission";
    
    // Emergency alerts
    public static final String EMERGENCY_ALERT = "thing/product/{device_sn}/emergency";
}

// Message publishing for TSA updates
@Component
public class TSAMqttPublisher {
    
    @Autowired
    private IMqttMessageGateway mqttGateway;
    
    public void publishPositionUpdate(String deviceSn, DevicePositionDTO position) {
        String topic = TSAMqttTopics.DEVICE_POSITION.replace("{device_sn}", deviceSn);
        
        PositionUpdateMessage message = PositionUpdateMessage.builder()
            .deviceSn(deviceSn)
            .latitude(position.getLatitude())
            .longitude(position.getLongitude())
            .altitude(position.getAltitude())
            .accuracy(position.getAccuracy())
            .timestamp(System.currentTimeMillis())
            .build();
            
        mqttGateway.publish(topic, message, 1); // QoS 1 for reliable delivery
    }
    
    public void publishEmergencyAlert(String deviceSn, EmergencyAlertDTO alert) {
        String topic = TSAMqttTopics.EMERGENCY_ALERT.replace("{device_sn}", deviceSn);
        
        EmergencyAlertMessage message = EmergencyAlertMessage.builder()
            .deviceSn(deviceSn)
            .alertType(alert.getType())
            .severity(alert.getSeverity())
            .description(alert.getDescription())
            .location(alert.getLocation())
            .timestamp(System.currentTimeMillis())
            .build();
            
        mqttGateway.publish(topic, message, 2); // QoS 2 for critical messages
    }
}
```

## Data Models

### TSA-Specific Data Structures

#### Device Topology Response

```typescript
interface TopologyResponse {
    code: number;
    message: string;
    timestamp: number;
    data: {
        list: TopologyGroup[];
    };
}

interface TopologyGroup {
    parents: DeviceTopology[];  // Gateway devices (RC, Dock)
    hosts: DeviceTopology[];    // Connected devices (Aircraft, Payloads)
}

interface DeviceTopology {
    device_sn: string;
    device_name: string;
    device_model: {
        domain: number;
        type: number;
        sub_type: number;
    };
    device_desc: string;
    icon_urls: {
        normal_icon_url: string;
        selected_icon_url: string;
    };
    location?: LocationData;
    telemetry?: DeviceTelemetryData;
    operator_info?: OperatorInfo;
    mission_status?: MissionStatus;
}

interface LocationData {
    latitude: number;
    longitude: number;
    altitude: number;
    accuracy: number;
    timestamp: number;
}

interface DeviceTelemetryData {
    battery_level: number;
    signal_strength: number;
    flight_mode?: string;
    velocity?: {
        horizontal: number;
        vertical: number;
    };
    attitude?: {
        pitch: number;
        roll: number;
        yaw: number;
    };
    home_distance?: number;
    flight_time?: number;
    wind_speed?: number;
    wind_direction?: string;
    gps_info?: {
        satellite_count: number;
        quality: string;
    };
}

interface OperatorInfo {
    name: string;
    team?: string;
    role?: string;
    contact?: string;
}

interface MissionStatus {
    active: boolean;
    mission_type: string;
    mission_id?: string;
    current_waypoint?: number;
    total_waypoints?: number;
    progress: number;
    estimated_completion?: string;
}
```

### WebSocket Message Formats

#### Device OSD Update Message

```json
{
    "biz_code": "device_osd",
    "version": "1.0.0",
    "timestamp": 1724241720000,
    "data": {
        "device_sn": "1ZNDH8G001P001",
        "location": {
            "latitude": 40.713125,
            "longitude": -74.005701,
            "altitude": 52.3,
            "accuracy": 0.2,
            "timestamp": 1724241720000
        },
        "telemetry": {
            "attitude_head": 245.7,
            "attitude_pitch": -1.8,
            "attitude_roll": 0.9,
            "elevation": 52.3,
            "battery": {
                "capacity": 87,
                "voltage": 23.8,
                "temperature": 28.5,
                "remaining_flight_time": 1245
            },
            "firmware_version": "07.01.10.03",
            "gear": 1,
            "height": 42.1,
            "home_distance": 28.7,
            "horizontal_speed": 3.2,
            "mode_code": 6,
            "total_flight_distance": 2.847,
            "total_flight_time": 543,
            "vertical_speed": 0.1,
            "wind_direction": 2,
            "wind_speed": 2.3,
            "position_state": {
                "gps_number": 18,
                "rtk_number": 12,
                "position_fixed": true,
                "quality": "RTK_FIXED"
            }
        },
        "mission_info": {
            "wayline_id": "wl_20250821_001",
            "current_waypoint": 8,
            "total_waypoints": 24,
            "progress": 33,
            "estimated_completion": 1247
        }
    }
}
```

## Communication Workflows

### TSA Data Flow Sequence

```mermaid
sequenceDiagram
    participant RC as Remote Controller
    participant Aircraft as Aircraft
    participant MQTT as MQTT Broker
    participant TSA as TSA Service
    participant WebSocket as WebSocket Gateway
    participant P2A as Pilot 2 (Team A)
    participant P2B as Pilot 2 (Team B)
    participant P2C as Pilot 2 (Team C)

    Note over RC,P2C: Initial Setup Phase
    P2A->>TSA: Load TSA Module
    P2B->>TSA: Load TSA Module  
    P2C->>TSA: Load TSA Module
    TSA->>TSA: Register WebSocket Connections

    Note over RC,P2C: Position Broadcasting
    loop Every 1 Second
        RC->>MQTT: Publish Position Update
        Aircraft->>MQTT: Publish OSD Telemetry
        MQTT->>TSA: Process Updates
        TSA->>WebSocket: Broadcast to All Teams
        WebSocket->>P2A: Push Updates
        WebSocket->>P2B: Push Updates
        WebSocket->>P2C: Push Updates
        P2A->>P2A: Update Map Display
        P2B->>P2B: Update Map Display
        P2C->>P2C: Update Map Display
    end

    Note over RC,P2C: Event-Driven Updates
    alt Aircraft Emergency
        Aircraft->>MQTT: Emergency Alert
        MQTT->>TSA: Process Emergency
        TSA->>WebSocket: Priority Broadcast
        WebSocket->>P2A: Emergency Notification
        WebSocket->>P2B: Emergency Notification
        WebSocket->>P2C: Emergency Notification
    else New Device Online
        RC->>MQTT: Device Online
        MQTT->>TSA: Update Topology
        TSA->>WebSocket: Topology Update
        WebSocket->>P2A: Request Topology Refresh
        P2A->>TSA: GET Updated Topology
    end
```

### Error Handling & Fallbacks

```mermaid
flowchart TD
    A[TSA Data Update] --> B{Data Valid?}
    B -->|Yes| C[Update Device State]
    B -->|No| D[Log Validation Error]
    
    C --> E{WebSocket Connected?}
    E -->|Yes| F[Broadcast Update]
    E -->|No| G[Queue Message]
    
    F --> H{All Clients Reached?}
    H -->|Yes| I[Update Complete]
    H -->|No| J[Retry Failed Connections]
    
    G --> K[Attempt Reconnection]
    K --> L{Reconnection Success?}
    L -->|Yes| M[Send Queued Messages]
    L -->|No| N[Exponential Backoff]
    
    N --> O{Max Retries?}
    O -->|No| K
    O -->|Yes| P[Mark Client Offline]
    
    D --> Q[Send Error Response]
    J --> I
    M --> I
    P --> R[Cleanup Resources]
```

## Security Considerations

### Data Privacy & Access Control

1. **Workspace Isolation**:
   - Strict workspace-based data segregation
   - Role-based access controls for TSA data
   - Audit logging for data access

```java
@PreAuthorize("hasPermission(#workspaceId, 'WORKSPACE', 'READ')")
public List<TopologyList> getDeviceTopology(String workspaceId) {
    // Implementation
}
```

2. **Location Data Protection**:
   - Encrypt sensitive location data at rest
   - Implement location data retention policies
   - Support location data anonymization

3. **Real-time Data Security**:
   - WebSocket connection authentication
   - Message-level encryption for sensitive data
   - Rate limiting for TSA updates

### Performance Optimizations

1. **Caching Strategy**:
   - Redis caching for frequently accessed topology data
   - Client-side caching with TTL policies
   - Efficient diff-based updates

2. **Scalability Considerations**:
   - WebSocket connection pooling
   - Horizontal scaling of TSA services
   - Load balancing for high-throughput scenarios

## Configuration & Deployment

### TSA-Specific Configuration

```yaml
# application.yml - TSA Configuration
tsa:
  enabled: true
  update_interval: 1000  # 1 second
  max_devices_per_workspace: 100
  position_accuracy_threshold: 10.0  # meters
  telemetry_retention: 3600  # 1 hour in seconds
  
  websocket:
    max_connections_per_workspace: 50
    heartbeat_interval: 30000  # 30 seconds
    
  caching:
    topology_ttl: 300  # 5 minutes
    telemetry_ttl: 60   # 1 minute
    position_ttl: 30    # 30 seconds

  icons:
    cache_size: 1000
    cdn_base_url: "https://cdn.example.com/tsa-icons/"
    default_icon_set: "dji_standard"
```

### Monitoring & Metrics

```yaml
# Prometheus metrics for TSA
management:
  endpoints:
    web:
      exposure:
        include: "health,metrics,prometheus"
  metrics:
    tags:
      service: "tsa-service"
    export:
      prometheus:
        enabled: true
        
# Custom TSA metrics
tsa:
  metrics:
    - name: "tsa_active_devices"
      type: "gauge"
      description: "Number of active devices in TSA system"
    - name: "tsa_websocket_connections"
      type: "gauge" 
      description: "Active WebSocket connections"
    - name: "tsa_position_updates_total"
      type: "counter"
      description: "Total position updates processed"
```

This comprehensive specification provides developers with complete technical documentation for implementing and maintaining the Pilot Situation Awareness functionality. The document covers all aspects from API integration to real-time data handling, security considerations, and deployment configurations.