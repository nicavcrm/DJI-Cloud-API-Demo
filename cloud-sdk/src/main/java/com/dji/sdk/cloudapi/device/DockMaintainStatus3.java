package com.dji.sdk.cloudapi.device;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Dock 3 specific maintain status with array
 */
public class DockMaintainStatus3 {
    @JsonProperty("maintain_status_array")
    private MaintainStatusItem[] maintainStatusArray;

    public DockMaintainStatus3() {
    }

    public MaintainStatusItem[] getMaintainStatusArray() {
        return maintainStatusArray;
    }

    public DockMaintainStatus3 setMaintainStatusArray(MaintainStatusItem[] maintainStatusArray) {
        this.maintainStatusArray = maintainStatusArray;
        return this;
    }

    public static class MaintainStatusItem {
        @JsonProperty("state")
        private Integer state;

        @JsonProperty("last_maintain_type")
        private Integer lastMaintainType;

        @JsonProperty("last_maintain_time")
        private Long lastMaintainTime;

        @JsonProperty("last_maintain_work_sorties")
        private Integer lastMaintainWorkSorties;

        public MaintainStatusItem() {
        }

        public Integer getState() {
            return state;
        }

        public MaintainStatusItem setState(Integer state) {
            this.state = state;
            return this;
        }

        public Integer getLastMaintainType() {
            return lastMaintainType;
        }

        public MaintainStatusItem setLastMaintainType(Integer lastMaintainType) {
            this.lastMaintainType = lastMaintainType;
            return this;
        }

        public Long getLastMaintainTime() {
            return lastMaintainTime;
        }

        public MaintainStatusItem setLastMaintainTime(Long lastMaintainTime) {
            this.lastMaintainTime = lastMaintainTime;
            return this;
        }

        public Integer getLastMaintainWorkSorties() {
            return lastMaintainWorkSorties;
        }

        public MaintainStatusItem setLastMaintainWorkSorties(Integer lastMaintainWorkSorties) {
            this.lastMaintainWorkSorties = lastMaintainWorkSorties;
            return this;
        }
    }
}