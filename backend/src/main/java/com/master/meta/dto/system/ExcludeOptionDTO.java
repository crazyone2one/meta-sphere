package com.master.meta.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Created by 11's papa on 2025/11/7
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExcludeOptionDTO extends OptionDTO {
    @Schema(description =  "是否已经关联过")
    private Boolean disabled = false;
}
