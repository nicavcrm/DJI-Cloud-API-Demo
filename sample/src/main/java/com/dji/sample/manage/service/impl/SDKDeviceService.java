package com.dji.sample.manage.service.impl;

import com.dji.sample.component.websocket.model.BizCodeEnum;
import com.dji.sample.component.websocket.service.IWebSocketMessageService;
import com.dji.sample.manage.model.dto.DeviceDTO;
import com.dji.sample.manage.model.dto.DevicePayloadReceiver;
import com.dji.sample.manage.model.enums.DeviceFirmwareStatusEnum;
import com.dji.sample.manage.model.param.DeviceQueryParam;
import com.dji.sample.manage.service.IDeviceDictionaryService;
import com.dji.sample.manage.service.IDevicePayloadService;
import com.dji.sample.manage.service.IDeviceRedisService;
import com.dji.sample.manage.service.IDeviceService;
import com.dji.sdk.cloudapi.device.*;
import com.dji.sdk.cloudapi.device.api.AbstractDeviceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.dji.sdk.cloudapi.tsa.DeviceIconUrl;
import com.dji.sdk.cloudapi.tsa.IconUrlEnum;
import com.dji.sdk.config.version.GatewayManager;
import com.dji.sdk.common.SDKManager;
import com.dji.sdk.mqtt.MqttReply;
import com.dji.sdk.mqtt.osd.TopicOsdRequest;
import com.dji.sdk.mqtt.state.TopicStateRequest;
import com.dji.sdk.mqtt.status.TopicStatusRequest;
import com.dji.sdk.mqtt.status.TopicStatusResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author sean
 * @version 1.7
 * @date 2023/7/4
 */
@Service
@Slf4j
public class SDKDeviceService extends AbstractDeviceService {

    @Autowired
    private IDeviceRedisService deviceRedisService;

    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private IDeviceDictionaryService dictionaryService;

    @Autowired
    private IWebSocketMessageService webSocketMessageService;

    @Autowired
    private IDevicePayloadService devicePayloadService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public TopicStatusResponse<MqttReply> updateTopoOnline(TopicStatusRequest<UpdateTopo> request, MessageHeaders headers) {
        UpdateTopoSubDevice updateTopoSubDevice = request.getData().getSubDevices().get(0);
        String deviceSn = updateTopoSubDevice.getSn();

        Optional<DeviceDTO> deviceOpt = deviceRedisService.getDeviceOnline(deviceSn);
        Optional<DeviceDTO> gatewayOpt = deviceRedisService.getDeviceOnline(request.getFrom());
        GatewayManager gatewayManager = SDKManager.registerDevice(request.getFrom(), deviceSn,
                request.getData().getDomain(), request.getData().getType(),
                request.getData().getSubType(), request.getData().getThingVersion(), updateTopoSubDevice.getThingVersion());

        if (deviceOpt.isPresent() && gatewayOpt.isPresent()) {
            deviceOnlineAgain(deviceOpt.get().getWorkspaceId(), request.getFrom(), deviceSn);
            return new TopicStatusResponse<MqttReply>().setData(MqttReply.success());
        }

        changeSubDeviceParent(deviceSn, request.getFrom());

        DeviceDTO gateway = deviceGatewayConvertToDevice(request.getFrom(), request.getData());
        Optional<DeviceDTO> gatewayEntityOpt = onlineSaveDevice(gateway, deviceSn, null);
        if (gatewayEntityOpt.isEmpty()) {
            log.error("Failed to go online, please check the status data or code logic.");
            return null;
        }
        DeviceDTO subDevice = subDeviceConvertToDevice(updateTopoSubDevice);
        Optional<DeviceDTO> subDeviceEntityOpt = onlineSaveDevice(subDevice, null, gateway.getDeviceSn());
        if (subDeviceEntityOpt.isEmpty()) {
            log.error("Failed to go online, please check the status data or code logic.");
            return null;
        }
        subDevice = subDeviceEntityOpt.get();
        gateway = gatewayEntityOpt.get();
        dockGoOnline(gateway, subDevice);
        deviceService.gatewayOnlineSubscribeTopic(gatewayManager);

        if (!StringUtils.hasText(subDevice.getWorkspaceId())) {
            return new TopicStatusResponse<MqttReply>().setData(MqttReply.success());
        }

        // Subscribe to topic related to drone devices.
        deviceService.subDeviceOnlineSubscribeTopic(gatewayManager);
        deviceService.pushDeviceOnlineTopo(gateway.getWorkspaceId(), gateway.getDeviceSn(), subDevice.getDeviceSn());

        log.debug("{} online.", subDevice.getDeviceSn());
        return new TopicStatusResponse<MqttReply>().setData(MqttReply.success());
    }

    @Override
    public TopicStatusResponse<MqttReply> updateTopoOffline(TopicStatusRequest<UpdateTopo> request, MessageHeaders headers) {
        GatewayManager gatewayManager = SDKManager.registerDevice(request.getFrom(), null,
                request.getData().getDomain(), request.getData().getType(),
                request.getData().getSubType(), request.getData().getThingVersion(), null);
        deviceService.gatewayOnlineSubscribeTopic(gatewayManager);
        // Only the remote controller is logged in and the aircraft is not connected.
        Optional<DeviceDTO> deviceOpt = deviceRedisService.getDeviceOnline(request.getFrom());
        if (deviceOpt.isEmpty()) {
            // When connecting for the first time
            DeviceDTO gatewayDevice = deviceGatewayConvertToDevice(request.getFrom(), request.getData());
            Optional<DeviceDTO> gatewayDeviceOpt = onlineSaveDevice(gatewayDevice, null, null);
            if (gatewayDeviceOpt.isEmpty()) {
                return null;
            }
            deviceService.pushDeviceOnlineTopo(gatewayDeviceOpt.get().getWorkspaceId(), request.getFrom(), null);
            return new TopicStatusResponse<MqttReply>().setData(MqttReply.success());
        }

        String deviceSn = deviceOpt.get().getChildDeviceSn();
        if (!StringUtils.hasText(deviceSn)) {
            return new TopicStatusResponse<MqttReply>().setData(MqttReply.success());
        }

        deviceService.subDeviceOffline(deviceSn);
        return new TopicStatusResponse<MqttReply>().setData(MqttReply.success());
    }

