package com.master.meta.entity;

import com.master.meta.handle.validation.Created;
import com.master.meta.handle.validation.Updated;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 变更记录 实体类。
 *
 * @author 11's papa
 * @since 2025-11-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "变更记录")
@Table("operation_history")
public class OperationHistory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operation_history.id.not_blank}", groups = {Updated.class})
    private String id;

    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operation_history.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{operation_history.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    /**
     * 操作时间
     */
    @Column(onInsertValue = "now()")
    @Schema(description = "操作时间")
    private LocalDateTime createTime;

    /**
     * 操作人
     */
    @Schema(description = "操作人")
    private String createUser;

    /**
     * 资源id
     */
    @Schema(description = "资源id")
    private String sourceId;

    @Schema(description = "操作类型/add/update/delete", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{operation_history.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{operation_history.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    @Schema(description = "")
    private String module;

    /**
     * 关联id（关联变更记录id来源）
     */
    @Schema(description = "关联id（关联变更记录id来源）")
    private String refId;

}
