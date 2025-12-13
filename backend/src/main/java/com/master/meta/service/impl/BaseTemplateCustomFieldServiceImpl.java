package com.master.meta.service.impl;

import com.master.meta.dto.system.CustomFieldDTO;
import com.master.meta.dto.system.request.TemplateCustomFieldRequest;
import com.master.meta.entity.CustomField;
import com.master.meta.entity.TemplateCustomField;
import com.master.meta.handle.resolver.field.AbstractCustomFieldResolver;
import com.master.meta.handle.resolver.field.CustomFieldResolverFactory;
import com.master.meta.mapper.TemplateCustomFieldMapper;
import com.master.meta.service.BaseCustomFieldService;
import com.master.meta.service.BaseTemplateCustomFieldService;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 模板和字段的关联关系 服务层实现。
 *
 * @author 11's papa
 * @since 2025-11-20
 */
@Service("baseTemplateCustomFieldService")
public class BaseTemplateCustomFieldServiceImpl extends ServiceImpl<TemplateCustomFieldMapper, TemplateCustomField> implements BaseTemplateCustomFieldService {
    private final BaseCustomFieldService baseCustomFieldService;

    public BaseTemplateCustomFieldServiceImpl(@Qualifier("baseCustomFieldService") BaseCustomFieldService baseCustomFieldService) {
        this.baseCustomFieldService = baseCustomFieldService;
    }

    public static final ThreadLocal<Boolean> validateDefaultValue = new ThreadLocal<>();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCustomFieldByTemplateId(String id, List<TemplateCustomFieldRequest> customFieldRequests) {
        if (CollectionUtils.isEmpty(customFieldRequests)) {
            return;
        }
        // 过滤下不存在的字段
        List<String> ids = customFieldRequests.stream().map(TemplateCustomFieldRequest::getFieldId).toList();
        Set<String> fieldIdSet = baseCustomFieldService.listByIds(ids)
                .stream()
                .map(CustomField::getId)
                .collect(Collectors.toSet());
        customFieldRequests = customFieldRequests.stream()
                .filter(item -> fieldIdSet.contains(item.getFieldId()))
                .toList();
        this.addByTemplateId(id, customFieldRequests, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSystemFieldByTemplateId(String id, List<TemplateCustomFieldRequest> customFieldRequests) {
        if (CollectionUtils.isEmpty(customFieldRequests)) {
            return;
        }
        this.addByTemplateId(id, customFieldRequests, true);
    }

    @Override
    public void deleteByTemplateId(String templateId) {
        QueryChain<TemplateCustomField> chain = queryChain().where(TemplateCustomField::getTemplateId).eq(templateId);
        mapper.deleteByQuery(chain);
    }

    @Override
    public void deleteByTemplateIdAndSystem(String templateId, boolean isSystem) {
        QueryChain<TemplateCustomField> chain = queryChain().where(TemplateCustomField::getTemplateId).eq(templateId)
                .and(TemplateCustomField::getSystemField).eq(isSystem);
        mapper.deleteByQuery(chain);
    }

    @Override
    public List<TemplateCustomField> getByTemplateId(String id) {
        return queryChain().where(TemplateCustomField::getTemplateId).eq(id).list();
    }

    private void addByTemplateId(String templateId, List<TemplateCustomFieldRequest> customFieldRequests, boolean isSystem) {
        AtomicReference<Integer> pos = new AtomicReference<>(0);
        List<TemplateCustomField> templateCustomFields = customFieldRequests.stream().map(field -> {
            TemplateCustomField templateCustomField = new TemplateCustomField();
            BeanUtils.copyProperties(field, templateCustomField);
            templateCustomField.setTemplateId(templateId);
            templateCustomField.setPos(pos.getAndSet(pos.get() + 1));
            templateCustomField.setDefaultValue(isSystem ? field.getDefaultValue().toString() : parseDefaultValue(field));
            templateCustomField.setSystemField(isSystem);
            return templateCustomField;
        }).toList();
        if (!templateCustomFields.isEmpty()) {
            mapper.insertBatch(templateCustomFields);
        }
    }

    private String parseDefaultValue(TemplateCustomFieldRequest field) {
        CustomField customField = baseCustomFieldService.getWithCheck(field.getFieldId());
        AbstractCustomFieldResolver customFieldResolver = CustomFieldResolverFactory.getResolver(customField.getType());
        CustomFieldDTO customFieldDTO = new CustomFieldDTO();
        BeanUtils.copyProperties(customField, customFieldDTO);
        customFieldDTO.setRequired(false);
        if (BooleanUtils.isNotFalse(validateDefaultValue.get())) {
            // 创建项目时不校验默认值
            customFieldResolver.validate(customFieldDTO, field.getDefaultValue());
        }
        return customFieldResolver.parse2String(field.getDefaultValue());
    }
}
