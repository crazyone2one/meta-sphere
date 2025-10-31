package com.master.meta.controller;

import com.master.meta.dto.UserRoleUpdateRequest;
import com.master.meta.dto.permission.PermissionDefinitionItem;
import com.master.meta.dto.permission.PermissionSettingUpdateRequest;
import com.master.meta.entity.UserRole;
import com.master.meta.handle.validation.Created;
import com.master.meta.handle.validation.Updated;
import com.master.meta.service.GlobalUserRoleService;
import com.master.meta.utils.SessionUtils;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户组 控制层。
 *
 * @author 11's papa
 * @since 2025-10-24
 */
@RestController
@Tag(name = "用户组接口")
@RequestMapping("/user/role/global")
public class GlobalUserRoleController {

    private final GlobalUserRoleService globalUserRoleService;

    public GlobalUserRoleController(@Qualifier("globalUserRoleService") GlobalUserRoleService globalUserRoleService) {
        this.globalUserRoleService = globalUserRoleService;
    }

    @PostMapping("save")
    @Operation(description = "系统设置-系统-用户组-添加自定义全局用户组")
    public UserRole save(@RequestBody @Parameter(description = "用户组") @Validated({Created.class}) UserRoleUpdateRequest request) {
        UserRole userRole = new UserRole();
        userRole.setCreateUser(SessionUtils.getCurrentUserId());
        BeanUtils.copyProperties(request, userRole);
        return globalUserRoleService.add(userRole);
    }

    @GetMapping("remove/{id}")
    @Operation(description = "系统设置-系统-用户组-删除自定义全局用户组")
    public void remove(@PathVariable @Parameter(description = "用户组主键") String id) {
        globalUserRoleService.delete(id);
    }

    @PostMapping("update")
    @Operation(description = "系统设置-系统-用户组-更新自定义全局用户组")
    public UserRole update(@RequestBody @Parameter(description = "用户组主键") @Validated({Updated.class}) UserRoleUpdateRequest request) {
        UserRole userRole = new UserRole();
        BeanUtils.copyProperties(request, userRole);
        return globalUserRoleService.update(userRole);
    }

    /**
     * 查询所有用户组。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    @Operation(description = "查询所有用户组")
    public List<UserRole> list() {
        return globalUserRoleService.list();
    }

    /**
     * 根据主键获取用户组。
     *
     * @param id 用户组主键
     * @return 用户组详情
     */
    @GetMapping("getInfo/{id}")
    @Operation(description = "根据主键获取用户组")
    public UserRole getInfo(@PathVariable @Parameter(description = "用户组主键") String id) {
        return globalUserRoleService.getById(id);
    }

    /**
     * 分页查询用户组。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    @Operation(description = "分页查询用户组")
    public Page<UserRole> page(@Parameter(description = "分页信息") Page<UserRole> page) {
        return globalUserRoleService.page(page);
    }

    @GetMapping("/permission/setting/{id}")
    @Operation(summary = "系统设置-系统-用户组-获取全局用户组对应的权限配置")
    public List<PermissionDefinitionItem> getPermissionSetting(@PathVariable String id) {
        return globalUserRoleService.getPermissionSetting(id);
    }

    @PostMapping("/permission/update")
    @Operation(summary = "系统设置-系统-用户组-编辑全局用户组对应的权限配置")
    public void updatePermissionSetting(@Validated @RequestBody PermissionSettingUpdateRequest request) {
        globalUserRoleService.updatePermissionSetting(request);
    }
}
