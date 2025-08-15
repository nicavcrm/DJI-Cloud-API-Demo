package com.dji.sdk.cloudapi.device;

/**
 * Camera watermark settings data model
 * 
 * @author claude
 * @version 1.0
 * @date 2025-08-15
 */
public class CameraWatermarkSettings {

    private Boolean enabled;
    
    private String text;
    
    private Integer opacity;
    
    private String position;

    public CameraWatermarkSettings() {
    }

    @Override
    public String toString() {
        return "CameraWatermarkSettings{" +
                "enabled=" + enabled +
                ", text='" + text + '\'' +
                ", opacity=" + opacity +
                ", position='" + position + '\'' +
                '}';
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public CameraWatermarkSettings setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getText() {
        return text;
    }

    public CameraWatermarkSettings setText(String text) {
        this.text = text;
        return this;
    }

    public Integer getOpacity() {
        return opacity;
    }

    public CameraWatermarkSettings setOpacity(Integer opacity) {
        this.opacity = opacity;
        return this;
    }

    public String getPosition() {
        return position;
    }

    public CameraWatermarkSettings setPosition(String position) {
        this.position = position;
        return this;
    }
}