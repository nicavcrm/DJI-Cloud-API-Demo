package com.dji.sdk.cloudapi.device;

import com.dji.sdk.annotations.CloudSDKVersion;
import com.dji.sdk.config.version.CloudSDKVersionEnum;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author sean
 * @version 1.0
 * @date 2023/9/7
 *
 * Dock 3 specific OSD class with snake_case to camelCase field mapping
 * Dock 3 sends MQTT messages with snake_case field names instead of camelCase
 * This class handles the proper JSON deserialization for Dock 3 devices
 */
public class OsdDock3 {

    @JsonProperty("network_state")
    private NetworkState networkState;

    @JsonProperty("drone_in_dock")
    private Boolean droneInDock;

    @JsonProperty("drone_charge_state")
    private DroneChargeState droneChargeState;

    private RainfallEnum rainfall;

    @JsonProperty("wind_speed")
    private Float windSpeed;

    @JsonProperty("environment_temperature")
    private Float environmentTemperature;

    private Float temperature;

    private Integer humidity;

    private Float latitude;

    private Float longitude;

    private Float height;

    @JsonProperty("alternate_land_point")
    private AlternateLandPoint alternateLandPoint;

    @JsonProperty("first_power_on")
    private Long firstPowerOn;

    @JsonProperty("position_state")
    private DockPositionState positionState;

    private Storage storage;

    @JsonProperty("mode_code")
    private DockModeCodeEnum modeCode;

    @JsonProperty("cover_state")
    private CoverStateEnum coverState;

    @JsonProperty("supplement_light_state")
    private Boolean supplementLightState;

    @JsonProperty("emergency_stop_state")
    private Boolean emergencyStopState;

    private AirConditioner airConditioner;

    @JsonProperty("battery_store_mode")
    private BatteryStoreModeEnum batteryStoreMode;

    @JsonProperty("alarm_state")
    private Boolean alarmState;

    @JsonProperty("putter_state")
    private PutterStateEnum putterState;

    @JsonProperty("sub_device")
    private DockSubDevice subDevice;

    @JsonProperty("job_number")
    private Integer jobNumber;

    @JsonProperty("acc_time")
    private Long accTime;

    @JsonProperty("activation_time")
    private Long activationTime;

  
    @JsonProperty("electric_supply_voltage")
    private Integer electricSupplyVoltage;

    @JsonProperty("working_voltage")
    private Integer workingVoltage;

    @JsonProperty("working_current")
    private Integer workingCurrent;

    @JsonProperty("backup_battery")
    private BackupBattery backupBattery;

    
    @JsonProperty("flighttask_step_code")
    private FlighttaskStepCodeEnum flighttaskStepCode;

    @JsonProperty("flighttask_prepare_capacity")
    private Integer flighttaskPrepareCapacity;

    @JsonProperty("media_file_detail")
    private MediaFileDetail mediaFileDetail;

    @JsonProperty("wireless_link")
    private WirelessLink wirelessLink;

    @JsonProperty("drc_state")
    private DrcStateEnum drcState;

    /**
     * User experience improvement program
     */
    @CloudSDKVersion(since = CloudSDKVersionEnum.V1_0_0)
    @JsonProperty("user_experience_improvement")
    private UserExperienceImprovementEnum userExperienceImprovement;

    // Dock 3 specific additional fields
    @JsonProperty("deployment_mode")
    private Integer deploymentMode;

    @JsonProperty("relative_alternate_land_point")
    private RelativeAlternateLandPoint relativeAlternateLandPoint;

    @JsonProperty("self_converge_coordinate")
    private SelfConvergeCoordinate selfConvergeCoordinate;

    @JsonProperty("maintain_status")
    private DockMaintainStatus3 maintainStatus3;

    @JsonProperty("gimbal_holder_state")
    private Integer gimbalHolderState;

    @JsonProperty("drone_battery_maintenance_info")
    private DroneBatteryMaintenanceInfo3 droneBatteryMaintenanceInfo3;

    public OsdDock3() {
    }

