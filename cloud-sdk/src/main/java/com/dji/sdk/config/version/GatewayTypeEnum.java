package com.dji.sdk.config.version;

import java.util.Arrays;

import com.dji.sdk.cloudapi.device.DeviceEnum;
import com.dji.sdk.exception.CloudSDKException;

/**
 * @author sean
 * @version 1.7
 * @date 2023/5/19
 */
public enum GatewayTypeEnum {

    RC(DeviceEnum.RC, DeviceEnum.RC_PLUS, DeviceEnum.RC_PRO, DeviceEnum.RC_PRO_TWO),

    DOCK(DeviceEnum.DOCK),

    DOCK2(DeviceEnum.DOCK2),

    DOCK3(DeviceEnum.DOCK3),
    ;

    private final DeviceEnum[] gateway;

    GatewayTypeEnum(DeviceEnum... gateway) {
        this.gateway = gateway;
    }

    public DeviceEnum[] getGateway() {
        return gateway;
    }

    public static GatewayTypeEnum find(DeviceEnum device) {
        return Arrays.stream(values()).filter(gateway -> Arrays.stream(gateway.gateway).anyMatch(deviceEnum -> device == deviceEnum))
                .findAny().orElseThrow(() -> new CloudSDKException(GatewayTypeEnum.class, device));
    }
}
