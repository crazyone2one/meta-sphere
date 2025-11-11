package com.master.meta.controller;

import com.master.meta.constants.PermissionConstants;
import com.master.meta.constants.UserSource;
import com.master.meta.dto.BasePageRequest;
import com.master.meta.dto.TableBatchProcessDTO;
import com.master.meta.dto.system.*;
import com.master.meta.entity.SystemUser;
import com.master.meta.handle.annotation.RequiresPermissions;
import com.master.meta.handle.validation.Updated;
import com.master.meta.service.GlobalUserRoleService;
import com.master.meta.service.SystemUserService;
import com.master.meta.utils.SessionUtils;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户 控制层。
 *
 * @author 11's papa
 * @since 2025-10-11
 */
@RestController
@Tag(name = "用户接口")
@RequestMapping("/system-user")
public class SystemUserController {

    private final SystemUserService systemUserService;
    private final GlobalUserRoleService globalUserRoleService;

    public SystemUserController(SystemUserService systemUserService,
                                @Qualifier("globalUserRoleService") GlobalUserRoleService globalUserRoleService) {
        this.systemUserService = systemUserService;
        this.globalUserRoleService = globalUserRoleService;
    }

    @PostMapping("save")
    @Operation(description = "添加用户")
    @PreAuthorize("@rpe.hasPermission('SYSTEM_USER:READ+ADD')")
    public UserBatchCreateResponse save(@RequestBody @Parameter(description = "用户") @Validated UserBatchCreateRequest request) {
        return systemUserService.addUser(request, UserSource.LOCAL.name(), SessionUtils.getUserName());
    }

    /**
     * 根据主键删除用户。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    @Operation(description = "根据主键删除用户")
    @PreAuthorize("@rpe.hasPermission('SYSTEM_USER:READ+DELETE')")
    public int remove(@PathVariable @Parameter(description = "用户主键") String id) {
        return systemUserService.deleteUserById(id);
    }

    @PostMapping("/delete")
    @Operation(summary = "系统设置-系统-用户-删除用户")
//    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#request)", msClass = UserLogService.class)
//    @RequiresPermissions(PermissionConstants.SYSTEM_USER_DELETE)
    public TableBatchProcessResponse deleteUser(@Validated @RequestBody TableBatchProcessDTO request) {
        return systemUserService.deleteUser(request, SessionUtils.getCurrentUserId(), SessionUtils.getUserName());
    }

    @PostMapping("update")
    @Operation(description = "修改用户")
    @PreAuthorize("@rpe.hasPermission('SYSTEM_USER:READ+UPDATE')")
    public UserEditRequest update(@RequestBody @Parameter(description = "用户主键") @Validated({Updated.class}) UserEditRequest request) {
        return systemUserService.updateUser(request);
    }

    @PostMapping("/update/enable")
    @Operation(summary = "系统设置-系统-用户-启用/禁用用户")
    public TableBatchProcessResponse updateUserEnable(@Validated @RequestBody UserChangeEnableRequest request) {
        return systemUserService.updateUserEnable(request, SessionUtils.getUserName());
    }

    /**
     * 查询所有用户。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    @Operation(description = "查询所有用户")
    public List<SystemUser> list() {
        return systemUserService.list();
    }

    @GetMapping("getInfo/{keyword}")
    @Operation(description = "通过email或id查找用户")
    @PreAuthorize("@rpe.hasPermission('SYSTEM_USER:READ')")
    public UserDTO getInfo(@PathVariable @Parameter(description = "用户主键") String keyword) {
        return systemUserService.getUserDTOByKeyword(keyword);
    }

    @GetMapping("get-user-info")
    @Operation(description = "获取用户信息")
    public UserDTO getInfo() {
        return systemUserService.getUserInfo();
    }

    @PostMapping("page")
    @Operation(description = "分页查询用户")
    @PreAuthorize("@rpe.hasPermission('SYSTEM_USER:READ')")
    public Page<UserTableResponse> page(@Validated @RequestBody BasePageRequest request) {
        return systemUserService.pageUserTable(request);
    }

    @GetMapping("/get/global/system/role")
    @Operation(summary = "系统设置-系统-用户-查找系统级用户组")
    @PreAuthorize("@rpe.hasPermission('SYSTEM_USER:READ')")
    public List<UserSelectOption> getGlobalSystemRole() {
        return globalUserRoleService.getGlobalSystemRoleList();
    }
    @PostMapping("/reset/password")
    @Operation(summary = "系统设置-系统-用户-重置用户密码")
//    @RequiresPermissions(PermissionConstants.SYSTEM_USER_UPDATE)
//    @Log(type = OperationLogType.UPDATE, expression = "#msClass.resetPasswordLog(#request)", msClass = UserLogService.class)
    public TableBatchProcessResponse resetPassword(@Validated @RequestBody TableBatchProcessDTO request) {
        return systemUserService.resetPassword(request, SessionUtils.getCurrentUserId());
    }

}