    /**
     * Convert Dock 3 OSD to standard OSD format for compatibility
     */
    public OsdDock toStandardOsdDock() {
        OsdDock standardDock = new OsdDock();
        standardDock.setNetworkState(this.networkState);
        standardDock.setDroneInDock(this.droneInDock);
        standardDock.setDroneChargeState(this.droneChargeState);
        standardDock.setRainfall(this.rainfall);
        standardDock.setWindSpeed(this.windSpeed);
        standardDock.setEnvironmentTemperature(this.environmentTemperature);
        standardDock.setTemperature(this.temperature);
        standardDock.setHumidity(this.humidity);
        standardDock.setLatitude(this.latitude);
        standardDock.setLongitude(this.longitude);
        standardDock.setHeight(this.height);
        standardDock.setAlternateLandPoint(this.alternateLandPoint);
        standardDock.setFirstPowerOn(this.firstPowerOn);
        standardDock.setPositionState(this.positionState);
        standardDock.setStorage(this.storage);
        standardDock.setModeCode(this.modeCode);
        standardDock.setCoverState(this.coverState);
        standardDock.setSupplementLightState(this.supplementLightState);
        standardDock.setEmergencyStopState(this.emergencyStopState);
        standardDock.setAirConditioner(this.airConditioner);
        standardDock.setBatteryStoreMode(this.batteryStoreMode);
        standardDock.setAlarmState(this.alarmState);
        standardDock.setPutterState(this.putterState);
        standardDock.setSubDevice(this.subDevice);
        standardDock.setJobNumber(this.jobNumber);
        standardDock.setAccTime(this.accTime);
        standardDock.setActivationTime(this.activationTime);
        // Convert Dock 3 maintain_status to standard format if available
        if (this.maintainStatus3 != null) {
            // Convert DockMaintainStatus3 to OsdDockMaintainStatus
            OsdDockMaintainStatus standardMaintainStatus = new OsdDockMaintainStatus();
            // Add conversion logic as needed
            standardDock.setMaintainStatus(standardMaintainStatus);
        }
        standardDock.setElectricSupplyVoltage(this.electricSupplyVoltage);
        standardDock.setWorkingVoltage(this.workingVoltage);
        standardDock.setWorkingCurrent(this.workingCurrent);
        standardDock.setBackupBattery(this.backupBattery);
        // Convert Dock 3 drone battery maintenance info to standard format if available
        if (this.droneBatteryMaintenanceInfo3 != null) {
            // Convert DroneBatteryMaintenanceInfo3 to DroneBatteryMaintenanceInfo
            DroneBatteryMaintenanceInfo standardBatteryInfo = new DroneBatteryMaintenanceInfo();
            // Add conversion logic as needed
            standardDock.setDroneBatteryMaintenanceInfo(standardBatteryInfo);
        }
        standardDock.setFlighttaskStepCode(this.flighttaskStepCode);
        standardDock.setFlighttaskPrepareCapacity(this.flighttaskPrepareCapacity);
        standardDock.setMediaFileDetail(this.mediaFileDetail);
        standardDock.setWirelessLink(this.wirelessLink);
        standardDock.setDrcState(this.drcState);
        standardDock.setUserExperienceImprovement(this.userExperienceImprovement);
        return standardDock;
    }

    // Getters and Setters
    public NetworkState getNetworkState() {
        return networkState;
    }

    public OsdDock3 setNetworkState(NetworkState networkState) {
        this.networkState = networkState;
        return this;
    }

    public Boolean getDroneInDock() {
        return droneInDock;
    }

    public OsdDock3 setDroneInDock(Boolean droneInDock) {
        this.droneInDock = droneInDock;
        return this;
    }

    public DroneChargeState getDroneChargeState() {
        return droneChargeState;
    }

    public OsdDock3 setDroneChargeState(DroneChargeState droneChargeState) {
        this.droneChargeState = droneChargeState;
        return this;
    }

    public RainfallEnum getRainfall() {
        return rainfall;
    }

    public OsdDock3 setRainfall(RainfallEnum rainfall) {
        this.rainfall = rainfall;
        return this;
    }

    public Float getWindSpeed() {
        return windSpeed;
    }

    public OsdDock3 setWindSpeed(Float windSpeed) {
        this.windSpeed = windSpeed;
        return this;
    }

    public Float getEnvironmentTemperature() {
        return environmentTemperature;
    }

    public OsdDock3 setEnvironmentTemperature(Float environmentTemperature) {
        this.environmentTemperature = environmentTemperature;
        return this;
    }

    public Float getTemperature() {
        return temperature;
    }

