package com.master.meta.service.impl;

import com.master.meta.entity.ProjectApplication;
import com.master.meta.mapper.ProjectApplicationMapper;
import com.master.meta.service.ProjectApplicationService;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.master.meta.entity.table.ProjectApplicationTableDef.PROJECT_APPLICATION;

/**
 * 项目应用 服务层实现。
 *
 * @author 11's papa
 * @since 2025-11-21
 */
@Service
public class ProjectApplicationServiceImpl extends ServiceImpl<ProjectApplicationMapper, ProjectApplication> implements ProjectApplicationService {

    @Override
    public ProjectApplication getByType(String projectId, String type) {
        List<ProjectApplication> list = queryChain().where(PROJECT_APPLICATION.PROJECT_ID.eq(projectId).and(PROJECT_APPLICATION.TYPE.eq(type))).list();
        return !list.isEmpty() ? list.getFirst() : null;
    }

    @Override
    public void createOrUpdateConfig(ProjectApplication projectApplication) {
        QueryChain<ProjectApplication> queryChain = queryChain().where(PROJECT_APPLICATION.PROJECT_ID.eq(projectApplication.getProjectId()).and(PROJECT_APPLICATION.TYPE.eq(projectApplication.getType())));
        if (queryChain.exists()) {
            mapper.updateByQuery(projectApplication, queryChain);
        } else {
            mapper.insertSelective(projectApplication);
        }
    }
}
