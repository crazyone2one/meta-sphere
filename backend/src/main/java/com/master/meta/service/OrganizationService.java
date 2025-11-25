package com.master.meta.service;

import com.master.meta.dto.system.OptionDTO;
import com.master.meta.dto.system.request.MemberRequest;
import com.master.meta.dto.system.request.OrganizationMemberRequest;
import com.master.meta.dto.system.user.UserExtendDTO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.master.meta.entity.Organization;

import java.util.List;
import java.util.Map;

/**
 * 组织 服务层。
 *
 * @author 11's papa
 * @since 2025-11-05
 */
public interface OrganizationService extends IService<Organization> {

    Page<UserExtendDTO> getMemberPage(MemberRequest request);

    List<OptionDTO> getUserRoleList(String organizationId);

    void addMemberBySystem(OrganizationMemberRequest request, String createUser);

    Map<String, Long> getTotal(String organizationId);
}
