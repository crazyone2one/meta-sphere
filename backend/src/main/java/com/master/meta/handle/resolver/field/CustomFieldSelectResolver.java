package com.master.meta.handle.resolver.field;

import com.master.meta.dto.system.CustomFieldDTO;
import com.master.meta.entity.CustomFieldOption;
import com.master.meta.service.BaseCustomFieldOptionService;
import com.master.meta.utils.CommonBeanFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomFieldSelectResolver extends AbstractCustomFieldResolver {
    @Override
    public void validate(CustomFieldDTO customField, Object value) {
        validateRequired(customField, value);
        if (value == null) {
            return;
        }
        validateString(customField.getName(), value);
        if (StringUtils.isBlank((String) value)) {
            return;
        }
        List<CustomFieldOption> options = getOptions(customField.getId());
        Set<String> values = options.stream().map(CustomFieldOption::getValue).collect(Collectors.toSet());
        if (!values.contains(value)) {
            throwValidateException(customField.getName());
        }
    }

    protected List<CustomFieldOption> getOptions(String id) {
        BaseCustomFieldOptionService customFieldOptionService = CommonBeanFactory.getBean(BaseCustomFieldOptionService.class);
        assert customFieldOptionService != null;
        return customFieldOptionService.getByFieldId(id);
    }
}
