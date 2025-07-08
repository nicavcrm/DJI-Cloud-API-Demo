package com.dji.sdk.cloudapi.device;

import java.util.Map;

/**
 * PSDK widget values
 *
 * @author avcrm
 * @version 1.10
 * @date 2025/07/08
 */
public class PsdkWidgetValues {

    /**
     * Widget values as key-value pairs
     */
    private Map<String, Object> values;

    public PsdkWidgetValues() {
    }

    @Override
    public String toString() {
        return "PsdkWidgetValues{" +
                "values=" + values +
                '}';
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public PsdkWidgetValues setValues(Map<String, Object> values) {
        this.values = values;
        return this;
    }
}
