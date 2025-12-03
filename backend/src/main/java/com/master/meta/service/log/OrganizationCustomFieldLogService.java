package com.master.meta.service.log;

import com.master.meta.constants.OperationLogConstants;
import com.master.meta.constants.TemplateScene;
import com.master.meta.dto.system.request.CustomFieldUpdateRequest;
import com.master.meta.entity.CustomField;
import com.master.meta.handle.log.LogDTO;
import com.master.meta.handle.log.constants.OperationLogModule;
import com.master.meta.handle.log.constants.OperationLogType;
import com.master.meta.service.OrganizationCustomFieldService;
import com.master.meta.utils.EnumValidator;
import com.master.meta.utils.JSON;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationCustomFieldLogService {
    @Resource
    private OrganizationCustomFieldService organizationCustomFieldService;

    public LogDTO addLog(CustomFieldUpdateRequest request) {
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                null,
                null,
                null,
                OperationLogType.ADD.name(),
                getOperationLogModule(request.getScene()),
                request.getName());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    public String getOperationLogModule(String scene) {
        TemplateScene templateScene = EnumValidator.validateEnum(TemplateScene.class, scene);
        assert templateScene != null;
        return switch (templateScene) {
            case API -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_API_FIELD;
            case FUNCTIONAL -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_FUNCTIONAL_FIELD;
            case UI -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_UI_FIELD;
            case BUG -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_BUG_FIELD;
            case TEST_PLAN -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_TEST_PLAN_FIELD;
            case SCHEDULE -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_SCHEDULE_FIELD;
        };
    }

    public LogDTO updateLog(CustomFieldUpdateRequest request) {
        CustomField customField = organizationCustomFieldService.getWithCheck(request.getId());
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                null,
                customField.getId(),
                null,
                OperationLogType.UPDATE.name(),
                getOperationLogModule(customField.getScene()),
                customField.getName());
        dto.setOriginalValue(JSON.toJSONBytes(customField));
        return dto;
    }

    public LogDTO deleteLog(String id) {
        CustomField customField = organizationCustomFieldService.getWithCheck(id);
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
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
