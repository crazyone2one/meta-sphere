package com.master.meta.handle.resolver.field;

import com.master.meta.dto.system.CustomFieldDTO;
import com.master.meta.utils.DateFormatUtil;
import org.apache.commons.lang3.StringUtils;

public class CustomFieldDateTimeResolver extends AbstractCustomFieldResolver{
    @Override
    public void validate(CustomFieldDTO customField, Object value) {
        validateRequired(customField, value);
        try {
            if (value != null && StringUtils.isNotBlank(value.toString())) {
                DateFormatUtil.getTime(value.toString());
            }
        } catch (Exception e) {
            throwValidateException(customField.getName());
        }
    }
}
