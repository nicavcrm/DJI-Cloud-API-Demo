package com.dji.sdk.cloudapi.device;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Dock 3 specific drone battery maintenance info
 */
public class DroneBatteryMaintenanceInfo3 {
    @JsonProperty("maintenance_state")
    private Integer maintenanceState;

    @JsonProperty("maintenance_time_left")
    private Integer maintenanceTimeLeft;

    @JsonProperty("heat_state")
    private Integer heatState;

    @JsonProperty("batteries")
    private BatteryInfo[] batteries;

    public DroneBatteryMaintenanceInfo3() {
    }

    public Integer getMaintenanceState() {
        return maintenanceState;
    }

    public DroneBatteryMaintenanceInfo3 setMaintenanceState(Integer maintenanceState) {
        this.maintenanceState = maintenanceState;
        return this;
    }

    public Integer getMaintenanceTimeLeft() {
        return maintenanceTimeLeft;
    }

    public DroneBatteryMaintenanceInfo3 setMaintenanceTimeLeft(Integer maintenanceTimeLeft) {
        this.maintenanceTimeLeft = maintenanceTimeLeft;
        return this;
    }

    public Integer getHeatState() {
        return heatState;
    }

    public DroneBatteryMaintenanceInfo3 setHeatState(Integer heatState) {
        this.heatState = heatState;
        return this;
    }

    public BatteryInfo[] getBatteries() {
        return batteries;
    }

    public DroneBatteryMaintenanceInfo3 setBatteries(BatteryInfo[] batteries) {
        this.batteries = batteries;
        return this;
    }

    public static class BatteryInfo {
        @JsonProperty("index")
        private Integer index;

        @JsonProperty("capacity_percent")
        private Integer capacityPercent;

        @JsonProperty("voltage")
        private Integer voltage;

        @JsonProperty("temperature")
        private Double temperature;

        public BatteryInfo() {
        }

        public Integer getIndex() {
            return index;
        }

        public BatteryInfo setIndex(Integer index) {
            this.index = index;
            return this;
        }

        public Integer getCapacityPercent() {
            return capacityPercent;
        }

        public BatteryInfo setCapacityPercent(Integer capacityPercent) {
            this.capacityPercent = capacityPercent;
            return this;
        }

        public Integer getVoltage() {
            return voltage;
        }

        public BatteryInfo setVoltage(Integer voltage) {
            this.voltage = voltage;
            return this;
        }

        public Double getTemperature() {
            return temperature;
        }

        public BatteryInfo setTemperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }
    }
}