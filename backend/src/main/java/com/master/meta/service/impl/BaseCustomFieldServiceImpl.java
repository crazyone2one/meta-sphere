package com.master.meta.service.impl;

import com.master.meta.constants.CustomFieldType;
import com.master.meta.constants.TemplateRequiredCustomField;
import com.master.meta.constants.TemplateScene;
import com.master.meta.dto.system.CustomFieldDTO;
import com.master.meta.dto.system.request.CustomFieldOptionRequest;
import com.master.meta.entity.CustomField;
import com.master.meta.entity.CustomFieldOption;
import com.master.meta.handle.Translator;
import com.master.meta.handle.exception.CustomException;
import com.master.meta.mapper.CustomFieldMapper;
import com.master.meta.service.BaseCustomFieldOptionService;
import com.master.meta.service.BaseCustomFieldService;
import com.master.meta.utils.ServiceUtils;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.master.meta.entity.table.CustomFieldTableDef.CUSTOM_FIELD;
import static com.master.meta.handle.result.SystemResultCode.CUSTOM_FIELD_EXIST;
import static com.master.meta.handle.result.SystemResultCode.TEMPLATE_SCENE_ILLEGAL;

/**
 * 自定义字段 服务层实现。
 *
 * @author 11's papa
 * @since 2025-11-19
 */
@Service("baseCustomFieldService")
public class BaseCustomFieldServiceImpl extends ServiceImpl<CustomFieldMapper, CustomField> implements BaseCustomFieldService {
    protected final BaseCustomFieldOptionService baseCustomFieldOptionService;

    public BaseCustomFieldServiceImpl(BaseCustomFieldOptionService baseCustomFieldOptionService) {
        this.baseCustomFieldOptionService = baseCustomFieldOptionService;
    }

