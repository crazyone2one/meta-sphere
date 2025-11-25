package com.master.meta.service;

import com.master.meta.dto.system.TemplateDTO;
import com.master.meta.dto.system.request.TemplateUpdateRequest;
import com.master.meta.entity.Template;

import java.util.List;
import java.util.Map;

public interface ProjectTemplateService extends BaseTemplateService {
    Template add(TemplateUpdateRequest request, String userName);

    void checkProjectTemplateEnable(String projectId, String scene);

    void delete(String id);

    Template update(TemplateUpdateRequest request);

    List<Template> list(String projectId, String scene);

    TemplateDTO getTemplateDTOWithCheck(String id);

    void setDefaultTemplate(String projectId, String id);

    Map<String, Boolean> getProjectTemplateEnableConfig(String projectId);
}
