package com.master.meta.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.master.meta.entity.OperationHistory;
import com.master.meta.mapper.OperationHistoryMapper;
import com.master.meta.service.OperationHistoryService;
import org.springframework.stereotype.Service;

/**
 * 变更记录 服务层实现。
 *
 * @author 11's papa
 * @since 2025-11-14
 */
@Service
public class OperationHistoryServiceImpl extends ServiceImpl<OperationHistoryMapper, OperationHistory>  implements OperationHistoryService{

}
