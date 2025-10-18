package com.dji.sdk.cloudapi.device;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author sean
 * @version 1.0
 * @date 2024/10/17
 */
public class UomRealNameState {

    @JsonValue
    @JsonProperty("uom_real_name_state")
    private Integer state;

    public UomRealNameState() {
    }

    public UomRealNameState(Integer state) {
        this.state = state;
    }

    public Integer getState() {
        return state;
    }

    public UomRealNameState setState(Integer state) {
        this.state = state;
        return this;
    }

    @Override
    public String toString() {
        return "UomRealNameState{" +
                "state=" + state +
                '}';
    }
}