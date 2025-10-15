package com.master.meta.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Created by 11's papa on 2025/10/15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ScheduleRequest extends BasePageRequest {
    private String resourceType;
}