    @Override
    public void osdDock(TopicOsdRequest<OsdDock> request, MessageHeaders headers) {
        String from = request.getFrom();

        // Check if this might be a Dock 3 device by looking at the type first
        Optional<DeviceDTO> deviceOpt = deviceService.getDeviceBySn(from);
        boolean isPotentialDock3 = deviceOpt.isPresent() &&
                                 DeviceDomainEnum.DOCK == deviceOpt.get().getDomain() &&
                                 DeviceTypeEnum.DOCK3 == deviceOpt.get().getType();

        // If this is a Dock 3 device, try to handle snake_case format
        if (isPotentialDock3) {
            log.debug("Potential Dock 3 device detected: {}, attempting special JSON handling", from);
            try {
                // Get the raw payload as string to check the format
                String rawPayload = objectMapper.writeValueAsString(request.getData());
                log.debug("Raw payload for potential Dock 3 {}: {}", from, rawPayload);

                // Check if this is snake_case format (Dock 3)
                boolean isSnakeCaseFormat = rawPayload.contains("\"network_state\"") ||
                                          rawPayload.contains("\"drone_in_dock\"") ||
                                          rawPayload.contains("\"environment_temperature\"") ||
                                          rawPayload.contains("\"drone_charge_state\"");

                if (isSnakeCaseFormat) {
                    log.info("Detected Dock 3 snake_case format for device: {}, performing custom deserialization", from);

                    // Parse the raw JSON using Dock 3 specific class
                    OsdDock3 dock3Osd = objectMapper.readValue(rawPayload, OsdDock3.class);
                    OsdDock standardDock = dock3Osd.toStandardOsdDock();

                    log.debug("Successfully converted Dock 3 OSD to standard format for: {}", from);
                    log.debug("Converted data: networkState={}, droneInDock={}, temperature={}",
                        standardDock.getNetworkState(), standardDock.getDroneInDock(), standardDock.getTemperature());

                    // CRITICAL FIX: Handle Dock 3 sub-device online status from sub_device field
                    if (dock3Osd.getSubDevice() != null) {
                        String droneSn = dock3Osd.getSubDevice().getDeviceSn();
                        Boolean droneOnlineStatus = dock3Osd.getSubDevice().getDeviceOnlineStatus();

                        // CRITICAL FIX: Handle both online AND offline status for Dock 3 drones
                        if (StringUtils.hasText(droneSn)) {
                            Optional<DeviceDTO> droneOpt = deviceService.getDeviceBySn(droneSn);
                            if (droneOpt.isPresent()) {
                                DeviceDTO drone = droneOpt.get();

                                // Check if drone was previously offline to trigger subscription/unsubscription
                                Optional<DeviceDTO> existingDroneOpt = deviceRedisService.getDeviceOnline(droneSn);
                                boolean wasDroneOffline = existingDroneOpt.isEmpty();

                                if (Boolean.TRUE.equals(droneOnlineStatus)) {
                                    // Drone is ONLINE
                                    log.info("Dock 3 {} reports drone {} is online via sub_device data", from, droneSn);

                                    drone.setStatus(true);
                                    drone.setParentSn(from);

                                    // Ensure drone has workspace ID from parent dock
                                    if (!StringUtils.hasText(drone.getWorkspaceId()) && StringUtils.hasText(deviceOpt.get().getWorkspaceId())) {
                                        drone.setWorkspaceId(deviceOpt.get().getWorkspaceId());
                                    }

                                    deviceRedisService.setDeviceOnline(drone);
                                    log.info("Marked drone {} as online in Redis with parent Dock 3 {} (wasOffline: {})", droneSn, from, wasDroneOffline);

                                    // CRITICAL FIX: Force sub-device subscription for Dock 3 drones regardless of previous state
                                    // This ensures MQTT topics are subscribed to even when drone is already marked online in Redis
                                    if (StringUtils.hasText(drone.getWorkspaceId())) {
                                        try {
                                            // Get the SDK GatewayManager for this dock
                                            GatewayManager gatewayManager = SDKManager.getDeviceSDK(from);
                                            if (gatewayManager != null) {
                                                log.info("Dock 3 {} - FORCING sub-device subscription for drone: {} (wasOffline: {})", from, droneSn, wasDroneOffline);
                                                log.info("Dock 3 {} - GatewayManager found: {}, subscribing to MQTT topics for drone: {}", from, gatewayManager.getClass().getSimpleName(), droneSn);
                                                deviceService.subDeviceOnlineSubscribeTopic(gatewayManager);
                                                log.info("Dock 3 {} - successfully subscribed to drone MQTT topics for: {} - expecting messages on thing/product/{}/osd", from, droneSn, droneSn);
                                            } else {
                                                log.warn("Dock 3 {} - GatewayManager not found, cannot subscribe to sub-device topics for drone: {}", from, droneSn);
                                            }
                                        } catch (Exception e) {
                                            log.error("Dock 3 {} - Failed to subscribe to sub-device topics for drone {}: {}", from, droneSn, e.getMessage(), e);
                                        }
                                    } else {
                                        log.warn("Dock 3 {} - drone {} has no workspace ID, cannot subscribe to MQTT topics", from, droneSn);
                                    }
                                } else if (Boolean.FALSE.equals(droneOnlineStatus)) {
                                    // Drone is OFFLINE
                                    log.info("Dock 3 {} reports drone {} is offline via sub_device data", from, droneSn);

                                    // Only process offline if drone was previously online
                                    if (!wasDroneOffline) {
                                        drone.setStatus(false);
                                        deviceRedisService.setDeviceOnline(drone);

                                        // Unsubscribe from drone topics to match Dock 1/2 behavior
                                        try {
                                            log.info("Dock 3 {} - marking drone {} as offline and unsubscribing from topics", from, droneSn);
                                            deviceService.subDeviceOffline(droneSn);
                                            log.info("Dock 3 {} - successfully unsubscribed from drone topics for: {}", from, droneSn);
                                        } catch (Exception e) {
                                            log.error("Dock 3 {} - Failed to unsubscribe from sub-device topics for drone {}: {}", from, droneSn, e.getMessage(), e);
                                        }
                                    } else {
                                        log.debug("Dock 3 {} - drone {} was already offline, no action needed", from, droneSn);
                                    }
                                }
                            } else {
                                log.warn("Drone {} not found in database, cannot process status change", droneSn);
                            }
                        }
                    }

                    // Create a new request with the properly deserialized data
                    TopicOsdRequest<OsdDock> correctedRequest = new TopicOsdRequest<OsdDock>()
                            .setGateway(request.getGateway())
                            .setFrom(request.getFrom())
                            .setData(standardDock)
                            .setBid(request.getBid())
                            .setTid(request.getTid())
                            .setTimestamp(request.getTimestamp());

                    // Process the corrected request
                    processDockOsd(correctedRequest, headers);
                    return;
                }
            } catch (Exception e) {
                log.warn("Failed to process as Dock 3 format for device {}, falling back to standard processing: {}", from, e.getMessage());
            }
        }

        // Standard processing for Dock 1/2 or if Dock 3 processing failed
        processDockOsd(request, headers);
    }

