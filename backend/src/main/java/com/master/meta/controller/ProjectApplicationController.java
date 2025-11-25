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
import com.master.meta.entity.ProjectApplication;
import com.master.meta.service.ProjectApplicationService;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;

/**
 * 项目应用 控制层。
 *
 * @author 11's papa
 * @since 2025-11-21
 */
@RestController
@Tag(name = "项目应用接口")
@RequestMapping("/projectApplication")
public class ProjectApplicationController {

    @Autowired
    private ProjectApplicationService projectApplicationService;

    /**
     * 保存项目应用。
     *
     * @param projectApplication 项目应用
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    @Operation(description="保存项目应用")
    public boolean save(@RequestBody @Parameter(description="项目应用")ProjectApplication projectApplication) {
        return projectApplicationService.save(projectApplication);
    }

    /**
     * 根据主键删除项目应用。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    @Operation(description="根据主键删除项目应用")
    public boolean remove(@PathVariable @Parameter(description="项目应用主键") String id) {
        return projectApplicationService.removeById(id);
    }

    /**
     * 根据主键更新项目应用。
     *
     * @param projectApplication 项目应用
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    @Operation(description="根据主键更新项目应用")
    public boolean update(@RequestBody @Parameter(description="项目应用主键") ProjectApplication projectApplication) {
        return projectApplicationService.updateById(projectApplication);
    }

    /**
     * 查询所有项目应用。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    @Operation(description="查询所有项目应用")
    public List<ProjectApplication> list() {
        return projectApplicationService.list();
    }

    /**
     * 根据主键获取项目应用。
     *
     * @param id 项目应用主键
     * @return 项目应用详情
     */
    @GetMapping("getInfo/{id}")
    @Operation(description="根据主键获取项目应用")
    public ProjectApplication getInfo(@PathVariable @Parameter(description="项目应用主键") String id) {
        return projectApplicationService.getById(id);
    }

    /**
     * 分页查询项目应用。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    @Operation(description="分页查询项目应用")
    public Page<ProjectApplication> page(@Parameter(description="分页信息") Page<ProjectApplication> page) {
        return projectApplicationService.page(page);
    }

}
