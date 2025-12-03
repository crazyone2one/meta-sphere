package com.master.meta.controller;

import com.master.meta.constants.PermissionConstants;
import com.master.meta.handle.log.annotation.Log;
import com.master.meta.handle.log.constants.OperationLogType;
import com.master.meta.handle.permission.RequiresPermissions;
import com.master.meta.service.OrganizationTemplateService;
import com.master.meta.service.log.OrganizationTemplateLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/organization/template")
@Tag(name = "系统设置-组织-模版")
public class OrganizationTemplateController {
    private final OrganizationTemplateService organizationTemplateService;

    public OrganizationTemplateController(@Qualifier("organizationTemplateService") OrganizationTemplateService organizationTemplateService) {
        this.organizationTemplateService = organizationTemplateService;
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