    /**
     * Core OSD processing logic for all dock types
     */
    private void processDockOsd(TopicOsdRequest<OsdDock> request, MessageHeaders headers) {
        String from = request.getFrom();
        Optional<DeviceDTO> deviceOpt = deviceRedisService.getDeviceOnline(from);
        boolean wasOffline = deviceOpt.isEmpty();

        // Enhanced logging for Dock 3 debugging
        log.debug("Received OSD message from dock: {}, wasOffline: {}", from, wasOffline);

        if (deviceOpt.isEmpty() || !StringUtils.hasText(deviceOpt.get().getWorkspaceId())) {
            deviceOpt = deviceService.getDeviceBySn(from);
            if (deviceOpt.isEmpty()) {
                log.error("Dock {} not found in database. Please restart the dock or check device registration.", from);
                return;
            }
        }

        DeviceDTO device = deviceOpt.get();

        // Enhanced device validation and logging
        log.debug("Processing OSD for dock: {} - Domain: {}, Type: {}, SubType: {}, WorkspaceId: {}, ChildDeviceSn: {}",
            from, device.getDomain(), device.getType(), device.getSubType(), device.getWorkspaceId(), device.getChildDeviceSn());

        // CRITICAL FIX: For Dock 3, ensure child device SN is synchronized from database
        // because Redis might not have the latest child_device_sn information
        boolean isDock3 = DeviceDomainEnum.DOCK == device.getDomain() &&
                         DeviceTypeEnum.DOCK3 == device.getType();

        if (isDock3 && !StringUtils.hasText(device.getChildDeviceSn())) {
            log.info("Dock 3 {} has no child device SN in Redis, syncing from database", from);
            Optional<DeviceDTO> freshDeviceOpt = deviceService.getDeviceBySn(from);
            if (freshDeviceOpt.isPresent() && StringUtils.hasText(freshDeviceOpt.get().getChildDeviceSn())) {
                device.setChildDeviceSn(freshDeviceOpt.get().getChildDeviceSn());
                deviceRedisService.setDeviceOnline(device); // Update Redis with synced data
                log.info("Dock 3 {} - synced child device SN from database: {}", from, device.getChildDeviceSn());
            } else {
                log.warn("Dock 3 {} - no child device SN found in database either", from);
            }
        }

        if (!StringUtils.hasText(device.getWorkspaceId())) {
            log.error("Dock {} is not bound to any workspace. Please bind the dock first.", from);
        }
        if (StringUtils.hasText(device.getChildDeviceSn())) {
            deviceService.getDeviceBySn(device.getChildDeviceSn()).ifPresent(device::setChildren);
        }

        // Mark device as online and store OSD data
        deviceRedisService.setDeviceOnline(device);
        fillDockOsd(from, request.getData());

        // Always send DOCK_OSD WebSocket event for real-time monitoring
        if (StringUtils.hasText(device.getWorkspaceId())) {
            deviceService.pushOsdDataToWeb(device.getWorkspaceId(), BizCodeEnum.DOCK_OSD, from, request.getData());
            log.debug("Sent DOCK_OSD WebSocket event for dock: {}", from);
        } else {
            log.warn("Skipping DOCK_OSD WebSocket event for dock {} - no workspace ID configured", from);
        }

        // Enhanced online event handling for Dock 3 and other docks
        if (wasOffline && StringUtils.hasText(device.getWorkspaceId())) {
            log.info("Dock {} (Domain: {}, Type: {}) came online via OSD, firing DEVICE_ONLINE event",
                from, device.getDomain(), device.getType());

            if (isDock3) {
                log.info("Successfully processed Dock 3 OSD data for device: {}", from);

                // CRITICAL FIX: Dock 3 comes online via OSD, not via updateTopoOnline
                // We need to ensure sub-device subscription happens for Dock 3 as well
                if (StringUtils.hasText(device.getChildDeviceSn())) {
                    try {
                        // Get the SDK GatewayManager for this dock
                        GatewayManager gatewayManager = SDKManager.getDeviceSDK(from);
                        if (gatewayManager != null) {
                            log.info("Dock 3 {} - triggering sub-device subscription for child: {}", from, device.getChildDeviceSn());
                            deviceService.subDeviceOnlineSubscribeTopic(gatewayManager);
                            log.info("Dock 3 {} - successfully subscribed to drone topics for: {}", from, device.getChildDeviceSn());
                        } else {
                            log.warn("Dock 3 {} - GatewayManager not found, cannot subscribe to sub-device topics", from);
                        }
                    } catch (Exception e) {
                        log.error("Dock 3 {} - Failed to subscribe to sub-device topics: {}", from, e.getMessage(), e);
                    }
                } else {
                    log.warn("Dock 3 {} - no child device SN available for subscription, childDeviceSn: {}", from, device.getChildDeviceSn());
                }
            }

            // Fire the DEVICE_ONLINE WebSocket event
            try {
                deviceService.pushDeviceOnlineTopo(device.getWorkspaceId(), from, device.getChildDeviceSn());
                log.info("Successfully fired DEVICE_ONLINE WebSocket event for dock: {} (Dock3: {})", from, isDock3);
            } catch (Exception e) {
                log.error("Failed to fire DEVICE_ONLINE WebSocket event for dock {}: {}", from, e.getMessage(), e);
            }
        } else if (wasOffline) {
            log.warn("Dock {} came online but no workspace ID configured - skipping DEVICE_ONLINE event", from);
        }
    }

  
    @Override
    public void osdDockDrone(TopicOsdRequest<OsdDockDrone> request, MessageHeaders headers) {
        String from = request.getFrom();

        // Check if this drone might belong to a Dock 3 by checking its parent
        Optional<DeviceDTO> deviceOpt = deviceService.getDeviceBySn(from);
        boolean isPotentialDock3Drone = false;

        if (deviceOpt.isPresent() && StringUtils.hasText(deviceOpt.get().getParentSn())) {
            Optional<DeviceDTO> parentOpt = deviceService.getDeviceBySn(deviceOpt.get().getParentSn());
            isPotentialDock3Drone = parentOpt.isPresent() &&
                                   DeviceDomainEnum.DOCK == parentOpt.get().getDomain() &&
                                   DeviceTypeEnum.DOCK3 == parentOpt.get().getType();
        }

        // If this drone might belong to a Dock 3, try to handle snake_case format
        if (isPotentialDock3Drone) {
            log.debug("Potential Dock 3 drone detected: {}, attempting special JSON handling", from);
            try {
                // Get the raw payload as string to check the format
                String rawPayload = objectMapper.writeValueAsString(request.getData());
                log.debug("Raw payload for potential Dock 3 drone {}: {}", from, rawPayload);

                // Check if this is snake_case format (Dock 3)
                // Look for actual snake_case patterns that Dock 3 sends
                boolean isSnakeCaseFormat = rawPayload.contains("\"position_state\"") &&
                                          rawPayload.contains("\"total_flight_distance\"") &&
                                          rawPayload.contains("\"horizontal_speed\"") &&
                                          rawPayload.contains("\"vertical_speed\"") &&
                                          (rawPayload.contains("\"payload_bindings\"") || rawPayload.contains("\"elevation\""));

                if (isSnakeCaseFormat) {
                    log.info("Detected Dock 3 snake_case format for drone: {}, performing custom deserialization", from);

                    // Parse the raw JSON using Dock 3 specific class
                    OsdDockDrone3 dock3DroneOsd = objectMapper.readValue(rawPayload, OsdDockDrone3.class);
                    OsdDockDrone standardDrone = dock3DroneOsd.toStandardOsdDockDrone();

                    log.debug("Successfully converted Dock 3 drone OSD to standard format for: {}", from);
                    log.debug("Converted data: attitudeHead={}, positionState={}, modeCode={}",
                        standardDrone.getAttitudeHead(), standardDrone.getPositionState(), standardDrone.getModeCode());

                    // Create a new request with the properly deserialized data
                    TopicOsdRequest<OsdDockDrone> correctedRequest = new TopicOsdRequest<OsdDockDrone>()
                            .setGateway(request.getGateway())
                            .setFrom(request.getFrom())
                            .setData(standardDrone)
                            .setBid(request.getBid())
                            .setTid(request.getTid())
                            .setTimestamp(request.getTimestamp());

                    // Process the corrected request
                    processDroneOsd(correctedRequest, headers);
                    return;
                }
            } catch (Exception e) {
                log.warn("Failed to process as Dock 3 drone format for device {}, falling back to standard processing: {}", from, e.getMessage());
            }
        }

        // Standard processing for non-Dock 3 drones or if Dock 3 processing failed
        processDroneOsd(request, headers);
    }

