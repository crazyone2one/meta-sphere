package com.master.meta.service;

import com.master.meta.dto.system.project.ProjectAddMemberRequest;
import com.master.meta.dto.system.request.ProjectUserRequest;
import com.master.meta.dto.system.user.UserExtendDTO;
import com.mybatisflex.core.paginate.Page;

public interface OrganizationProjectService extends CommonProjectService {
    Page<UserExtendDTO> getUserPage(ProjectUserRequest request);

    void orgAddProjectMember(ProjectAddMemberRequest request, String createUser);
}
