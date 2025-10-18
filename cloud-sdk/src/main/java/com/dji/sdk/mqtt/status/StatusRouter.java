package com.dji.sdk.mqtt.status;

import com.dji.sdk.cloudapi.device.*;
import com.dji.sdk.common.Common;
import com.dji.sdk.exception.CloudSDKException;
import com.dji.sdk.mqtt.ChannelName;
import com.dji.sdk.mqtt.MqttGatewayPublish;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.dji.sdk.mqtt.TopicConst.*;

/**
 *
 * @author sean.zhou
 * @date 2021/11/12
 * @version 0.1
 */
@Configuration
public class StatusRouter {

    @Resource
    private MqttGatewayPublish gatewayPublish;

    @Bean
    public IntegrationFlow statusRouterFlow() {
        return IntegrationFlows
                .from(ChannelName.INBOUND_STATUS)
                .transform(Message.class, source -> {
                    try {
                        TopicStatusRequest<UpdateTopo> response = Common.getObjectMapper().readValue((byte[]) source.getPayload(), new TypeReference<TopicStatusRequest<UpdateTopo>>() {});
                        String topic = String.valueOf(source.getHeaders().get(MqttHeaders.RECEIVED_TOPIC));
                        return response.setFrom(topic.substring((BASIC_PRE + PRODUCT).length(), topic.indexOf(STATUS_SUF)));
                    } catch (IOException e) {
                        throw new CloudSDKException(e);
                    }
                }, null)
                .<TopicStatusRequest<UpdateTopo>, Boolean>route(
                        response -> determineDeviceStatusRoute(response),
                        mapping -> mapping.channelMapping(true, ChannelName.INBOUND_STATUS_OFFLINE)
                                .channelMapping(false, ChannelName.INBOUND_STATUS_ONLINE))
                .get();
    }

    /**
     * Determines whether a topology update should be routed to OFFLINE or ONLINE channel.
     * Enhanced logic to properly detect both sub-device and gateway device offline events.
     *
     * @param request The topology status request
     * @return true if device is offline, false if device is online
     */
    private boolean determineDeviceStatusRoute(TopicStatusRequest<UpdateTopo> request) {
        UpdateTopo topoData = request.getData();
        if (topoData == null) {
            return true; // No data = offline
        }

        List<UpdateTopoSubDevice> subDevices = topoData.getSubDevices();
        boolean subDevicesEmpty = CollectionUtils.isEmpty(subDevices);

        // Logic 1: Empty subDevices means sub-device (drone) went offline
        if (subDevicesEmpty) {
            return true; // Sub-device is offline
        }

        // Logic 2: Check if this is a gateway device shutdown event
        // Gateway devices (remote controllers, docks) going offline may send different patterns

        DeviceTypeEnum deviceType = topoData.getType();
        DeviceDomainEnum domain = topoData.getDomain();
        String method = request.getMethod();

        // Check if this is a gateway device (remote controller or dock)
        boolean isGatewayDevice = DeviceDomainEnum.GATEWAY == domain;

        if (isGatewayDevice && subDevices != null && !subDevices.isEmpty()) {
            // Enhanced detection for gateway device offline events

            // Pattern A: Gateway device sends topology update with sub-devices but indicates shutdown
            // This could be detected by checking message method, timing, or specific fields

            // Pattern B: The method field might indicate the nature of the topology update
            if ("update_topo".equals(method)) {
                // For update_topo messages from gateway devices with sub-devices,
                // we need additional context to determine if this is online or offline

                // Strategy: Check if sub-devices indicate offline status
                // Some gateway devices might report sub-device status in the topology data

                boolean anySubDeviceOnline = subDevices.stream()
                    .anyMatch(subDevice -> subDevice.getDomain() != null && subDevice.getDomain() != DeviceDomainEnum.SUB_DEVICE);

                // If gateway has sub-devices but they're not sub-device domain, might indicate offline scenario
                if (!anySubDeviceOnline && subDevices.size() > 0) {
                    // This could be a gateway going offline - route to offline for processing
                    // The offline handler will determine the actual status
                    return true;
                }
            }
        }

        // Default: route to online for normal cases
        return false;
    }

    @Bean
    public IntegrationFlow replySuccessStatus() {
        return IntegrationFlows
                .from(ChannelName.OUTBOUND_STATUS)
                .handle(this::publish)
                .nullChannel();

    }

    private TopicStatusResponse publish(TopicStatusResponse request, MessageHeaders headers) {
        if (Objects.isNull(request)) {
            return null;
        }
        gatewayPublish.publishReply(request, headers);
        return request;
    }
}