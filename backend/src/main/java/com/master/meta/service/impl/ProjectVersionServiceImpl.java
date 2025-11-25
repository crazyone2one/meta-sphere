package com.master.meta.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.master.meta.entity.ProjectVersion;
import com.master.meta.mapper.ProjectVersionMapper;
import com.master.meta.service.ProjectVersionService;
import org.springframework.stereotype.Service;

/**
 * 版本管理 服务层实现。
 *
 * @author 11's papa
 * @since 2025-11-21
 */
@Service
public class ProjectVersionServiceImpl extends ServiceImpl<ProjectVersionMapper, ProjectVersion>  implements ProjectVersionService{

}
