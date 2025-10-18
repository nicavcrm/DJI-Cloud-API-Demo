# Pilot Live Stream - Comprehensive Technical Specification

## Overview & Purpose

The Pilot Live Stream functionality enables real-time video streaming from DJI aircraft cameras and payloads to third-party cloud platforms through the remote controller gateway. This system supports multiple streaming protocols and provides low-latency video transmission for remote monitoring, surveillance, inspection, and broadcasting applications.

### Business Value
- **Remote Visual Monitoring**: Real-time oversight of operations without physical presence
- **Multi-viewer Broadcasting**: Stream to multiple viewers simultaneously across different locations
- **Incident Documentation**: Live recording and streaming of critical events or inspections
- **Training & Demonstration**: Remote training sessions and equipment demonstrations
- **Emergency Response**: Real-time visual intelligence for first responders and command centers
- **Quality Assurance**: Live monitoring of inspection and survey activities

### Use Cases
1. **Infrastructure Inspection**: Stream live video during bridge, power line, or building inspections
2. **Search & Rescue Operations**: Provide real-time aerial imagery to command centers
3. **Security & Surveillance**: Monitor large areas or events with live streaming capabilities
4. **Broadcasting & Media**: Professional drone footage for live television or web broadcasts
5. **Remote Training**: Train operators by streaming live flight operations
6. **Construction Monitoring**: Real-time progress monitoring of construction sites

### Supported Streaming Protocols

| Protocol | Description | Use Case | Latency | Quality |
|----------|-------------|----------|---------|---------|
| **Agora** | Interactive Live Streaming with low latency | Real-time communication, training | 100-300ms | Excellent |
| **RTMP** | Real-Time Messaging Protocol over TCP | Broadcasting, social media streaming | 2-5 seconds | Good |
| **RTSP** | Real-Time Streaming Protocol over TCP/UDP | Security systems, IP cameras | 1-3 seconds | Good |
| **GB28181** | Chinese security video standard | Surveillance systems, government | 1-4 seconds | Good |
| **WebRTC** | Web Real-Time Communication | Browser-based viewing | 100-500ms | Excellent |

## Technical Architecture

### System Architecture Overview

```mermaid
graph TB
    subgraph "Aircraft & Payload Systems"
        Aircraft["DJI Aircraft"]
        MainCamera["Main Camera"]
        ThermalCamera["Thermal Camera"]
        ZoomCamera["Zoom Camera"]
        PayloadSDK["Payload SDK Cameras"]
    end
    
    subgraph "Remote Controller Gateway"
        RC["RC Plus/Pro"]
        Pilot2["DJI Pilot 2"]
        LiveModule["Live Stream Module"]
        Encoder["Video Encoder"]
        NetworkStack["Network Stack"]
    end
    
    subgraph "Cloud Platform"
        LoadBalancer["Load Balancer"]
        StreamingGateway["Streaming Gateway"]
        AgoraService["Agora Service"]
        RTMPServer["RTMP Server"]
        RTSPServer["RTSP Server"]
        GB28181Gateway["GB28181 Gateway"]
        MQTTBroker["MQTT Broker"]
    end
    
    subgraph "Viewer Applications"
        WebBrowser["Web Browser"]
        MobileApp["Mobile App"]
        RTSPClient["RTSP Client"]
        SecuritySystem["Security System"]
    end
    
    Aircraft --> MainCamera
    Aircraft --> ThermalCamera
    Aircraft --> ZoomCamera
    Aircraft --> PayloadSDK
    
    MainCamera --> RC
    ThermalCamera --> RC
    ZoomCamera --> RC
    PayloadSDK --> RC
    
    RC --> Pilot2
    Pilot2 --> LiveModule
    LiveModule --> Encoder
    Encoder --> NetworkStack
    
    NetworkStack --> LoadBalancer
    LoadBalancer --> StreamingGateway
    LoadBalancer --> MQTTBroker
    
    StreamingGateway --> AgoraService
    StreamingGateway --> RTMPServer
    StreamingGateway --> RTSPServer
    StreamingGateway --> GB28181Gateway
    
    AgoraService --> WebBrowser
    RTMPServer --> WebBrowser
    RTSPServer --> RTSPClient
    GB28181Gateway --> SecuritySystem
    RTMPServer --> MobileApp
```

### Live Stream Initialization Flow

```mermaid
sequenceDiagram
    participant P2 as DJI Pilot 2
    participant WV as WebView H5
    participant Cloud as Cloud Platform
    participant MQTT as MQTT Broker
    participant Stream as Streaming Server
    participant Viewer as Viewer Client

    Note over P2,Viewer: Phase 1: Module Loading & Capability Discovery
    
    WV->>P2: Load Live Stream Module
    P2->>P2: Initialize Live Stream Module
    P2->>MQTT: Publish Live Capabilities
    MQTT->>Cloud: Forward Capabilities
    Cloud->>Cloud: Store Live Capabilities
    
    Note over P2,Viewer: Phase 2: Stream Configuration
    
    WV->>Cloud: Request Live Capabilities
    Cloud-->>WV: Return Available Cameras/Quality
    WV->>Cloud: Configure Stream (Protocol, Quality, Camera)
    Cloud->>MQTT: Send Live Start Command
    MQTT->>P2: Deliver Start Command
    
    Note over P2,Viewer: Phase 3: Stream Establishment
    
    P2->>P2: Validate Stream Requirements
    P2->>P2: Initialize Video Encoder
    P2->>Stream: Establish Stream Connection
    Stream-->>P2: Connection Confirmed
    P2->>MQTT: Report Stream Status
    MQTT->>Cloud: Update Stream Status
    
    Note over P2,Viewer: Phase 4: Active Streaming
    
    loop Continuous Streaming
        P2->>Stream: Push Video Data
        Stream->>Viewer: Deliver Video Stream
        P2->>MQTT: Stream Health Metrics
        MQTT->>Cloud: Metrics & Status Updates
    end
    
    Note over P2,Viewer: Phase 5: Stream Termination
    
    alt User Stop Request
        WV->>Cloud: Stop Stream Request
        Cloud->>MQTT: Send Stop Command
        MQTT->>P2: Stop Stream
    else Connection Lost
        P2->>P2: Detect Connection Loss
        P2->>P2: Auto-stop Stream
    end
    
    P2->>Stream: Close Stream Connection
    P2->>MQTT: Report Stream Stopped
    MQTT->>Cloud: Update Status
    Cloud->>WV: Confirm Stream Stopped
```

## API Documentation

### REST API Endpoints

#### 1. Get Live Stream Capabilities

**Endpoint:** `GET /manage/api/v1/livestream/capacity/{workspace_id}`

**Purpose:** Retrieve available live stream capabilities for all devices in a workspace

**Authentication:** Bearer JWT Token

