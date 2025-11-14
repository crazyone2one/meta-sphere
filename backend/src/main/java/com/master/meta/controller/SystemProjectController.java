package com.master.meta.controller;

import com.master.meta.constants.PermissionConstants;
import com.master.meta.dto.ProjectSwitchRequest;
import com.master.meta.dto.system.project.*;
import com.master.meta.dto.system.user.UserExtendDTO;
import com.master.meta.entity.SystemProject;
import com.master.meta.entity.SystemUser;
import com.master.meta.handle.log.annotation.Log;
import com.master.meta.handle.log.constants.OperationLogType;
import com.master.meta.handle.permission.RequiresPermissions;
import com.master.meta.handle.result.ResultHolder;
import com.master.meta.handle.validation.Created;
import com.master.meta.handle.validation.Updated;
import com.master.meta.service.SystemProjectService;
import com.master.meta.service.log.SystemProjectLogService;
import com.master.meta.utils.SessionUtils;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目 控制层。
 *
 * @author 11's papa
 * @since 2025-10-13
 */
@RestController
@Tag(name = "项目接口")
@RequestMapping("/system-project")
public class SystemProjectController {

    private final SystemProjectService systemProjectService;

    public SystemProjectController(@Qualifier("systemProjectService") SystemProjectService systemProjectService) {
        this.systemProjectService = systemProjectService;
    }

    /**
     * 保存项目。
     *
     * @param request 项目
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_ADD)
    @Operation(summary = "系统设置-系统-组织与项目-项目-创建项目")
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = SystemProjectLogService.class)
    public ProjectDTO save(@RequestBody @Validated({Created.class}) AddProjectRequest request) {
        return systemProjectService.add(request, SessionUtils.getUserName());
    }

    /**
     * 根据主键删除项目。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @GetMapping("remove/{id}")
    @Operation(description = "系统设置-系统-组织与项目-项目-删除")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = SystemProjectLogService.class)
    public int remove(@PathVariable @Parameter(description = "项目主键") String id) {
        return systemProjectService.delete(id, SessionUtils.getUserName());
    }

    @PostMapping("update")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = SystemProjectLogService.class)
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE)
    @Operation(summary = "系统设置-系统-组织与项目-项目-编辑")
    public boolean update(@RequestBody @Validated({Updated.class}) UpdateProjectRequest request) {
        return systemProjectService.update(request, SessionUtils.getUserName());
    }

    /**
     * 查询所有项目。
     *
     * @return 所有数据
     */
    @GetMapping("/list/options/{organizationId}")
    @Operation(description = "查询所有项目")
    public List<SystemProject> list(@PathVariable String organizationId) {
        return systemProjectService.listProject(organizationId);
    }

    /**
     * 根据主键获取项目。
     *
     * @param id 项目主键
     * @return 项目详情
     */
    @GetMapping("getInfo/{id}")
    @Operation(description = "根据主键获取项目")
    @RequiresPermissions(value = {PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ})
    public ProjectDTO getInfo(@PathVariable @NotBlank @Parameter(description = "项目主键") String id) {
        return systemProjectService.get(id);
    }

    @PostMapping("page")
    @Operation(description = "系统设置-系统-组织与项目-项目-获取项目列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public Page<ProjectDTO> page(@Validated @RequestBody ProjectRequest request) {
        return systemProjectService.getProjectPage(request);
    }

    @PostMapping("/switch")
    @Operation(summary = "切换项目")
    public ResultHolder switchProject(@RequestBody ProjectSwitchRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userInfo", systemProjectService.switchProject(request, SessionUtils.getCurrentUserId()));
        return ResultHolder.success(result);
    }

    @GetMapping("/enable/{id}")
    @Operation(summary = "系统设置-系统-组织与项目-项目-启用")
    @Parameter(name = "id", description = "项目ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#id)", msClass = SystemProjectLogService.class)
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE)
    public void enable(@PathVariable String id) {
        systemProjectService.enable(id, SessionUtils.getUserName());
    }

    @GetMapping("/disable/{id}")
    @Operation(summary = "系统设置-系统-组织与项目-项目-禁用")
    @Parameter(name = "id", description = "项目ID", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#id)", msClass = SystemProjectLogService.class)
    public void disable(@PathVariable String id) {
        systemProjectService.disable(id, SessionUtils.getUserName());
    }

    @PostMapping("/member-list")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    @Operation(summary = "系统设置-系统-组织与项目-项目-成员列表")
    public Page<UserExtendDTO> getProjectMember(@Validated @RequestBody ProjectMemberRequest request) {
        return systemProjectService.getProjectMember(request);
    }

    @PostMapping("/add-member")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_MEMBER_ADD)
    @Operation(summary = "系统设置-系统-组织与项目-项目-添加成员")
    public void addProjectMember(@Validated @RequestBody ProjectAddMemberRequest request) {
        systemProjectService.addMemberByProject(request, SessionUtils.getUserName());
    }

    @GetMapping("/remove-member/{projectId}/{userId}")
    @Operation(summary = "系统设置-系统-组织与项目-项目-移除成员")
    @Parameter(name = "userId", description = "用户id", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @Parameter(name = "projectId", description = "项目id", schema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED))
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_MEMBER_DELETE)
    public int removeProjectMember(@PathVariable String projectId, @PathVariable String userId) {
        return systemProjectService.removeProjectMember(projectId, userId, SessionUtils.getUserName());
    }

    @GetMapping("/user-list")
    @Operation(summary = "系统设置-系统-组织与项目-项目-系统-组织及项目, 获取管理员下拉选项")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public List<SystemUser> getUserList(@Schema(description = "查询关键字，根据邮箱和用户名查询")
                                        @RequestParam(value = "keyword", required = false) String keyword) {
        return systemProjectService.getUserList(keyword);
    }
}
