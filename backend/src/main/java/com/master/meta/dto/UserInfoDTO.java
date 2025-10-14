package com.master.meta.dto;

import lombok.Data;

/**
 * @author Created by 11's papa on 2025/10/14
 */
@Data
public class UserInfoDTO {
    private String id;
    private String name;
    private String email;
    private String lastOrganizationId;
    private String lastProjectId;
}
