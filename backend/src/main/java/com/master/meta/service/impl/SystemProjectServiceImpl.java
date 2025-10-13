package com.master.meta.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.master.meta.entity.SystemProject;
import com.master.meta.mapper.SystemProjectMapper;
import com.master.meta.service.SystemProjectService;
import org.springframework.stereotype.Service;

/**
 * 项目 服务层实现。
 *
 * @author 11's papa
 * @since 2025-10-13
 */
@Service
public class SystemProjectServiceImpl extends ServiceImpl<SystemProjectMapper, SystemProject>  implements SystemProjectService{

}
