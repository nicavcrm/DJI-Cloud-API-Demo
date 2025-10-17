package com.dji.sdk.cloudapi.device;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author sean
 * @version 1.0
 * @date 2023/9/7
 *
 * Dock 3 specific drone OSD class with snake_case to camelCase field mapping
 * Dock 3 sends MQTT messages with snake_case field names instead of camelCase
 * This class handles the proper JSON deserialization for Dock 3 drone devices
 */
public class OsdDockDrone3 {

    @JsonProperty("attitude_head")
    private Float attitudeHead;

    @JsonProperty("attitude_pitch")
    private Double attitudePitch;

    @JsonProperty("attitude_roll")
    private Double attitudeRoll;

    private Float elevation;

    private DroneBattery battery;

    @JsonProperty("firmware_version")
    private String firmwareVersion;

    private GearEnum gear;

    private Float height;

    @JsonProperty("home_distance")
    private Float homeDistance;

    @JsonProperty("horizontal_speed")
    private Float horizontalSpeed;

    private Float latitude;

    private Float longitude;

    @JsonProperty("mode_code")
    private DroneModeCodeEnum modeCode;

    @JsonProperty("total_flight_distance")
    private Double totalFlightDistance;

    @JsonProperty("total_flight_time")
    private Float totalFlightTime;

    @JsonProperty("vertical_speed")
    private Float verticalSpeed;

    @JsonProperty("wind_direction")
    private WindDirectionEnum windDirection;

    @JsonProperty("wind_speed")
    private Float windSpeed;

    @JsonProperty("position_state")
    private DronePositionState positionState;

    @JsonProperty("payload_bindings")
    private List<DockDronePayload> payloads;

    private Storage storage;

    @JsonProperty("night_lights_state")
    private SwitchActionEnum nightLightsState;

    @JsonProperty("height_limit")
    private Integer heightLimit;

    @JsonProperty("distance_limit_status")
    private DockDistanceLimitStatus distanceLimitStatus;

    @JsonProperty("obstacle_avoidance")
    private ObstacleAvoidance obstacleAvoidance;

    @JsonProperty("activation_time")
    private Long activationTime;

    private List<OsdCamera> cameras;

    @JsonProperty("rc_lost_action")
    private RcLostActionEnum rcLostAction;

    @JsonProperty("rth_altitude")
    private Integer rthAltitude;

    @JsonProperty("total_flight_sorties")
    private Integer totalFlightSorties;

    private String country;

    @JsonProperty("rid_state")
    private Boolean ridState;

    @JsonProperty("is_near_area_limit")
    private Boolean nearAreaLimit;

    @JsonProperty("is_near_height_limit")
    private Boolean nearHeightLimit;

    @JsonProperty("maintain_status")
    private OsdDroneMaintainStatus maintainStatus;

    private String trackId;

    public OsdDockDrone3() {
    }

    /**
     * Convert Dock 3 drone OSD to standard OSD format for compatibility
     */
    public OsdDockDrone toStandardOsdDockDrone() {
        OsdDockDrone standardDrone = new OsdDockDrone();
        standardDrone.setAttitudeHead(this.attitudeHead);
        standardDrone.setAttitudePitch(this.attitudePitch);
        standardDrone.setAttitudeRoll(this.attitudeRoll);
        standardDrone.setElevation(this.elevation);
        standardDrone.setBattery(this.battery);
        standardDrone.setFirmwareVersion(this.firmwareVersion);
        standardDrone.setGear(this.gear);
        standardDrone.setHeight(this.height);
        standardDrone.setHomeDistance(this.homeDistance);
        standardDrone.setHorizontalSpeed(this.horizontalSpeed);
        standardDrone.setLatitude(this.latitude);
        standardDrone.setLongitude(this.longitude);
        standardDrone.setModeCode(this.modeCode);
        standardDrone.setTotalFlightDistance(this.totalFlightDistance);
        standardDrone.setTotalFlightTime(this.totalFlightTime);
        standardDrone.setVerticalSpeed(this.verticalSpeed);
        standardDrone.setWindDirection(this.windDirection);
        standardDrone.setWindSpeed(this.windSpeed);
        standardDrone.setPositionState(this.positionState);
        standardDrone.setPayloads(this.payloads);
        standardDrone.setStorage(this.storage);
        standardDrone.setNightLightsState(this.nightLightsState);
        standardDrone.setHeightLimit(this.heightLimit);
        standardDrone.setDistanceLimitStatus(this.distanceLimitStatus);
        standardDrone.setObstacleAvoidance(this.obstacleAvoidance);
        standardDrone.setActivationTime(this.activationTime);
        standardDrone.setCameras(this.cameras);
        standardDrone.setRcLostAction(this.rcLostAction);
        standardDrone.setRthAltitude(this.rthAltitude);
        standardDrone.setTotalFlightSorties(this.totalFlightSorties);
        standardDrone.setCountry(this.country);
        standardDrone.setRidState(this.ridState);
        standardDrone.setNearAreaLimit(this.nearAreaLimit);
        standardDrone.setNearHeightLimit(this.nearHeightLimit);
        standardDrone.setMaintainStatus(this.maintainStatus);
        standardDrone.setTrackId(this.trackId);
        return standardDrone;
    }

