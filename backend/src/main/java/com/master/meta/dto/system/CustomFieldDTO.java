package com.master.meta.dto.system;

import com.master.meta.entity.CustomField;
import com.master.meta.entity.CustomFieldOption;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class CustomFieldDTO extends CustomField {
    private List<CustomFieldOption> options;
    /**
     * 是否被模板使用
     */
    private Boolean used = false;
    /**
     * 模板中该字段是否必选
     */
    private Boolean templateRequired = false;
    /**
     * 内置字段的 key
     */
    private String internalFieldKey;
    private Boolean required;

    private String defaultValue;

    private Object value;
}