    /**
     * Core OSD processing logic for all drone types
     */
    private void processDroneOsd(TopicOsdRequest<OsdDockDrone> request, MessageHeaders headers) {
        String from = request.getFrom();
        Optional<DeviceDTO> deviceOpt = deviceRedisService.getDeviceOnline(from);
        boolean wasOffline = deviceOpt.isEmpty();

        // CRITICAL LOG: Track all drone OSD messages received
        log.info("🚁 Received drone OSD message from: {} (wasOffline: {})", from, wasOffline);

        if (deviceOpt.isEmpty()) {
            deviceOpt = deviceService.getDeviceBySn(from);
            if (deviceOpt.isEmpty()) {
                log.error("❌ Drone {} not found in database. Please restart the drone.", from);
                return;
            }
        }

        DeviceDTO device = deviceOpt.get();
        if (!StringUtils.hasText(device.getWorkspaceId())) {
            log.error("❌ Drone {} is not bound to any workspace. Please bind the drone first.", from);
        }

        // Enhanced logging for drone OSD processing
        log.info("🚁 Processing OSD for drone: {} - Parent: {}, Domain: {}, Type: {}, WorkspaceId: {}, Mode: {}, Alt: {}",
            from, device.getParentSn(), device.getDomain(), device.getType(), device.getWorkspaceId(),
            request.getData().getModeCode(), request.getData().getHeight());

        // Ensure device is marked as online when OSD messages are received
        device.setStatus(true);
        deviceRedisService.setDeviceOnline(device);
        deviceRedisService.setDeviceOsd(from, request.getData());

        // Always send DEVICE_OSD WebSocket event for real-time monitoring
        if (StringUtils.hasText(device.getWorkspaceId())) {
            deviceService.pushOsdDataToWeb(device.getWorkspaceId(), BizCodeEnum.DEVICE_OSD, from, request.getData());
            log.info("✅ SENT DEVICE_OSD WebSocket event for drone: {} (Parent: {}, Mode: {}, Alt: {})",
                from, device.getParentSn(),
                request.getData().getModeCode(),
                request.getData().getHeight());
        } else {
            log.warn("❌ Cannot send DEVICE_OSD WebSocket event for drone: {} - no workspace ID configured", from);
        }

        // If this is a drone from Dock 3 that was previously offline, ensure proper online event handling
        if (wasOffline && StringUtils.hasText(device.getWorkspaceId())) {
            log.info("Drone {} (from dock: {}) came online via OSD, ensuring WebSocket topology is updated",
                from, device.getParentSn());

            // Check if this drone belongs to a Dock 3
            if (StringUtils.hasText(device.getParentSn())) {
                deviceRedisService.getDeviceOnline(device.getParentSn()).ifPresent(parentDevice -> {
                    boolean isDock3 = DeviceDomainEnum.DOCK == parentDevice.getDomain() &&
                                   DeviceTypeEnum.DOCK3 == parentDevice.getType();
                    if (isDock3) {
                        log.info("Drone {} belongs to Dock 3 {} - enhanced online event handling", from, device.getParentSn());
                    }
                });
            }
        }
    }