    // Getters and Setters
    public Float getAttitudeHead() {
        return attitudeHead;
    }

    public OsdDockDrone3 setAttitudeHead(Float attitudeHead) {
        this.attitudeHead = attitudeHead;
        return this;
    }

    public Double getAttitudePitch() {
        return attitudePitch;
    }

    public OsdDockDrone3 setAttitudePitch(Double attitudePitch) {
        this.attitudePitch = attitudePitch;
        return this;
    }

    public Double getAttitudeRoll() {
        return attitudeRoll;
    }

    public OsdDockDrone3 setAttitudeRoll(Double attitudeRoll) {
        this.attitudeRoll = attitudeRoll;
        return this;
    }

    public Float getElevation() {
        return elevation;
    }

    public OsdDockDrone3 setElevation(Float elevation) {
        this.elevation = elevation;
        return this;
    }

    public DroneBattery getBattery() {
        return battery;
    }

    public OsdDockDrone3 setBattery(DroneBattery battery) {
        this.battery = battery;
        return this;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public OsdDockDrone3 setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
        return this;
    }

    public GearEnum getGear() {
        return gear;
    }

    public OsdDockDrone3 setGear(GearEnum gear) {
        this.gear = gear;
        return this;
    }

    public Float getHeight() {
        return height;
    }

    public OsdDockDrone3 setHeight(Float height) {
        this.height = height;
        return this;
    }

    public Float getHomeDistance() {
        return homeDistance;
    }

    public OsdDockDrone3 setHomeDistance(Float homeDistance) {
        this.homeDistance = homeDistance;
        return this;
    }

    public Float getHorizontalSpeed() {
        return horizontalSpeed;
    }

    public OsdDockDrone3 setHorizontalSpeed(Float horizontalSpeed) {
        this.horizontalSpeed = horizontalSpeed;
        return this;
    }

    public Float getLatitude() {
        return latitude;
    }

    public OsdDockDrone3 setLatitude(Float latitude) {
        this.latitude = latitude;
        return this;
    }

    public Float getLongitude() {
        return longitude;
    }

    public OsdDockDrone3 setLongitude(Float longitude) {
        this.longitude = longitude;
        return this;
    }

    public DroneModeCodeEnum getModeCode() {
        return modeCode;
    }

    public OsdDockDrone3 setModeCode(DroneModeCodeEnum modeCode) {
        this.modeCode = modeCode;
        return this;
    }

    public Double getTotalFlightDistance() {
        return totalFlightDistance;
    }

    public OsdDockDrone3 setTotalFlightDistance(Double totalFlightDistance) {
        this.totalFlightDistance = totalFlightDistance;
        return this;
    }

    public Float getTotalFlightTime() {
        return totalFlightTime;
    }

    public OsdDockDrone3 setTotalFlightTime(Float totalFlightTime) {
        this.totalFlightTime = totalFlightTime;
        return this;
    }

    public Float getVerticalSpeed() {
        return verticalSpeed;
    }

    public OsdDockDrone3 setVerticalSpeed(Float verticalSpeed) {
        this.verticalSpeed = verticalSpeed;
        return this;
    }

    public WindDirectionEnum getWindDirection() {
        return windDirection;
    }

    public OsdDockDrone3 setWindDirection(WindDirectionEnum windDirection) {
        this.windDirection = windDirection;
        return this;
    }

    public Float getWindSpeed() {
        return windSpeed;
    }

    public OsdDockDrone3 setWindSpeed(Float windSpeed) {
        this.windSpeed = windSpeed;
        return this;
    }

    public DronePositionState getPositionState() {
        return positionState;
    }

    public OsdDockDrone3 setPositionState(DronePositionState positionState) {
        this.positionState = positionState;
        return this;
    }

    public List<DockDronePayload> getPayloads() {
        return payloads;
    }

    public OsdDockDrone3 setPayloads(List<DockDronePayload> payloads) {
        this.payloads = payloads;
        return this;
    }

    public Storage getStorage() {
        return storage;
    }

    public OsdDockDrone3 setStorage(Storage storage) {
        this.storage = storage;
        return this;
    }

    public SwitchActionEnum getNightLightsState() {
        return nightLightsState;
    }

    public OsdDockDrone3 setNightLightsState(SwitchActionEnum nightLightsState) {
        this.nightLightsState = nightLightsState;
        return this;
    }

