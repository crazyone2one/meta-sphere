package com.master.meta.service.impl;

import com.master.meta.constants.TemplateScene;
import com.master.meta.constants.TemplateScopeType;
import com.master.meta.dto.system.request.TemplateCustomFieldRequest;
import com.master.meta.dto.system.request.TemplateSystemCustomFieldRequest;
import com.master.meta.dto.system.request.TemplateUpdateRequest;
import com.master.meta.entity.OrganizationParameter;
import com.master.meta.entity.SystemProject;
import com.master.meta.entity.Template;
import com.master.meta.handle.exception.CustomException;
import com.master.meta.mapper.OrganizationMapper;
import com.master.meta.service.*;
import com.master.meta.utils.CommonBeanFactory;
import com.master.meta.utils.ServiceUtils;
import com.mybatisflex.core.query.QueryChain;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.master.meta.entity.table.SystemProjectTableDef.SYSTEM_PROJECT;
import static com.master.meta.entity.table.TemplateTableDef.TEMPLATE;
import static com.master.meta.handle.result.SystemResultCode.ORGANIZATION_TEMPLATE_PERMISSION;

@Service("organizationTemplateService")
public class OrganizationTemplateServiceImpl extends BaseTemplateServiceImpl implements OrganizationTemplateService {
    private final OrganizationParameterService organizationParameterService;
    private final OrganizationMapper organizationMapper;

    public OrganizationTemplateServiceImpl(@Qualifier("baseTemplateCustomFieldService") BaseTemplateCustomFieldService baseTemplateCustomFieldService,
                                           @Qualifier("baseCustomFieldService") BaseCustomFieldService baseCustomFieldService,
                                           @Qualifier("baseCustomFieldOptionService") BaseCustomFieldOptionService baseCustomFieldOptionService, OrganizationParameterService organizationParameterService, OrganizationMapper organizationMapper) {
        super(baseTemplateCustomFieldService, baseCustomFieldService, baseCustomFieldOptionService);
        this.organizationParameterService = organizationParameterService;
        this.organizationMapper = organizationMapper;
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

    @Override
    public Template add(TemplateUpdateRequest request, String userName) {
        Template template = new Template();
        BeanUtils.copyProperties(request, template);
        template.setCreateUser(userName);
        checkOrgResourceExist(template);
        checkOrganizationTemplateEnable(template.getScopeId(), template.getScene());
        template.setScopeType(TemplateScopeType.ORGANIZATION.name());
        template.setRefId(null);
        template = super.add(template, request.getCustomFields(), request.getSystemFields());
        // 同步创建项目级别模板
        addRefProjectTemplate(template, request.getCustomFields(), request.getSystemFields());
        // saveUploadImages(request);
        return template;
    }

    private void addRefProjectTemplate(Template orgTemplate, List<TemplateCustomFieldRequest> customFields, List<TemplateSystemCustomFieldRequest> systemFields) {
        String orgId = orgTemplate.getScopeId();
        List<String> projectIds = QueryChain.of(SystemProject.class)
                .select(SYSTEM_PROJECT.ID).where(SYSTEM_PROJECT.ORGANIZATION_ID.eq(orgId)).listAs(String.class);
        Template template = new Template();
        BeanUtils.copyProperties(orgTemplate, template);
        projectIds.forEach(projectId -> {
            template.setScopeId(projectId);
            template.setRefId(orgTemplate.getId());
            template.setScopeType(TemplateScopeType.PROJECT.name());
            List<TemplateCustomFieldRequest> refCustomFields = getRefTemplateCustomFieldRequest(projectId, customFields);
            super.baseAdd(template, refCustomFields, systemFields);
        });
    }

    private void checkOrganizationTemplateEnable(String orgId, String scene) {
        if (!isOrganizationTemplateEnable(orgId, scene)) {
            throw new CustomException(ORGANIZATION_TEMPLATE_PERMISSION);
        }
    }

    private void checkOrgResourceExist(Template template) {
        ServiceUtils.checkResourceExist(organizationMapper.selectOneById(template.getScopeId()), "permission.system_organization_project.name");
    }

    @Override
    public Template update(TemplateUpdateRequest request) {
        Template template = new Template();
        BeanUtils.copyProperties(request, template);
        Template originTemplate = super.getWithCheck(template.getId());
        checkOrganizationTemplateEnable(originTemplate.getScopeId(), originTemplate.getScene());
        template.setScopeId(originTemplate.getScopeId());
        template.setScene(originTemplate.getScene());
        checkOrgResourceExist(originTemplate);
        updateRefProjectTemplate(template, request.getCustomFields(), request.getSystemFields());
        template.setRefId(null);
        template = super.update(template, request.getCustomFields(), request.getSystemFields());
        // saveUploadImages(request);
        return template;
    }

    private void updateRefProjectTemplate(Template orgTemplate, List<TemplateCustomFieldRequest> customFields, List<TemplateSystemCustomFieldRequest> systemFields) {
        List<Template> projectTemplates = queryChain().where(TEMPLATE.REF_ID.eq(orgTemplate.getId())).list();
        Template template = new Template();
        BeanUtils.copyProperties(orgTemplate, template);
        projectTemplates.forEach(projectTemplate -> {
            template.setId(projectTemplate.getId());
            template.setScopeId(projectTemplate.getScopeId());
            template.setRefId(orgTemplate.getId());
            template.setScene(orgTemplate.getScene());
            List<TemplateCustomFieldRequest> refCustomFields = getRefTemplateCustomFieldRequest(projectTemplate.getScopeId(), customFields);
            super.update(template, refCustomFields, systemFields);
        });
    }
}
