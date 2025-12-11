package com.master.meta.controller;

import com.master.meta.constants.PermissionConstants;
import com.master.meta.dto.system.CustomFieldDTO;
import com.master.meta.dto.system.request.CustomFieldRequest;
import com.master.meta.dto.system.request.CustomFieldUpdateRequest;
import com.master.meta.entity.CustomField;
import com.master.meta.handle.log.annotation.Log;
import com.master.meta.handle.log.constants.OperationLogType;
import com.master.meta.handle.permission.RequiresPermissions;
import com.master.meta.handle.validation.Created;
import com.master.meta.handle.validation.Updated;
import com.master.meta.service.OrganizationCustomFieldService;
import com.master.meta.service.log.OrganizationCustomFieldLogService;
import com.master.meta.utils.SessionUtils;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "系统设置-组织-自定义字段")
@RestController
@RequestMapping("/organization/custom/field")
public class OrganizationCustomFieldController {
    private final OrganizationCustomFieldService organizationCustomFieldService;

    public OrganizationCustomFieldController(@Qualifier("organizationCustomFieldService") OrganizationCustomFieldService organizationCustomFieldService) {
        this.organizationCustomFieldService = organizationCustomFieldService;
    }

    @PostMapping("/add")
    @Operation(summary = "创建自定义字段")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_ADD)
    @Log(type = OperationLogType.ADD, expression = "#msClass.addLog(#request)", msClass = OrganizationCustomFieldLogService.class)
    public CustomField add(@Validated({Created.class}) @RequestBody CustomFieldUpdateRequest request) {
        CustomField customField = new CustomField();
        BeanUtils.copyProperties(request, customField);
        customField.setCreateUser(SessionUtils.getUserName());
        return organizationCustomFieldService.add(customField, request.getOptions());
    }

    @PostMapping("/update")
    @Operation(summary = "更新自定义字段")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateLog(#request)", msClass = OrganizationCustomFieldLogService.class)
    public CustomField update(@Validated({Updated.class}) @RequestBody CustomFieldUpdateRequest request) {
        CustomField customField = new CustomField();
        BeanUtils.copyProperties(request, customField);
        return organizationCustomFieldService.update(customField, request.getOptions());
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "删除自定义字段")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#id)", msClass = OrganizationCustomFieldLogService.class)
    public void delete(@PathVariable String id) {
        organizationCustomFieldService.delete(id);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "获取自定义字段详情")
    @RequiresPermissions(PermissionConstants.ORGANIZATION_TEMPLATE_READ)
    public CustomFieldDTO get(@PathVariable String id) {
        return organizationCustomFieldService.getCustomFieldDTOWithCheck(id);
    }

    @PostMapping("page")
    @Operation(description = "分页查询自定义字段")
    @RequiresPermissions(PermissionConstants.PROJECT_TEMPLATE_READ)
    public Page<CustomFieldDTO> page(@Validated @RequestBody CustomFieldRequest request) {
        return organizationCustomFieldService.page(request);
    }
}
