package com.dji.sdk.cloudapi.device;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Dock 3 specific self converge coordinate
 */
public class SelfConvergeCoordinate {
    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("height")
    private Double height;

    public SelfConvergeCoordinate() {
    }

    public Double getLongitude() {
        return longitude;
    }

    public SelfConvergeCoordinate setLongitude(Double longitude) {
        this.longitude = longitude;
        return this;
    }

    public Double getLatitude() {
        return latitude;
    }

    public SelfConvergeCoordinate setLatitude(Double latitude) {
        this.latitude = latitude;
        return this;
    }

    public Double getHeight() {
        return height;
    }

    public SelfConvergeCoordinate setHeight(Double height) {
        this.height = height;
        return this;
    }
}