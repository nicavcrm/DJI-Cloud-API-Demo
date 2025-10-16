package com.dji.sample.manage.service.impl;

import com.dji.sdk.cloudapi.airsense.AirsenseWarning;
import com.dji.sdk.cloudapi.airsense.api.AbstractAirsenseService;
import com.dji.sdk.mqtt.MqttReply;
import com.dji.sdk.mqtt.events.TopicEventsRequest;
import com.dji.sdk.mqtt.events.TopicEventsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Airsense warning event handler service
 * @author claude
 * @version 1.0
 */
@Service
@Slf4j
public class DeviceAirsenseServiceImpl extends AbstractAirsenseService {

    @Override
    public TopicEventsResponse<MqttReply> airsenseWarning(TopicEventsRequest<List<AirsenseWarning>> request, MessageHeaders headers) {
        log.info("Received airsense warning from device {}: {} warnings", 
                request.getFrom(), request.getData().size());
        
        // Log warning details for debugging
        request.getData().forEach(warning -> {
            log.debug("Airsense warning - ICAO: {}, Level: {}, Position: [{}, {}], Altitude: {}m, Distance: {}m", 
                    warning.getIcao(), 
                    warning.getWarningLevel(),
                    warning.getLatitude(), 
                    warning.getLongitude(),
                    warning.getAltitude(),
                    warning.getDistance());
        });

        // Return successful response (no reply needed for events)
        return new TopicEventsResponse<MqttReply>().setData(MqttReply.success());
    }
}