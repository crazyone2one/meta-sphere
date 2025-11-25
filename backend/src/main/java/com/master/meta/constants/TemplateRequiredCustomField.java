package com.master.meta.constants;

import lombok.Getter;

@Getter
public enum TemplateRequiredCustomField {
    BUG_DEGREE("functional_priority");

    private final String name;

    TemplateRequiredCustomField(String name) {
        this.name = name;
    }

}
