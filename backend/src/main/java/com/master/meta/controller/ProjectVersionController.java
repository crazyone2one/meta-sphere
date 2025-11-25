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
import com.master.meta.entity.ProjectVersion;
import com.master.meta.service.ProjectVersionService;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;

/**
 * 版本管理 控制层。
 *
 * @author 11's papa
 * @since 2025-11-21
 */
@RestController
@Tag(name = "版本管理接口")
@RequestMapping("/projectVersion")
public class ProjectVersionController {

    @Autowired
    private ProjectVersionService projectVersionService;

    /**
     * 保存版本管理。
     *
     * @param projectVersion 版本管理
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    @Operation(description="保存版本管理")
    public boolean save(@RequestBody @Parameter(description="版本管理")ProjectVersion projectVersion) {
        return projectVersionService.save(projectVersion);
    }

    /**
     * 根据主键删除版本管理。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    @Operation(description="根据主键删除版本管理")
    public boolean remove(@PathVariable @Parameter(description="版本管理主键") String id) {
        return projectVersionService.removeById(id);
    }

    /**
     * 根据主键更新版本管理。
     *
     * @param projectVersion 版本管理
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    @Operation(description="根据主键更新版本管理")
    public boolean update(@RequestBody @Parameter(description="版本管理主键") ProjectVersion projectVersion) {
        return projectVersionService.updateById(projectVersion);
    }

    /**
     * 查询所有版本管理。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    @Operation(description="查询所有版本管理")
    public List<ProjectVersion> list() {
        return projectVersionService.list();
    }

    /**
     * 根据主键获取版本管理。
     *
     * @param id 版本管理主键
     * @return 版本管理详情
     */
    @GetMapping("getInfo/{id}")
    @Operation(description="根据主键获取版本管理")
    public ProjectVersion getInfo(@PathVariable @Parameter(description="版本管理主键") String id) {
        return projectVersionService.getById(id);
    }

    /**
     * 分页查询版本管理。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    @Operation(description="分页查询版本管理")
    public Page<ProjectVersion> page(@Parameter(description="分页信息") Page<ProjectVersion> page) {
        return projectVersionService.page(page);
    }

}
