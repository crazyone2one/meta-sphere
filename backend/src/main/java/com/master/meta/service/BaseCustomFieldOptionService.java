package com.master.meta.service;

import com.master.meta.dto.system.request.CustomFieldOptionRequest;
import com.master.meta.entity.CustomFieldOption;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 自定义字段选项 服务层。
 *
 * @author 11's papa
 * @since 2025-11-19
 */
public interface BaseCustomFieldOptionService extends IService<CustomFieldOption> {

    void addByFieldId(String fieldId, List<CustomFieldOption> customFieldOptions);

    void updateByFieldId(String fieldId, List<CustomFieldOptionRequest> customFieldOptionRequests);

    List<CustomFieldOption> getByFieldId(String fieldId);

    List<CustomFieldOption> getByFieldIds(List<String> fieldIds);

    void deleteByFieldId(String fieldId);

    void deleteByFieldIds(List<String> fieldIds);
}
