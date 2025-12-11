package com.dji.sdk.cloudapi.device;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author system
 * @version 1.0
 * @date 2024/12/11
 */
public class RcCameras {

    private List<OsdCamera> cameras;

    public RcCameras() {
    }

    @JsonCreator
    public RcCameras(@JsonProperty("cameras") List<OsdCamera> cameras) {
        this.cameras = cameras;
    }

    @JsonValue
    public List<OsdCamera> getCameras() {
        return cameras;
    }

    public RcCameras setCameras(List<OsdCamera> cameras) {
        this.cameras = cameras;
        return this;
    }

    @Override
    public String toString() {
        return "RcCameras{" +
                "cameras=" + cameras +
                '}';
    }
}