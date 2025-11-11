package com.master.meta.dto.system;

import lombok.Data;

/**
 * @author Created by 11's papa on 2025/11/10
 */
@Data
public class UserResponseDTO {
    private String id;
    private String name;
    private String email;
    private String phone;
    private Boolean enable;
}