    private static final String CREATE_USER = "CREATE_USER";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomField add(CustomField customField, List<CustomFieldOptionRequest> options) {
        customField.setInternal(false);
        List<CustomFieldOption> customFieldOptions = parseCustomFieldOptionRequest2Option(options);
        return baseAdd(customField, customFieldOptions);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomField baseAdd(CustomField customField, List<CustomFieldOption> options) {
        checkAddExist(customField);
        customField.setEnableOptionKey(BooleanUtils.isTrue(customField.getEnableOptionKey()));
        mapper.insert(customField);
        baseCustomFieldOptionService.addByFieldId(customField.getId(), options);
        return customField;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomField update(CustomField customField, List<CustomFieldOptionRequest> options) {
        checkUpdateExist(customField);
        checkResourceExist(customField.getId());
        mapper.update(customField);
        if (options != null) {
            baseCustomFieldOptionService.updateByFieldId(customField.getId(), options);
        }
        return customField;
    }

    private void checkUpdateExist(CustomField customField) {
        if (StringUtils.isBlank(customField.getName())) {
            return;
        }
        val exists = queryChain()
                .where(CUSTOM_FIELD.NAME.eq(customField.getName())
                        .and(CUSTOM_FIELD.SCOPE_ID.eq(customField.getScopeId()))
                        .and(CUSTOM_FIELD.SCENE.eq(customField.getScene())))
                .and(CUSTOM_FIELD.ID.ne(customField.getId()))
                .exists();
        if (exists) {
            throw new CustomException(CUSTOM_FIELD_EXIST);
        }
    }

    @Override
    public CustomField checkResourceExist(String id) {
        return ServiceUtils.checkResourceExist(mapper.selectOneById(id), "permission.organization_custom_field.name");
    }

    @Override
    public List<CustomFieldDTO> list(String scopeId, String scene) {
        checkScene(scene);
        List<CustomField> customFields = getByScopeIdAndScene(scopeId, scene);
//        List<String> usedFieldIds = new ArrayList<>();
//        if (CollectionUtils.isNotEmpty(customFields)) {
//            usedFieldIds.addAll(selectUsedFieldIds(customFields.stream().map(CustomField::getId).toList()));
//        }
        List<CustomFieldOption> customFieldOptions = baseCustomFieldOptionService.getByFieldIds(customFields.stream().map(CustomField::getId).toList());
        Map<String, List<CustomFieldOption>> optionMap = customFieldOptions.stream().collect(Collectors.groupingBy(CustomFieldOption::getFieldId));
        return customFields.stream().map(item -> {
            CustomFieldDTO customFieldDTO = new CustomFieldDTO();
            BeanUtils.copyProperties(item, customFieldDTO);
//            if (usedFieldIds.contains(item.getId())) {
//                customFieldDTO.setUsed(true);
//            }
            customFieldDTO.setOptions(optionMap.get(item.getId()));
            if (CustomFieldType.getHasOptionValueSet().contains(customFieldDTO.getType()) && customFieldDTO.getOptions() == null) {
                customFieldDTO.setOptions(List.of());
            }
            if (Strings.CS.equalsAny(item.getType(), CustomFieldType.MEMBER.name(), CustomFieldType.MULTIPLE_MEMBER.name())) {
                // 成员选项添加默认的选项
                CustomFieldOption createUserOption = new CustomFieldOption();
                createUserOption.setFieldId(item.getId());
                createUserOption.setText(Translator.get("message.domain.createUser"));
                createUserOption.setValue(CREATE_USER);
                createUserOption.setInternal(false);
                customFieldDTO.setOptions(List.of(createUserOption));
            }
            if (BooleanUtils.isTrue(item.getInternal())) {
                // 设置哪些内置字段是模板里必选的
                Set<String> templateRequiredCustomFieldSet = Arrays.stream(TemplateRequiredCustomField.values())
                        .map(TemplateRequiredCustomField::getName)
                        .collect(Collectors.toSet());
                customFieldDTO.setTemplateRequired(templateRequiredCustomFieldSet.contains(item.getName()));
                customFieldDTO.setInternalFieldKey(item.getName());
                // 翻译内置字段名称
                customFieldDTO.setName(translateInternalField(item.getName()));
            }
            return customFieldDTO;
        }).toList();
    }

    @Override
    public List<CustomField> getByScopeIdAndScene(String scopeId, String scene) {
        return queryChain().where(CUSTOM_FIELD.SCOPE_ID.eq(scopeId).and(CUSTOM_FIELD.SCENE.eq(scene))).list();
    }

    @Override
    public String translateInternalField(String filedName) {
        return Translator.get("custom_field." + filedName);
    }

    @Override
    public boolean isOrganizationTemplateEnable(String orgId, String scene) {
        // todo 从组织模板中获取
        return true;
    }

    @Override
    public CustomField getWithCheck(String id) {
        checkResourceExist(id);
        return mapper.selectOneById(id);
    }

    @Override
    public List<CustomField> getByIds(List<String> fieldIds) {
        if (fieldIds.isEmpty()) {
            return List.of();
        }
        return mapper.selectListByIds(fieldIds);
    }

    private void checkScene(String scene) {
        Arrays.stream(TemplateScene.values()).map(TemplateScene::name)
                .filter(item -> item.equals(scene))
                .findFirst()
                .orElseThrow(() -> new CustomException(TEMPLATE_SCENE_ILLEGAL));
    }

    private void checkAddExist(CustomField customField) {
        val exists = queryChain()
                .where(CUSTOM_FIELD.NAME.eq(customField.getName())
                        .and(CUSTOM_FIELD.SCOPE_ID.eq(customField.getScopeId()))
                        .and(CUSTOM_FIELD.SCENE.eq(customField.getScene())))
                .exists();
        if (exists) {
            throw new CustomException(CUSTOM_FIELD_EXIST);
        }
    }

    private List<CustomFieldOption> parseCustomFieldOptionRequest2Option(List<CustomFieldOptionRequest> options) {
        return options == null ? null : options.stream().map(item -> {
            CustomFieldOption customFieldOption = new CustomFieldOption();
            BeanUtils.copyProperties(item, customFieldOption);
            return customFieldOption;
        }).toList();
    }
}