**Request:**
```http
GET /manage/api/v1/livestream/capacity/workspace_abc123
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Response (Success - 200):**
```json
{
    "code": 0,
    "message": "Live capabilities retrieved successfully",
    "timestamp": 1724241600000,
    "data": [
        {
            "name": "Survey-M350-Alpha",
            "sn": "1ZNDH8G001P001",
            "device_type": 89,
            "online_status": true,
            "cameras_list": [
                {
                    "camera_index": "52-0-0",
                    "camera_name": "H30 Main Camera",
                    "camera_type": "main",
                    "available_video_number": 4,
                    "coexist_video_number_max": 2,
                    "video_list": [
                        {
                            "video_index": "52-0-0",
                            "video_type": "wide",
                            "resolution": "1920x1080",
                            "max_bitrate": 8000,
                            "supported_qualities": [0, 1, 2, 3],
                            "switchable_video_types": ["wide", "zoom"]
                        },
                        {
                            "video_index": "52-0-1", 
                            "video_type": "zoom",
                            "resolution": "1920x1080",
                            "max_bitrate": 10000,
                            "supported_qualities": [0, 1, 2, 3],
                            "zoom_range": "1x-56x"
                        },
                        {
                            "video_index": "52-0-2",
                            "video_type": "ir",
                            "resolution": "640x512",
                            "max_bitrate": 2000,
                            "supported_qualities": [0, 1, 2],
                            "thermal_modes": ["white_hot", "black_hot", "rainbow"]
                        }
                    ]
                }
            ],
            "supported_protocols": ["agora", "rtmp", "rtsp", "gb28181"],
            "max_concurrent_streams": 2,
            "stream_status": {
                "active_streams": 0,
                "available_streams": 2
            }
        }
    ]
}
```

#### 2. Start Live Stream

**Endpoint:** `POST /manage/api/v1/livestream/start`

**Purpose:** Start a live stream for a specific camera and video feed

**Request:**
```http
POST /manage/api/v1/livestream/start
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
    "device_sn": "1ZNDH8G001P001",
    "video_id": "1ZNDH8G001P001/52-0-0/normal",
    "protocol": "agora",
    "video_quality": 2,
    "protocol_config": {
        "agora": {
            "app_id": "your_agora_app_id",
            "channel": "livestream_channel_001",
            "token": "your_agora_token",
            "uid": 12345
        }
    },
    "stream_settings": {
        "auto_bitrate": true,
        "max_bitrate": 8000,
        "frame_rate": 30,
        "resolution": "1920x1080"
    }
}
```

**Response (Success - 200):**
```json
{
    "code": 0,
    "message": "Live stream started successfully",
    "timestamp": 1724241720000,
    "data": {
        "stream_id": "stream_20250821_001",
        "device_sn": "1ZNDH8G001P001",
        "video_id": "1ZNDH8G001P001/52-0-0/normal",
        "protocol": "agora",
        "stream_url": "agora://your_channel_name",
        "viewer_info": {
            "web_player_url": "https://demo.example.com/player?stream=stream_20250821_001",
            "mobile_player_url": "https://demo.example.com/mobile-player?stream=stream_20250821_001",
            "rtmp_playback_url": "rtmp://streaming.example.com/live/stream_20250821_001"
        },
        "stream_status": "starting",
        "start_time": 1724241720000,
        "estimated_startup_time": 5000
    }
}
```

#### 3. Update Stream Quality

**Endpoint:** `PUT /manage/api/v1/livestream/quality`

**Purpose:** Change the video quality of an active live stream

**Request:**
```http
PUT /manage/api/v1/livestream/quality
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
    "stream_id": "stream_20250821_001",
    "video_id": "1ZNDH8G001P001/52-0-0/normal", 
    "video_quality": 3,
    "bitrate_settings": {
        "target_bitrate": 10000,
        "min_bitrate": 5000,
        "max_bitrate": 12000
    }
}
```

**Response:**
```json
{
    "code": 0,
    "message": "Stream quality updated successfully",
    "timestamp": 1724241780000,
    "data": {
        "stream_id": "stream_20250821_001",
        "previous_quality": 2,
        "new_quality": 3,
        "update_time": 1724241780000
    }
}
```

#### 4. Stop Live Stream

**Endpoint:** `DELETE /manage/api/v1/livestream/stop`

**Purpose:** Stop an active live stream

**Request:**
```http
DELETE /manage/api/v1/livestream/stop
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
    "stream_id": "stream_20250821_001",
    "video_id": "1ZNDH8G001P001/52-0-0/normal"
}
```

**Response:**
```json
{
    "code": 0,
    "message": "Live stream stopped successfully",
    "timestamp": 1724241840000,
    "data": {
        "stream_id": "stream_20250821_001",
        "stop_time": 1724241840000,
        "total_duration": 120000,
        "total_data_transferred": "1.2 GB",
        "final_status": "stopped"
    }
}
```

### MQTT Communication

#### 1. Live Stream Capability Report

**Topic:** `thing/product/{gateway_sn}/state`  
**QoS:** 1

```json
{
    "tid": "capability_report_001",
    "bid": "state_update_001",
    "timestamp": 1724241600000,
    "data": {
        "live_capacity": {
            "available_video_number": 4,
            "coexist_video_number_max": 2,
            "device_list": [
                {
                    "sn": "1ZNDH8G001P001",
                    "available_video_number": 4,
                    "coexist_video_number_max": 2,
                    "camera_list": [
                        {
                            "camera_index": "52-0-0",
                            "camera_name": "H30 Main Camera",
                            "available_video_number": 4,
                            "coexist_video_number_max": 2,
                            "video_list": [
                                {
                                    "video_index": "52-0-0",
                                    "video_type": "wide",
                                    "switchable_video_types": ["wide", "zoom", "ir"]
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    }
}
```

#### 2. Start Live Stream Command

**Topic:** `thing/product/{gateway_sn}/services`  
**QoS:** 1

```json
{
    "tid": "live_start_001", 
    "bid": "cmd_live_start_001",
    "timestamp": 1724241720000,
    "method": "live_start_push",
    "data": {
        "video_id": "1ZNDH8G001P001/52-0-0/normal",
        "video_quality": 2,
        "url_type": 1,
        "url": {
            "app_id": "your_agora_app_id",
            "channel": "livestream_channel_001", 
            "token": "your_agora_token",
            "uid": 12345
        }
    }
}
```

#### 3. Live Stream Status Update

**Topic:** `thing/product/{gateway_sn}/osd`  
**QoS:** 0

```json
{
    "tid": "osd_update_001",
    "timestamp": 1724241750000,
    "data": {
        "live_status": [
            {
                "video_id": "1ZNDH8G001P001/52-0-0/normal",
                "video_quality": 2,
                "live_time": 30,
                "live_trendline": 7,
                "error_status": 0,
                "stream_metrics": {
                    "bitrate": 8500,
                    "frame_rate": 29.8,
                    "resolution": "1920x1080",
                    "dropped_frames": 2,
                    "network_quality": "good"
                }
            }
        ]
    }
}
```

### WebSocket Events

#### 1. Stream Status Change Event

**Event Type:** `livestream_status`

```json
{
    "biz_code": "livestream_status",
    "version": "1.0.0",
    "timestamp": 1724241780000,
    "data": {
        "stream_id": "stream_20250821_001",
        "device_sn": "1ZNDH8G001P001",
        "video_id": "1ZNDH8G001P001/52-0-0/normal",
        "status": "streaming",
        "previous_status": "starting",
        "stream_health": {
            "bitrate": 8500,
            "frame_rate": 29.8,
            "dropped_frames": 2,
            "latency": 150,
            "network_quality": "good"
        },
        "viewer_count": 3
    }
}
```

#### 2. Stream Error Event

**Event Type:** `livestream_error`

```json
{
    "biz_code": "livestream_error",
    "version": "1.0.0",
    "timestamp": 1724241840000,
    "data": {
        "stream_id": "stream_20250821_001",
        "device_sn": "1ZNDH8G001P001",
        "error_code": 311002,
        "error_message": "Network connection unstable",
        "severity": "warning",
        "recovery_action": "auto_retry",
        "retry_count": 2,
        "max_retries": 5
    }
}
```

## Frontend Implementation

### Vue.js Live Stream Integration

#### 1. Live Stream Service

**File:** `/Cloud-API-Demo-Web/src/services/LiveStreamService.ts`

```typescript
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PilotBridge from '@/api/pilot-bridge'
import { cloudConnectionService } from './CloudConnectionService'

export interface StreamCapability {
    name: string
    sn: string
    device_type: number
    online_status: boolean
    cameras_list: CameraCapability[]
    supported_protocols: string[]
    max_concurrent_streams: number
}

export interface CameraCapability {
    camera_index: string
    camera_name: string
    camera_type: string
    available_video_number: number
    coexist_video_number_max: number
    video_list: VideoCapability[]
}

export interface VideoCapability {
    video_index: string
    video_type: string
    resolution: string
    max_bitrate: number
    supported_qualities: number[]
    switchable_video_types?: string[]
}

export interface StreamConfig {
    device_sn: string
    video_id: string
    protocol: 'agora' | 'rtmp' | 'rtsp' | 'gb28181'
    video_quality: number
    protocol_config: any
}

export interface StreamStatus {
    stream_id: string
    device_sn: string
    video_id: string
    status: 'starting' | 'streaming' | 'stopping' | 'stopped' | 'error'
    start_time: number
    viewer_count?: number
    stream_health?: {
        bitrate: number
        frame_rate: number
        dropped_frames: number
        latency: number
        network_quality: string
    }
}

class LiveStreamService {
    private state = reactive({
        capabilities: [] as StreamCapability[],
        activeStreams: new Map<string, StreamStatus>(),
        isLoaded: false,
        error: null as string | null
    })

    public readonly streamState = readonly(this.state)

    async initializeLiveStream(): Promise<boolean> {
        try {
            // Load live stream module in DJI Pilot 2
            const liveConfig = {
                videoPublishType: 'video-demand-aux-manual', // Mixed mode
                statusCallback: 'liveStatusCallback'
            }

            const loadResult = PilotBridge.loadComponent('Liveshare', liveConfig)
            if (!loadResult) {
                throw new Error('Failed to load live stream module')
            }

            // Set up status callback
            this.setupStatusCallback()

            // Load initial capabilities
            await this.refreshCapabilities()

            this.state.isLoaded = true
            this.state.error = null

            console.log('Live stream service initialized successfully')
            return true

        } catch (error) {
            this.state.error = error.message
            message.error(`Live stream initialization failed: ${error.message}`)
            return false
        }
    }

    private setupStatusCallback(): void {
        window.liveStatusCallback = (status: any) => {
            console.log('Live stream status callback:', status)
            this.handleStatusUpdate(status)
        }
    }

    private handleStatusUpdate(status: any): void {
        // Process live stream status updates from DJI Pilot 2
        const streamId = status.stream_id || 'default'
        
        const streamStatus: StreamStatus = {
            stream_id: streamId,
            device_sn: status.device_sn,
            video_id: status.video_id,
            status: this.mapStatusCode(status.status),
            start_time: status.start_time || Date.now(),
            viewer_count: status.viewer_count,
            stream_health: status.stream_health
        }

        this.state.activeStreams.set(streamId, streamStatus)
    }

    private mapStatusCode(statusCode: number): StreamStatus['status'] {
        switch (statusCode) {
            case 0: return 'stopped'
            case 1: return 'starting'
            case 2: return 'streaming'
            case 3: return 'stopping'
            case 4: return 'error'
            default: return 'stopped'
        }
    }

    async refreshCapabilities(): Promise<void> {
        try {
            const workspaceId = localStorage.getItem('currentWorkspaceId')
            if (!workspaceId) {
                throw new Error('No workspace selected')
            }

            const response = await fetch(`/manage/api/v1/livestream/capacity/${workspaceId}`, {
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

            this.state.capabilities = result.data

        } catch (error) {
            console.error('Error refreshing live stream capabilities:', error)
            throw error
        }
    }

    async startStream(config: StreamConfig): Promise<string> {
        try {
            // Validate configuration
            if (!this.validateStreamConfig(config)) {
                throw new Error('Invalid stream configuration')
            }

            // Send start request to server
            const response = await fetch('/manage/api/v1/livestream/start', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${cloudConnectionService.getToken()}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(config)
            })

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`)
            }

            const result = await response.json()
            if (result.code !== 0) {
                throw new Error(result.message)
            }

            const streamId = result.data.stream_id

            // Update local state
            const streamStatus: StreamStatus = {
                stream_id: streamId,
                device_sn: config.device_sn,
                video_id: config.video_id,
                status: 'starting',
                start_time: Date.now()
            }

            this.state.activeStreams.set(streamId, streamStatus)

            message.success('Live stream started successfully')
            return streamId

        } catch (error) {
            message.error(`Failed to start live stream: ${error.message}`)
            throw error
        }
    }

    async stopStream(streamId: string): Promise<void> {
        try {
            const stream = this.state.activeStreams.get(streamId)
            if (!stream) {
                throw new Error('Stream not found')
            }

            // Send stop request to server
            const response = await fetch('/manage/api/v1/livestream/stop', {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${cloudConnectionService.getToken()}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    stream_id: streamId,
                    video_id: stream.video_id
                })
            })

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`)
            }

            const result = await response.json()
            if (result.code !== 0) {
                throw new Error(result.message)
            }

            // Update local state
            stream.status = 'stopped'
            this.state.activeStreams.set(streamId, stream)

            message.success('Live stream stopped successfully')

        } catch (error) {
            message.error(`Failed to stop live stream: ${error.message}`)
            throw error
        }
    }

    async updateStreamQuality(streamId: string, quality: number): Promise<void> {
        try {
            const stream = this.state.activeStreams.get(streamId)
            if (!stream) {
                throw new Error('Stream not found')
            }

            const response = await fetch('/manage/api/v1/livestream/quality', {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${cloudConnectionService.getToken()}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    stream_id: streamId,
                    video_id: stream.video_id,
                    video_quality: quality
                })
            })

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`)
            }

            const result = await response.json()
            if (result.code !== 0) {
                throw new Error(result.message)
            }

            message.success('Stream quality updated successfully')

        } catch (error) {
            message.error(`Failed to update stream quality: ${error.message}`)
            throw error
        }
    }

    private validateStreamConfig(config: StreamConfig): boolean {
        if (!config.device_sn || !config.video_id) {
            return false
        }

        if (!['agora', 'rtmp', 'rtsp', 'gb28181'].includes(config.protocol)) {
            return false
        }

        if (config.video_quality < 0 || config.video_quality > 3) {
            return false
        }

        return true
    }

    getStreamStatus(streamId: string): StreamStatus | null {
        return this.state.activeStreams.get(streamId) || null
    }

    getActiveStreams(): StreamStatus[] {
        return Array.from(this.state.activeStreams.values())
    }

    async unloadLiveStream(): Promise<void> {
        try {
            // Stop all active streams
            const activeStreamIds = Array.from(this.state.activeStreams.keys())
            for (const streamId of activeStreamIds) {
                const stream = this.state.activeStreams.get(streamId)
                if (stream && stream.status === 'streaming') {
                    await this.stopStream(streamId)
                }
            }

            // Unload module
            PilotBridge.unloadComponent('Liveshare')

            // Clear state
            this.state.capabilities = []
            this.state.activeStreams.clear()
            this.state.isLoaded = false
            this.state.error = null

        } catch (error) {
            console.error('Error unloading live stream:', error)
        }
    }
}

export const liveStreamService = new LiveStreamService()
```

#### 2. Live Stream Control Component

**File:** `/Cloud-API-Demo-Web/src/components/LiveStreamControl.vue`

```vue
<template>
    <div class="live-stream-control">
        <a-card title="Live Stream Control" size="small">
            <!-- Stream Configuration -->
            <div class="stream-config" v-if="!hasActiveStream">
                <a-row :gutter="16">
                    <a-col :span="6">
                        <a-select
                            v-model:value="selectedDevice"
                            placeholder="Select Device"
                            style="width: 100%"
                            @change="onDeviceChange"
                        >
                            <a-select-option
                                v-for="device in streamState.capabilities"
                                :key="device.sn"
                                :value="device.sn"
                            >
                                {{ device.name }}
                            </a-select-option>
                        </a-select>
                    </a-col>

                    <a-col :span="6">
                        <a-select
                            v-model:value="selectedCamera"
                            placeholder="Select Camera"
                            style="width: 100%"
                            @change="onCameraChange"
                            :disabled="!selectedDevice"
                        >
                            <a-select-option
                                v-for="camera in availableCameras"
                                :key="camera.camera_index"
                                :value="camera.camera_index"
                            >
                                {{ camera.camera_name }}
                            </a-select-option>
                        </a-select>
                    </a-col>

                    <a-col :span="6">
                        <a-select
                            v-model:value="selectedVideo"
                            placeholder="Select Video"
                            style="width: 100%"
                            :disabled="!selectedCamera"
                        >
                            <a-select-option
                                v-for="video in availableVideos"
                                :key="video.video_index"
                                :value="video.video_index"
                            >
                                {{ video.video_type }} ({{ video.resolution }})
                            </a-select-option>
                        </a-select>
                    </a-col>

                    <a-col :span="6">
                        <a-select
                            v-model:value="selectedProtocol"
                            placeholder="Protocol"
                            style="width: 100%"
                        >
                            <a-select-option value="agora">Agora</a-select-option>
                            <a-select-option value="rtmp">RTMP</a-select-option>
                            <a-select-option value="rtsp">RTSP</a-select-option>
                            <a-select-option value="gb28181">GB28181</a-select-option>
                        </a-select>
                    </a-col>
                </a-row>

                <div class="quality-selector" style="margin-top: 16px;">
                    <label>Video Quality:</label>
                    <a-radio-group v-model:value="selectedQuality" button-style="solid">
                        <a-radio-button :value="0">Auto</a-radio-button>
                        <a-radio-button :value="1">Smooth</a-radio-button>
                        <a-radio-button :value="2">HD</a-radio-button>
                        <a-radio-button :value="3">Ultra HD</a-radio-button>
                    </a-radio-group>
                </div>

                <!-- Protocol-specific configuration -->
                <div v-if="selectedProtocol === 'agora'" class="protocol-config" style="margin-top: 16px;">
                    <a-row :gutter="16">
                        <a-col :span="8">
                            <a-input v-model:value="agoraConfig.appId" placeholder="Agora App ID" />
                        </a-col>
                        <a-col :span="8">
                            <a-input v-model:value="agoraConfig.channel" placeholder="Channel Name" />
                        </a-col>
                        <a-col :span="8">
                            <a-input v-model:value="agoraConfig.token" placeholder="Token" />
                        </a-col>
                    </a-row>
                </div>

                <div v-if="selectedProtocol === 'rtmp'" class="protocol-config" style="margin-top: 16px;">
                    <a-input v-model:value="rtmpConfig.url" placeholder="RTMP URL (rtmp://server/app/stream)" />
                </div>

                <div v-if="selectedProtocol === 'rtsp'" class="protocol-config" style="margin-top: 16px;">
                    <a-row :gutter="16">
                        <a-col :span="8">
                            <a-input v-model:value="rtspConfig.username" placeholder="Username" />
                        </a-col>
                        <a-col :span="8">
                            <a-input-password v-model:value="rtspConfig.password" placeholder="Password" />
                        </a-col>
                        <a-col :span="8">
                            <a-input-number v-model:value="rtspConfig.port" placeholder="Port" style="width: 100%" />
                        </a-col>
                    </a-row>
                </div>

                <div class="stream-actions" style="margin-top: 20px; text-align: center;">
                    <a-button
                        type="primary"
                        size="large"
                        @click="startStream"
                        :loading="starting"
                        :disabled="!canStartStream"
                    >
                        Start Stream
                    </a-button>

                    <a-button
                        style="margin-left: 12px;"
                        @click="refreshCapabilities"
                        :loading="refreshing"
                    >
                        Refresh Devices
                    </a-button>
                </div>
            </div>

            <!-- Active Stream Control -->
            <div v-if="hasActiveStream" class="active-stream">
                <a-row :gutter="16" align="middle">
                    <a-col :span="12">
                        <div class="stream-info">
                            <a-tag :color="getStatusColor(activeStreamStatus.status)">
                                {{ activeStreamStatus.status.toUpperCase() }}
                            </a-tag>
                            <span style="margin-left: 8px;">
                                {{ getSelectedDeviceName() }} - {{ getSelectedCameraName() }}
                            </span>
                        </div>
                    </a-col>

                    <a-col :span="6">
                        <a-select
                            v-model:value="selectedQuality"
                            @change="updateQuality"
                            style="width: 100%"
                        >
                            <a-select-option :value="0">Auto</a-select-option>
                            <a-select-option :value="1">Smooth</a-select-option>
                            <a-select-option :value="2">HD</a-select-option>
                            <a-select-option :value="3">Ultra HD</a-select-option>
                        </a-select>
                    </a-col>

                    <a-col :span="6">
                        <a-button
                            danger
                            @click="stopStream"
                            :loading="stopping"
                        >
                            Stop Stream
                        </a-button>
                    </a-col>
                </a-row>

                <!-- Stream metrics -->
                <div v-if="activeStreamStatus.stream_health" class="stream-metrics" style="margin-top: 16px;">
                    <a-row :gutter="16">
                        <a-col :span="6">
                            <a-statistic title="Bitrate" :value="activeStreamStatus.stream_health.bitrate" suffix="kbps" />
                        </a-col>
                        <a-col :span="6">
                            <a-statistic title="Frame Rate" :value="activeStreamStatus.stream_health.frame_rate" suffix="fps" />
                        </a-col>
                        <a-col :span="6">
                            <a-statistic title="Latency" :value="activeStreamStatus.stream_health.latency" suffix="ms" />
                        </a-col>
                        <a-col :span="6">
                            <a-statistic title="Dropped Frames" :value="activeStreamStatus.stream_health.dropped_frames" />
                        </a-col>
                    </a-row>
                </div>
            </div>
        </a-card>

        <!-- Video Player -->
        <div v-if="hasActiveStream && showPlayer" class="video-player" style="margin-top: 16px;">
            <a-card title="Live Video" size="small">
                <div id="livePlayer" style="width: 100%; height: 400px; background: #000;"></div>
            </a-card>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import { liveStreamService, StreamCapability, CameraCapability, VideoCapability } from '@/services/LiveStreamService'

const streamState = liveStreamService.streamState

// Selection state
const selectedDevice = ref('')
const selectedCamera = ref('')
const selectedVideo = ref('')
const selectedProtocol = ref('agora')
const selectedQuality = ref(2)

// Protocol configurations
const agoraConfig = ref({
    appId: '',
    channel: '',
    token: ''
})

const rtmpConfig = ref({
    url: ''
})

const rtspConfig = ref({
    username: '',
    password: '',
    port: 8554
})

// UI state
const starting = ref(false)
const stopping = ref(false)
const refreshing = ref(false)
const showPlayer = ref(true)

// Computed properties
const availableCameras = computed(() => {
    if (!selectedDevice.value) return []
    const device = streamState.capabilities.find(d => d.sn === selectedDevice.value)
    return device?.cameras_list || []
})

const availableVideos = computed(() => {
    if (!selectedCamera.value) return []
    const camera = availableCameras.value.find(c => c.camera_index === selectedCamera.value)
    return camera?.video_list || []
})

const canStartStream = computed(() => {
    return selectedDevice.value && selectedCamera.value && selectedVideo.value && selectedProtocol.value
})

const hasActiveStream = computed(() => {
    return liveStreamService.getActiveStreams().length > 0
})

const activeStreamStatus = computed(() => {
    const activeStreams = liveStreamService.getActiveStreams()
    return activeStreams.length > 0 ? activeStreams[0] : null
})

// Methods
const onDeviceChange = () => {
    selectedCamera.value = ''
    selectedVideo.value = ''
}

const onCameraChange = () => {
    selectedVideo.value = ''
}

const getStatusColor = (status: string) => {
    switch (status) {
        case 'streaming': return 'green'
        case 'starting': return 'blue'
        case 'stopping': return 'orange'
        case 'error': return 'red'
        default: return 'gray'
    }
}

const getSelectedDeviceName = () => {
    const device = streamState.capabilities.find(d => d.sn === selectedDevice.value)
    return device?.name || 'Unknown Device'
}

const getSelectedCameraName = () => {
    const camera = availableCameras.value.find(c => c.camera_index === selectedCamera.value)
    return camera?.camera_name || 'Unknown Camera'
}

const startStream = async () => {
    starting.value = true
    try {
        const protocolConfig = getProtocolConfig()
        
        const config = {
            device_sn: selectedDevice.value,
            video_id: `${selectedDevice.value}/${selectedVideo.value}`,
            protocol: selectedProtocol.value as 'agora' | 'rtmp' | 'rtsp' | 'gb28181',
            video_quality: selectedQuality.value,
            protocol_config: protocolConfig
        }

        await liveStreamService.startStream(config)

    } catch (error) {
        console.error('Start stream error:', error)
    } finally {
        starting.value = false
    }
}

const stopStream = async () => {
    stopping.value = true
    try {
        const activeStreams = liveStreamService.getActiveStreams()
        if (activeStreams.length > 0) {
            await liveStreamService.stopStream(activeStreams[0].stream_id)
        }
    } catch (error) {
        console.error('Stop stream error:', error)
    } finally {
        stopping.value = false
    }
}

const updateQuality = async () => {
    try {
        const activeStreams = liveStreamService.getActiveStreams()
        if (activeStreams.length > 0) {
            await liveStreamService.updateStreamQuality(activeStreams[0].stream_id, selectedQuality.value)
        }
    } catch (error) {
        console.error('Update quality error:', error)
    }
}

const refreshCapabilities = async () => {
    refreshing.value = true
    try {
        await liveStreamService.refreshCapabilities()
    } catch (error) {
        message.error('Failed to refresh capabilities')
    } finally {
        refreshing.value = false
    }
}

const getProtocolConfig = () => {
    switch (selectedProtocol.value) {
        case 'agora':
            return { agora: agoraConfig.value }
        case 'rtmp':
            return { rtmp: rtmpConfig.value }
        case 'rtsp':
            return { rtsp: rtspConfig.value }
        default:
            return {}
    }
}

onMounted(async () => {
    // Initialize live stream service if not already loaded
    if (!streamState.isLoaded) {
        await liveStreamService.initializeLiveStream()
    }
    
    // Load initial capabilities
    await refreshCapabilities()
})

onUnmounted(() => {
    // Keep service running for other components
})
</script>

<style scoped>
.live-stream-control {
    width: 100%;
}

.stream-config {
    padding: 16px 0;
}

.protocol-config {
    background: #f5f5f5;
    padding: 12px;
    border-radius: 6px;
}

.stream-info {
    display: flex;
    align-items: center;
}

.stream-metrics {
    background: #fafafa;
    padding: 16px;
    border-radius: 6px;
}

.video-player {
    width: 100%;
}

#livePlayer {
    border: 1px solid #d9d9d9;
    border-radius: 6px;
}
</style>
```

## Backend Implementation

### Spring Boot Service Layer

#### 1. Live Stream Service Implementation

**File:** `/sample/src/main/java/com/dji/sample/manage/service/impl/LiveStreamServiceImpl.java`

```java
@Service
@Transactional
@Slf4j
public class LiveStreamServiceImpl implements ILiveStreamService {

    @Autowired
    private ICapacityCameraService capacityCameraService;

    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private IDeviceRedisService deviceRedisService;

    @Autowired
    private IWebSocketMessageService webSocketMessageService;

    @Autowired
    private AbstractLivestreamService abstractLivestreamService;

    @Autowired
    private LiveStreamProperties liveStreamProperties;

    private final Map<String, ActiveStreamInfo> activeStreams = new ConcurrentHashMap<>();

    @Override
    public List<CapacityDeviceDTO> getLiveCapacity(String workspaceId) {
        log.info("Retrieving live stream capacity for workspace: {}", workspaceId);

        try {
            // Query all online devices in the workspace
            List<DeviceDTO> devicesList = deviceService.getDevicesByParams(
                DeviceQueryParam.builder()
                    .workspaceId(workspaceId)
                    .domains(Arrays.asList(
                        DeviceDomainEnum.AIRCRAFT.getDomain(),
                        DeviceDomainEnum.DOCK.getDomain()
                    ))
                    .status(true) // Only online devices
                    .build()
            );

            // Build capacity information for each device
            List<CapacityDeviceDTO> capacityList = devicesList.stream()
                .filter(device -> deviceRedisService.checkDeviceOnline(device.getDeviceSn()))
                .map(this::buildDeviceCapacity)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

            log.info("Retrieved live capacity for {} devices in workspace {}", capacityList.size(), workspaceId);
            return capacityList;

        } catch (Exception e) {
            log.error("Error retrieving live stream capacity for workspace {}", workspaceId, e);
            throw new RuntimeException("Failed to retrieve live stream capacity", e);
        }
    }

    private Optional<CapacityDeviceDTO> buildDeviceCapacity(DeviceDTO device) {
        try {
            // Get camera capabilities from Redis cache
            List<CapacityCameraDTO> camerasList = capacityCameraService.getCapacityCameraByDeviceSn(device.getDeviceSn());
            
            if (camerasList.isEmpty()) {
                log.debug("No camera capabilities found for device {}", device.getDeviceSn());
                return Optional.empty();
            }

            // Enhance camera capabilities with additional metadata
            List<CapacityCameraDTO> enhancedCameras = camerasList.stream()
                .map(camera -> enhanceCameraCapability(camera, device))
                .collect(Collectors.toList());

            CapacityDeviceDTO capacity = CapacityDeviceDTO.builder()
                .name(Objects.requireNonNullElse(device.getNickname(), device.getDeviceName()))
                .sn(device.getDeviceSn())
                .deviceType(device.getDeviceType())
                .onlineStatus(true)
                .camerasList(enhancedCameras)
                .supportedProtocols(getSupportedProtocols(device))
                .maxConcurrentStreams(getMaxConcurrentStreams(device))
                .streamStatus(getDeviceStreamStatus(device.getDeviceSn()))
                .build();

            return Optional.of(capacity);

        } catch (Exception e) {
            log.error("Error building capacity for device {}", device.getDeviceSn(), e);
            return Optional.empty();
        }
    }

    private CapacityCameraDTO enhanceCameraCapability(CapacityCameraDTO camera, DeviceDTO device) {
        // Add additional metadata to camera capability
        List<CapacityVideoDTO> enhancedVideos = camera.getVideosList().stream()
            .map(video -> enhanceVideoCapability(video, device))
            .collect(Collectors.toList());

        return CapacityCameraDTO.builder()
            .cameraIndex(camera.getCameraIndex())
            .cameraName(camera.getCameraName())
            .cameraType(determineCameraType(camera.getCameraIndex()))
            .availableVideoNumber(camera.getAvailableVideoNumber())
            .coexistVideoNumberMax(camera.getCoexistVideoNumberMax())
            .videosList(enhancedVideos)
            .build();
    }

    private CapacityVideoDTO enhanceVideoCapability(CapacityVideoDTO video, DeviceDTO device) {
        return CapacityVideoDTO.builder()
            .videoIndex(video.getVideoIndex())
            .videoType(video.getVideoType())
            .resolution(determineResolution(video.getVideoType(), device.getDeviceType()))
            .maxBitrate(getMaxBitrate(video.getVideoType(), device.getDeviceType()))
            .supportedQualities(Arrays.asList(0, 1, 2, 3)) // Auto, Smooth, HD, Ultra HD
            .switchableVideoTypes(video.getSwitchableVideoTypes())
            .build();
    }

    @Override
    public HttpResultResponse<StreamStartResponseDTO> liveStart(LiveStreamStartRequestDTO liveParam) {
        log.info("Starting live stream for device {} with video {}", liveParam.getDeviceSn(), liveParam.getVideoId());

        try {
            // Validate stream request
            HttpResultResponse<DeviceDTO> validationResult = validateStreamRequest(liveParam);
            if (validationResult.getCode() != HttpResultResponse.CODE_SUCCESS) {
                return HttpResultResponse.error(validationResult.getMessage());
            }

            DeviceDTO device = validationResult.getData();

            // Check concurrent stream limits
            if (exceedsConcurrentLimit(device.getDeviceSn())) {
                return HttpResultResponse.error("Maximum concurrent streams exceeded for device");
            }

            // Build streaming URL based on protocol
            ILivestreamUrl streamUrl = buildStreamingUrl(liveParam);

            // Send start command to device via MQTT
            TopicServicesResponse<ServicesReplyData<String>> response = abstractLivestreamService.liveStartPush(
                SDKManager.getDeviceSDK(device.getDeviceSn()),
                LiveStartPushRequest.builder()
                    .url(streamUrl)
                    .urlType(liveParam.getProtocol())
                    .videoId(liveParam.getVideoId())
                    .videoQuality(liveParam.getVideoQuality())
                    .build()
            );

            if (!response.getData().getResult().isSuccess()) {
                log.error("Device rejected stream start request: {}", response.getData().getResult().getMessage());
                return HttpResultResponse.error("Device rejected stream start: " + response.getData().getResult().getMessage());
            }

            // Create stream record
            String streamId = generateStreamId();
            ActiveStreamInfo streamInfo = ActiveStreamInfo.builder()
                .streamId(streamId)
                .deviceSn(device.getDeviceSn())
                .videoId(liveParam.getVideoId())
                .protocol(liveParam.getProtocol())
                .startTime(System.currentTimeMillis())
                .status(StreamStatus.STARTING)
                .viewerInfo(buildViewerInfo(streamId, liveParam))
                .build();

            activeStreams.put(streamId, streamInfo);

            // Broadcast stream start event
            broadcastStreamStatusUpdate(streamInfo);

            StreamStartResponseDTO responseData = StreamStartResponseDTO.builder()
                .streamId(streamId)
                .deviceSn(device.getDeviceSn())
                .videoId(liveParam.getVideoId())
                .protocol(liveParam.getProtocol())
                .streamUrl(streamUrl.toString())
                .viewerInfo(streamInfo.getViewerInfo())
                .streamStatus("starting")
                .startTime(streamInfo.getStartTime())
                .estimatedStartupTime(5000L)
                .build();

            log.info("Live stream {} started successfully for device {}", streamId, device.getDeviceSn());
            return HttpResultResponse.success(responseData);

        } catch (Exception e) {
            log.error("Error starting live stream", e);
            return HttpResultResponse.error("Failed to start live stream: " + e.getMessage());
        }
    }

    @Override
    public HttpResultResponse<Void> liveStop(LiveStreamStopRequestDTO stopRequest) {
        log.info("Stopping live stream {} for video {}", stopRequest.getStreamId(), stopRequest.getVideoId());

        try {
            ActiveStreamInfo streamInfo = activeStreams.get(stopRequest.getStreamId());
            if (streamInfo == null) {
                return HttpResultResponse.error("Stream not found");
            }

            // Send stop command to device
            TopicServicesResponse<ServicesReplyData<String>> response = abstractLivestreamService.liveStopPush(
                SDKManager.getDeviceSDK(streamInfo.getDeviceSn()),
                LiveStopPushRequest.builder()
                    .videoId(stopRequest.getVideoId())
                    .build()
            );

            if (!response.getData().getResult().isSuccess()) {
                log.warn("Device reported error stopping stream: {}", response.getData().getResult().getMessage());
            }

            // Update stream status
            streamInfo.setStatus(StreamStatus.STOPPED);
            streamInfo.setStopTime(System.currentTimeMillis());

            // Broadcast stream stop event
            broadcastStreamStatusUpdate(streamInfo);

            // Remove from active streams after delay to allow status updates
            CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS)
                .execute(() -> activeStreams.remove(stopRequest.getStreamId()));

            log.info("Live stream {} stopped successfully", stopRequest.getStreamId());
            return HttpResultResponse.success();

        } catch (Exception e) {
            log.error("Error stopping live stream", e);
            return HttpResultResponse.error("Failed to stop live stream: " + e.getMessage());
        }
    }

    @Override
    public HttpResultResponse<Void> updateStreamQuality(StreamQualityUpdateRequestDTO qualityRequest) {
        log.info("Updating quality for stream {} to {}", qualityRequest.getStreamId(), qualityRequest.getVideoQuality());

        try {
            ActiveStreamInfo streamInfo = activeStreams.get(qualityRequest.getStreamId());
            if (streamInfo == null) {
                return HttpResultResponse.error("Stream not found");
            }

            if (streamInfo.getStatus() != StreamStatus.STREAMING) {
                return HttpResultResponse.error("Stream is not currently streaming");
            }

            // Send quality update command to device
            TopicServicesResponse<ServicesReplyData<String>> response = abstractLivestreamService.liveSetQuality(
                SDKManager.getDeviceSDK(streamInfo.getDeviceSn()),
                LiveSetQualityRequest.builder()
                    .videoId(qualityRequest.getVideoId())
                    .videoQuality(qualityRequest.getVideoQuality())
                    .build()
            );

            if (!response.getData().getResult().isSuccess()) {
                return HttpResultResponse.error("Device rejected quality update: " + response.getData().getResult().getMessage());
            }

            // Update stream info
            streamInfo.setVideoQuality(qualityRequest.getVideoQuality());

            log.info("Stream {} quality updated to {}", qualityRequest.getStreamId(), qualityRequest.getVideoQuality());
            return HttpResultResponse.success();

        } catch (Exception e) {
            log.error("Error updating stream quality", e);
            return HttpResultResponse.error("Failed to update stream quality: " + e.getMessage());
        }
    }

    @EventListener
    public void handleLiveStatusUpdate(LiveStatusUpdateEvent event) {
        try {
            String deviceSn = event.getDeviceSn();
            LiveStatus liveStatus = event.getLiveStatus();

            // Find active stream for this device and video
            Optional<ActiveStreamInfo> streamInfoOpt = activeStreams.values().stream()
                .filter(stream -> stream.getDeviceSn().equals(deviceSn) && 
                                 stream.getVideoId().equals(liveStatus.getVideoId()))
                .findFirst();

            if (streamInfoOpt.isPresent()) {
                ActiveStreamInfo streamInfo = streamInfoOpt.get();
                
                // Update stream health metrics
                updateStreamHealth(streamInfo, liveStatus);
                
                // Update stream status based on device feedback
                updateStreamStatus(streamInfo, liveStatus);

                // Broadcast status update
                broadcastStreamStatusUpdate(streamInfo);
            }

        } catch (Exception e) {
            log.error("Error handling live status update", e);
        }
    }

    private void updateStreamHealth(ActiveStreamInfo streamInfo, LiveStatus liveStatus) {
        StreamHealthMetrics health = StreamHealthMetrics.builder()
            .bitrate(calculateBitrate(liveStatus))
            .frameRate(calculateFrameRate(liveStatus))
            .droppedFrames(liveStatus.getDroppedFrames())
            .latency(liveStatus.getLatency())
            .networkQuality(determineNetworkQuality(liveStatus.getLiveTrendline()))
            .timestamp(System.currentTimeMillis())
            .build();

        streamInfo.setStreamHealth(health);
    }

    private void updateStreamStatus(ActiveStreamInfo streamInfo, LiveStatus liveStatus) {
        if (liveStatus.getErrorStatus() != null && liveStatus.getErrorStatus() != 0) {
            streamInfo.setStatus(StreamStatus.ERROR);
            streamInfo.setErrorCode(liveStatus.getErrorStatus());
        } else if (liveStatus.getLiveTime() > 0) {
            streamInfo.setStatus(StreamStatus.STREAMING);
        }
    }

    private void broadcastStreamStatusUpdate(ActiveStreamInfo streamInfo) {
        try {
            // Get device info
            Optional<DeviceDTO> deviceOpt = deviceService.getDeviceBySn(streamInfo.getDeviceSn());
            if (deviceOpt.isEmpty()) {
                return;
            }

            DeviceDTO device = deviceOpt.get();

            // Create WebSocket message
            StreamStatusWebSocketMessage wsMessage = StreamStatusWebSocketMessage.builder()
                .streamId(streamInfo.getStreamId())
                .deviceSn(streamInfo.getDeviceSn())
                .videoId(streamInfo.getVideoId())
                .status(streamInfo.getStatus().name().toLowerCase())
                .streamHealth(streamInfo.getStreamHealth())
                .viewerCount(streamInfo.getViewerCount())
                .build();

            WebSocketMessageResponse<StreamStatusWebSocketMessage> response = 
                WebSocketMessageResponse.<StreamStatusWebSocketMessage>builder()
                    .bizCode(BizCodeEnum.LIVESTREAM_STATUS.getCode())
                    .version("1.0.0")
                    .timestamp(System.currentTimeMillis())
                    .data(wsMessage)
                    .build();

            // Send to all clients in the device's workspace
            webSocketMessageService.sendMessageToWorkspace(device.getWorkspaceId(), response);

        } catch (Exception e) {
            log.error("Error broadcasting stream status update", e);
        }
    }

    private ILivestreamUrl buildStreamingUrl(LiveStreamStartRequestDTO request) {
        switch (request.getProtocol()) {
            case AGORA:
                AgoraConfig agoraConfig = request.getProtocolConfig().getAgora();
                return LivestreamAgoraUrl.builder()
                    .appId(agoraConfig.getAppId())
                    .channel(agoraConfig.getChannel())
                    .token(agoraConfig.getToken())
                    .uid(agoraConfig.getUid())
                    .build();

            case RTMP:
                RtmpConfig rtmpConfig = request.getProtocolConfig().getRtmp();
                return LivestreamRtmpUrl.builder()
                    .url(rtmpConfig.getUrl())
                    .build();

            case RTSP:
                RtspConfig rtspConfig = request.getProtocolConfig().getRtsp();
                return LivestreamRtspUrl.builder()
                    .username(rtspConfig.getUsername())
                    .password(rtspConfig.getPassword())
                    .port(rtspConfig.getPort())
                    .build();

            case GB28181:
                Gb28181Config gb28181Config = request.getProtocolConfig().getGb28181();
                return LivestreamGb28181Url.builder()
                    .serverIp(gb28181Config.getServerIp())
                    .serverPort(gb28181Config.getServerPort())
                    .serverId(gb28181Config.getServerId())
                    .agentId(gb28181Config.getAgentId())
                    .password(gb28181Config.getPassword())
                    .agentPort(gb28181Config.getAgentPort())
                    .agentChannel(gb28181Config.getAgentChannel())
                    .build();

            default:
                throw new IllegalArgumentException("Unsupported streaming protocol: " + request.getProtocol());
        }
    }

    private String generateStreamId() {
        return "stream_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private List<String> getSupportedProtocols(DeviceDTO device) {
        // Return supported protocols based on device type and capabilities
        List<String> protocols = new ArrayList<>();
        protocols.add("agora");
        protocols.add("rtmp");
        protocols.add("rtsp");
        
        // GB28181 might be region-specific or require special configuration
        if (supportsGb28181(device)) {
            protocols.add("gb28181");
        }
        
        return protocols;
    }

    private boolean supportsGb28181(DeviceDTO device) {
        // Check if device supports GB28181 protocol
        // This could be based on device type, region, or configuration
        return true; // Simplified for example
    }

    private int getMaxConcurrentStreams(DeviceDTO device) {
        // Return max concurrent streams based on device capabilities
        switch (device.getDeviceType()) {
            case 89: // M350
                return 2;
            case 67: // M30 series
                return 2;
            case 1: // Dock
                return 1;
            default:
                return 1;
        }
    }
}
```

## Device Integration

### Aircraft Camera Integration

#### Supported Camera Types & Capabilities

```java
// Camera capability mapping by device type
public class CameraCapabilityMapper {
    
    private static final Map<Integer, List<CameraSpec>> DEVICE_CAMERA_MAP = Map.of(
        89, Arrays.asList( // M350
            CameraSpec.builder()
                .cameraIndex("52-0-0")
                .cameraName("H30 Main Camera")
                .cameraType("main")
                .videoTypes(Arrays.asList("wide", "zoom", "ir"))
                .maxResolution("1920x1080")
                .maxBitrate(10000)
                .thermalSupport(true)
                .zoomRange("1x-56x")
                .build(),
            CameraSpec.builder()
                .cameraIndex("39-0-0") 
                .cameraName("FPV Camera")
                .cameraType("fpv")
                .videoTypes(Arrays.asList("wide"))
                .maxResolution("1920x1080")
                .maxBitrate(8000)
                .build()
        ),
        67, Arrays.asList( // M30/M30T
            CameraSpec.builder()
                .cameraIndex("52-0-0")
                .cameraName("M30 Integrated Camera")
                .cameraType("integrated")
                .videoTypes(Arrays.asList("wide", "zoom", "ir"))
                .maxResolution("1920x1080")
                .maxBitrate(8000)
                .thermalSupport(true)
                .zoomRange("1x-7x")
                .build()
        )
    );
    
    public static List<CameraSpec> getCameraSpecs(Integer deviceType) {
        return DEVICE_CAMERA_MAP.getOrDefault(deviceType, Collections.emptyList());
    }
}
```

### Real-time Stream Monitoring

```java
// Stream health monitoring service
@Service
public class StreamHealthMonitorService {
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    @PostConstruct
    public void startMonitoring() {
        // Monitor stream health every 5 seconds
        scheduler.scheduleAtFixedRate(this::checkStreamHealth, 5, 5, TimeUnit.SECONDS);
    }
    
    private void checkStreamHealth() {
        liveStreamService.getActiveStreams().forEach(stream -> {
            StreamHealthMetrics health = stream.getStreamHealth();
            if (health != null) {
                // Check for poor stream quality
                if (health.getDroppedFrames() > 100) {
                    sendStreamAlert(stream, "High frame drop rate detected");
                }
                
                if (health.getLatency() > 3000) {
                    sendStreamAlert(stream, "High latency detected");
                }
                
                if (health.getBitrate() < 1000) {
                    sendStreamAlert(stream, "Low bitrate detected");
                }
            }
        });
    }
    
    private void sendStreamAlert(ActiveStreamInfo stream, String message) {
        StreamAlertMessage alert = StreamAlertMessage.builder()
            .streamId(stream.getStreamId())
            .deviceSn(stream.getDeviceSn())
            .alertType("performance")
            .severity("warning")
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
            
        webSocketService.broadcastStreamAlert(alert);
    }
}
```

## Data Models

### Stream Configuration Models

```typescript
// TypeScript interfaces for stream configuration
interface StreamConfiguration {
    device_sn: string;
    video_id: string;
    protocol: StreamProtocol;
    video_quality: VideoQuality;
    protocol_config: ProtocolConfig;
    stream_settings?: StreamSettings;
}

type StreamProtocol = 'agora' | 'rtmp' | 'rtsp' | 'gb28181';
type VideoQuality = 0 | 1 | 2 | 3; // Auto, Smooth, HD, Ultra HD

interface ProtocolConfig {
    agora?: AgoraConfig;
    rtmp?: RtmpConfig;
    rtsp?: RtspConfig;
    gb28181?: Gb28181Config;
}

interface AgoraConfig {
    app_id: string;
    channel: string;
    token: string;
    uid: number;
}

interface RtmpConfig {
    url: string;
    key?: string;
}

interface RtspConfig {
    username: string;
    password: string;
    port: number;
}

interface Gb28181Config {
    server_ip: string;
    server_port: string;
    server_id: string;
    agent_id: string;
    password: string;
    agent_port: string;
    agent_channel: string;
}

interface StreamSettings {
    auto_bitrate: boolean;
    max_bitrate: number;
    frame_rate: number;
    resolution: string;
}
```

### Stream Status Models

```java
// Java models for stream status
@Data
@Builder
public class StreamStatus {
    private String streamId;
    private String deviceSn;
    private String videoId;
    private StreamState status;
    private Long startTime;
    private Long stopTime;
    private StreamHealthMetrics streamHealth;
    private Integer viewerCount;
    private String errorMessage;
    private Integer errorCode;
    
    public enum StreamState {
        STARTING, STREAMING, STOPPING, STOPPED, ERROR
    }
}

@Data
@Builder
public class StreamHealthMetrics {
    private Integer bitrate;
    private Double frameRate;
    private Integer droppedFrames;
    private Integer latency;
    private String networkQuality;
    private Long timestamp;
    
    // Advanced metrics
    private Double cpuUsage;
    private Double memoryUsage;
    private Integer bandwidth;
    private String encoderStats;
}
```

## Communication Workflows

### Stream Lifecycle Management

```mermaid
stateDiagram-v2
    [*] --> Idle
    
    Idle --> Starting : Start Stream Command
    Starting --> Streaming : Stream Established
    Starting --> Error : Start Failed
    
    Streaming --> QualityChange : Update Quality
    QualityChange --> Streaming : Quality Updated
    QualityChange --> Error : Update Failed
    
    Streaming --> Stopping : Stop Command
    Streaming --> Error : Stream Error
    
    Stopping --> Stopped : Stream Closed
    Stopping --> Error : Stop Failed
    
    Error --> Idle : Reset/Retry
    Stopped --> Idle : Cleanup Complete
    
    Error --> Starting : Retry Stream
```

### Error Recovery Mechanisms

```mermaid
flowchart TD
    A[Stream Error Detected] --> B{Error Type?}
    
    B -->|Network Loss| C[Wait for Reconnection]
    B -->|Device Error| D[Send Stop Command]
    B -->|Protocol Error| E[Switch Protocol]
    B -->|Quality Issue| F[Reduce Quality]
    
    C --> G{Connection Restored?}
    G -->|Yes| H[Resume Stream]
    G -->|No| I[Timeout Reached?]
    I -->|No| C
    I -->|Yes| J[Force Stop]
    
    D --> K[Cleanup Resources]
    E --> L[Restart with New Protocol]
    F --> M[Continue with Lower Quality]
    
    H --> N[Normal Operation]
    J --> O[Report Failure]
    K --> O
    L --> P{Start Success?}
    M --> Q{Quality Acceptable?}
    
    P -->|Yes| N
    P -->|No| O
    Q -->|Yes| N
    Q -->|No| R[Try Different Settings]
```

## Security Considerations

### Stream Security

1. **Authentication & Authorization**:
   - JWT-based API authentication
   - Role-based stream access control
   - Device-specific streaming permissions

2. **Protocol Security**:
   - RTMPS (RTMP over SSL) for encrypted streaming
   - RTSP over TLS for secure transmission
   - Token-based Agora authentication

3. **Content Protection**:
   - Stream encryption for sensitive content
   - Watermarking support for identification
   - Access logging and audit trails

### Performance Optimization

1. **Adaptive Bitrate**:
   - Dynamic quality adjustment based on network conditions
   - Multiple quality levels for different viewers
   - Bandwidth monitoring and optimization

2. **Caching Strategy**:
   - CDN integration for global distribution
   - Edge server deployment for reduced latency
   - Stream segment caching

## Configuration & Deployment

### Live Stream Configuration

```yaml
# application.yml - Live Stream Configuration
livestream:
  enabled: true
  max_concurrent_streams_per_device: 2
  max_total_streams: 50
  stream_timeout: 300000  # 5 minutes
  quality_update_timeout: 10000  # 10 seconds
  
  protocols:
    agora:
      enabled: true
      default_app_id: "your_default_app_id"
      token_expiry: 86400  # 24 hours
      
    rtmp:
      enabled: true
      server_url: "rtmp://streaming.example.com/live/"
      max_bitrate: 10000
      
    rtsp:
      enabled: true
      port_range: "8554-8594"
      authentication_required: true
      
    gb28181:
      enabled: true
      default_server_ip: "192.168.1.100"
      default_server_port: "5060"

  health_monitoring:
    enabled: true
    check_interval: 5000  # 5 seconds
    alert_thresholds:
      max_dropped_frames: 100
      max_latency_ms: 3000
      min_bitrate: 1000
      
  cdn:
    enabled: false
    provider: "cloudflare"
    base_url: "https://stream.example.com/"
```

This comprehensive specification provides developers with complete technical documentation for implementing and maintaining the Pilot Live Stream functionality. The document covers all aspects from stream initialization to advanced monitoring, security considerations, and deployment configurations.