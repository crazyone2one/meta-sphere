package com.master.meta.service.impl;

import com.master.meta.config.PermissionCache;
import com.master.meta.constants.InternalUserRole;
import com.master.meta.constants.UserRoleType;
import com.master.meta.dto.permission.Permission;
import com.master.meta.dto.permission.PermissionDefinitionItem;
import com.master.meta.dto.permission.PermissionSettingUpdateRequest;
import com.master.meta.entity.UserRole;
import com.master.meta.entity.UserRolePermission;
import com.master.meta.entity.UserRoleRelation;
import com.master.meta.handle.Translator;
import com.master.meta.handle.exception.CustomException;
import com.master.meta.mapper.UserRoleMapper;
import com.master.meta.mapper.UserRolePermissionMapper;
import com.master.meta.service.BaseUserRolePermissionService;
import com.master.meta.service.BaseUserRoleRelationService;
import com.master.meta.service.BaseUserRoleService;
import com.master.meta.utils.JSON;
import com.master.meta.utils.ServiceUtils;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.master.meta.entity.table.UserRoleTableDef.USER_ROLE;
import static com.master.meta.handle.result.CommonResultCode.INTERNAL_USER_ROLE_PERMISSION;

/**
 * 用户组 服务层实现。
 *
 * @author 11's papa
 * @since 2025-10-24
 */
@Service("baseUserRoleService")
public class BaseUserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements BaseUserRoleService {
    protected final UserRolePermissionMapper userRolePermissionMapper;
    protected final BaseUserRoleRelationService baseUserRoleRelationService;
    protected final BaseUserRolePermissionService baseUserRolePermissionService;
    private final PermissionCache permissionCache;

