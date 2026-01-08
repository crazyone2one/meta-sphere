package com.master.meta.service.impl;

import com.master.meta.constants.TemplateScene;
import com.master.meta.dto.system.TemplateCustomFieldDTO;
import com.master.meta.dto.system.TemplateDTO;
import com.master.meta.dto.system.request.TemplateCustomFieldRequest;
import com.master.meta.dto.system.request.TemplateRequest;
import com.master.meta.dto.system.request.TemplateSystemCustomFieldRequest;
import com.master.meta.entity.CustomField;
import com.master.meta.entity.CustomFieldOption;
import com.master.meta.entity.Template;
import com.master.meta.entity.TemplateCustomField;
import com.master.meta.handle.Translator;
import com.master.meta.handle.exception.CustomException;
import com.master.meta.handle.resolver.field.AbstractCustomFieldResolver;
import com.master.meta.handle.resolver.field.CustomFieldResolverFactory;
import com.master.meta.mapper.TemplateMapper;
import com.master.meta.service.BaseCustomFieldOptionService;
import com.master.meta.service.BaseCustomFieldService;
import com.master.meta.service.BaseTemplateCustomFieldService;
import com.master.meta.service.BaseTemplateService;
import com.master.meta.utils.ServiceUtils;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.master.meta.entity.table.TemplateTableDef.TEMPLATE;
import static com.master.meta.handle.result.CommonResultCode.INTERNAL_TEMPLATE_PERMISSION;
import static com.master.meta.handle.result.SystemResultCode.TEMPLATE_EXIST;
import static com.master.meta.handle.result.SystemResultCode.TEMPLATE_SCENE_ILLEGAL;

/**
 * 模版 服务层实现。
 *
 * @author 11's papa
 * @since 2025-11-20
 */
@Slf4j
@Service("baseTemplateService")
public class BaseTemplateServiceImpl extends ServiceImpl<TemplateMapper, Template> implements BaseTemplateService {
    protected final BaseCustomFieldService baseCustomFieldService;
    protected final BaseTemplateCustomFieldService baseTemplateCustomFieldService;
    protected final BaseCustomFieldOptionService baseCustomFieldOptionService;

    public BaseTemplateServiceImpl(@Qualifier("baseTemplateCustomFieldService") BaseTemplateCustomFieldService baseTemplateCustomFieldService,
                                   @Qualifier("baseCustomFieldService") BaseCustomFieldService baseCustomFieldService,
                                   @Qualifier("baseCustomFieldOptionService") BaseCustomFieldOptionService baseCustomFieldOptionService) {
        this.baseCustomFieldService = baseCustomFieldService;
        this.baseTemplateCustomFieldService = baseTemplateCustomFieldService;
        this.baseCustomFieldOptionService = baseCustomFieldOptionService;
    }

    @Override
    public Template getWithCheck(String id) {
        return checkResourceExist(id);
    }

    @Override
    public String translateInternalTemplate() {
        return Translator.get("template.default");
    }

    @Override
    public List<Template> translateInternalTemplate(List<Template> templates) {
        templates.forEach(item -> {
            if (item.getInternal()) {
                item.setName(translateInternalTemplate());
            }
        });
        return templates;
    }

