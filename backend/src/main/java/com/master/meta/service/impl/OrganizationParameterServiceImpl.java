package com.master.meta.service.impl;

import com.master.meta.constants.TemplateScene;
import com.master.meta.entity.OrganizationParameter;
import com.master.meta.mapper.OrganizationParameterMapper;
import com.master.meta.service.OrganizationParameterService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.master.meta.constants.OrganizationParameterConstants.*;
import static com.master.meta.entity.table.OrganizationParameterTableDef.ORGANIZATION_PARAMETER;

/**
 * 组织参数 服务层实现。
 *
 * @author 11's papa
 * @since 2025-11-21
 */
@Service
public class OrganizationParameterServiceImpl extends ServiceImpl<OrganizationParameterMapper, OrganizationParameter> implements OrganizationParameterService {

    @Override
    public String getValue(String orgId, String key) {
        return queryChain()
                .where(ORGANIZATION_PARAMETER.ORGANIZATION_ID.eq(orgId).and(ORGANIZATION_PARAMETER.PARAM_KEY.eq(key)))
                .oneOpt()
                .map(OrganizationParameter::getParamValue).orElse(null);
    }

    @Override
    public String getOrgTemplateEnableKeyByScene(String scene) {
        Map<String, String> sceneMap = new HashMap<>();
        sceneMap.put(TemplateScene.FUNCTIONAL.name(), ORGANIZATION_FUNCTIONAL_TEMPLATE_ENABLE_KEY);
        sceneMap.put(TemplateScene.BUG.name(), ORGANIZATION_BUG_TEMPLATE_ENABLE_KEY);
        sceneMap.put(TemplateScene.API.name(), ORGANIZATION_API_TEMPLATE_ENABLE_KEY);
        sceneMap.put(TemplateScene.UI.name(), ORGANIZATION_UI_TEMPLATE_ENABLE_KEY);
        sceneMap.put(TemplateScene.TEST_PLAN.name(), ORGANIZATION_TEST_PLAN_TEMPLATE_ENABLE_KEY);
        sceneMap.put(TemplateScene.SCHEDULE.name(), ORGANIZATION_SCHEDULE_TEMPLATE_ENABLE_KEY);
        return sceneMap.get(scene);
    }
}
