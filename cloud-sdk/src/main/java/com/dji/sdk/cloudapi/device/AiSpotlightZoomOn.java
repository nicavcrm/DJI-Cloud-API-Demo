package com.dji.sdk.cloudapi.device;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author sean
 * @version 1.0
 * @date 2024/10/17
 */
public class AiSpotlightZoomOn {

    @JsonValue
    @JsonProperty("ai_spotlight_zoom_on")
    private Integer state;

    public AiSpotlightZoomOn() {
    }

    public AiSpotlightZoomOn(Integer state) {
        this.state = state;
    }

    public Integer getState() {
        return state;
    }

    public AiSpotlightZoomOn setState(Integer state) {
        this.state = state;
        return this;
    }

    @Override
    public String toString() {
        return "AiSpotlightZoomOn{" +
                "state=" + state +
                '}';
    }
}