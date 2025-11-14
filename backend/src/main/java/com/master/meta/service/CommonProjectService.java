package com.master.meta.service;

import com.master.meta.dto.system.project.*;
import com.master.meta.dto.system.user.UserExtendDTO;
import com.master.meta.dto.system.user.UserRoleOptionDto;
import com.master.meta.entity.SystemProject;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import jakarta.validation.constraints.NotBlank;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Created by 11's papa on 2025/11/14
 */
public interface CommonProjectService extends IService<SystemProject> {
    ProjectDTO add(AddProjectRequest addProjectDTO, String createUser, String path, String module);

    void checkProjectNotExist(String id);

    ProjectDTO get(String id);

    List<ProjectDTO> buildUserInfo(List<ProjectDTO> projectList);

    boolean update(UpdateProjectRequest request, String updateUser, String updateProject, String settingSystemOrganization);

    int delete(String id, String deleteUser);

    void enable(String id, String updateUser);

    void disable(String id, String updateUser);

    Page<UserExtendDTO> getProjectMemberList(@Param("request") ProjectMemberRequest request);

    List<UserRoleOptionDto> selectProjectUserRoleByUserIds(List<String> userIds, @NotBlank(message = "{project.id.not_blank}") String projectId);

    void addProjectUser(ProjectAddMemberRequest request, String createUser, String path, String type, String content, String module);

    int removeProjectMember(String projectId, String userId, String createUser, String module, String path);
}
