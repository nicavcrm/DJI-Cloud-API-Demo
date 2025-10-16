package com.dji.sdk.cloudapi.device;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Dock 3 specific relative alternate land point
 */
public class RelativeAlternateLandPoint {
    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("safe_land_height")
    private Integer safeLandHeight;

    @JsonProperty("status")
    private Integer status;

    public RelativeAlternateLandPoint() {
    }

    public Double getLongitude() {
        return longitude;
    }

    public RelativeAlternateLandPoint setLongitude(Double longitude) {
        this.longitude = longitude;
        return this;
    }

    public Double getLatitude() {
        return latitude;
    }

    public RelativeAlternateLandPoint setLatitude(Double latitude) {
        this.latitude = latitude;
        return this;
    }

    public Integer getSafeLandHeight() {
        return safeLandHeight;
    }

    public RelativeAlternateLandPoint setSafeLandHeight(Integer safeLandHeight) {
        this.safeLandHeight = safeLandHeight;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public RelativeAlternateLandPoint setStatus(Integer status) {
        this.status = status;
        return this;
    }
}