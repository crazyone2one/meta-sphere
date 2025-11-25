package com.master.meta.service.log;

import com.master.meta.constants.TemplateScene;
import com.master.meta.dto.system.request.CustomFieldUpdateRequest;
import com.master.meta.entity.CustomField;
import com.master.meta.handle.log.LogDTO;
import com.master.meta.handle.log.constants.OperationLogModule;
import com.master.meta.handle.log.constants.OperationLogType;
import com.master.meta.service.ProjectCustomFieldService;
import com.master.meta.utils.EnumValidator;
import com.master.meta.utils.JSON;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectCustomFieldLogService {
    private final ProjectCustomFieldService projectCustomFieldService;

    public ProjectCustomFieldLogService(@Qualifier("projectCustomFieldService") ProjectCustomFieldService projectCustomFieldService) {
        this.projectCustomFieldService = projectCustomFieldService;
    }

    public LogDTO addLog(CustomFieldUpdateRequest request) {
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

    public LogDTO updateLog(CustomFieldUpdateRequest request) {
        CustomField customField = projectCustomFieldService.getWithCheck(request.getId());
        LogDTO dto = new LogDTO(
                null,
                null,
                customField.getId(),
                null,
                OperationLogType.UPDATE.name(),
                getOperationLogModule(customField.getScene()),
                customField.getName());
        dto.setOriginalValue(JSON.toJSONBytes(customField));
        return dto;
    }

    public String getOperationLogModule(String scene) {
        TemplateScene templateScene = EnumValidator.validateEnum(TemplateScene.class, scene);
        assert templateScene != null;
        return switch (templateScene) {
            case API -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_API_FIELD;
            case FUNCTIONAL -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_FUNCTIONAL_FIELD;
            case UI -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_UI_FIELD;
            case BUG -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_BUG_FIELD;
            case TEST_PLAN -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_TEST_PLAN_FIELD;
            case SCHEDULE -> OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_SCHEDULE_FIELD;
        };
    }
    public LogDTO deleteLog(String id) {
        CustomField customField = projectCustomFieldService.getWithCheck(id);
        LogDTO dto = new LogDTO(
                null,
                null,
                customField.getId(),
                null,
                OperationLogType.DELETE.name(),
                getOperationLogModule(customField.getScene()),
                customField.getName());
        dto.setOriginalValue(JSON.toJSONBytes(customField));
        return dto;
    }
}
