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
import com.master.meta.entity.SystemScheduleConfig;
import com.master.meta.service.SystemScheduleConfigService;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;

/**
 * 定时任务配置信息 控制层。
 *
 * @author 11's papa
 * @since 2025-11-18
 */
@RestController
@Tag(name = "定时任务配置信息接口")
@RequestMapping("/systemScheduleConfig")
public class SystemScheduleConfigController {

    @Autowired
    private SystemScheduleConfigService systemScheduleConfigService;

    /**
     * 保存定时任务配置信息。
     *
     * @param systemScheduleConfig 定时任务配置信息
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    @Operation(description="保存定时任务配置信息")
    public boolean save(@RequestBody @Parameter(description="定时任务配置信息")SystemScheduleConfig systemScheduleConfig) {
        return systemScheduleConfigService.save(systemScheduleConfig);
    }

    /**
     * 根据主键删除定时任务配置信息。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    @Operation(description="根据主键删除定时任务配置信息")
    public boolean remove(@PathVariable @Parameter(description="定时任务配置信息主键") String id) {
        return systemScheduleConfigService.removeById(id);
    }

    /**
     * 根据主键更新定时任务配置信息。
     *
     * @param systemScheduleConfig 定时任务配置信息
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    @Operation(description="根据主键更新定时任务配置信息")
    public boolean update(@RequestBody @Parameter(description="定时任务配置信息主键") SystemScheduleConfig systemScheduleConfig) {
        return systemScheduleConfigService.updateById(systemScheduleConfig);
    }

    /**
     * 查询所有定时任务配置信息。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    @Operation(description="查询所有定时任务配置信息")
    public List<SystemScheduleConfig> list() {
        return systemScheduleConfigService.list();
    }

    /**
     * 根据主键获取定时任务配置信息。
     *
     * @param id 定时任务配置信息主键
     * @return 定时任务配置信息详情
     */
    @GetMapping("getInfo/{id}")
    @Operation(description="根据主键获取定时任务配置信息")
    public SystemScheduleConfig getInfo(@PathVariable @Parameter(description="定时任务配置信息主键") String id) {
        return systemScheduleConfigService.getById(id);
    }

    /**
     * 分页查询定时任务配置信息。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    @Operation(description="分页查询定时任务配置信息")
    public Page<SystemScheduleConfig> page(@Parameter(description="分页信息") Page<SystemScheduleConfig> page) {
        return systemScheduleConfigService.page(page);
    }

}
