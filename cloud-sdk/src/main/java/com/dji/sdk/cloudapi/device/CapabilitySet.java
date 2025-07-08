package com.dji.sdk.cloudapi.device;

import java.util.Map;

/**
 * Device capability set
 *
 * @author avcrm
 * @version 1.10
 * @date 2025/07/08
 */
public class CapabilitySet {

    /**
     * Capability values as key-value pairs
     */
    private Map<String, Object> capabilities;

    public CapabilitySet() {
    }

    @Override
    public String toString() {
        return "CapabilitySet{" +
                "capabilities=" + capabilities +
                '}';
    }

    public Map<String, Object> getCapabilities() {
        return capabilities;
    }

    public CapabilitySet setCapabilities(Map<String, Object> capabilities) {
        this.capabilities = capabilities;
        return this;
    }
}
