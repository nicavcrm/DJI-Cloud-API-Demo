package com.dji.sdk.cloudapi.device;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author sean
 * @version 1.0
 * @date 2024/10/17
 */
public class AiIdentifyOn {

    @JsonValue
    @JsonProperty("ai_identify_on")
    private Integer state;

    public AiIdentifyOn() {
    }

    public AiIdentifyOn(Integer state) {
        this.state = state;
    }

    public Integer getState() {
        return state;
    }

    public AiIdentifyOn setState(Integer state) {
        this.state = state;
        return this;
    }

    @Override
    public String toString() {
        return "AiIdentifyOn{" +
                "state=" + state +
                '}';
    }
}