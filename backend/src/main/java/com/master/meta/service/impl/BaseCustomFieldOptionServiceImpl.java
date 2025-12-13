package com.master.meta.service.impl;

import com.master.meta.dto.system.request.CustomFieldOptionRequest;
import com.master.meta.entity.CustomFieldOption;
import com.master.meta.mapper.CustomFieldOptionMapper;
import com.master.meta.service.BaseCustomFieldOptionService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 自定义字段选项 服务层实现。
 *
 * @author 11's papa
 * @since 2025-11-19
 */
@Service("baseCustomFieldOptionService")
public class BaseCustomFieldOptionServiceImpl extends ServiceImpl<CustomFieldOptionMapper, CustomFieldOption> implements BaseCustomFieldOptionService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addByFieldId(String fieldId, List<CustomFieldOption> customFieldOptions) {
        if (CollectionUtils.isEmpty(customFieldOptions)) {
            return;
        }
        customFieldOptions.forEach(item -> {
            item.setFieldId(fieldId);
            item.setInternal(BooleanUtils.isTrue(item.getInternal()));
        });
        mapper.insertBatch(customFieldOptions);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateByFieldId(String fieldId, List<CustomFieldOptionRequest> customFieldOptionRequests) {
        List<CustomFieldOption> originOptions = getByFieldId(fieldId);
        // 查询原有选项
        Map<String, CustomFieldOption> optionMap =
                originOptions.stream().collect(Collectors.toMap(CustomFieldOption::getValue, i -> i));

        // 先删除选项，再添加
        deleteByFieldId(fieldId);
        List<CustomFieldOption> customFieldOptions = customFieldOptionRequests.stream().map(item -> {
            CustomFieldOption customFieldOption = new CustomFieldOption();
            BeanUtils.copyProperties(item, customFieldOption);
            if (optionMap.get(item.getValue()) != null) {
                // 保留选项是否是内置的选项
                customFieldOption.setInternal(optionMap.get(item.getValue()).getInternal());
            } else {
                customFieldOption.setInternal(false);
            }
            customFieldOption.setFieldId(fieldId);
            return customFieldOption;
        }).toList();
        if (CollectionUtils.isNotEmpty(customFieldOptions)) {
            mapper.insertBatch(customFieldOptions);
        }
    }

    @Override
    public List<CustomFieldOption> getByFieldId(String fieldId) {
        val options = queryChain().where(CustomFieldOption::getFieldId).eq(fieldId).list();
        if (CollectionUtils.isNotEmpty(options)) {
            options.sort(Comparator.comparing(CustomFieldOption::getPos));
        }
        return options;
    }

    @Override
    public List<CustomFieldOption> getByFieldIds(List<String> fieldIds) {
        if (fieldIds.isEmpty()) {
            return List.of();
        }
        val options = queryChain().where(CustomFieldOption::getFieldId).in(fieldIds).list();
        if (CollectionUtils.isNotEmpty(options)) {
            options.sort(Comparator.comparing(CustomFieldOption::getPos));
        }
        return options;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByFieldId(String fieldId) {
        mapper.deleteByQuery(queryChain().where(CustomFieldOption::getFieldId).eq(fieldId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByFieldIds(List<String> fieldIds) {
        if (CollectionUtils.isEmpty(fieldIds)) {
            return;
        }
        mapper.deleteByQuery(queryChain().where(CustomFieldOption::getFieldId).in(fieldIds));
    }
}
