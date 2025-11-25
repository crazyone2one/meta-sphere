package com.master.meta.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.master.meta.entity.SystemScheduleConfig;
import com.master.meta.mapper.SystemScheduleConfigMapper;
import com.master.meta.service.SystemScheduleConfigService;
import org.springframework.stereotype.Service;

/**
 * 定时任务配置信息 服务层实现。
 *
 * @author 11's papa
 * @since 2025-11-18
 */
@Service
public class SystemScheduleConfigServiceImpl extends ServiceImpl<SystemScheduleConfigMapper, SystemScheduleConfig>  implements SystemScheduleConfigService{

}
