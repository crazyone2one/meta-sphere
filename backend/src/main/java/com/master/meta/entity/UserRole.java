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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户组 实体类。
 *
 * @author 11's papa
 * @since 2025-10-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户组")
@Table("user_role")
public class UserRole implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 组ID
     */
    @Id
    @Schema(description =  "组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{user_role.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "组名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{user_role.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    /**
     * code
     */
    @Schema(description =  "code", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_role.name.length_range}", groups = {Created.class, Updated.class})
    private String code;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    @Schema(description =  "是否是内置用户组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{user_role.internal.not_blank}", groups = {Created.class})
    private Boolean internal;

    @Schema(description =  "所属类型 SYSTEM ORGANIZATION PROJECT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{user_role.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建人(操作人）
     */
    @Schema(description = "创建人(操作人）")
    private String createUser;

    @Schema(description =  "应用范围", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role.scope_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{user_role.scope_id.length_range}", groups = {Created.class, Updated.class})
    private String scopeId;

}
