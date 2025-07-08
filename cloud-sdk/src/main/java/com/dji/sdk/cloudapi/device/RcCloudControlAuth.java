package com.dji.sdk.cloudapi.device;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

/**
 * RC cloud control authentication state data
 */
public class RcCloudControlAuth {

    @NotNull
    @JsonProperty("cloud_control_auth")
    private Object cloudControlAuth;

    public RcCloudControlAuth() {
    }

    public Object getCloudControlAuth() {
        return cloudControlAuth;
    }

    public void setCloudControlAuth(Object cloudControlAuth) {
        this.cloudControlAuth = cloudControlAuth;
    }

    @Override
    public String toString() {
        return "RcCloudControlAuth{" +
                "cloudControlAuth=" + cloudControlAuth +
                '}';
    }
}