package com.master.meta.controller;

import com.master.meta.dto.system.GlobalUserRoleRelationQueryRequest;
import com.master.meta.dto.system.UserExcludeOptionDTO;
import com.master.meta.dto.system.UserRoleRelationUpdateRequest;
import com.master.meta.dto.system.UserRoleRelationUserDTO;
import com.master.meta.entity.UserRoleRelation;
import com.master.meta.handle.validation.Created;
import com.master.meta.service.GlobalUserRoleRelationService;
import com.master.meta.utils.SessionUtils;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户组关系 控制层。
 *
 * @author 11's papa
 * @since 2025-10-24
 */
@RestController
@Tag(name = "用户组关系接口")
@RequestMapping("/user/role/relation/global")
public class UserRoleRelationController {

    private final GlobalUserRoleRelationService globalUserRoleRelationService;

    public UserRoleRelationController(@Qualifier("globalUserRoleRelationService") GlobalUserRoleRelationService globalUserRoleRelationService) {
        this.globalUserRoleRelationService = globalUserRoleRelationService;
    }

    /**
     * 保存用户组关系。
     *
     * @param request 用户组关系
     */
    @PostMapping("save")
    @Operation(description = "保存用户组关系")
    public void save(@RequestBody @Parameter(description = "用户组关系") @Validated({Created.class}) UserRoleRelationUpdateRequest request) {
        request.setCreateUser(SessionUtils.getCurrentUserId());
        globalUserRoleRelationService.add(request);
    }

    /**
     * 根据主键删除用户组关系。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    @Operation(description = "根据主键删除用户组关系")
    public boolean remove(@PathVariable @Parameter(description = "用户组关系主键") String id) {
        return globalUserRoleRelationService.removeById(id);
    }

    /**
     * 根据主键更新用户组关系。
     *
     * @param userRoleRelation 用户组关系
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    @Operation(description = "根据主键更新用户组关系")
    public boolean update(@RequestBody @Parameter(description = "用户组关系主键") UserRoleRelation userRoleRelation) {
        return globalUserRoleRelationService.updateById(userRoleRelation);
    }

    /**
     * 查询所有用户组关系。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    @Operation(description = "查询所有用户组关系")
    public List<UserRoleRelation> list() {
        return globalUserRoleRelationService.list();
    }

    /**
     * 根据主键获取用户组关系。
     *
     * @param id 用户组关系主键
     * @return 用户组关系详情
     */
    @GetMapping("getInfo/{id}")
    @Operation(description = "根据主键获取用户组关系")
    public UserRoleRelation getInfo(@PathVariable @Parameter(description = "用户组关系主键") String id) {
        return globalUserRoleRelationService.getById(id);
    }

    /**
     * 分页查询用户组关系。
     *
     * @param request 分页对象
     * @return 分页对象
     */
    @PostMapping("page")
    @Operation(description = "系统设置-系统-用户组-用户关联关系-获取全局用户组对应的用户列表")
    public Page<UserRoleRelationUserDTO> page(@Validated @RequestBody GlobalUserRoleRelationQueryRequest request) {
        return globalUserRoleRelationService.page(request);
    }

    @GetMapping("/user/option/{roleCode}")
    @Operation(summary = "系统设置-系统-用户组-用户关联关系-获取需要关联的用户选项")
    @PreAuthorize("@rpe.hasPermission('SYSTEM_USER_ROLE:READ')")
    public List<UserExcludeOptionDTO> getSelectOption(@Schema(description = "用户组ID", requiredMode = Schema.RequiredMode.REQUIRED)
                                                      @PathVariable String roleCode,
                                                      @Schema(description = "查询关键字，根据邮箱和用户名查询", requiredMode = Schema.RequiredMode.REQUIRED)
                                                      @RequestParam(value = "keyword", required = false) String keyword) {
        return globalUserRoleRelationService.getExcludeSelectOption(roleCode, keyword);
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "系统设置-系统-用户组-用户关联关系-删除全局用户组和用户的关联关系")
//    @RequiresPermissions(PermissionConstants.SYSTEM_USER_ROLE_UPDATE)
//    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = GlobalUserRoleRelationLogService.class)
    public void delete(@PathVariable String id) {
        globalUserRoleRelationService.delete(id);
    }
}
