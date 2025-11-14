package com.master.meta.service;

import com.master.meta.handle.log.LogDTO;
import com.mybatisflex.core.service.IService;
import com.master.meta.entity.OperationLog;

import java.util.List;

/**
 * 操作日志 服务层。
 *
 * @author 11's papa
 * @since 2025-11-14
 */
public interface OperationLogService extends IService<OperationLog> {

    void add(LogDTO first);

    void batchAdd(List<LogDTO> logDTOList);
}
