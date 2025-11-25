package com.master.meta.service.log;

import com.alibaba.excel.util.BooleanUtils;
import com.master.meta.constants.TemplateScene;
import com.master.meta.dto.system.request.TemplateUpdateRequest;
import com.master.meta.entity.Template;
import com.master.meta.handle.Translator;
import com.master.meta.handle.log.LogDTO;
import com.master.meta.handle.log.constants.OperationLogModule;
import com.master.meta.handle.log.constants.OperationLogType;
import com.master.meta.service.ProjectTemplateService;
import com.master.meta.utils.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectTemplateLogService {
    private final ProjectTemplateService projectTemplateService;

    public ProjectTemplateLogService(@Qualifier("projectTemplateService") ProjectTemplateService projectTemplateService) {
        this.projectTemplateService = projectTemplateService;
    }

    public LogDTO addLog(TemplateUpdateRequest request) {
        LogDTO dto = new LogDTO(
                null,
                null,
                null,
                null,
                OperationLogType.ADD.name(),
                getOperationLogModule(request.getScene()),
                request.getName());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    public String getOperationLogModule(TemplateScene scene) {
//        TemplateScene templateScene = EnumValidator.validateEnum(TemplateScene.class, scene);
        return switch (scene) {
            case API -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_API_TEMPLATE;
            case FUNCTIONAL -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_FUNCTIONAL_TEMPLATE;
            case UI -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_UI_TEMPLATE;
            case BUG -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_BUG_TEMPLATE;
            case TEST_PLAN -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_TEST_PLAN_TEMPLATE;
            case SCHEDULE -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_SCHEDULE_TEMPLATE;
        };
    }

    public LogDTO updateLog(TemplateUpdateRequest request) {
        Template template = projectTemplateService.getWithCheck(request.getId());
        LogDTO dto = null;
        if (template != null) {
            dto = new LogDTO(
                    null,
                    null,
                    template.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    getOperationLogModule(template.getScene()),
                    BooleanUtils.isTrue(template.getInternal()) ? Translator.get("template.default") : template.getName());
            dto.setOriginalValue(JSON.toJSONBytes(template));
        }
        return dto;
    }

    public LogDTO setDefaultTemplateLog(String id) {
        Template template = projectTemplateService.getWithCheck(id);
        LogDTO dto = null;
        if (template != null) {
            dto = new LogDTO(
                    null,
                    null,
                    template.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    getOperationLogModule(template.getScene()),
                    StringUtils.join(Translator.get("set_default_template"), ":",
                            template.getInternal() ? projectTemplateService.translateInternalTemplate() : template.getName()));
            dto.setOriginalValue(JSON.toJSONBytes(template));
        }
        return dto;
    }

    public LogDTO deleteLog(String id) {
        Template template = projectTemplateService.getWithCheck(id);
        LogDTO dto = new LogDTO(
                null,
                null,
                template.getId(),
                null,
                OperationLogType.DELETE.name(),
                getOperationLogModule(template.getScene()),
                template.getName());
        dto.setOriginalValue(JSON.toJSONBytes(template));
        return dto;
    }
}
