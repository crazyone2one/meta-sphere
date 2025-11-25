package com.master.meta.controller;

import com.mybatisflex.core.paginate.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import com.master.meta.entity.OrganizationParameter;
import com.master.meta.service.OrganizationParameterService;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;

/**
 * 组织参数 控制层。
 *
 * @author 11's papa
 * @since 2025-11-21
 */
@RestController
@Tag(name = "组织参数接口")
@RequestMapping("/organizationParameter")
public class OrganizationParameterController {

    @Autowired
    private OrganizationParameterService organizationParameterService;

    /**
     * 保存组织参数。
     *
     * @param organizationParameter 组织参数
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    @Operation(description="保存组织参数")
    public boolean save(@RequestBody @Parameter(description="组织参数")OrganizationParameter organizationParameter) {
        return organizationParameterService.save(organizationParameter);
    }

    /**
     * 根据主键删除组织参数。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    @Operation(description="根据主键删除组织参数")
    public boolean remove(@PathVariable @Parameter(description="组织参数主键") String id) {
        return organizationParameterService.removeById(id);
    }

    /**
     * 根据主键更新组织参数。
     *
     * @param organizationParameter 组织参数
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    @Operation(description="根据主键更新组织参数")
    public boolean update(@RequestBody @Parameter(description="组织参数主键") OrganizationParameter organizationParameter) {
        return organizationParameterService.updateById(organizationParameter);
    }

    /**
     * 查询所有组织参数。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    @Operation(description="查询所有组织参数")
    public List<OrganizationParameter> list() {
        return organizationParameterService.list();
    }

    /**
     * 根据主键获取组织参数。
     *
     * @param id 组织参数主键
     * @return 组织参数详情
     */
    @GetMapping("getInfo/{id}")
    @Operation(description="根据主键获取组织参数")
    public OrganizationParameter getInfo(@PathVariable @Parameter(description="组织参数主键") String id) {
        return organizationParameterService.getById(id);
    }

    /**
     * 分页查询组织参数。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    @Operation(description="分页查询组织参数")
    public Page<OrganizationParameter> page(@Parameter(description="分页信息") Page<OrganizationParameter> page) {
        return organizationParameterService.page(page);
    }

}
