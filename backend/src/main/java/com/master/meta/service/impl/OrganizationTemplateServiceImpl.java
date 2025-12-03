package com.master.meta.service.impl;

import com.master.meta.constants.TemplateScene;
import com.master.meta.entity.OrganizationParameter;
import com.master.meta.mapper.OrganizationMapper;
import com.master.meta.service.*;
import com.master.meta.utils.CommonBeanFactory;
import com.master.meta.utils.ServiceUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service("organizationTemplateService")
public class OrganizationTemplateServiceImpl extends BaseTemplateServiceImpl implements OrganizationTemplateService {
    private final OrganizationParameterService organizationParameterService;
    public OrganizationTemplateServiceImpl(@Qualifier("baseTemplateCustomFieldService") BaseTemplateCustomFieldService baseTemplateCustomFieldService,
                                           @Qualifier("baseCustomFieldService") BaseCustomFieldService baseCustomFieldService,
                                           @Qualifier("baseCustomFieldOptionService") BaseCustomFieldOptionService baseCustomFieldOptionService, OrganizationParameterService organizationParameterService) {
        super(baseTemplateCustomFieldService, baseCustomFieldService, baseCustomFieldOptionService);
        this.organizationParameterService = organizationParameterService;
    }


    @Override
    public Map<String, Boolean> getOrganizationTemplateEnableConfig(String organizationId) {
        ServiceUtils.checkResourceExist(Objects.requireNonNull(CommonBeanFactory.getBean(OrganizationMapper.class)).selectOneById(organizationId), "permission.system_organization_project.name");
        HashMap<String, Boolean> templateEnableConfig = new HashMap<>();
        List.of(TemplateScene.SCHEDULE, TemplateScene.BUG)
                .forEach(scene ->
                        templateEnableConfig.put(scene.name(), isOrganizationTemplateEnable(organizationId, scene.name())));
        return templateEnableConfig;
    }

    @Override
    public void disableOrganizationTemplate(String orgId, String scene) {
        if (StringUtils.isBlank(organizationParameterService.getValue(orgId, scene))) {
            OrganizationParameter organizationParameter = new OrganizationParameter();
            organizationParameter.setOrganizationId(orgId);
            organizationParameter.setParamKey(organizationParameterService.getOrgTemplateEnableKeyByScene(scene));
            organizationParameter.setParamValue(BooleanUtils.toStringTrueFalse(false));
            organizationParameterService.save(organizationParameter);
        }
    }
}
