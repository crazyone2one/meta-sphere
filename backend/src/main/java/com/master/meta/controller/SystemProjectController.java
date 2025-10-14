package com.master.meta.controller;

import com.master.meta.dto.ProjectSwitchRequest;
import com.master.meta.entity.SystemProject;
import com.master.meta.handle.result.ResultHolder;
import com.master.meta.service.SystemProjectService;
import com.master.meta.utils.SessionUtils;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@RequestMapping("/system-project")
public class SystemProjectController {

    private final SystemProjectService systemProjectService;

    /**
     * 保存项目。
     *
     * @param systemProject 项目
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    @Operation(description = "保存项目")
    public int save(@RequestBody @Parameter(description = "项目") SystemProject systemProject) {
        return systemProjectService.add(systemProject, SessionUtils.getUserName());
    }

    /**
     * 根据主键删除项目。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    @Operation(description = "根据主键删除项目")
    public boolean remove(@PathVariable @Parameter(description = "项目主键") String id) {
        return systemProjectService.removeById(id);
    }

    /**
     * 根据主键更新项目。
     *
     * @param systemProject 项目
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    @Operation(description = "根据主键更新项目")
    public boolean update(@RequestBody @Parameter(description = "项目主键") SystemProject systemProject) {
        return systemProjectService.updateById(systemProject);
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
    public SystemProject getInfo(@PathVariable @Parameter(description = "项目主键") String id) {
        return systemProjectService.getById(id);
    }

    /**
     * 分页查询项目。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    @Operation(description = "分页查询项目")
    public Page<SystemProject> page(@Parameter(description = "分页信息") Page<SystemProject> page) {
        return systemProjectService.page(page);
    }

    @PostMapping("/switch")
    @Operation(summary = "切换项目")
    public ResultHolder switchProject(@RequestBody ProjectSwitchRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userInfo", systemProjectService.switchProject(request, SessionUtils.getCurrentUserId()));
        return ResultHolder.success(result);
    }
}
