package com.master.meta.constants;

import java.util.Arrays;

public class ProjectApplicationType {
    public enum DEFAULT_TEMPLATE {
        FUNCTIONAL_DEFAULT_TEMPLATE,
        BUG_DEFAULT_TEMPLATE,
        API_DEFAULT_TEMPLATE,
        UI_DEFAULT_TEMPLATE,
        TEST_PLAN_DEFAULT_TEMPLATE,
        SCHEDULE_DEFAULT_TEMPLATE;

        public static DEFAULT_TEMPLATE getByTemplateScene(String scene) {
            return Arrays.stream(DEFAULT_TEMPLATE.values())
                    .filter(e -> e.name().startsWith(scene))
                    .findFirst()
                    .orElse(null);
        }
    }
}