    @Override
    public void osdRemoteControl(TopicOsdRequest<OsdRemoteControl> request, MessageHeaders headers) {
        String from = request.getFrom();
        Optional<DeviceDTO> deviceOpt = deviceRedisService.getDeviceOnline(from);
        if (deviceOpt.isEmpty()) {
            deviceOpt = deviceService.getDeviceBySn(from);
            if (deviceOpt.isEmpty()) {
                log.error("Please restart the drone.");
                return;
            }
        }
        DeviceDTO device = deviceOpt.get();
        if (StringUtils.hasText(device.getChildDeviceSn())) {
            deviceService.getDeviceBySn(device.getChildDeviceSn()).ifPresent(device::setChildren);
        }
        deviceRedisService.setDeviceOnline(device);

        OsdRemoteControl data = request.getData();
        String workspaceId = device.getWorkspaceId();
        if (StringUtils.hasText(workspaceId)) {
            deviceService.pushOsdDataToPilot(workspaceId, from,
                    new DeviceOsdHost()
                            .setLatitude(data.getLatitude())
                            .setLongitude(data.getLongitude())
                            .setHeight(data.getHeight()));
            deviceService.pushOsdDataToWeb(workspaceId, BizCodeEnum.RC_OSD, from, data);
        } else {
            log.warn("Device {} has no workspace ID configured, skipping WebSocket broadcast", from);
        }

    }

    @Override
    public void osdRcDrone(TopicOsdRequest<OsdRcDrone> request, MessageHeaders headers) {
        String from = request.getFrom();
        Optional<DeviceDTO> deviceOpt = deviceRedisService.getDeviceOnline(from);
        if (deviceOpt.isEmpty()) {
            deviceOpt = deviceService.getDeviceBySn(from);
            if (deviceOpt.isEmpty()) {
                log.error("Please restart the drone.");
                return;
            }
        }
        DeviceDTO device = deviceOpt.get();
        if (!StringUtils.hasText(device.getWorkspaceId())) {
            log.error("Please bind the drone first.");
        }

        // Ensure device is marked as online when OSD messages are received
        device.setStatus(true);
        deviceRedisService.setDeviceOnline(device);

        OsdRcDrone data = request.getData();
        deviceService.pushOsdDataToPilot(device.getWorkspaceId(), from,
                new DeviceOsdHost()
                        .setLatitude(data.getLatitude())
                        .setLongitude(data.getLongitude())
                        .setElevation(data.getElevation())
                        .setHeight(data.getHeight())
                        .setAttitudeHead(data.getAttitudeHead())
                        .setElevation(data.getElevation())
                        .setHorizontalSpeed(data.getHorizontalSpeed())
                        .setVerticalSpeed(data.getVerticalSpeed()));
        deviceService.pushOsdDataToWeb(device.getWorkspaceId(), BizCodeEnum.DEVICE_OSD, from, data);
    }

    @Override
    public void dockFirmwareVersionUpdate(TopicStateRequest<DockFirmwareVersion> request, MessageHeaders headers) {
        // If the reported version is empty, it will not be processed to prevent misleading page.
        if (!StringUtils.hasText(request.getData().getFirmwareVersion())) {
            return;
        }

        DeviceDTO device = DeviceDTO.builder()
                .deviceSn(request.getFrom())
                .firmwareVersion(request.getData().getFirmwareVersion())
                .firmwareStatus(request.getData().getNeedCompatibleStatus() ?
                        DeviceFirmwareStatusEnum.UNKNOWN : DeviceFirmwareStatusEnum.CONSISTENT_UPGRADE)
                .build();
        boolean isUpd = deviceService.updateDevice(device);
        if (!isUpd) {
            log.error("Data update of firmware version failed. SN: {}", request.getFrom());
        }
    }

