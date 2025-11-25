package com.master.meta.controller;

import com.master.meta.constants.Logical;
import com.master.meta.constants.PermissionConstants;
import com.master.meta.dto.system.OptionDTO;
import com.master.meta.handle.permission.RequiresPermissions;
import com.master.meta.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "系统设置-组织-成员")
@RestController
@RequestMapping("/organization")
public class OrganizationController {
    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping("/user/role/list/{organizationId}")
    @Operation(summary = "系统设置-组织-成员-获取当前组织下的所有自定义用户组以及组织级别的用户组")
    @RequiresPermissions(value = {PermissionConstants.ORGANIZATION_MEMBER_READ, PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ}, logical = Logical.OR)
    public List<OptionDTO> getUserRoleList(@PathVariable(value = "organizationId") String organizationId) {
        return organizationService.getUserRoleList(organizationId);
    }
}
