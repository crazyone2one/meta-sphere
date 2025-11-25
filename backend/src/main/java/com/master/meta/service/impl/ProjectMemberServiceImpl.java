package com.master.meta.service.impl;

import com.master.meta.constants.UserRoleEnum;
import com.master.meta.constants.UserRoleType;
import com.master.meta.dto.system.OptionDTO;
import com.master.meta.entity.UserRole;
import com.master.meta.service.ProjectMemberService;
import com.mybatisflex.core.query.QueryChain;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static com.master.meta.entity.table.UserRoleTableDef.USER_ROLE;

@Service
public class ProjectMemberServiceImpl implements ProjectMemberService {
    @Override
    public List<OptionDTO> getRoleOption(String projectId) {
        List<UserRole> userRoles = QueryChain.of(UserRole.class)
                .where(USER_ROLE.SCOPE_ID.in(Arrays.asList(projectId, UserRoleEnum.GLOBAL.toString()))
                        .and(USER_ROLE.TYPE.eq(UserRoleType.PROJECT.toString()))).list();
        return userRoles.stream().map(userRole -> new OptionDTO(userRole.getCode(), userRole.getName())).toList();
    }
}
