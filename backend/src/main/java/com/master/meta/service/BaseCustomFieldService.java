package com.master.meta.service;

import com.master.meta.dto.system.CustomFieldDTO;
import com.master.meta.dto.system.request.CustomFieldOptionRequest;
import com.master.meta.dto.system.request.CustomFieldRequest;
import com.master.meta.entity.CustomField;
import com.master.meta.entity.CustomFieldOption;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 自定义字段 服务层。
 *
 * @author 11's papa
 * @since 2025-11-19
 */
public interface BaseCustomFieldService extends IService<CustomField> {
    CustomField add(CustomField customField, List<CustomFieldOptionRequest> options);

    CustomField baseAdd(CustomField customField, List<CustomFieldOption> options);

    CustomField update(CustomField customField, List<CustomFieldOptionRequest> options);

    CustomField checkResourceExist(String id);

    List<CustomFieldDTO> list(String scopeId, String scene);

    List<CustomField> getByScopeIdAndScene(String scopeId, String scene);

    String translateInternalField(String filedName);

    boolean isOrganizationTemplateEnable(String orgId, String scene);

    CustomField getWithCheck(String fieldId);

    List<CustomField> getByIds(List<String> fieldIds);

    void delete(String id);

    CustomFieldDTO getCustomFieldDTOWithCheck(String id);

    Page<CustomFieldDTO> page(CustomFieldRequest request);

    List<CustomField> getByRefIdsAndScopeId(List<String> fieldIds, String scopeId);
}
