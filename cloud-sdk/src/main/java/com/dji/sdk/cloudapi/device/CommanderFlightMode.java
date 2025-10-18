package com.dji.sdk.cloudapi.device;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author sean
 * @version 1.0
 * @date 2024/10/17
 */
public class CommanderFlightMode {

    @JsonValue
    @JsonProperty("commander_flight_mode")
    private Integer mode;

    public CommanderFlightMode() {
    }

    public CommanderFlightMode(Integer mode) {
        this.mode = mode;
    }

    public Integer getMode() {
        return mode;
    }

    public CommanderFlightMode setMode(Integer mode) {
        this.mode = mode;
        return this;
    }

    @Override
    public String toString() {
        return "CommanderFlightMode{" +
                "mode=" + mode +
                '}';
    }
}