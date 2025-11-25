package com.master.meta.controller;

import com.master.meta.constants.PermissionConstants;
import com.master.meta.dto.system.TemplateDTO;
import com.master.meta.dto.system.request.TemplateUpdateRequest;
import com.master.meta.entity.Template;
import com.master.meta.handle.log.annotation.Log;
import com.master.meta.handle.log.constants.OperationLogType;
import com.master.meta.handle.permission.RequiresPermissions;
import com.master.meta.handle.validation.Created;
import com.master.meta.handle.validation.Updated;
import com.master.meta.service.ProjectTemplateService;
import com.master.meta.service.log.ProjectTemplateLogService;
import com.master.meta.utils.SessionUtils;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 模版 控制层。
 *
 * @author 11's papa
 * @since 2025-11-20
 */
@RestController
@Tag(name = "项目管理-模版")
@RequestMapping("/project/template")
public class ProjectTemplateController {

    private final ProjectTemplateService projectTemplateService;

    public ProjectTemplateController(@Qualifier("projectTemplateService") ProjectTemplateService projectTemplateService) {
        this.projectTemplateService = projectTemplateService;
    }

    /**
     * 保存模版。
     *
     * @param request 模版
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    @Operation(description = "保存模版")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = ProjectTemplateLogService.class)
    public Template save(@RequestBody @Parameter(description = "模版") @Validated({Created.class}) TemplateUpdateRequest request) {
        return projectTemplateService.add(request, SessionUtils.getUserName());
    }

    @GetMapping("remove/{id}")
    @Operation(description = "根据主键删除模版")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = ProjectTemplateLogService.class)
    public void remove(@PathVariable @Parameter(description = "模版主键") String id) {
        projectTemplateService.delete(id);
    }

    @PostMapping("update")
    @Operation(description = "更新模版")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = ProjectTemplateLogService.class)
    public Template update(@RequestBody @Parameter(description = "模版") @Validated({Updated.class}) TemplateUpdateRequest request) {
        return projectTemplateService.update(request);
    }

    /**
     * 查询所有模版。
     *
     * @return 所有数据
     */
    @GetMapping("/list/{projectId}/{scene}")
    @Operation(description = "获取模版列表")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ)
    public List<Template> list(@Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
                               @PathVariable String projectId,
                               @Schema(description = "模板的使用场景（FUNCTIONAL,BUG,API,UI,TEST_PLAN,SCHEDULE）", requiredMode = Schema.RequiredMode.REQUIRED)
                               @PathVariable String scene) {
        return projectTemplateService.list(projectId, scene);
    }

    /**
     * 根据主键获取模版。
     *
     * @param id 模版主键
     * @return 模版详情
     */
    @GetMapping("getInfo/{id}")
    @Operation(description = "根据主键获取模版")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ)
    public TemplateDTO getInfo(@PathVariable @Parameter(description = "模版主键") String id) {
        return projectTemplateService.getTemplateDTOWithCheck(id);
    }

    @GetMapping("/set-default/{projectId}/{id}")
    @Operation(summary = "设置默认模板")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.setDefaultTemplateLog(#id)", msClass = ProjectTemplateLogService.class)
    public void setDefaultTemplate(@PathVariable String projectId, @PathVariable String id) {
        projectTemplateService.setDefaultTemplate(projectId, id);
    }

    @GetMapping("/enable/config/{projectId}")
    @Operation(summary = "是否启用组织模版")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ)
    public Map<String, Boolean> getProjectTemplateEnableConfig(@PathVariable String projectId) {
        return projectTemplateService.getProjectTemplateEnableConfig(projectId);
    }

    /**
     * 分页查询模版。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    @Operation(description = "分页查询模版")
    public Page<Template> page(@Parameter(description = "分页信息") Page<Template> page) {
        return projectTemplateService.page(page);
    }

}
