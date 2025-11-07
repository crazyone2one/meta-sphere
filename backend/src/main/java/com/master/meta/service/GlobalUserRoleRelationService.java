package com.master.meta.service;

import com.master.meta.dto.system.UserExcludeOptionDTO;
import com.master.meta.dto.system.UserRoleRelationUpdateRequest;

import java.util.List;

/**
 * @author Created by 11's papa on 2025/11/7
 */
public interface GlobalUserRoleRelationService extends BaseUserRoleRelationService{
    List<UserExcludeOptionDTO> getExcludeSelectOption(String roleCode, String keyword);

    void add(UserRoleRelationUpdateRequest request);
}
