package com.master.meta.controller;

import com.master.meta.constants.PermissionConstants;
import com.master.meta.dto.system.request.MemberRequest;
import com.master.meta.dto.system.request.OrganizationMemberRequest;
import com.master.meta.dto.system.user.UserExtendDTO;
import com.master.meta.entity.Organization;
import com.master.meta.handle.permission.RequiresPermissions;
import com.master.meta.service.OrganizationService;
import com.master.meta.utils.SessionUtils;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 组织 控制层。
 *
 * @author 11's papa
 * @since 2025-11-05
 */
@RestController
@Tag(name = "组织接口")
@RequestMapping("/system/organization")
public class SystemOrganizationController {

    private final OrganizationService organizationService;

    public SystemOrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    /**
     * 保存组织。
     *
     * @param organization 组织
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    @Operation(description = "保存组织")
    public boolean save(@RequestBody @Parameter(description = "组织") Organization organization) {
        return organizationService.save(organization);
    }

    /**
     * 根据主键删除组织。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    @Operation(description = "根据主键删除组织")
    public boolean remove(@PathVariable @Parameter(description = "组织主键") String id) {
        return organizationService.removeById(id);
    }

    /**
     * 根据主键更新组织。
     *
     * @param organization 组织
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    @Operation(description = "根据主键更新组织")
    public boolean update(@RequestBody @Parameter(description = "组织主键") Organization organization) {
        return organizationService.updateById(organization);
    }

    /**
     * 查询所有组织。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    @Operation(description = "查询所有组织")
    public List<Organization> list() {
        return organizationService.list();
    }

    /**
     * 根据主键获取组织。
     *
     * @param id 组织主键
     * @return 组织详情
     */
    @GetMapping("getInfo/{id}")
    @Operation(description = "根据主键获取组织")
    public Organization getInfo(@PathVariable @Parameter(description = "组织主键") String id) {
        return organizationService.getById(id);
    }

    /**
     * 分页查询组织。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    @Operation(description = "分页查询组织")
    public Page<Organization> page(@Parameter(description = "分页信息") Page<Organization> page) {
        return organizationService.page(page);
    }

    @PostMapping("/member-list")
    @Operation(summary = "系统设置-系统-组织与项目-获取添加成员列表")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public Page<UserExtendDTO> getMemberOptionList(@Validated @RequestBody MemberRequest request) {
        return organizationService.getMemberPage(request);
    }

    @PostMapping("/add-member")
    @Operation(summary = "系统设置-系统-组织与项目-组织-添加组织成员")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_MEMBER_ADD)
    public void addMember(@Validated @RequestBody OrganizationMemberRequest request) {
        organizationService.addMemberBySystem(request, SessionUtils.getUserName());
    }

    @GetMapping("/total")
    @Operation(summary = "系统设置-系统-组织与项目-组织-获取组织和项目总数")
    @RequiresPermissions(PermissionConstants.SYSTEM_ORGANIZATION_PROJECT_READ)
    public Map<String, Long> getTotal(@RequestParam(value = "organizationId", required = false) String organizationId) {
        return organizationService.getTotal(organizationId);
    }
}
