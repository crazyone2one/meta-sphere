package com.master.meta.service;

import com.mybatisflex.core.service.IService;
import com.master.meta.entity.ProjectApplication;

/**
 * 项目应用 服务层。
 *
 * @author 11's papa
 * @since 2025-11-21
 */
public interface ProjectApplicationService extends IService<ProjectApplication> {

    ProjectApplication getByType(String projectId, String type);

    void createOrUpdateConfig(ProjectApplication projectApplication);
}
