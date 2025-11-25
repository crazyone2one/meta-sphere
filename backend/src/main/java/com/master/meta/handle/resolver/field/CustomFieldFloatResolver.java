package com.master.meta.handle.resolver.field;

import com.master.meta.dto.system.CustomFieldDTO;

public class CustomFieldFloatResolver extends AbstractCustomFieldResolver{
    @Override
    public void validate(CustomFieldDTO customField, Object value) {
        validateRequired(customField, value);
        if (value != null && !(value instanceof Number)) {
            throwValidateException(customField.getName());
        }
    }
    @Override
    public Object parse2Value(String value) {
        return value == null ? null : Float.parseFloat(value);
    }
}
