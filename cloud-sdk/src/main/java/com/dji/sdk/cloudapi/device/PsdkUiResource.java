package com.dji.sdk.cloudapi.device;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author sean
 * @version 1.0
 * @date 2024/10/17
 */
public class PsdkUiResource {

    @JsonProperty("psdk_ui_resource")
    private List<Object> resources;

    public PsdkUiResource() {
    }

    public List<Object> getResources() {
        return resources;
    }

    public PsdkUiResource setResources(List<Object> resources) {
        this.resources = resources;
        return this;
    }

    @Override
    public String toString() {
        return "PsdkUiResource{" +
                "resources=" + resources +
                '}';
    }
}