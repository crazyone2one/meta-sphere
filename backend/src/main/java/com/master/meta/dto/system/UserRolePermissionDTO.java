package com.master.meta.dto.system;

import com.master.meta.entity.UserRole;
import com.master.meta.entity.UserRoleRelation;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by 11's papa on 2025/11/5
 */
@Data
public class UserRolePermissionDTO {
    List<UserRoleResourceDTO> list = new ArrayList<>();
    List<UserRole> userRoles = new ArrayList<>();
    List<UserRoleRelation> userRoleRelations = new ArrayList<>();
}
