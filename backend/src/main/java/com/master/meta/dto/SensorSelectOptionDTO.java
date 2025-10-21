package com.master.meta.dto;

/**
 * @author Created by 11's papa on 2025/10/21
 */
public record SensorSelectOptionDTO(String label,
                                    String value,
                                    String sensorCode,
                                    String sensorLocation,
                                    String sensorType,
                                    String sensorValueType,
                                    String sensorValueUnit,
                                    Boolean disabled) {
}
