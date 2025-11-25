package com.master.meta.handle.resolver.field;

import com.master.meta.dto.system.CustomFieldDTO;

public class CustomFieldTextResolver extends AbstractCustomFieldResolver{
    @Override
    public void validate(CustomFieldDTO customField, Object value) {
        validateRequired(customField, value);
        validateString(customField.getName(), value);
    }
}