    @Override
    public void rcAndDroneFirmwareVersionUpdate(TopicStateRequest<FirmwareVersion> request, MessageHeaders headers) {
        // If the reported version is empty, it will not be processed to prevent misleading page.
        if (!StringUtils.hasText(request.getData().getFirmwareVersion())) {
            return;
        }

        DeviceDTO device = DeviceDTO.builder()
                .deviceSn(request.getFrom())
                .firmwareVersion(request.getData().getFirmwareVersion())
                .build();
        boolean isUpd = deviceService.updateDevice(device);
        if (!isUpd) {
            log.error("Data update of firmware version failed. SN: {}", request.getFrom());
        }
    }

    @Override
    public void rcPayloadFirmwareVersionUpdate(TopicStateRequest<PayloadFirmwareVersion> request, MessageHeaders headers) {
        // If the reported version is empty, it will not be processed to prevent misleading page.
        if (!StringUtils.hasText(request.getData().getFirmwareVersion())) {
            return;
        }

        boolean isUpd = devicePayloadService.updateFirmwareVersion(request.getFrom(), request.getData());
        if (!isUpd) {
            log.error("Data update of payload firmware version failed. SN: {}", request.getFrom());
        }
    }

    @Override
    public void dockControlSourceUpdate(TopicStateRequest<DockDroneControlSource> request, MessageHeaders headers) {
        // If the control source is empty, it will not be processed.
        if (ControlSourceEnum.UNKNOWN == request.getData().getControlSource()) {
            return;
        }
        Optional<DeviceDTO> deviceOpt = deviceRedisService.getDeviceOnline(request.getFrom());
        if (deviceOpt.isEmpty()) {
            return;
        }
        Optional<DeviceDTO> dockOpt = deviceRedisService.getDeviceOnline(request.getGateway());
        if (dockOpt.isEmpty()) {
            return;
        }

        deviceService.updateFlightControl(dockOpt.get(), request.getData().getControlSource());
        devicePayloadService.updatePayloadControl(deviceOpt.get(),
                request.getData().getPayloads().stream()
                        .map(p -> DevicePayloadReceiver.builder()
                                .controlSource(p.getControlSource())
                                .payloadIndex(p.getPayloadIndex())
                                .sn(p.getSn())
                                .deviceSn(request.getFrom())
                                .build()).collect(Collectors.toList()));
    }

    @Override
    public void rcControlSourceUpdate(TopicStateRequest<RcDroneControlSource> request, MessageHeaders headers) {
        // If the control source is empty, it will not be processed.
        if (ControlSourceEnum.UNKNOWN == request.getData().getControlSource()) {
            return;
        }
        Optional<DeviceDTO> deviceOpt = deviceRedisService.getDeviceOnline(request.getFrom());
        if (deviceOpt.isEmpty()) {
            return;
        }
        Optional<DeviceDTO> dockOpt = deviceRedisService.getDeviceOnline(request.getGateway());
        if (dockOpt.isEmpty()) {
            return;
        }

        deviceService.updateFlightControl(dockOpt.get(), request.getData().getControlSource());
        devicePayloadService.updatePayloadControl(deviceOpt.get(),
                request.getData().getPayloads().stream()
                        .map(p -> DevicePayloadReceiver.builder()
                                .controlSource(p.getControlSource())
                                .payloadIndex(p.getPayloadIndex())
                                .sn(p.getSn())
                                .deviceSn(request.getFrom())
                                .build()).collect(Collectors.toList()));
    }

    private void dockGoOnline(DeviceDTO gateway, DeviceDTO subDevice) {
        if (DeviceDomainEnum.DOCK != gateway.getDomain()) {
            return;
        }
        if (!StringUtils.hasText(gateway.getWorkspaceId())) {
            log.error("The dock is not bound, please bind it first and then go online.");
            return;
        }

        // Check if this is a Dock 3 for enhanced logging
        boolean isDock3 = DeviceTypeEnum.DOCK3 == gateway.getType();
        if (isDock3) {
            log.info("Dock 3 {} going online - enhanced processing initiated", gateway.getDeviceSn());
        }

        if (!Objects.requireNonNullElse(subDevice.getBoundStatus(), false)) {
            // Directly bind the drone of the dock to the same workspace as the dock.
            deviceService.bindDevice(DeviceDTO.builder().deviceSn(subDevice.getDeviceSn()).workspaceId(gateway.getWorkspaceId()).build());
            subDevice.setWorkspaceId(gateway.getWorkspaceId());
        }
        deviceRedisService.setDeviceOnline(subDevice);

        // Ensure dock is also marked as online for proper WebSocket event handling
        deviceRedisService.setDeviceOnline(gateway);

        log.debug("Dock {} (Type: {}) completed online process, sub-device: {}",
            gateway.getDeviceSn(), gateway.getType(), subDevice.getDeviceSn());
    }

    private void changeSubDeviceParent(String deviceSn, String gatewaySn) {
        List<DeviceDTO> gatewaysList = deviceService.getDevicesByParams(
                DeviceQueryParam.builder()
                        .childSn(deviceSn)
                        .build());
        gatewaysList.stream()
                .filter(gateway -> !gateway.getDeviceSn().equals(gatewaySn))
                .forEach(gateway -> {
                    gateway.setChildDeviceSn("");
                    deviceService.updateDevice(gateway);
                    deviceRedisService.getDeviceOnline(gateway.getDeviceSn())
                            .ifPresent(device -> {
                                device.setChildDeviceSn(null);
                                deviceRedisService.setDeviceOnline(device);
                            });
                });
    }


