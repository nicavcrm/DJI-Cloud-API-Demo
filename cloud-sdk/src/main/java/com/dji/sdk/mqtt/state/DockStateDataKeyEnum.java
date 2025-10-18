package com.dji.sdk.mqtt.state;

import com.dji.sdk.cloudapi.device.*;
import com.dji.sdk.cloudapi.livestream.DockLivestreamAbilityUpdate;
import com.dji.sdk.cloudapi.property.DockDroneCommanderFlightHeight;
import com.dji.sdk.cloudapi.property.DockDroneCommanderModeLostAction;
import com.dji.sdk.cloudapi.property.DockDroneOfflineMapEnable;
import com.dji.sdk.cloudapi.property.DockDroneRthMode;
import com.dji.sdk.exception.CloudSDKException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 *
 * @author sean.zhou
 * @date 2021/11/18
 * @version 0.1
 */
public enum DockStateDataKeyEnum {

    FIRMWARE_VERSION(Set.of("firmware_version"), DockFirmwareVersion.class),

    LIVE_CAPACITY(Set.of("live_capacity"), DockLivestreamAbilityUpdate.class),

    CONTROL_SOURCE(Set.of("control_source"), DockDroneControlSource.class),

    LIVE_STATUS(Set.of("live_status"), DockLiveStatus.class),

    WPMZ_VERSION(Set.of("wpmz_version"), DockDroneWpmzVersion.class),

    THERMAL_SUPPORTED_PALETTE_STYLE(PayloadModelConst.getAllIndexWithPosition(), DockDroneThermalSupportedPaletteStyle.class),

    RTH_MODE(Set.of("rth_mode"), DockDroneRthMode.class),

    CURRENT_RTH_MODE(Set.of("current_rth_mode"), DockDroneCurrentRthMode.class),

    COMMANDER_MODE_LOST_ACTION(Set.of("commander_mode_lost_action"), DockDroneCommanderModeLostAction.class),

    CURRENT_COMMANDER_FLIGHT_MODE(Set.of("current_commander_flight_mode"), DockDroneCurrentCommanderFlightMode.class),

    COMMANDER_FLIGHT_HEIGHT(Set.of("commander_flight_height"), DockDroneCommanderFlightHeight.class),

    MODE_CODE_REASON(Set.of("mode_code_reason"), DockDroneModeCodeReason.class),

    OFFLINE_MAP_ENABLE(Set.of("offline_map_enable"), DockDroneOfflineMapEnable.class),

    DONGLE_INFOS(Set.of("dongle_infos"), DongleInfos.class),

    SILENT_MODE(Set.of("silent_mode"), DockSilentMode.class),

    UOM_REAL_NAME_STATE(Set.of("uom_real_name_state"), UomRealNameState.class),

    AI_SPOTLIGHT_ZOOM_ON(Set.of("ai_spotlight_zoom_on"), AiSpotlightZoomOn.class),

    AI_SPOTLIGHT_ZOOM_STATE(Set.of("ai_spotlight_zoom_state"), AiSpotlightZoomState.class),

    AI_IDENTIFY_ON(Set.of("ai_identify_on"), AiIdentifyOn.class),

    FLYSAFE_DATABASE_VERSION(Set.of("flysafe_database_version"), FlysafeDatabaseVersion.class),

    WIRELESS_LINK_TOPO(Set.of("wireless_link_topo"), WirelessLinkTopo.class),

    COMMANDER_FLIGHT_MODE(Set.of("commander_flight_mode"), CommanderFlightMode.class),

    PSDK_UI_RESOURCE(Set.of("psdk_ui_resource"), PsdkUiResource.class),

    PSDK_WIDGET_VALUES(Set.of("psdk_widget_values"), PsdkWidgetValues.class),

    GEO_CAGING_STATUS(Set.of("geo_caging_status"), GeoCagingStatus.class),

    ;

    private final Set<String> keys;

    private final Class classType;


    DockStateDataKeyEnum(Set<String> keys, Class classType) {
        this.keys = keys;
        this.classType = classType;
    }

    public Class getClassType() {
        return classType;
    }

    public Set<String> getKeys() {
        return keys;
    }

    public static DockStateDataKeyEnum find(Set<String> keys) {
        return Arrays.stream(values()).filter(keyEnum -> !Collections.disjoint(keys, keyEnum.keys)).findAny()
                .orElseThrow(() -> new CloudSDKException(DockStateDataKeyEnum.class, keys));
    }

}
