package com.master.meta.service;

import com.master.meta.dto.system.request.TemplateUpdateRequest;
import com.master.meta.entity.Template;

import java.util.Map;

public interface OrganizationTemplateService extends BaseTemplateService {
    Map<String, Boolean> getOrganizationTemplateEnableConfig(String organizationId);

    void disableOrganizationTemplate(String orgId, String scene);

    Template add(TemplateUpdateRequest request, String userName);

    Template update(TemplateUpdateRequest request);
}