    public void deviceOnlineAgain(String workspaceId, String gatewaySn, String deviceSn) {
        DeviceDTO device = DeviceDTO.builder().loginTime(LocalDateTime.now()).deviceSn(deviceSn).build();
        DeviceDTO gateway = DeviceDTO.builder()
                .loginTime(LocalDateTime.now())
                .deviceSn(gatewaySn)
                .childDeviceSn(deviceSn).build();
        deviceService.updateDevice(gateway);
        deviceService.updateDevice(device);
        gateway = deviceRedisService.getDeviceOnline(gatewaySn).map(g -> {
            g.setChildDeviceSn(deviceSn);
            return g;
        }).get();
        device = deviceRedisService.getDeviceOnline(deviceSn).map(d -> {
            d.setParentSn(gatewaySn);
            return d;
        }).get();
        deviceRedisService.setDeviceOnline(gateway);
        deviceRedisService.setDeviceOnline(device);
        if (StringUtils.hasText(workspaceId)) {
            deviceService.subDeviceOnlineSubscribeTopic(SDKManager.getDeviceSDK(gatewaySn));
        }

        log.warn("{} is already online.", deviceSn);
    }

    /**
     * Convert the received gateway device object into a database entity object.
     * @param gateway
     * @return
     */
    private DeviceDTO deviceGatewayConvertToDevice(String gatewaySn, UpdateTopo gateway) {
        if (null == gateway) {
            throw new IllegalArgumentException();
        }
        return DeviceDTO.builder()
                .deviceSn(gatewaySn)
                .subType(gateway.getSubType())
                .type(gateway.getType())
                .thingVersion(gateway.getThingVersion())
                .domain(gateway.getDomain())
                .controlSource(gateway.getSubDevices().isEmpty() ? null :
                        ControlSourceEnum.find(gateway.getSubDevices().get(0).getIndex().getControlSource()))
                .build();
    }

    /**
     * Convert the received drone device object into a database entity object.
     * @param device
     * @return
     */
    private DeviceDTO subDeviceConvertToDevice(UpdateTopoSubDevice device) {
        if (null == device) {
            throw new IllegalArgumentException();
        }
        return DeviceDTO.builder()
                .deviceSn(device.getSn())
                .type(device.getType())
                .subType(device.getSubType())
                .thingVersion(device.getThingVersion())
                .domain(device.getDomain())
                .build();
    }

    private Optional<DeviceDTO> onlineSaveDevice(DeviceDTO device, String childSn, String parentSn) {

        device.setChildDeviceSn(childSn);
        device.setLoginTime(LocalDateTime.now());

        Optional<DeviceDTO> deviceOpt = deviceService.getDeviceBySn(device.getDeviceSn());

        if (deviceOpt.isEmpty()) {
            device.setIconUrl(new DeviceIconUrl());
            // Set the icon of the gateway device displayed in the pilot's map, required in the TSA module.
            device.getIconUrl().setNormalIconUrl(IconUrlEnum.NORMAL_PERSON.getUrl());
            // Set the icon of the gateway device displayed in the pilot's map when it is selected, required in the TSA module.
            device.getIconUrl().setSelectIconUrl(IconUrlEnum.SELECT_PERSON.getUrl());
            device.setBoundStatus(false);

            // Query the model information of this gateway device.
            dictionaryService.getOneDictionaryInfoByTypeSubType(
                    device.getDomain().getDomain(), device.getType().getType(), device.getSubType().getSubType())
                    .ifPresent(entity -> {
                        device.setDeviceName(entity.getDeviceName());
                        device.setNickname(entity.getDeviceName());
                        device.setDeviceDesc(entity.getDeviceDesc());
                    });
        }
        boolean success = deviceService.saveOrUpdateDevice(device);
        if (!success) {
            return Optional.empty();
        }

        deviceOpt = deviceService.getDeviceBySn(device.getDeviceSn());
        DeviceDTO redisDevice = deviceOpt.get();
        redisDevice.setStatus(true);
        redisDevice.setParentSn(parentSn);

        deviceRedisService.setDeviceOnline(redisDevice);
        return deviceOpt;
    }

    private void fillDockOsd(String dockSn, OsdDock dock) {
        Optional<OsdDock> oldDockOpt = deviceRedisService.getDeviceOsd(dockSn, OsdDock.class);
        if (Objects.nonNull(dock.getJobNumber())) {
            return;
        }
        if (oldDockOpt.isEmpty()) {
            deviceRedisService.setDeviceOsd(dockSn, dock);
            return;
        }
        OsdDock oldDock = oldDockOpt.get();
        if (Objects.nonNull(dock.getModeCode())) {
            dock.setDrcState(oldDock.getDrcState());
            deviceRedisService.setDeviceOsd(dockSn, dock);
            return;
        }
        if (Objects.nonNull(dock.getDrcState()) ) {
            oldDock.setDrcState(dock.getDrcState());
            deviceRedisService.setDeviceOsd(dockSn, oldDock);
        }
    }

    @Override
    public void dockWpmzVersionUpdate(TopicStateRequest<DockDroneWpmzVersion> request, MessageHeaders headers) {
        log.info("Received WPMZ version update from {} with version: {}",
            request.getFrom(), request.getData().getWpmzVersion());

        // Get the device information
        Optional<DeviceDTO> deviceOpt = deviceRedisService.getDeviceOnline(request.getFrom());
        if (deviceOpt.isEmpty()) {
            log.warn("Device {} not found online for WPMZ version update", request.getFrom());
            return;
        }

        DeviceDTO device = deviceOpt.get();
        if (!StringUtils.hasText(device.getWorkspaceId())) {
            log.warn("Device {} has no workspace ID for WPMZ version update", request.getFrom());
            return;
        }

        // Process WPMZ version update - store version information if needed
        // For now, just log the version information
        String wpmzVersion = request.getData().getWpmzVersion();
        if (StringUtils.hasText(wpmzVersion)) {
            log.info("Device {} WPMZ version updated to: {}", request.getFrom(), wpmzVersion);
            // You can add custom logic here to store or process the WPMZ version
            // For example, store it in Redis or update device metadata
        } else {
            log.warn("Device {} sent empty WPMZ version", request.getFrom());
        }
    }

