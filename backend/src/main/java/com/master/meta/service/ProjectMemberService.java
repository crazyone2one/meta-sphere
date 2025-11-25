package com.master.meta.service;

import com.master.meta.dto.system.OptionDTO;

import java.util.List;

public interface ProjectMemberService {
    List<OptionDTO> getRoleOption(String projectId);
}
