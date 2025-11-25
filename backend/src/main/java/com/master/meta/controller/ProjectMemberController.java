package com.master.meta.controller;

import com.master.meta.constants.Logical;
import com.master.meta.constants.PermissionConstants;
import com.master.meta.dto.system.OptionDTO;
import com.master.meta.handle.permission.RequiresPermissions;
import com.master.meta.service.ProjectMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "项目管理-成员")
@RestController
@RequestMapping("/project/member")
public class ProjectMemberController {
    private final ProjectMemberService projectMemberService;

    public ProjectMemberController(ProjectMemberService projectMemberService) {
        this.projectMemberService = projectMemberService;
    }

    @GetMapping("/get-role/option/{projectId}")
    @Operation(summary = "项目管理-成员-获取用户组下拉选项")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_USER_READ, PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ}, logical = Logical.OR)
    public List<OptionDTO> getRoleOption(@PathVariable String projectId) {
        return projectMemberService.getRoleOption(projectId);
    }
}