    @Override
    public void dockDroneModeCodeReason(TopicStateRequest<DockDroneModeCodeReason> request, MessageHeaders headers) {
        log.info("Received mode code reason update from {} with reason: {}",
            request.getFrom(), request.getData().getModeCodeReason());

        // Get the device information
        Optional<DeviceDTO> deviceOpt = deviceRedisService.getDeviceOnline(request.getFrom());
        if (deviceOpt.isEmpty()) {
            log.warn("Device {} not found online for mode code reason update", request.getFrom());
            return;
        }

        DeviceDTO device = deviceOpt.get();
        if (!StringUtils.hasText(device.getWorkspaceId())) {
            log.warn("Device {} has no workspace ID for mode code reason update", request.getFrom());
            return;
        }

        // Process mode code reason update
        if (request.getData().getModeCodeReason() != null) {
            log.info("Device {} mode code reason updated to: {}", request.getFrom(), request.getData().getModeCodeReason());
            // You can add custom logic here to handle mode code reason changes
        }
    }

    @Override
    public void dockDroneRthMode(TopicStateRequest<DockDroneRthMode> request, MessageHeaders headers) {
        log.info("Received RTH mode update from {} with mode: {}",
            request.getFrom(), request.getData().getRthMode());

        // Get the device information
        Optional<DeviceDTO> deviceOpt = deviceRedisService.getDeviceOnline(request.getFrom());
        if (deviceOpt.isEmpty()) {
            log.warn("Device {} not found online for RTH mode update", request.getFrom());
            return;
        }

        DeviceDTO device = deviceOpt.get();
        if (!StringUtils.hasText(device.getWorkspaceId())) {
            log.warn("Device {} has no workspace ID for RTH mode update", request.getFrom());
            return;
        }

        // Process RTH mode update
        if (request.getData().getRthMode() != null) {
            log.info("Device {} RTH mode updated to: {}", request.getFrom(), request.getData().getRthMode());
            // You can add custom logic here to handle RTH mode changes
        }
    }

    @Override
    public void dockDroneCommanderFlightHeight(TopicStateRequest<DockDroneCommanderFlightHeight> request, MessageHeaders headers) {
        log.info("Received commander flight height update from {} with height: {}",
            request.getFrom(), request.getData().getCommanderFlightHeight());

        // Get the device information
        Optional<DeviceDTO> deviceOpt = deviceRedisService.getDeviceOnline(request.getFrom());
        if (deviceOpt.isEmpty()) {
            log.warn("Device {} not found online for commander flight height update", request.getFrom());
            return;
        }

        DeviceDTO device = deviceOpt.get();
        if (!StringUtils.hasText(device.getWorkspaceId())) {
            log.warn("Device {} has no workspace ID for commander flight height update", request.getFrom());
            return;
        }

        // Process commander flight height update
        if (request.getData().getCommanderFlightHeight() != null) {
            log.info("Device {} commander flight height updated to: {} meters",
                request.getFrom(), request.getData().getCommanderFlightHeight());
            // You can add custom logic here to handle flight height changes
        }
    }

    @Override
    public void dockDroneCommanderModeLostAction(TopicStateRequest<DockDroneCommanderModeLostAction> request, MessageHeaders headers) {
        log.info("Received commander mode lost action update from {} with action: {}",
            request.getFrom(), request.getData().getCommanderModeLostAction());

        // Get the device information
        Optional<DeviceDTO> deviceOpt = deviceRedisService.getDeviceOnline(request.getFrom());
        if (deviceOpt.isEmpty()) {
            log.warn("Device {} not found online for commander mode lost action update", request.getFrom());
            return;
        }

        DeviceDTO device = deviceOpt.get();
        if (!StringUtils.hasText(device.getWorkspaceId())) {
            log.warn("Device {} has no workspace ID for commander mode lost action update", request.getFrom());
            return;
        }

        // Process commander mode lost action update
        if (request.getData().getCommanderModeLostAction() != null) {
            log.info("Device {} commander mode lost action updated to: {}",
                request.getFrom(), request.getData().getCommanderModeLostAction());
            // You can add custom logic here to handle commander mode lost action changes
        }
    }

    @Override
    public void rcLiveStatusUpdate(TopicStateRequest<RcLiveStatus> request, MessageHeaders headers) {
        log.debug("Received RC live status update from {} with data: {}", request.getFrom(), request.getData());

        // Get the device information
        Optional<DeviceDTO> deviceOpt = deviceRedisService.getDeviceOnline(request.getFrom());
        if (deviceOpt.isEmpty()) {
            log.warn("Device {} not found online for live status update", request.getFrom());
            return;
        }

        DeviceDTO device = deviceOpt.get();
        if (!StringUtils.hasText(device.getWorkspaceId())) {
            log.warn("Device {} has no workspace ID for live status update", request.getFrom());
            return;
        }

        // Process live status update - you can add custom logic here
        // For now, just log the status
        if (request.getData() != null && request.getData().getLiveStatus() != null) {
            request.getData().getLiveStatus().forEach(status ->
                log.info("RC {} live status - Video ID: {}, Status: {}, Quality: {}",
                    request.getFrom(), status.getVideoId(), status.getStatus(), status.getVideoQuality())
            );
        }
    }
}
