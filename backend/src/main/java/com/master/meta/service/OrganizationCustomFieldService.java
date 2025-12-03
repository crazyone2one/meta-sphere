package com.master.meta.service;

import com.master.meta.dto.system.CustomFieldDTO;
import com.master.meta.dto.system.request.CustomFieldOptionRequest;
import com.master.meta.entity.CustomField;

import java.util.List;

public interface OrganizationCustomFieldService extends BaseCustomFieldService {
    CustomField add(CustomField customField, List<CustomFieldOptionRequest> options);

    CustomField update(CustomField customField, List<CustomFieldOptionRequest> options);

    void delete(String id);

    CustomFieldDTO getCustomFieldDTOWithCheck(String id);
}
