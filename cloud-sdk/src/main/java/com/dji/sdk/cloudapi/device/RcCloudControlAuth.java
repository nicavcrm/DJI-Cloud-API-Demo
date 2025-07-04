package com.dji.sdk.cloudapi.device;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

/**
 * RC cloud control authentication state data
 */
public class RcCloudControlAuth {

    @NotNull
    @JsonProperty("cloud_control_auth")
    private Integer cloudControlAuth;

    public RcCloudControlAuth() {
    }

    public Integer getCloudControlAuth() {
        return cloudControlAuth;
    }

    public void setCloudControlAuth(Integer cloudControlAuth) {
        this.cloudControlAuth = cloudControlAuth;
    }

    @Override
    public String toString() {
        return "RcCloudControlAuth{" +
                "cloudControlAuth=" + cloudControlAuth +
                '}';
    }
}