    public Integer getHeightLimit() {
        return heightLimit;
    }

    public OsdDockDrone3 setHeightLimit(Integer heightLimit) {
        this.heightLimit = heightLimit;
        return this;
    }

    public DockDistanceLimitStatus getDistanceLimitStatus() {
        return distanceLimitStatus;
    }

    public OsdDockDrone3 setDistanceLimitStatus(DockDistanceLimitStatus distanceLimitStatus) {
        this.distanceLimitStatus = distanceLimitStatus;
        return this;
    }

    public ObstacleAvoidance getObstacleAvoidance() {
        return obstacleAvoidance;
    }

    public OsdDockDrone3 setObstacleAvoidance(ObstacleAvoidance obstacleAvoidance) {
        this.obstacleAvoidance = obstacleAvoidance;
        return this;
    }

    public Long getActivationTime() {
        return activationTime;
    }

    public OsdDockDrone3 setActivationTime(Long activationTime) {
        this.activationTime = activationTime;
        return this;
    }

    public List<OsdCamera> getCameras() {
        return cameras;
    }

    public OsdDockDrone3 setCameras(List<OsdCamera> cameras) {
        this.cameras = cameras;
        return this;
    }

    public RcLostActionEnum getRcLostAction() {
        return rcLostAction;
    }

    public OsdDockDrone3 setRcLostAction(RcLostActionEnum rcLostAction) {
        this.rcLostAction = rcLostAction;
        return this;
    }

    public Integer getRthAltitude() {
        return rthAltitude;
    }

    public OsdDockDrone3 setRthAltitude(Integer rthAltitude) {
        this.rthAltitude = rthAltitude;
        return this;
    }

    public Integer getTotalFlightSorties() {
        return totalFlightSorties;
    }

    public OsdDockDrone3 setTotalFlightSorties(Integer totalFlightSorties) {
        this.totalFlightSorties = totalFlightSorties;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public OsdDockDrone3 setCountry(String country) {
        this.country = country;
        return this;
    }

    public Boolean getRidState() {
        return ridState;
    }

    public OsdDockDrone3 setRidState(Boolean ridState) {
        this.ridState = ridState;
        return this;
    }

    public Boolean getNearAreaLimit() {
        return nearAreaLimit;
    }

    public OsdDockDrone3 setNearAreaLimit(Boolean nearAreaLimit) {
        this.nearAreaLimit = nearAreaLimit;
        return this;
    }

    public Boolean getNearHeightLimit() {
        return nearHeightLimit;
    }

    public OsdDockDrone3 setNearHeightLimit(Boolean nearHeightLimit) {
        this.nearHeightLimit = nearHeightLimit;
        return this;
    }

    public OsdDroneMaintainStatus getMaintainStatus() {
        return maintainStatus;
    }

    public OsdDockDrone3 setMaintainStatus(OsdDroneMaintainStatus maintainStatus) {
        this.maintainStatus = maintainStatus;
        return this;
    }

    public String getTrackId() {
        return trackId;
    }

    public OsdDockDrone3 setTrackId(String trackId) {
        this.trackId = trackId;
        return this;
    }

    @Override
    public String toString() {
        return "OsdDockDrone3{" +
                "attitudeHead=" + attitudeHead +
                ", attitudePitch=" + attitudePitch +
                ", attitudeRoll=" + attitudeRoll +
                ", elevation=" + elevation +
                ", battery=" + battery +
                ", firmwareVersion='" + firmwareVersion + '\'' +
                ", gear=" + gear +
                ", height=" + height +
                ", homeDistance=" + homeDistance +
                ", horizontalSpeed=" + horizontalSpeed +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", modeCode=" + modeCode +
                ", totalFlightDistance=" + totalFlightDistance +
                ", totalFlightTime=" + totalFlightTime +
                ", verticalSpeed=" + verticalSpeed +
                ", windDirection=" + windDirection +
                ", windSpeed=" + windSpeed +
                ", positionState=" + positionState +
                ", payloads=" + payloads +
                ", storage=" + storage +
                ", nightLightsState=" + nightLightsState +
                ", heightLimit=" + heightLimit +
                ", distanceLimitStatus=" + distanceLimitStatus +
                ", obstacleAvoidance=" + obstacleAvoidance +
                ", activationTime=" + activationTime +
                ", cameras=" + cameras +
                ", rcLostAction=" + rcLostAction +
                ", rthAltitude=" + rthAltitude +
                ", totalFlightSorties=" + totalFlightSorties +
                ", country='" + country + '\'' +
                ", ridState=" + ridState +
                ", nearAreaLimit=" + nearAreaLimit +
                ", nearHeightLimit=" + nearHeightLimit +
                ", maintainStatus=" + maintainStatus +
                ", trackId='" + trackId + '\'' +
                '}';
    }
}