    public OsdDock3 setTemperature(Float temperature) {
        this.temperature = temperature;
        return this;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public OsdDock3 setHumidity(Integer humidity) {
        this.humidity = humidity;
        return this;
    }

    public Float getLatitude() {
        return latitude;
    }

    public OsdDock3 setLatitude(Float latitude) {
        this.latitude = latitude;
        return this;
    }

    public Float getLongitude() {
        return longitude;
    }

    public OsdDock3 setLongitude(Float longitude) {
        this.longitude = longitude;
        return this;
    }

    public Float getHeight() {
        return height;
    }

    public OsdDock3 setHeight(Float height) {
        this.height = height;
        return this;
    }

    public AlternateLandPoint getAlternateLandPoint() {
        return alternateLandPoint;
    }

    public OsdDock3 setAlternateLandPoint(AlternateLandPoint alternateLandPoint) {
        this.alternateLandPoint = alternateLandPoint;
        return this;
    }

    public Long getFirstPowerOn() {
        return firstPowerOn;
    }

    public OsdDock3 setFirstPowerOn(Long firstPowerOn) {
        this.firstPowerOn = firstPowerOn;
        return this;
    }

    public DockPositionState getPositionState() {
        return positionState;
    }

    public OsdDock3 setPositionState(DockPositionState positionState) {
        this.positionState = positionState;
        return this;
    }

    public Storage getStorage() {
        return storage;
    }

    public OsdDock3 setStorage(Storage storage) {
        this.storage = storage;
        return this;
    }

    public DockModeCodeEnum getModeCode() {
        return modeCode;
    }

    public OsdDock3 setModeCode(DockModeCodeEnum modeCode) {
        this.modeCode = modeCode;
        return this;
    }

    public CoverStateEnum getCoverState() {
        return coverState;
    }

    public OsdDock3 setCoverState(CoverStateEnum coverState) {
        this.coverState = coverState;
        return this;
    }

    public Boolean getSupplementLightState() {
        return supplementLightState;
    }

    public OsdDock3 setSupplementLightState(Boolean supplementLightState) {
        this.supplementLightState = supplementLightState;
        return this;
    }

    public Boolean getEmergencyStopState() {
        return emergencyStopState;
    }

    public OsdDock3 setEmergencyStopState(Boolean emergencyStopState) {
        this.emergencyStopState = emergencyStopState;
        return this;
    }

    public AirConditioner getAirConditioner() {
        return airConditioner;
    }

    public OsdDock3 setAirConditioner(AirConditioner airConditioner) {
        this.airConditioner = airConditioner;
        return this;
    }

    public BatteryStoreModeEnum getBatteryStoreMode() {
        return batteryStoreMode;
    }

    public OsdDock3 setBatteryStoreMode(BatteryStoreModeEnum batteryStoreMode) {
        this.batteryStoreMode = batteryStoreMode;
        return this;
    }

    public Boolean getAlarmState() {
        return alarmState;
    }

    public OsdDock3 setAlarmState(Boolean alarmState) {
        this.alarmState = alarmState;
        return this;
    }

    public PutterStateEnum getPutterState() {
        return putterState;
    }

    public OsdDock3 setPutterState(PutterStateEnum putterState) {
        this.putterState = putterState;
        return this;
    }

    public DockSubDevice getSubDevice() {
        return subDevice;
    }

    public OsdDock3 setSubDevice(DockSubDevice subDevice) {
        this.subDevice = subDevice;
        return this;
    }

    public Integer getJobNumber() {
        return jobNumber;
    }

    public OsdDock3 setJobNumber(Integer jobNumber) {
        this.jobNumber = jobNumber;
        return this;
    }

    public Long getAccTime() {
        return accTime;
    }

    public OsdDock3 setAccTime(Long accTime) {
        this.accTime = accTime;
        return this;
    }

    public Long getActivationTime() {
        return activationTime;
    }

    public OsdDock3 setActivationTime(Long activationTime) {
        this.activationTime = activationTime;
        return this;
    }

  
    public Integer getElectricSupplyVoltage() {
        return electricSupplyVoltage;
    }

    public OsdDock3 setElectricSupplyVoltage(Integer electricSupplyVoltage) {
        this.electricSupplyVoltage = electricSupplyVoltage;
        return this;
    }

    public Integer getWorkingVoltage() {
        return workingVoltage;
    }

    public OsdDock3 setWorkingVoltage(Integer workingVoltage) {
        this.workingVoltage = workingVoltage;
        return this;
    }

    public Integer getWorkingCurrent() {
        return workingCurrent;
    }

    public OsdDock3 setWorkingCurrent(Integer workingCurrent) {
        this.workingCurrent = workingCurrent;
        return this;
    }

    public BackupBattery getBackupBattery() {
        return backupBattery;
    }

    public OsdDock3 setBackupBattery(BackupBattery backupBattery) {
        this.backupBattery = backupBattery;
        return this;
    }

    
    public FlighttaskStepCodeEnum getFlighttaskStepCode() {
        return flighttaskStepCode;
    }

    public OsdDock3 setFlighttaskStepCode(FlighttaskStepCodeEnum flighttaskStepCode) {
        this.flighttaskStepCode = flighttaskStepCode;
        return this;
    }

    public Integer getFlighttaskPrepareCapacity() {
        return flighttaskPrepareCapacity;
    }

    public OsdDock3 setFlighttaskPrepareCapacity(Integer flighttaskPrepareCapacity) {
        this.flighttaskPrepareCapacity = flighttaskPrepareCapacity;
        return this;
    }

    public MediaFileDetail getMediaFileDetail() {
        return mediaFileDetail;
    }

    public OsdDock3 setMediaFileDetail(MediaFileDetail mediaFileDetail) {
        this.mediaFileDetail = mediaFileDetail;
        return this;
    }

    public WirelessLink getWirelessLink() {
        return wirelessLink;
    }

    public OsdDock3 setWirelessLink(WirelessLink wirelessLink) {
        this.wirelessLink = wirelessLink;
        return this;
    }

    public DrcStateEnum getDrcState() {
        return drcState;
    }

    public OsdDock3 setDrcState(DrcStateEnum drcState) {
        this.drcState = drcState;
        return this;
    }

    public UserExperienceImprovementEnum getUserExperienceImprovement() {
        return userExperienceImprovement;
    }

    public OsdDock3 setUserExperienceImprovement(UserExperienceImprovementEnum userExperienceImprovement) {
        this.userExperienceImprovement = userExperienceImprovement;
        return this;
    }

    public Integer getDeploymentMode() {
        return deploymentMode;
    }

    public OsdDock3 setDeploymentMode(Integer deploymentMode) {
        this.deploymentMode = deploymentMode;
        return this;
    }

    public RelativeAlternateLandPoint getRelativeAlternateLandPoint() {
        return relativeAlternateLandPoint;
    }

    public OsdDock3 setRelativeAlternateLandPoint(RelativeAlternateLandPoint relativeAlternateLandPoint) {
        this.relativeAlternateLandPoint = relativeAlternateLandPoint;
        return this;
    }

    public SelfConvergeCoordinate getSelfConvergeCoordinate() {
        return selfConvergeCoordinate;
    }

    public OsdDock3 setSelfConvergeCoordinate(SelfConvergeCoordinate selfConvergeCoordinate) {
        this.selfConvergeCoordinate = selfConvergeCoordinate;
        return this;
    }

    public DockMaintainStatus3 getMaintainStatus3() {
        return maintainStatus3;
    }

    public OsdDock3 setMaintainStatus3(DockMaintainStatus3 maintainStatus3) {
        this.maintainStatus3 = maintainStatus3;
        return this;
    }

    public Integer getGimbalHolderState() {
        return gimbalHolderState;
    }

    public OsdDock3 setGimbalHolderState(Integer gimbalHolderState) {
        this.gimbalHolderState = gimbalHolderState;
        return this;
    }

    public DroneBatteryMaintenanceInfo3 getDroneBatteryMaintenanceInfo3() {
        return droneBatteryMaintenanceInfo3;
    }

    public OsdDock3 setDroneBatteryMaintenanceInfo3(DroneBatteryMaintenanceInfo3 droneBatteryMaintenanceInfo3) {
        this.droneBatteryMaintenanceInfo3 = droneBatteryMaintenanceInfo3;
        return this;
    }

    @Override
    public String toString() {
        return "OsdDock3{" +
                "networkState=" + networkState +
                ", droneInDock=" + droneInDock +
                ", droneChargeState=" + droneChargeState +
                ", rainfall=" + rainfall +
                ", windSpeed=" + windSpeed +
                ", environmentTemperature=" + environmentTemperature +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", height=" + height +
                ", alternateLandPoint=" + alternateLandPoint +
                ", firstPowerOn=" + firstPowerOn +
                ", positionState=" + positionState +
                ", storage=" + storage +
                ", modeCode=" + modeCode +
                ", coverState=" + coverState +
                ", supplementLightState=" + supplementLightState +
                ", emergencyStopState=" + emergencyStopState +
                ", airConditioner=" + airConditioner +
                ", batteryStoreMode=" + batteryStoreMode +
                ", alarmState=" + alarmState +
                ", putterState=" + putterState +
                ", subDevice=" + subDevice +
                ", jobNumber=" + jobNumber +
                ", accTime=" + accTime +
                ", activationTime=" + activationTime +
                                ", electricSupplyVoltage=" + electricSupplyVoltage +
                ", workingVoltage=" + workingVoltage +
                ", workingCurrent=" + workingCurrent +
                ", backupBattery=" + backupBattery +
                                ", flighttaskStepCode=" + flighttaskStepCode +
                ", flighttaskPrepareCapacity=" + flighttaskPrepareCapacity +
                ", mediaFileDetail=" + mediaFileDetail +
                ", wirelessLink=" + wirelessLink +
                ", drcState=" + drcState +
                ", userExperienceImprovement=" + userExperienceImprovement +
                '}';
    }
}