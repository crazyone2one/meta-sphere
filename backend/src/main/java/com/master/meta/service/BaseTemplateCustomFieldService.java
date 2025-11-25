package com.master.meta.service;

import com.master.meta.dto.system.request.TemplateCustomFieldRequest;
import com.master.meta.entity.TemplateCustomField;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 模板和字段的关联关系 服务层。
 *
 * @author 11's papa
 * @since 2025-11-20
 */
public interface BaseTemplateCustomFieldService extends IService<TemplateCustomField> {
    void addCustomFieldByTemplateId(String id, List<TemplateCustomFieldRequest> customFieldRequests);

    void addSystemFieldByTemplateId(String id, List<TemplateCustomFieldRequest> templateCustomFieldRequests);

    void deleteByTemplateId(String templateId);

    void deleteByTemplateIdAndSystem(String templateId, boolean isSystem);

    List<TemplateCustomField> getByTemplateId(String id);
}
