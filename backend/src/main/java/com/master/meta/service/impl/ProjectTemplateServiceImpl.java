package com.master.meta.service.impl;

import com.master.meta.constants.ProjectApplicationType;
import com.master.meta.constants.TemplateScene;
import com.master.meta.constants.TemplateScopeType;
import com.master.meta.dto.system.TemplateDTO;
import com.master.meta.dto.system.request.TemplateRequest;
import com.master.meta.dto.system.request.TemplateUpdateRequest;
import com.master.meta.entity.ProjectApplication;
import com.master.meta.entity.SystemProject;
import com.master.meta.entity.Template;
import com.master.meta.handle.exception.CustomException;
import com.master.meta.service.*;
import com.mybatisflex.core.paginate.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.master.meta.handle.result.CommonResultCode.DEFAULT_TEMPLATE_PERMISSION;
import static com.master.meta.handle.result.SystemResultCode.PROJECT_TEMPLATE_PERMISSION;

@Service("projectTemplateService")
public class ProjectTemplateServiceImpl extends BaseTemplateServiceImpl implements ProjectTemplateService {
    private final SystemProjectService projectService;
    private final ProjectApplicationService projectApplicationService;

    public ProjectTemplateServiceImpl(@Qualifier("systemProjectService") SystemProjectService projectService,
                                      BaseCustomFieldService baseCustomFieldService,
                                      BaseTemplateCustomFieldService baseTemplateCustomFieldService,
                                      ProjectApplicationService projectApplicationService,
                                      BaseCustomFieldOptionService baseCustomFieldOptionService) {

        super(baseTemplateCustomFieldService, baseCustomFieldService, baseCustomFieldOptionService);
        this.projectService = projectService;
        this.projectApplicationService = projectApplicationService;
    }

    @Override
    public Template add(TemplateUpdateRequest request, String creator) {
        Template template = new Template();
        BeanUtils.copyProperties(request, template);
        template.setCreateUser(creator);
        checkProjectResourceExist(template);
        checkProjectTemplateEnable(template.getScopeId(), template.getScene());
        template.setScopeType(TemplateScopeType.PROJECT.name());
        template.setRefId(null);
        template = super.add(template, request.getCustomFields(), request.getSystemFields());
        saveUploadImages(request);
        return template;
    }

    private void saveUploadImages(TemplateUpdateRequest request) {
        // todo 保存上传的图片
    }

    @Override
    public void checkProjectTemplateEnable(String projectId, String scene) {
        SystemProject project = projectService.getById(projectId);
        if (isOrganizationTemplateEnable(project.getOrganizationId(), scene)) {
            throw new CustomException(PROJECT_TEMPLATE_PERMISSION);
        }
    }

    @Override
    public void delete(String id) {
        Template template = getWithCheck(id);
        checkProjectTemplateEnable(template.getScopeId(), template.getScene());
        checkDefault(template);
        super.delete(id);
    }

    @Override
    public Template update(TemplateUpdateRequest request) {
        Template template = new Template();
        BeanUtils.copyProperties(request, template);
        Template originTemplate = super.getWithCheck(template.getId());
        if (originTemplate.getInternal()) {
            // 内置模板不能修改名字
            template.setName(null);
        }
        checkProjectTemplateEnable(originTemplate.getScopeId(), originTemplate.getScene());
        template.setScopeId(originTemplate.getScopeId());
        template.setScene(originTemplate.getScene());
        checkProjectResourceExist(originTemplate);
        template = super.update(template, request.getCustomFields(), request.getSystemFields());
        saveUploadImages(request);
        return template;
    }

