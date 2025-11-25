package com.master.meta.controller;

import com.master.meta.constants.PermissionConstants;
import com.master.meta.dto.system.project.ProjectAddMemberRequest;
import com.master.meta.dto.system.request.ProjectUserRequest;
import com.master.meta.dto.system.user.UserExtendDTO;
import com.master.meta.handle.permission.RequiresPermissions;
import com.master.meta.service.OrganizationProjectService;
import com.master.meta.utils.SessionUtils;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "项目接口")
@RequestMapping("/organization/project")
public class OrgProjectController {
    private final OrganizationProjectService organizationProjectService;

    public OrgProjectController(@Qualifier("orgProjectService") OrganizationProjectService organizationProjectService) {
        this.organizationProjectService = organizationProjectService;
    }

    @PostMapping("/user-list")
    @Operation(summary = "系统设置-系统-组织与项目-项目-系统-组织及项目, 获取管理员下拉选项")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public Page<UserExtendDTO> getMemberList(@Validated @RequestBody ProjectUserRequest request) {
        return organizationProjectService.getUserPage(request);
    }

    @PostMapping("/add-members")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_PROJECT_MEMBER_ADD)
    @Operation(summary = "系统设置-组织-项目-添加成员")
    public void addProjectMember(@Validated @RequestBody ProjectAddMemberRequest request) {
        organizationProjectService.orgAddProjectMember(request, SessionUtils.getUserName());
    }
}
