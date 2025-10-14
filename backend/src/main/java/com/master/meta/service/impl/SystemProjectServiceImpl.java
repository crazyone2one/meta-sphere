package com.master.meta.service.impl;

import com.master.meta.dto.ProjectSwitchRequest;
import com.master.meta.entity.SystemProject;
import com.master.meta.entity.SystemUser;
import com.master.meta.handle.exception.CustomException;
import com.master.meta.mapper.SystemProjectMapper;
import com.master.meta.service.SystemProjectService;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.master.meta.entity.table.SystemProjectTableDef.SYSTEM_PROJECT;
import static com.master.meta.entity.table.SystemUserTableDef.SYSTEM_USER;

/**
 * 项目 服务层实现。
 *
 * @author 11's papa
 * @since 2025-10-13
 */
@Service
public class SystemProjectServiceImpl extends ServiceImpl<SystemProjectMapper, SystemProject> implements SystemProjectService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int add(SystemProject request, String createUser) {
        checkProjectExistByName(request);
        return mapper.insertSelective(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SystemUser switchProject(ProjectSwitchRequest request, String currentUserId) {
        if (!Strings.CS.equals(currentUserId, request.getUserId())) {
            throw new CustomException("未经授权");
        }
        if (mapper.selectOneById(request.getProjectId()) == null) {
            throw new CustomException("<项目不存在>");
        }
        UpdateChain.of(SystemUser.class).set(SYSTEM_USER.LAST_PROJECT_ID, request.getProjectId())
                .where(SYSTEM_USER.ID.eq(request.getUserId())).update();
        return QueryChain.of(SystemUser.class).where(SYSTEM_USER.ID.eq(request.getUserId())).one();
    }

    @Override
    public List<SystemProject> listProject(String organizationId) {
        return queryChain().where(SYSTEM_PROJECT.ORGANIZATION_ID.eq(organizationId)).list();
    }

    private void checkProjectExistByName(SystemProject request) {
        boolean exists = queryChain().where(SYSTEM_PROJECT.NAME.eq(request.getName())
                .and(SYSTEM_PROJECT.ORGANIZATION_ID.eq(request.getOrganizationId()))
                .and(SYSTEM_PROJECT.ID.ne(request.getId()))).exists();
        if (exists) {
            throw new CustomException("<项目名称已存在>");
        }
    }
}
