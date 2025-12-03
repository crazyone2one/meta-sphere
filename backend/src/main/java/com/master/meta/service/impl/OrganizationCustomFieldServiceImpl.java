package com.master.meta.service.impl;

import com.master.meta.constants.TemplateScopeType;
import com.master.meta.dto.system.CustomFieldDTO;
import com.master.meta.dto.system.request.CustomFieldOptionRequest;
import com.master.meta.entity.CustomField;
import com.master.meta.entity.CustomFieldOption;
import com.master.meta.entity.SystemProject;
import com.master.meta.handle.exception.CustomException;
import com.master.meta.mapper.OrganizationMapper;
import com.master.meta.mapper.TemplateCustomFieldMapper;
import com.master.meta.service.BaseCustomFieldOptionService;
import com.master.meta.service.OrganizationCustomFieldService;
import com.master.meta.service.OrganizationParameterService;
import com.master.meta.utils.CommonBeanFactory;
import com.master.meta.utils.ServiceUtils;
import com.mybatisflex.core.query.QueryChain;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.master.meta.entity.table.CustomFieldTableDef.CUSTOM_FIELD;
import static com.master.meta.entity.table.SystemProjectTableDef.SYSTEM_PROJECT;
import static com.master.meta.handle.result.SystemResultCode.ORGANIZATION_TEMPLATE_PERMISSION;

@Service("organizationCustomFieldService")
public class OrganizationCustomFieldServiceImpl extends BaseCustomFieldServiceImpl implements OrganizationCustomFieldService {
    public OrganizationCustomFieldServiceImpl(@Qualifier("baseCustomFieldOptionService") BaseCustomFieldOptionService baseCustomFieldOptionService,
                                              OrganizationParameterService organizationParameterService,
                                              TemplateCustomFieldMapper templateCustomFieldMapper) {
        super(baseCustomFieldOptionService, organizationParameterService, templateCustomFieldMapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomField add(CustomField customField, List<CustomFieldOptionRequest> options) {
        checkOrganizationTemplateEnable(customField.getScopeId(), customField.getScene());
        ServiceUtils.checkResourceExist(Objects.requireNonNull(CommonBeanFactory.getBean(OrganizationMapper.class))
                .selectOneById(customField.getScopeId()), "permission.system_organization_project.name");
        customField.setScopeType(TemplateScopeType.ORGANIZATION.name());
        customField = super.add(customField, options);
        // 同步创建项目级别字段
        addRefProjectCustomField(customField, options);
        return customField;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomField update(CustomField customField, List<CustomFieldOptionRequest> options) {
        CustomField originCustomField = getWithCheck(customField.getId());
        if (originCustomField.getInternal()) {
            // 内置字段不能修改名字
            customField.setName(null);
        }
        checkOrganizationTemplateEnable(customField.getScopeId(), originCustomField.getScene());
        customField.setScopeId(originCustomField.getScopeId());
        customField.setScene(originCustomField.getScene());
        ServiceUtils.checkResourceExist(Objects.requireNonNull(CommonBeanFactory.getBean(OrganizationMapper.class))
                .selectOneById(originCustomField.getScopeId()), "permission.system_organization_project.name");
        // 同步创建项目级别字段
        updateRefProjectCustomField(customField, options);
        return super.update(customField, options);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        CustomField customField = getWithCheck(id);
        checkOrganizationTemplateEnable(customField.getScopeId(), customField.getScene());
        checkInternal(customField);
        ServiceUtils.checkResourceExist(Objects.requireNonNull(CommonBeanFactory.getBean(OrganizationMapper.class))
                .selectOneById(customField.getScopeId()), "permission.system_organization_project.name");
        // 同步删除项目级别字段
        deleteRefProjectTemplate(id);
        super.delete(id);
    }

    @Override
    public CustomFieldDTO getCustomFieldDTOWithCheck(String id) {
        CustomFieldDTO customField = super.getCustomFieldDTOWithCheck(id);
        ServiceUtils.checkResourceExist(Objects.requireNonNull(CommonBeanFactory.getBean(OrganizationMapper.class))
                .selectOneById(customField.getScopeId()), "permission.system_organization_project.name");
        return customField;
    }

    private void deleteRefProjectTemplate(String orgCustomFieldId) {
        // 删除关联的项目字段
        mapper.deleteByQuery(queryChain().where(CUSTOM_FIELD.REF_ID.eq(orgCustomFieldId)));

        // 删除字段选项
        List<String> projectCustomFieldIds = queryChain().select(CUSTOM_FIELD.ID).where(CUSTOM_FIELD.REF_ID.eq(orgCustomFieldId)).listAs(String.class);
        // 分批删除
        baseCustomFieldOptionService.deleteByFieldIds(projectCustomFieldIds);
    }

    private void updateRefProjectCustomField(CustomField orgCustomField, List<CustomFieldOptionRequest> options) {
        List<CustomField> projectFields = queryChain().where(CUSTOM_FIELD.REF_ID.eq(orgCustomField.getId())).list();
        CustomField customField = new CustomField();
        BeanUtils.copyProperties(orgCustomField, customField);
        projectFields.forEach(projectField -> {
            customField.setId(projectField.getId());
            customField.setScopeId(projectField.getScopeId());
            customField.setRefId(orgCustomField.getId());
            customField.setScene(orgCustomField.getScene());
            super.update(customField, options);
        });
    }

    private void addRefProjectCustomField(CustomField orgCustomField, List<CustomFieldOptionRequest> options) {
        String orgId = orgCustomField.getScopeId();
        CustomField customField = new CustomField();
        BeanUtils.copyProperties(orgCustomField, customField);
        List<CustomFieldOption> customFieldOptions = parseCustomFieldOptionRequest2Option(options);
        List<String> projectIds = QueryChain.of(SystemProject.class).select(SYSTEM_PROJECT.ID).where(SYSTEM_PROJECT.ORGANIZATION_ID.eq(orgId)).listAs(String.class);
        projectIds.forEach(projectId -> {
            customField.setScopeType(TemplateScopeType.PROJECT.name());
            customField.setScopeId(projectId);
            customField.setRefId(orgCustomField.getId());
            super.baseAdd(customField, customFieldOptions);
        });
    }

    private void checkOrganizationTemplateEnable(String orgId, String scene) {
        if (!isOrganizationTemplateEnable(orgId, scene)) {
            throw new CustomException(ORGANIZATION_TEMPLATE_PERMISSION);
        }
    }
}
