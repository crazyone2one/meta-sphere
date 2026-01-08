package com.master.meta.service;

import com.master.meta.dto.system.TemplateDTO;
import com.master.meta.dto.system.request.TemplateCustomFieldRequest;
import com.master.meta.dto.system.request.TemplateRequest;
import com.master.meta.dto.system.request.TemplateSystemCustomFieldRequest;
import com.master.meta.entity.Template;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 模版 服务层。
 *
 * @author 11's papa
 * @since 2025-11-20
 */
public interface BaseTemplateService extends IService<Template> {

    Template getWithCheck(String id);

    String translateInternalTemplate();

    List<Template> translateInternalTemplate(List<Template> templates);

    boolean isOrganizationTemplateEnable(String orgId, String scene);

    Template add(Template template, List<TemplateCustomFieldRequest> customFields, List<TemplateSystemCustomFieldRequest> systemFields);

    Template baseAdd(Template template, List<TemplateCustomFieldRequest> customFields, List<TemplateSystemCustomFieldRequest> systemFields);

    void delete(String id);

    Template update(Template template, @Valid List<TemplateCustomFieldRequest> customFields, @Valid List<TemplateSystemCustomFieldRequest> systemFields);

    List<Template> list(String projectId, String scene);

    List<Template> getTemplates(String scopeId, String scene);

    TemplateDTO getTemplateDTO(Template template);

    Page<Template> templatePage(TemplateRequest request);

    List<TemplateCustomFieldRequest> getRefTemplateCustomFieldRequest(String projectId, List<TemplateCustomFieldRequest> customFields);
}
