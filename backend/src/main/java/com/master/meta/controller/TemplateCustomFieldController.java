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
import com.master.meta.entity.TemplateCustomField;
import com.master.meta.service.BaseTemplateCustomFieldService;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;

/**
 * 模板和字段的关联关系 控制层。
 *
 * @author 11's papa
 * @since 2025-11-20
 */
@RestController
@Tag(name = "模板和字段的关联关系接口")
@RequestMapping("/templateCustomField")
public class TemplateCustomFieldController {

    @Autowired
    private BaseTemplateCustomFieldService baseTemplateCustomFieldService;

    /**
     * 保存模板和字段的关联关系。
     *
     * @param templateCustomField 模板和字段的关联关系
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    @Operation(description="保存模板和字段的关联关系")
    public boolean save(@RequestBody @Parameter(description="模板和字段的关联关系")TemplateCustomField templateCustomField) {
        return baseTemplateCustomFieldService.save(templateCustomField);
    }

    /**
     * 根据主键删除模板和字段的关联关系。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    @Operation(description="根据主键删除模板和字段的关联关系")
    public boolean remove(@PathVariable @Parameter(description="模板和字段的关联关系主键") String id) {
        return baseTemplateCustomFieldService.removeById(id);
    }

    /**
     * 根据主键更新模板和字段的关联关系。
     *
     * @param templateCustomField 模板和字段的关联关系
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    @Operation(description="根据主键更新模板和字段的关联关系")
    public boolean update(@RequestBody @Parameter(description="模板和字段的关联关系主键") TemplateCustomField templateCustomField) {
        return baseTemplateCustomFieldService.updateById(templateCustomField);
    }

    /**
     * 查询所有模板和字段的关联关系。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    @Operation(description="查询所有模板和字段的关联关系")
    public List<TemplateCustomField> list() {
        return baseTemplateCustomFieldService.list();
    }

    /**
     * 根据主键获取模板和字段的关联关系。
     *
     * @param id 模板和字段的关联关系主键
     * @return 模板和字段的关联关系详情
     */
    @GetMapping("getInfo/{id}")
    @Operation(description="根据主键获取模板和字段的关联关系")
    public TemplateCustomField getInfo(@PathVariable @Parameter(description="模板和字段的关联关系主键") String id) {
        return baseTemplateCustomFieldService.getById(id);
    }

    /**
     * 分页查询模板和字段的关联关系。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    @Operation(description="分页查询模板和字段的关联关系")
    public Page<TemplateCustomField> page(@Parameter(description="分页信息") Page<TemplateCustomField> page) {
        return baseTemplateCustomFieldService.page(page);
    }

}
