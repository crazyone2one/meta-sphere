package com.master.meta.service.impl;

import com.master.meta.constants.TemplateScopeType;
import com.master.meta.dto.system.CustomFieldDTO;
import com.master.meta.dto.system.request.CustomFieldOptionRequest;
import com.master.meta.entity.CustomField;
import com.master.meta.service.BaseCustomFieldOptionService;
import com.master.meta.service.ProjectCustomFieldService;
import com.master.meta.service.SystemProjectService;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("projectCustomFieldService")
public class ProjectCustomFieldServiceImpl extends BaseCustomFieldServiceImpl implements ProjectCustomFieldService {
    private final SystemProjectService systemProjectService;
    public ProjectCustomFieldServiceImpl(BaseCustomFieldOptionService baseCustomFieldOptionService,
                                         SystemProjectService systemProjectService) {
        super(baseCustomFieldOptionService);
        this.systemProjectService = systemProjectService;
    }

    @Override
    public CustomField add(CustomField customField, List<CustomFieldOptionRequest> options) {
        val project = systemProjectService.checkResourceExist(customField.getScopeId());
//        checkProjectTemplateEnable(project.getOrganizationId(), customField.getScene());
        customField.setScopeType(TemplateScopeType.PROJECT.name());
        return super.add(customField, options);
    }

    @Override
    public CustomField update(CustomField customField, List<CustomFieldOptionRequest> options) {
        CustomField originCustomField = getWithCheck(customField.getId());
        if (originCustomField.getInternal()) {
            // 内置字段不能修改名字
            customField.setName(null);
        }
        customField.setScopeId(originCustomField.getScopeId());
        customField.setScene(originCustomField.getScene());
        val project = systemProjectService.checkResourceExist(originCustomField.getScopeId());
//        checkProjectTemplateEnable(project.getOrganizationId(), originCustomField.getScene());
        return super.update(customField, options);
    }

//    private void checkProjectTemplateEnable(String orgId, String scene) {
//        if (isOrganizationTemplateEnable(orgId, scene)) {
//            throw new CustomException(PROJECT_TEMPLATE_PERMISSION);
//        }
//    }

    @Override
    public CustomField getWithCheck(String id) {
        checkResourceExist(id);
        return mapper.selectOneById(id);
    }

    @Override
    public List<CustomFieldDTO> list(String projectId, String scene) {
        systemProjectService.checkResourceExist(projectId);
        return super.list(projectId, scene);
    }
}
