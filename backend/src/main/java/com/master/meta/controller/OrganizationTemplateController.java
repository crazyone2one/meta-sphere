package com.master.meta.controller;

import com.master.meta.constants.PermissionConstants;
import com.master.meta.dto.system.request.TemplateUpdateRequest;
import com.master.meta.entity.Template;
import com.master.meta.handle.log.annotation.Log;
import com.master.meta.handle.log.constants.OperationLogType;
import com.master.meta.handle.permission.RequiresPermissions;
import com.master.meta.handle.validation.Created;
import com.master.meta.handle.validation.Updated;
import com.master.meta.service.OrganizationTemplateService;
import com.master.meta.service.log.OrganizationTemplateLogService;
import com.master.meta.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/organization/template")
@Tag(name = "系统设置-组织-模版")
public class OrganizationTemplateController {
    private final OrganizationTemplateService organizationTemplateService;

    public OrganizationTemplateController(@Qualifier("organizationTemplateService") OrganizationTemplateService organizationTemplateService) {
        this.organizationTemplateService = organizationTemplateService;
    }

    @PostMapping("save")
    @Operation(description = "保存模版")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = OrganizationTemplateLogService.class)
    public Template save(@RequestBody @Parameter(description = "模版") @Validated({Created.class}) TemplateUpdateRequest request) {
        return organizationTemplateService.add(request, SessionUtils.getUserName());
    }

    @PostMapping("/update")
    @Operation(summary = "更新模版")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.ADD, expression = "#msClass.updateLog(#request)", msClass = OrganizationTemplateLogService.class)
    public Template update(@Validated({Updated.class}) @RequestBody TemplateUpdateRequest request) {
        return organizationTemplateService.update(request);
    }

    @GetMapping("/enable/config/{organizationId}")
    @Operation(summary = "是否启用组织模版")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_READ)
    public Map<String, Boolean> getOrganizationTemplateEnableConfig(@PathVariable String organizationId) {
        return organizationTemplateService.getOrganizationTemplateEnableConfig(organizationId);
    }

    @GetMapping("/disable/{organizationId}/{scene}")
    @Operation(summary = "关闭组织模板，开启项目模板")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_ENABLE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.disableOrganizationTemplateLog(#organizationId,#scene)", msClass = OrganizationTemplateLogService.class)
    public void disableOrganizationTemplate(@PathVariable String organizationId, @PathVariable String scene) {
        organizationTemplateService.disableOrganizationTemplate(organizationId, scene);
    }
}
