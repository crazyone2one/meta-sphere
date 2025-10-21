package com.master.meta.service;

import com.master.meta.dto.ProjectSwitchRequest;
import com.master.meta.entity.SystemUser;
import com.mybatisflex.core.service.IService;
import com.master.meta.entity.SystemProject;

import java.util.List;

/**
 * 项目 服务层。
 *
 * @author 11's papa
 * @since 2025-10-13
 */
public interface SystemProjectService extends IService<SystemProject> {
    int add(SystemProject request, String createUser);

    SystemUser switchProject(ProjectSwitchRequest request, String currentUserId);

    List<SystemProject> listProject(String organizationId);

    SystemProject checkProjectExit(String projectId);
}
