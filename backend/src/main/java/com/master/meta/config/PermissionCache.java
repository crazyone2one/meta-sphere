package com.master.meta.config;

import com.master.meta.dto.permission.PermissionDefinitionItem;
import lombok.Data;

import java.util.List;

/**
 * @author Created by 11's papa on 2025/10/24
 */
@Data
public class PermissionCache {
    private List<PermissionDefinitionItem> permissionDefinition;
}
