package com.master.meta.service;

import com.master.meta.dto.ProjectSwitchRequest;
import com.master.meta.dto.system.OptionDTO;
import com.master.meta.dto.system.project.*;
import com.master.meta.dto.system.user.UserExtendDTO;
import com.master.meta.entity.SystemProject;
import com.master.meta.entity.SystemUser;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * 项目 服务层。
 *
 * @author 11's papa
 * @since 2025-10-13
 */
public interface SystemProjectService extends CommonProjectService {
    int add(SystemProject request, String createUser);

    ProjectDTO add(AddProjectRequest addProjectDTO, String createUser);

    SystemUser switchProject(ProjectSwitchRequest request, String currentUserId);

    List<SystemProject> listProject(String organizationId);

    SystemProject checkProjectExit(String projectId);

    ProjectDTO get(@NotBlank String id);

    Page<ProjectDTO> getProjectPage(ProjectRequest request);

    boolean update(UpdateProjectRequest request, String updateUser);

    void delete(String id, String userName);

    void enable(String id, String updateUser);
    void disable(String id, String updateUser);

    Page<UserExtendDTO> getProjectMember(ProjectMemberRequest request);

    void addMemberByProject(ProjectAddMemberRequest request, String createUser);

    int removeProjectMember(String projectId, String userId, String userName);

    List<SystemUser> getUserList(String keyword);

    void rename(UpdateProjectNameRequest request, String userName);

    List<OptionDTO> listOptions(String keyword);

    SystemProject checkResourceExist(String id);

    void revoke(String id, String userName);
}
