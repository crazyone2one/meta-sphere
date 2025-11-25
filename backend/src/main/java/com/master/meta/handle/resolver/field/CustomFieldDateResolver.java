package com.master.meta.handle.resolver.field;

import com.master.meta.dto.system.CustomFieldDTO;
import com.master.meta.utils.DateFormatUtil;
import org.apache.commons.lang3.StringUtils;

public class CustomFieldDateResolver extends AbstractCustomFieldResolver{
    @Override
    public void validate(CustomFieldDTO customField, Object value) {
        validateRequired(customField, value);
        validateString(customField.getName(), value);
        try {
            if (value != null && StringUtils.isNotBlank(value.toString())) {
                DateFormatUtil.getDate(value.toString());
            }
        } catch (Exception e) {
            throwValidateException(customField.getName());
        }
    }
}
