package com.master.meta.service.impl;

import com.master.meta.entity.OperationHistory;
import com.master.meta.entity.OperationLog;
import com.master.meta.handle.log.LogDTO;
import com.master.meta.mapper.OperationHistoryMapper;
import com.master.meta.mapper.OperationLogMapper;
import com.master.meta.service.OperationLogService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作日志 服务层实现。
 *
 * @author 11's papa
 * @since 2025-11-14
 */
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {
    private final OperationHistoryMapper operationHistoryMapper;

    @Override
    public void add(LogDTO log) {
        if (StringUtils.isBlank(log.getProjectId())) {
            log.setProjectId("none");
        }
        if (StringUtils.isBlank(log.getCreateUser())) {
            log.setCreateUser("admin");
        }
        log.setContent(subStrContent(log.getContent()));
        mapper.insert(log);
        if (log.getHistory()) {
            operationHistoryMapper.insert(getHistory(log));
        }
    }

    private OperationHistory getHistory(LogDTO log) {
        OperationHistory history = new OperationHistory();
        BeanUtils.copyProperties(log, history);
        return history;
    }

    @Override
    public void batchAdd(List<LogDTO> logs) {
        if (CollectionUtils.isEmpty(logs)) {
            return;
        }
        logs.forEach(this::add);
    }

    private String subStrContent(String content) {
        if (StringUtils.isNotBlank(content) && content.length() > 500) {
            return content.substring(0, 499);
        }
        return content;
    }
}