    @Override
    public List<Template> list(String projectId, String scene) {
        projectService.checkResourceExist(projectId);
        List<Template> templates = super.list(projectId, scene);

        // 标记默认模板
        // 查询项目下设置中配置的默认模板
        String defaultProjectId = getDefaultTemplateId(projectId, scene);
        Template defaultTemplate = templates.stream()
                .filter(t -> Objects.equals(defaultProjectId, t.getId()))
                .findFirst()
                .orElse(null);

        // 如果查询不到默认模板，设置内置模板为默认模板
        if (defaultTemplate == null) {
            Optional<Template> internalTemplate = templates.stream()
                    .filter(Template::getInternal).findFirst();
            if (internalTemplate.isPresent()) {
                defaultTemplate = internalTemplate.get();
            }
        }
        if (defaultTemplate != null) {
            defaultTemplate.setEnableDefault(true);
        }
        return templates;
    }

    @Override
    public TemplateDTO getTemplateDTOWithCheck(String id) {
        Template template = super.getWithCheck(id);
        checkProjectResourceExist(template);
        TemplateDTO templateDTO = super.getTemplateDTO(template);
        translateInternalTemplate(List.of(templateDTO));
        return templateDTO;
    }

    @Override
    public void setDefaultTemplate(String projectId, String id) {
        Template template = mapper.selectOneById(id);
        if (template == null) {
            // 为空check抛出异常
            template = getWithCheck(id);
        }
        String paramType = ProjectApplicationType.DEFAULT_TEMPLATE.getByTemplateScene(template.getScene()).name();
        ProjectApplication projectApplication = new ProjectApplication();
        projectApplication.setProjectId(projectId);
        projectApplication.setTypeValue(id);
        projectApplication.setType(paramType);
        projectApplicationService.createOrUpdateConfig(projectApplication);
    }

    @Override
    public Map<String, Boolean> getProjectTemplateEnableConfig(String projectId) {
        SystemProject project = projectService.checkResourceExist(projectId);
        HashMap<String, Boolean> templateEnableConfig = new HashMap<>();
        List.of(TemplateScene.FUNCTIONAL, TemplateScene.BUG, TemplateScene.SCHEDULE)
                .forEach(scene ->
                        templateEnableConfig.put(scene.name(), !isOrganizationTemplateEnable(project.getOrganizationId(), scene.name())));
        return templateEnableConfig;
    }

    @Override
    public Page<Template> getTemplatePage(TemplateRequest request) {
        projectService.checkResourceExist(request.getScopedId());
        Page<Template> templates = super.templatePage(request);
        // 标记默认模板
        // 查询项目下设置中配置的默认模板
        String defaultProjectId = getDefaultTemplateId(request.getScopedId(), request.getScene());
        Template defaultTemplate = templates.getRecords().stream()
                .filter(t -> Objects.equals(defaultProjectId, t.getId()))
                .findFirst()
                .orElse(null);

        // 如果查询不到默认模板，设置内置模板为默认模板
        if (defaultTemplate == null) {
            Optional<Template> internalTemplate = templates.getRecords().stream()
                    .filter(Template::getInternal).findFirst();
            if (internalTemplate.isPresent()) {
                defaultTemplate = internalTemplate.get();
            }
        }
        if (defaultTemplate != null) {
            defaultTemplate.setEnableDefault(true);
        }
        return templates;
    }

    private void checkDefault(Template template) {
        String defaultTemplateId = getDefaultTemplateId(template.getScopeId(), template.getScene());
        if (template.getId().equals(defaultTemplateId)) {
            throw new CustomException(DEFAULT_TEMPLATE_PERMISSION);
        }
    }

    private String getDefaultTemplateId(String projectId, String scene) {
        ProjectApplicationType.DEFAULT_TEMPLATE defaultTemplateParam = ProjectApplicationType.DEFAULT_TEMPLATE.getByTemplateScene(scene);
        ProjectApplication projectApplication = projectApplicationService.getByType(projectId, defaultTemplateParam.name());
        return projectApplication == null ? null : projectApplication.getTypeValue();
    }

    private void checkProjectResourceExist(Template template) {
        projectService.checkResourceExist(template.getScopeId());
    }
}
