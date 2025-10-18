package com.dji.sdk.cloudapi.device;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author sean
 * @version 1.0
 * @date 2024/10/17
 */
public class GeoCagingStatus {

    @JsonProperty("geo_caging_status")
    private GeoCagingData data;

    public static class GeoCagingData {
        private Integer state;

        public Integer getState() {
            return state;
        }

        public GeoCagingData setState(Integer state) {
            this.state = state;
            return this;
        }

        @Override
        public String toString() {
            return "GeoCagingData{" +
                    "state=" + state +
                    '}';
        }
    }

    public GeoCagingData getData() {
        return data;
    }

    public GeoCagingStatus setData(GeoCagingData data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "GeoCagingStatus{" +
                "data=" + data +
                '}';
    }
}