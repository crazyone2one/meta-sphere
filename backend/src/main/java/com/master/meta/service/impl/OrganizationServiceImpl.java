package com.master.meta.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.master.meta.entity.Organization;
import com.master.meta.mapper.OrganizationMapper;
import com.master.meta.service.OrganizationService;
import org.springframework.stereotype.Service;

/**
 * 组织 服务层实现。
 *
 * @author 11's papa
 * @since 2025-11-05
 */
@Service
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, Organization>  implements OrganizationService{

}
