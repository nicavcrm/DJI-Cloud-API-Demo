package com.dji.sdk.cloudapi.device;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author sean
 * @version 1.0
 * @date 2024/10/17
 */
public class FlysafeDatabaseVersion {

    @JsonValue
    @JsonProperty("flysafe_database_version")
    private String version;

    public FlysafeDatabaseVersion() {
    }

    public FlysafeDatabaseVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public FlysafeDatabaseVersion setVersion(String version) {
        this.version = version;
        return this;
    }

    @Override
    public String toString() {
        return "FlysafeDatabaseVersion{" +
                "version='" + version + '\'' +
                '}';
    }
}