    public BaseUserRoleServiceImpl(UserRolePermissionMapper userRolePermissionMapper,
                                   BaseUserRoleRelationService baseUserRoleRelationService,
                                   BaseUserRolePermissionService baseUserRolePermissionService, PermissionCache permissionCache) {
        this.userRolePermissionMapper = userRolePermissionMapper;
        this.baseUserRoleRelationService = baseUserRoleRelationService;
        this.baseUserRolePermissionService = baseUserRolePermissionService;
        this.permissionCache = permissionCache;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserRole add(UserRole userRole) {
        mapper.insertSelective(userRole);
        if (Objects.equals(userRole.getType(), UserRoleType.PROJECT.name())) {
            // 项目级别用户组, 初始化基本信息权限
            UserRolePermission initPermission = new UserRolePermission();
            initPermission.setRoleCode(userRole.getId());
            initPermission.setPermissionId("PROJECT_BASE_INFO:READ");
            userRolePermissionMapper.insertSelective(initPermission);
        }
        return userRole;
    }

    @Override
    public UserRole checkResourceExist(UserRole userRole) {
        return ServiceUtils.checkResourceExist(userRole, "permission.system_user_role.name");
    }

    @Override
    public void checkInternalUserRole(UserRole userRole) {
        if (BooleanUtils.isTrue(userRole.getInternal())) {
            throw new CustomException(INTERNAL_USER_ROLE_PERMISSION);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(UserRole userRole, String defaultRoleId, String currentUserId, String orgId) {
        String id = userRole.getId();
        checkInternalUserRole(userRole);
        // 删除用户组的权限设置
        baseUserRolePermissionService.deleteByRoleId(id);
        // 删除用户组
        mapper.deleteById(id);
        // 检查是否只有一个用户组，如果是则添加系统成员等默认用户组
        checkOneLimitRole(id, defaultRoleId, currentUserId, orgId);
        // 删除用户组与用户的关联关系
        baseUserRoleRelationService.deleteByRoleId(id);
    }

    @Override
    public List<PermissionDefinitionItem> getPermissionSetting(UserRole userRole) {
        // 获取该用户组拥有的权限
        Set<String> permissionIds = baseUserRolePermissionService.getPermissionIdSetByRoleId(userRole.getCode());
        // 获取所有的权限
        List<PermissionDefinitionItem> permissionDefinition = permissionCache.getPermissionDefinition();
        // 深拷贝
        permissionDefinition = JSON.parseArray(JSON.toJSONString(permissionDefinition), PermissionDefinitionItem.class);
        // 过滤该用户组级别的菜单，例如系统级别 (管理员返回所有权限位)
        permissionDefinition = permissionDefinition.stream()
                .filter(item -> Objects.equals(item.getType(), userRole.getType())
                        || Objects.equals(userRole.getCode(), InternalUserRole.ADMIN.getValue()))
                .sorted(Comparator.comparing(PermissionDefinitionItem::getOrder))
                .toList();
        for (PermissionDefinitionItem firstLevel : permissionDefinition) {
            List<PermissionDefinitionItem> children = firstLevel.getChildren();
            boolean allCheck = true;
            firstLevel.setName(Translator.get(firstLevel.getName()));
            for (PermissionDefinitionItem secondLevel : children) {
                List<Permission> permissions = secondLevel.getPermissions();
                secondLevel.setName(Translator.get(secondLevel.getName()));
                if (CollectionUtils.isEmpty(permissions)) {
                    continue;
                }
                boolean secondAllCheck = true;
                for (Permission p : permissions) {
                    if (StringUtils.isNotBlank(p.getName())) {
                        // 有 name 字段翻译 name 字段
                        p.setName(Translator.get(p.getName()));
                    } else {
                        p.setName(translateDefaultPermissionName(p));
                    }
                    // 管理员默认勾选全部二级权限位
                    if (permissionIds.contains(p.getId()) || Objects.equals(userRole.getCode(), InternalUserRole.ADMIN.getValue())) {
                        p.setEnable(true);
                    } else {
                        // 如果权限有未勾选，则二级菜单设置为未勾选
                        p.setEnable(false);
                        secondAllCheck = false;
                    }
                }
                secondLevel.setEnable(secondAllCheck);
                if (!secondAllCheck) {
                    // 如果二级菜单有未勾选，则一级菜单设置为未勾选
                    allCheck = false;
                }
            }
            firstLevel.setEnable(allCheck);
        }
        // todo 仅显示部分权限，若有需求注释掉这段代码
        // *******
        Set<String> excludeIds = Set.of("SYSTEM_PLUGIN", "SYSTEM_AUTHORIZATION_MANAGEMENT", "SYSTEM_TEST_RESOURCE_POOL", "SYSTEM_PARAMETER_SETTING");
        Set<String> excludePermissionIds = Set.of("SYSTEM_USER:READ+INVITE", "SYSTEM_ORGANIZATION_PROJECT:READ+RECOVER");
        permissionDefinition = permissionDefinition.stream().filter(item -> item.getId().equals("SYSTEM")).toList();
        permissionDefinition.forEach(p -> {
            List<PermissionDefinitionItem> children = p.getChildren().stream()
                    .filter(c -> !excludeIds.contains(c.getId()))
                    .peek(c -> {
                        List<Permission> permissions = c.getPermissions().stream()
                                .filter(permission -> !excludePermissionIds.contains(permission.getId()))
                                .toList();
                        c.setPermissions(permissions);
                    })
                    .toList();
            p.setChildren(children);
        });
        // *******
        return permissionDefinition;
    }

    @Override
    public List<UserRole> selectByUserRoleRelations(List<UserRoleRelation> userRoleRelations) {
        if (CollectionUtils.isNotEmpty(userRoleRelations)) {
            List<String> userRoleIds = userRoleRelations.stream().map(UserRoleRelation::getRoleCode).distinct().toList();
            return mapper.selectListByIds(userRoleIds);
        }
        return List.of();
    }

    @Override
    public void checkRoleIsGlobalAndHaveMember(List<String> roleIdList, boolean isSystem) {
        long count = queryChain().where(USER_ROLE.CODE.in(roleIdList))
                .and(USER_ROLE.TYPE.eq("SYSTEM").when(isSystem))
                .and(USER_ROLE.SCOPE_ID.eq("global")).count();
        if (count != roleIdList.size()) {
            throw new CustomException(Translator.get("role.not.global"));
        }
    }

    @Override
    public UserRole getWithCheck(String roleId) {
        return checkResourceExist(mapper.selectOneById(roleId));
    }

    @Override
    public UserRole getWithCheckByCode(String roleCode) {
        return checkResourceExist(queryChain().where(USER_ROLE.CODE.eq(roleCode)).one());
    }

    protected void updatePermissionSetting(PermissionSettingUpdateRequest request) {
        baseUserRolePermissionService.updatePermissionSetting(request);
    }

    private String translateDefaultPermissionName(Permission p) {
        String[] idSplit = p.getId().split(":");
        String permissionKey = idSplit[idSplit.length - 1];
        Map<String, String> translationMap = Map.of(
                "READ", "permission.read",
                "READ+ADD", "permission.add",
                "READ+UPDATE", "permission.edit",
                "READ+DELETE", "permission.delete",
                "READ+IMPORT", "permission.import",
                "READ+RECOVER", "permission.recover",
                "READ+EXPORT", "permission.export",
                "READ+EXECUTE", "permission.execute",
                "READ+DEBUG", "permission.debug"
        );
        return Translator.get(translationMap.get(permissionKey));
    }

    private void checkOneLimitRole(String roleId, String defaultRoleId, String currentUserId, String orgId) {
        // 查询要删除的用户组关联的用户ID
        List<String> userIds = baseUserRoleRelationService.getUserIdByRoleId(roleId);

        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        // 查询用户列表与所有用户组的关联关系，并分组（UserRoleRelation 中只有 userId 和 sourceId）
        Map<String, List<UserRoleRelation>> userRoleRelationMap = baseUserRoleRelationService
                .getUserIdAndSourceIdByUserIds(userIds)
                .stream()
                .collect(Collectors.groupingBy(i -> i.getUserId() + i.getSourceId()));
        List<UserRoleRelation> addRelations = new ArrayList<>();
        userRoleRelationMap.forEach((groupId, relations) -> {
            // 如果当前用户组只有一个用户，并且就是要删除的用户组，则添加组织成员等默认用户组
            if (relations.size() == 1 && Objects.equals(relations.getFirst().getRoleCode(), roleId)) {
                UserRoleRelation relation = new UserRoleRelation();
                relation.setUserId(relations.getFirst().getUserId());
                relation.setSourceId(relations.getFirst().getSourceId());
                relation.setRoleCode(defaultRoleId);
                relation.setCreateUser(currentUserId);
                relation.setOrganizationId(orgId);
                addRelations.add(relation);
            }
        });
        baseUserRoleRelationService.saveBatch(addRelations);
    }
}