    @Override
    public boolean isOrganizationTemplateEnable(String orgId, String scene) {
        return baseCustomFieldService.isOrganizationTemplateEnable(orgId, scene);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Template add(Template template, List<TemplateCustomFieldRequest> customFields, List<TemplateSystemCustomFieldRequest> systemFields) {
        template.setInternal(false);
        return this.baseAdd(template, customFields, systemFields);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Template baseAdd(Template template, List<TemplateCustomFieldRequest> customFields, List<TemplateSystemCustomFieldRequest> systemFields) {
        checkAddExist(template);
        mapper.insert(template);
        baseTemplateCustomFieldService.addCustomFieldByTemplateId(template.getId(), customFields);
        baseTemplateCustomFieldService.addSystemFieldByTemplateId(template.getId(), parse2TemplateCustomFieldRequests(systemFields));
        return template;
    }

    @Override
    public void delete(String id) {
        Template template = checkResourceExist(id);
        checkInternal(template);
        mapper.deleteById(id);
        baseTemplateCustomFieldService.deleteByTemplateId(id);
    }

    @Override
    public Template update(Template template, List<TemplateCustomFieldRequest> customFields, List<TemplateSystemCustomFieldRequest> systemFields) {
        checkResourceExist(template.getId());
        checkUpdateExist(template);
        if (customFields != null) {
            baseTemplateCustomFieldService.deleteByTemplateIdAndSystem(template.getId(), false);
            baseTemplateCustomFieldService.addCustomFieldByTemplateId(template.getId(), customFields);
        }
        if (systemFields != null) {
            // 系统字段
            baseTemplateCustomFieldService.deleteByTemplateIdAndSystem(template.getId(), true);
            baseTemplateCustomFieldService.addSystemFieldByTemplateId(template.getId(), parse2TemplateCustomFieldRequests(systemFields));
        }
        mapper.update(template);
        return template;
    }

    @Override
    public List<Template> list(String scopeId, String scene) {
        checkScene(scene);
        List<Template> templates = getTemplates(scopeId, scene);
        translateInternalTemplate(templates);
        return templates;
    }

    private void checkScene(String scene) {
        Arrays.stream(TemplateScene.values()).map(TemplateScene::name)
                .filter(item -> item.equals(scene))
                .findFirst()
                .orElseThrow(() -> new CustomException(TEMPLATE_SCENE_ILLEGAL));
    }

    @Override
    public List<Template> getTemplates(String scopeId, String scene) {
        return queryChain().where(TEMPLATE.SCOPE_ID.eq(scopeId).and(TEMPLATE.SCENE.eq(scene))).list();
    }

    @Override
    public TemplateDTO getTemplateDTO(Template template) {
        List<TemplateCustomField> templateCustomFields = baseTemplateCustomFieldService.getByTemplateId(template.getId());
        // 查找字段名称
        List<String> fieldIds = templateCustomFields.stream().map(TemplateCustomField::getFieldId).toList();
        List<CustomField> customFields = baseCustomFieldService.getByIds(fieldIds);
        Map<String, CustomField> fieldMap = customFields
                .stream()
                .collect(Collectors.toMap(CustomField::getId, Function.identity()));

        // 封装自定义字段信息
        List<TemplateCustomFieldDTO> fieldDTOS = templateCustomFields.stream()
                .filter(i -> !BooleanUtils.isTrue(i.getSystemField()) && fieldMap.containsKey(i.getFieldId()))
                .sorted(Comparator.comparingInt(TemplateCustomField::getPos))
                .map(i -> {
                    CustomField customField = fieldMap.get(i.getFieldId());
                    TemplateCustomFieldDTO templateCustomFieldDTO = new TemplateCustomFieldDTO();
                    BeanUtils.copyProperties(i, templateCustomFieldDTO);
                    templateCustomFieldDTO.setFieldName(customField.getName());
                    if (BooleanUtils.isTrue(customField.getInternal())) {
                        templateCustomFieldDTO.setInternalFieldKey(customField.getName());
                        templateCustomFieldDTO.setFieldName(baseCustomFieldService.translateInternalField(customField.getName()));
                    }
                    templateCustomFieldDTO.setType(customField.getType());
                    templateCustomFieldDTO.setInternal(customField.getInternal());
                    AbstractCustomFieldResolver customFieldResolver = CustomFieldResolverFactory.getResolver(customField.getType());
                    Object defaultValue = null;
                    try {
                        defaultValue = customFieldResolver.parse2Value(i.getDefaultValue());
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                    templateCustomFieldDTO.setDefaultValue(defaultValue);
                    return templateCustomFieldDTO;
                }).collect(Collectors.toList());

        List<String> ids = fieldDTOS.stream().map(TemplateCustomFieldDTO::getFieldId).toList();
        List<CustomFieldOption> fieldOptions = baseCustomFieldOptionService.getByFieldIds(ids);
        Map<String, List<CustomFieldOption>> collect = fieldOptions.stream().collect(Collectors.groupingBy(CustomFieldOption::getFieldId));

        fieldDTOS.forEach(item -> {
            item.setOptions(collect.get(item.getFieldId()));
        });

        // 封装系统字段信息
        List<TemplateCustomFieldDTO> systemFieldDTOS = templateCustomFields.stream()
                .filter(i -> BooleanUtils.isTrue(i.getSystemField()))
                .map(i -> {
                    TemplateCustomFieldDTO templateCustomFieldDTO = new TemplateCustomFieldDTO();
                    templateCustomFieldDTO.setFieldId(i.getFieldId());
                    templateCustomFieldDTO.setDefaultValue(i.getDefaultValue());
                    return templateCustomFieldDTO;
                }).toList();

        List<String> sysIds = systemFieldDTOS.stream().map(TemplateCustomFieldDTO::getFieldId).toList();
        List<CustomFieldOption> sysFieldOptions = baseCustomFieldOptionService.getByFieldIds(sysIds);
        Map<String, List<CustomFieldOption>> sysCollect = sysFieldOptions.stream().collect(Collectors.groupingBy(CustomFieldOption::getFieldId));

        systemFieldDTOS.forEach(item -> {
            item.setOptions(sysCollect.get(item.getFieldId()));
        });

        TemplateDTO templateDTO = new TemplateDTO();
        BeanUtils.copyProperties(template, templateDTO);
        templateDTO.setCustomFields(fieldDTOS);
        templateDTO.setSystemFields(systemFieldDTOS);
        return templateDTO;
    }

    @Override
    public Page<Template> templatePage(TemplateRequest request) {
        checkScene(request.getScene());
        Page<Template> page = queryChain().where(TEMPLATE.SCOPE_ID.eq(request.getScopedId()).and(TEMPLATE.SCENE.eq(request.getScene())))
                .and(TEMPLATE.NAME.like(request.getKeyword()))
                .page(new Page<>(request.getPage(), request.getPageSize()));
        translateInternalTemplate(page.getRecords());
        return page;
    }

    @Override
    public List<TemplateCustomFieldRequest> getRefTemplateCustomFieldRequest(String projectId, List<TemplateCustomFieldRequest> customFields) {
        if (customFields == null) {
            return null;
        }
        List<String> fieldIds = customFields.stream()
                .map(TemplateCustomFieldRequest::getFieldId).toList();
        // 查询当前组织字段所对应的项目字段，构建map，键为组织字段ID，值为项目字段ID
        Map<String, String> refFieldMap = baseCustomFieldService.getByRefIdsAndScopeId(fieldIds, projectId)
                .stream()
                .collect(Collectors.toMap(CustomField::getRefId, CustomField::getId));
        // 根据组织字段ID，替换为项目字段ID
        return customFields.stream()
                .map(item -> {
                    TemplateCustomFieldRequest request = new TemplateCustomFieldRequest();
                    BeanUtils.copyProperties(item, request);
                    request.setFieldId(refFieldMap.get(item.getFieldId()));
                    return request;
                })
                .filter(item -> StringUtils.isNotBlank(item.getFieldId()))
                .toList();
    }

    private void checkUpdateExist(Template template) {
        if (StringUtils.isBlank(template.getName())) {
            return;
        }
        boolean exists = queryChain().where(TEMPLATE.NAME.eq(template.getName())
                .and(TEMPLATE.SCOPE_ID.eq(template.getScopeId()))
                .and(TEMPLATE.ID.ne(template.getId()))
                .and(TEMPLATE.SCENE.eq(template.getScene()))).exists();
        if (exists) {
            throw new CustomException(TEMPLATE_EXIST);
        }
    }

    private void checkInternal(Template template) {
        if (template.getInternal()) {
            throw new CustomException(INTERNAL_TEMPLATE_PERMISSION);
        }
    }

    private List<TemplateCustomFieldRequest> parse2TemplateCustomFieldRequests(List<TemplateSystemCustomFieldRequest> systemFields) {
        if (CollectionUtils.isEmpty(systemFields)) {
            return List.of();
        }
        return systemFields.stream().map(systemFiled -> {
            TemplateCustomFieldRequest templateCustomFieldRequest = new TemplateCustomFieldRequest();
            BeanUtils.copyProperties(systemFiled, templateCustomFieldRequest);
            templateCustomFieldRequest.setRequired(false);
            return templateCustomFieldRequest;
        }).toList();
    }

    protected void checkAddExist(Template template) {
        boolean exists = queryChain().exists();
        if (exists) {
            throw new CustomException(TEMPLATE_EXIST);
        }
    }

    private Template checkResourceExist(String id) {
        return ServiceUtils.checkResourceExist(mapper.selectOneById(id), "permission.organization_template.name");
    }
}
