package com.dji.sdk.cloudapi.device;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author sean
 * @version 1.0
 * @date 2024/10/17
 */
public class AiSpotlightZoomState {

    @JsonProperty("exception_status")
    private Integer exceptionStatus;

    private Integer state;

    public AiSpotlightZoomState() {
    }

    public Integer getExceptionStatus() {
        return exceptionStatus;
    }

    public AiSpotlightZoomState setExceptionStatus(Integer exceptionStatus) {
        this.exceptionStatus = exceptionStatus;
        return this;
    }

    public Integer getState() {
        return state;
    }

    public AiSpotlightZoomState setState(Integer state) {
        this.state = state;
        return this;
    }

    @Override
    public String toString() {
        return "AiSpotlightZoomState{" +
                "exceptionStatus=" + exceptionStatus +
                ", state=" + state +
                '}';
    }
}