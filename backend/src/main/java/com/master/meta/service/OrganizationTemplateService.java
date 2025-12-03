package com.master.meta.service;

import java.util.Map;

public interface OrganizationTemplateService extends BaseTemplateService {
    Map<String, Boolean> getOrganizationTemplateEnableConfig(String organizationId);

    void disableOrganizationTemplate(String orgId, String scene);
}
