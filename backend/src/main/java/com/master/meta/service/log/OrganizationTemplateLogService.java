package com.master.meta.service.log;

import com.master.meta.constants.OperationLogConstants;
import com.master.meta.constants.TemplateScene;
import com.master.meta.dto.system.request.TemplateUpdateRequest;
import com.master.meta.entity.Template;
import com.master.meta.handle.Translator;
import com.master.meta.handle.log.LogDTO;
import com.master.meta.handle.log.constants.OperationLogModule;
import com.master.meta.handle.log.constants.OperationLogType;
import com.master.meta.service.OrganizationTemplateService;
import com.master.meta.utils.EnumValidator;
import com.master.meta.utils.JSON;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationTemplateLogService {
    private final OrganizationTemplateService organizationTemplateService;

    public OrganizationTemplateLogService(@Qualifier("organizationTemplateService") OrganizationTemplateService organizationTemplateService) {
        this.organizationTemplateService = organizationTemplateService;
    }

    public LogDTO addLog(TemplateUpdateRequest request) {
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                null,
                null,
                null,
                OperationLogType.ADD.name(),
                getOperationLogModule(request.getScene().name()),
                request.getName());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    public String getOperationLogModule(String scene) {
        TemplateScene templateScene = EnumValidator.validateEnum(TemplateScene.class, scene);
        assert templateScene != null;
        return switch (templateScene) {
            case API -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_API_TEMPLATE;
            case FUNCTIONAL -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_FUNCTIONAL_TEMPLATE;
            case UI -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_UI_TEMPLATE;
            case BUG -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_BUG_TEMPLATE;
            case TEST_PLAN -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_TEST_PLAN_TEMPLATE;
            case SCHEDULE -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_SCHEDULE_FIELD;
        };
    }


    public LogDTO disableOrganizationTemplateLog(String organizationId, String scene) {
        return new LogDTO(
                OperationLogConstants.ORGANIZATION,
                organizationId,
                scene,
                null,
                OperationLogType.UPDATE.name(),
                getDisableOrganizationTemplateModule(scene),
                Translator.get("project_template_enable"));
    }

    public String getDisableOrganizationTemplateModule(String scene) {
        TemplateScene templateScene = EnumValidator.validateEnum(TemplateScene.class, scene);
        assert templateScene != null;
        return switch (templateScene) {
            case API -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_API;
            case FUNCTIONAL -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_FUNCTIONAL;
            case UI -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_UI;
            case BUG -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_BUG;
            case TEST_PLAN -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_TEST_PLAN;
            case SCHEDULE -> OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_SCHEDULE_FIELD;
        };
    }

    public LogDTO updateLog(TemplateUpdateRequest request) {
        Template template = organizationTemplateService.getWithCheck(request.getId());
        LogDTO dto = null;
        if (template != null) {
            dto = new LogDTO(
                    OperationLogConstants.ORGANIZATION,
                    null,
                    template.getId(),
                    null,
                    OperationLogType.UPDATE.name(),
                    getOperationLogModule(template.getScene().name()),
                    BooleanUtils.isTrue(template.getInternal()) ? Translator.get("template.default") : template.getName());
            dto.setOriginalValue(JSON.toJSONBytes(template));
        }
        return dto;
    }

    public LogDTO deleteLog(String id) {
        Template template = organizationTemplateService.getWithCheck(id);
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                null,
                template.getId(),
                null,
                OperationLogType.DELETE.name(),
                getOperationLogModule(template.getScene().name()),
                template.getName());
        dto.setOriginalValue(JSON.toJSONBytes(template));
        return dto;
    }
}
