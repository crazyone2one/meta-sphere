package com.master.meta.service;

import com.master.meta.dto.system.CustomFieldDTO;
import com.master.meta.dto.system.request.CustomFieldOptionRequest;
import com.master.meta.dto.system.request.CustomFieldRequest;
import com.master.meta.entity.CustomField;
import com.mybatisflex.core.paginate.Page;

import java.util.List;

public interface ProjectCustomFieldService extends BaseCustomFieldService {
    CustomField update(CustomField customField, List<CustomFieldOptionRequest> options);

    CustomField add(CustomField customField, List<CustomFieldOptionRequest> options);

    CustomField getWithCheck(String id);

    List<CustomFieldDTO> list(String projectId, String scene);

    Page<CustomFieldDTO> page(CustomFieldRequest request);
}
