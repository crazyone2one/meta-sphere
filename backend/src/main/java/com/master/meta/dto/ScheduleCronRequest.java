package com.master.meta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Created by 11's papa on 2025/10/17
 */
@Data
public class ScheduleCronRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "列表id")
    private String id;

    @Schema(description = "cron表达式")
    private String cron;
}
