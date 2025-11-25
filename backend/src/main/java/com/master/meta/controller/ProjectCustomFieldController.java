package com.master.meta.controller;

import com.master.meta.constants.PermissionConstants;
import com.master.meta.dto.system.CustomFieldDTO;
import com.master.meta.dto.system.request.CustomFieldUpdateRequest;
import com.master.meta.entity.CustomField;
import com.master.meta.handle.log.annotation.Log;
import com.master.meta.handle.log.constants.OperationLogType;
import com.master.meta.handle.permission.RequiresPermissions;
import com.master.meta.handle.validation.Created;
import com.master.meta.handle.validation.Updated;
import com.master.meta.service.ProjectCustomFieldService;
import com.master.meta.service.log.ProjectCustomFieldLogService;
import com.master.meta.utils.SessionUtils;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 自定义字段 控制层。
 *
 * @author 11's papa
 * @since 2025-11-19
 */
@RestController
@Tag(name = "自定义字段接口")
@RequestMapping("/project/custom/field")
public class ProjectCustomFieldController {

    private final ProjectCustomFieldService projectCustomFieldService;

    public ProjectCustomFieldController(@Qualifier("projectCustomFieldService") ProjectCustomFieldService projectCustomFieldService) {
        this.projectCustomFieldService = projectCustomFieldService;
    }

    /**
     * 保存自定义字段。
     *
     * @param request 自定义字段
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    @Operation(description = "保存自定义字段")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = ProjectCustomFieldLogService.class)
    public CustomField save(@Parameter(description = "自定义字段") @Validated({Created.class}) @RequestBody CustomFieldUpdateRequest request) {
        CustomField customField = new CustomField();
        BeanUtils.copyProperties(request, customField);
        customField.setCreateUser(SessionUtils.getUserName());
        return projectCustomFieldService.add(customField, request.getOptions());
    }

    /**
     * 根据主键删除自定义字段。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    @Operation(description = "根据主键删除自定义字段")
    public boolean remove(@PathVariable @Parameter(description = "自定义字段主键") String id) {
        return projectCustomFieldService.removeById(id);
    }

    @PostMapping("update")
    @Operation(description = "根据主键更新自定义字段")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = ProjectCustomFieldLogService.class)
    public CustomField update(@Parameter(description = "自定义字段主键") @Validated({Updated.class}) @RequestBody CustomFieldUpdateRequest request) {
        CustomField customField = new CustomField();
        BeanUtils.copyProperties(request, customField);
        return projectCustomFieldService.update(customField, request.getOptions());
    }

    /**
     * 查询所有自定义字段。
     *
     * @return 所有数据
     */
    @GetMapping("/list/{projectId}/{scene}")
    @Operation(description = "获取自定义字段列表")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ)
    public List<CustomFieldDTO> list(@Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
                                      @PathVariable String projectId,
                                     @Schema(description = "模板的使用场景（FUNCTIONAL,BUG,API,UI,TEST_PLAN）", requiredMode = Schema.RequiredMode.REQUIRED)
                                      @PathVariable String scene) {
        return projectCustomFieldService.list(projectId, scene);
    }

    /**
     * 根据主键获取自定义字段。
     *
     * @param id 自定义字段主键
     * @return 自定义字段详情
     */
    @GetMapping("getInfo/{id}")
    @Operation(description = "根据主键获取自定义字段")
    public CustomField getInfo(@PathVariable @Parameter(description = "自定义字段主键") String id) {
        return projectCustomFieldService.getById(id);
    }

    /**
     * 分页查询自定义字段。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    @Operation(description = "分页查询自定义字段")
    public Page<CustomField> page(@Parameter(description = "分页信息") Page<CustomField> page) {
        return projectCustomFieldService.page(page);
    }

}
