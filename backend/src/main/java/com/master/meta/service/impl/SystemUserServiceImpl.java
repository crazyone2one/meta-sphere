package com.master.meta.service.impl;

import com.master.meta.constants.UserRoleType;
import com.master.meta.dto.BasePageRequest;
import com.master.meta.dto.TableBatchProcessDTO;
import com.master.meta.dto.system.*;
import com.master.meta.entity.*;
import com.master.meta.handle.Translator;
import com.master.meta.handle.exception.CustomException;
import com.master.meta.handle.result.SystemResultCode;
import com.master.meta.mapper.SystemUserMapper;
import com.master.meta.service.BaseUserRoleRelationService;
import com.master.meta.service.BaseUserRoleService;
import com.master.meta.service.SystemUserService;
import com.master.meta.utils.JSON;
import com.master.meta.utils.SessionUtils;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.validation.Valid;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.master.meta.entity.table.OrganizationTableDef.ORGANIZATION;
import static com.master.meta.entity.table.SystemProjectTableDef.SYSTEM_PROJECT;
import static com.master.meta.entity.table.SystemUserTableDef.SYSTEM_USER;
import static com.master.meta.entity.table.UserRolePermissionTableDef.USER_ROLE_PERMISSION;
import static com.master.meta.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
import static com.master.meta.entity.table.UserRoleTableDef.USER_ROLE;

/**
 * 用户 服务层实现。
 *
 * @author 11's papa
 * @since 2025-10-11
 */
@Service
public class SystemUserServiceImpl extends ServiceImpl<SystemUserMapper, SystemUser> implements SystemUserService {
    private final BaseUserRoleRelationService userRoleRelationService;
    private final BaseUserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;

    public SystemUserServiceImpl(@Qualifier("baseUserRoleRelationService") BaseUserRoleRelationService userRoleRelationService,
                                 @Qualifier("baseUserRoleService") BaseUserRoleService userRoleService,
                                 PasswordEncoder passwordEncoder) {
        this.userRoleRelationService = userRoleRelationService;
        this.userRoleService = userRoleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDTO getUserInfo() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        UserDTO user = queryChain().where(SYSTEM_USER.NAME.eq(name)).oneAs(UserDTO.class);
        autoSwitch(user);
        return user;
    }

    @Override
    public UserDTO getUserDTO(String userId) {
        UserDTO userDTO = mapper.selectOneWithRelationsByIdAs(userId, UserDTO.class);
        if (userDTO == null) {
            return null;
        }
        UserRolePermissionDTO dto = getUserRolePermission(userId);
        userDTO.setUserRoleRelations(dto.getUserRoleRelations());
        userDTO.setUserRoles(dto.getUserRoles());
        userDTO.setUserRolePermissions(dto.getList());
        return userDTO;
    }

    private UserRolePermissionDTO getUserRolePermission(String userId) {
        UserRolePermissionDTO permissionDTO = new UserRolePermissionDTO();
        List<UserRoleResourceDTO> list = new ArrayList<>();
        List<UserRoleRelation> userRoleRelations = userRoleRelationService.selectByUserId(userId);
        if (CollectionUtils.isEmpty(userRoleRelations)) {
            return permissionDTO;
        }
        permissionDTO.setUserRoleRelations(userRoleRelations);
        List<String> roleList = userRoleRelations.stream().map(UserRoleRelation::getRoleCode).toList();
        List<UserRole> userRoles = QueryChain.of(UserRole.class).where(USER_ROLE.CODE.in(roleList)).list();
        permissionDTO.setUserRoles(userRoles);
        for (UserRole gp : userRoles) {
            UserRoleResourceDTO dto = new UserRoleResourceDTO();
            dto.setUserRole(gp);
            List<UserRolePermission> userRolePermissions = QueryChain.of(UserRolePermission.class)
                    .where(USER_ROLE_PERMISSION.ROLE_CODE.eq(gp.getCode()))
                    .list();
            dto.setUserRolePermissions(userRolePermissions);
            list.add(dto);
        }
        permissionDTO.setList(list);
        return permissionDTO;
    }

    private void autoSwitch(UserDTO user) {
        // 判断是否是系统管理员
        if (isSystemAdmin(user)) {
            return;
        }
        // 用户有 last_project_id 权限
        if (hasLastProjectPermission(user)) {
            return;
        }
        // 用户有 last_organization_id 权限
        if (hasLastOrganizationPermission(user)) {
            return;
        }
        // 判断其他权限
        checkNewOrganizationAndProject(user);
    }

    private void checkNewOrganizationAndProject(UserDTO user) {
        List<UserRoleRelation> userRoleRelations = user.getUserRoleRelations();
        List<String> projectRoleIds = user.getUserRoles()
                .stream().filter(ug -> Objects.equals(ug.getType(), UserRoleType.PROJECT.name()))
                .map(UserRole::getCode)
                .toList();
        List<UserRoleRelation> project = userRoleRelations.stream().filter(ug -> projectRoleIds.contains(ug.getRoleCode()))
                .toList();
        if (CollectionUtils.isEmpty(project)) {
            List<String> organizationIds = user.getUserRoles()
                    .stream()
                    .filter(ug -> Objects.equals(ug.getType(), UserRoleType.ORGANIZATION.name()))
                    .map(UserRole::getCode)
                    .toList();
            List<UserRoleRelation> organizations = userRoleRelations.stream().filter(ug -> organizationIds.contains(ug.getRoleCode()))
                    .toList();
            if (CollectionUtils.isNotEmpty(organizations)) {
                //获取所有的组织
                List<String> orgIds = organizations.stream().map(UserRoleRelation::getSourceId).toList();
                List<Organization> organizationsList = QueryChain.of(Organization.class).where(ORGANIZATION.ID.in(orgIds)
                        .and(ORGANIZATION.ENABLE.eq(true))).list();
                if (org.apache.commons.collections.CollectionUtils.isNotEmpty(organizationsList)) {
                    String wsId = organizationsList.getFirst().getId();
                    switchUserResource(wsId, user);
                }
            } else {
                // 用户登录之后没有项目和组织的权限就把值清空
                user.setLastOrganizationId(StringUtils.EMPTY);
                user.setLastProjectId(StringUtils.EMPTY);
                updateUser(user);
            }
        } else {
            UserRoleRelation userRoleRelation = project.stream().filter(p -> StringUtils.isNotBlank(p.getSourceId()))
                    .toList().getFirst();
            String projectId = userRoleRelation.getSourceId();
            SystemProject p = QueryChain.of(SystemProject.class).where(SYSTEM_PROJECT.ID.eq(projectId)).one();
            String wsId = p.getOrganizationId();
            user.setId(user.getId());
            user.setLastProjectId(projectId);
            user.setLastOrganizationId(wsId);
            updateUser(user);
        }
    }

    private void switchUserResource(String sourceId, UserDTO user) {
        UserDTO userDTO = getUserDTO(user.getId());
        SystemUser newUser = new SystemUser();
        userDTO.setLastOrganizationId(sourceId);
        userDTO.setLastProjectId(StringUtils.EMPTY);
        List<SystemProject> projects = getProjectListByWsAndUserId(user.getId(), sourceId);
        if (CollectionUtils.isNotEmpty(projects)) {
            userDTO.setLastProjectId(projects.getFirst().getId());
        }
        BeanUtils.copyProperties(userDTO, newUser);
        mapper.insertOrUpdateSelective(newUser);
    }

    private boolean hasLastOrganizationPermission(UserDTO user) {
        if (StringUtils.isNotBlank(user.getLastOrganizationId())) {
            List<Organization> organizations = QueryChain.of(Organization.class).where(ORGANIZATION.ID.eq(user.getLastOrganizationId())
                    .and(ORGANIZATION.ENABLE.eq(true))).list();
            if (org.apache.commons.collections.CollectionUtils.isEmpty(organizations)) {
                return false;
            }
            List<UserRoleRelation> userRoleRelations = user.getUserRoleRelations().stream()
                    .filter(ug -> Objects.equals(user.getLastOrganizationId(), ug.getSourceId()))
                    .toList();
            if (CollectionUtils.isNotEmpty(userRoleRelations)) {
                List<SystemProject> projects = QueryChain.of(SystemProject.class)
                        .where(SYSTEM_PROJECT.ORGANIZATION_ID.eq(user.getLastOrganizationId())
                                .and(SYSTEM_PROJECT.ENABLE.eq(true))).list();
                // 组织下没有项目
                if (CollectionUtils.isEmpty(projects)) {
                    user.setLastProjectId(StringUtils.EMPTY);
                    updateUser(user);
                    return true;
                }
                // 组织下有项目，选中有权限的项目
                List<String> projectIds = projects.stream()
                        .map(SystemProject::getId)
                        .toList();

                List<UserRoleRelation> roleRelations = user.getUserRoleRelations();
                List<String> projectRoleIds = user.getUserRoles()
                        .stream().filter(ug -> Objects.equals(ug.getType(), UserRoleType.PROJECT.name()))
                        .map(UserRole::getCode)
                        .toList();
                List<String> projectIdsWithPermission = roleRelations.stream().filter(ug -> projectRoleIds.contains(ug.getRoleCode()))
                        .map(UserRoleRelation::getSourceId)
                        .filter(StringUtils::isNotBlank)
                        .filter(projectIds::contains)
                        .toList();

                List<String> intersection = projectIds.stream().filter(projectIdsWithPermission::contains).toList();
                // 当前组织下的所有项目都没有权限
                if (org.apache.commons.collections.CollectionUtils.isEmpty(intersection)) {
                    user.setLastProjectId(StringUtils.EMPTY);
                    updateUser(user);
                    return true;
                }
                Optional<SystemProject> first = projects.stream().filter(p -> Objects.equals(intersection.getFirst(), p.getId())).findFirst();
                if (first.isPresent()) {
                    SystemProject project = first.get();
                    String wsId = project.getOrganizationId();
                    user.setId(user.getId());
                    user.setLastProjectId(project.getId());
                    user.setLastOrganizationId(wsId);
                    updateUser(user);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasLastProjectPermission(UserDTO user) {
        if (StringUtils.isNotBlank(user.getLastProjectId())) {
            List<UserRoleRelation> userRoleRelations = user.getUserRoleRelations().stream()
                    .filter(ug -> Objects.equals(user.getLastProjectId(), ug.getSourceId()))
                    .toList();
            if (CollectionUtils.isNotEmpty(userRoleRelations)) {
                List<SystemProject> projects = QueryChain.of(SystemProject.class)
                        .where(SYSTEM_PROJECT.ID.eq(user.getLastProjectId())
                                .and(SYSTEM_PROJECT.ENABLE.eq(true))).list();
                if (org.apache.commons.collections.CollectionUtils.isNotEmpty(projects)) {
                    SystemProject project = projects.getFirst();
                    if (Objects.equals(project.getOrganizationId(), user.getLastOrganizationId())) {
                        return true;
                    }
                    // last_project_id 和 last_organization_id 对应不上了
                    user.setLastOrganizationId(project.getOrganizationId());
                    updateUser(user);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSystemAdmin(UserDTO user) {
        if (isSuperUser(user.getId())) {
            // 如果是系统管理员，判断是否有项目权限
            if (StringUtils.isNotBlank(user.getLastProjectId())) {
                List<SystemProject> projects = QueryChain.of(SystemProject.class).where(SYSTEM_PROJECT.ID.eq(user.getLastProjectId())
                        .and(SYSTEM_PROJECT.ENABLE.eq(true))).list();
                if (CollectionUtils.isNotEmpty(projects)) {
                    SystemProject project = projects.getFirst();
                    if (Objects.equals(project.getOrganizationId(), user.getLastOrganizationId())) {
                        return true;
                    }
                    // last_project_id 和 last_organization_id 对应不上了
                    user.setLastOrganizationId(project.getOrganizationId());
                    updateUser(user);
                    return true;
                }
            }
            // 项目没有权限  则取当前组织下的第一个项目
            if (StringUtils.isNotBlank(user.getLastOrganizationId())) {
                List<Organization> organizations = QueryChain.of(Organization.class).where(ORGANIZATION.ID.eq(user.getLastOrganizationId())
                        .and(ORGANIZATION.ENABLE.eq(true))).list();
                if (CollectionUtils.isNotEmpty(organizations)) {
                    Organization organization = organizations.getFirst();
                    List<SystemProject> projects = QueryChain.of(SystemProject.class)
                            .where(SYSTEM_PROJECT.ORGANIZATION_ID.eq(organization.getId())
                                    .and(SYSTEM_PROJECT.ENABLE.eq(true))).list();
                    if (CollectionUtils.isNotEmpty(projects)) {
                        SystemProject project = projects.getFirst();
                        user.setLastProjectId(project.getId());
                        updateUser(user);
                        return true;
                    } else {
                        // 组织下无项目, 走前端逻辑, 跳转到无项目的路由
                        updateUser(user);
                        return true;
                    }
                }
            }
            //项目和组织都没有权限
            SystemProject project = getEnableProjectAndOrganization();
            if (project != null) {
                user.setLastProjectId(project.getId());
                user.setLastOrganizationId(project.getOrganizationId());
                updateUser(user);
                return true;
            }
            return true;
        }
        return false;
    }

    private boolean isSuperUser(String id) {
        return QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.eq(id)
                .and(USER_ROLE_RELATION.ROLE_CODE.eq("admin"))).exists();
    }

    private SystemProject getEnableProjectAndOrganization() {
        return QueryChain.of(SystemProject.class).select(SYSTEM_PROJECT.ALL_COLUMNS)
                .from(SYSTEM_PROJECT).leftJoin(ORGANIZATION).on(ORGANIZATION.ID.eq(SYSTEM_PROJECT.ORGANIZATION_ID))
                .where(SYSTEM_PROJECT.ENABLE.eq(true).and(ORGANIZATION.ENABLE.eq(true)))
                .one();
    }

    @Override
    public UserDTO getUserDTOByKeyword(String keyword) {
        UserDTO userDTO = queryChain().where(SYSTEM_USER.EMAIL.like(keyword).or(SYSTEM_USER.NAME.like(keyword))).oneAs(UserDTO.class);
        if (userDTO != null) {
            userDTO.setUserRoleRelations(userRoleRelationService.selectByUserId(userDTO.getId()));
            userDTO.setUserRoles(userRoleService.selectByUserRoleRelations(userDTO.getUserRoleRelations()));
        }
        return userDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserBatchCreateResponse addUser(UserBatchCreateRequest request, String source, String operator) {
        userRoleService.checkRoleIsGlobalAndHaveMember(request.getUserRoleIdList(), false);
        UserBatchCreateResponse response = new UserBatchCreateResponse();
        //检查用户邮箱的合法性
        Map<String, String> errorEmails = validateUserInfo(request.getUserInfoList().stream().map(UserCreateInfo::getEmail).toList());
        if (MapUtils.isNotEmpty(errorEmails)) {
            response.setErrorEmails(errorEmails);
            throw new CustomException(SystemResultCode.INVITE_EMAIL_EXIST, JSON.toJSONString(errorEmails.keySet()));
        } else {
            response.setSuccessList(saveUserAndRole(request, source, operator, "/system/user/addUser"));
        }
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserEditRequest updateUser(UserEditRequest request) {
        userRoleService.checkRoleIsGlobalAndHaveMember(request.getUserRoleIdList(), true);
        //检查用户邮箱的合法性
        checkUserEmail(request.getId(), request.getEmail());
        SystemUser user = new SystemUser();
        BeanUtils.copyProperties(request, user);
        user.setUpdateUser(SessionUtils.getUserName());
        mapper.insertOrUpdateSelective(user);
        userRoleRelationService.updateUserSystemGlobalRole(user, user.getUpdateUser(), request.getUserRoleIdList());
        return request;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SystemUser user) {
        if (StringUtils.isNotBlank(user.getEmail())) {
            boolean exists = queryChain().where(SYSTEM_USER.EMAIL.eq(user.getEmail()).and(SYSTEM_USER.ID.ne(user.getId()))).exists();
            if (exists) {
                throw new CustomException(Translator.get("user_email_already_exists"));
            }
        }
        SystemUser userFromDB = mapper.selectOneById(user.getId());
        if (user.getLastOrganizationId() != null && !Objects.equals(user.getLastOrganizationId(), userFromDB.getLastOrganizationId())
                && !isSuperUser(user.getId())) {
            List<SystemProject> projects = getProjectListByWsAndUserId(user.getId(), user.getLastOrganizationId());
            if (!projects.isEmpty()) {
                // 如果传入的 last_project_id 是 last_organization_id 下面的
                boolean present = projects.stream().anyMatch(p -> Objects.equals(p.getId(), user.getLastProjectId()));
                if (!present) {
                    user.setLastProjectId(projects.getFirst().getId());
                }
            } else {
                user.setLastProjectId(StringUtils.EMPTY);
            }
        }
        mapper.insertOrUpdateSelective(user);
    }

    private List<SystemProject> getProjectListByWsAndUserId(String userId, String organizationId) {
        List<SystemProject> projects = QueryChain.of(SystemProject.class)
                .where(SYSTEM_PROJECT.ENABLE.eq(true).and(SYSTEM_PROJECT.ORGANIZATION_ID.eq(organizationId)))
                .list();
        List<UserRoleRelation> userRoleRelations = userRoleRelationService.selectByUserId(userId);
        List<SystemProject> projectList = new ArrayList<>();
        userRoleRelations.forEach(userRoleRelation -> projects.forEach(project -> {
            if (Objects.equals(userRoleRelation.getSourceId(), project.getId()) && !projectList.contains(project)) {
                projectList.add(project);
            }

        }));
        return projectList;
    }

    @Override
    public Page<UserTableResponse> pageUserTable(BasePageRequest request) {
        Page<UserTableResponse> responsePage = queryChain()
                .where(SYSTEM_USER.EMAIL.like(request.getKeyword()).or(SYSTEM_USER.NAME.like(request.getKeyword()))
                        .or(SYSTEM_USER.PHONE.like(request.getKeyword())))
                .pageAs(new Page<>(request.getPage(), request.getPageSize()), UserTableResponse.class);
        List<UserTableResponse> records = responsePage.getRecords();
        List<String> userIdList = records.stream().map(UserResponseDTO::getId).toList();
        Map<String, UserTableResponse> roleAndOrganizationMap = userRoleRelationService.selectGlobalUserRoleAndOrganization(userIdList);
        records.forEach(user -> {
            UserTableResponse roleOrgModel = roleAndOrganizationMap.get(user.getId());
            if (roleOrgModel != null) {
                user.setUserRoleList(roleOrgModel.getUserRoleList());
            }
        });
        return responsePage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TableBatchProcessResponse updateUserEnable(UserChangeEnableRequest request, String userName) {
        checkUserInDb(request.getSelectIds());
        boolean update = updateChain().set(SYSTEM_USER.ENABLE, request.isEnable())
                .where(SYSTEM_USER.ID.in(request.getSelectIds())).update();
        if (update) {
            TableBatchProcessResponse response = new TableBatchProcessResponse();
            response.setTotalCount(request.getSelectIds().size());
            response.setSuccessCount(request.getSelectIds().size());
            return response;
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TableBatchProcessResponse deleteUser(TableBatchProcessDTO request, String operatorId, String operatorName) {
        List<String> userIdList = getBatchUserIds(request);
        checkUserInDb(userIdList);
        checkProcessUserAndThrowException(userIdList, operatorId, operatorName, Translator.get("user.not.delete"));
        mapper.deleteBatchByIds(userIdList);
        //删除用户角色关系
        userRoleRelationService.deleteByUserIdList(userIdList);
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUserById(String id) {
        checkUserInDb(Collections.singletonList(id));
        checkProcessUserAndThrowException(Collections.singletonList(id), SessionUtils.getCurrentUserId(), SessionUtils.getUserName(), Translator.get("user.not.delete"));
        //删除用户角色关系
        userRoleRelationService.deleteByUserIdList(Collections.singletonList(id));
        return mapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TableBatchProcessResponse resetPassword(TableBatchProcessDTO request, String currentUserId) {
        request.setSelectIds(getBatchUserIds(request));
        checkUserInDb(request.getSelectIds());
        List<SystemUser> systemUsers = mapper.selectListByIds(request.getSelectIds());
        systemUsers.forEach(user -> updateChain().set(SYSTEM_USER.PASSWORD, passwordEncoder.encode(user.getEmail()))
                .set(SYSTEM_USER.UPDATE_USER, currentUserId)
                .where(SYSTEM_USER.ID.eq(user.getId())).update());
        TableBatchProcessResponse response = new TableBatchProcessResponse();
        response.setTotalCount(request.getSelectIds().size());
        response.setSuccessCount(request.getSelectIds().size());
        return response;
    }

    private void checkProcessUserAndThrowException(List<String> userIdList, String operatorId, String operatorName, String exceptionMessage) {
        for (String userId : userIdList) {
            //当前用户或admin不能被操作
            if (Objects.equals(userId, operatorId)) {
                throw new CustomException(exceptionMessage + ":" + operatorName);
            } else if ("admin".equals(userId)) {
                throw new CustomException(exceptionMessage + ": admin");
            }
        }
    }

    private List<String> getBatchUserIds(TableBatchProcessDTO request) {
        if (request.isSelectAll()) {
            List<SystemUser> userList = queryChain()
                    .where(SYSTEM_USER.EMAIL.like(request.getCondition().getKeyword())
                            .or(SYSTEM_USER.NAME.like(request.getCondition().getKeyword()))
                            .or(SYSTEM_USER.PHONE.like(request.getCondition().getKeyword()))).list();
            List<String> userIdList = userList.stream().map(SystemUser::getId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                userIdList.removeAll(request.getExcludeIds());
            }
            return userIdList;
        } else {
            return request.getSelectIds();
        }
    }

    private void checkUserInDb(@Valid List<String> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            throw new CustomException(Translator.get("user.not.exist"));
        }
        long count = queryChain().where(SYSTEM_USER.ID.in(userIdList)).count();
        if (count != userIdList.size()) {
            throw new CustomException(Translator.get("user.not.exist"));
        }
    }

    private void checkUserEmail(String id, String email) {
        long count = queryChain().where(SYSTEM_USER.ID.ne(id).and(SYSTEM_USER.EMAIL.eq(email))).count();
        if (count > 0) {
            throw new CustomException(Translator.get("user_email_already_exists"));
        }
    }

    private List<UserCreateInfo> saveUserAndRole(UserBatchCreateRequest request, String source, String operator, String path) {
        request.getUserInfoList().forEach(userInfo -> {
            SystemUser user = SystemUser.builder()
                    .name(userInfo.getName())
                    .email(userInfo.getEmail())
                    .phone(userInfo.getPhone())
                    .source(source)
                    .createUser(operator)
                    .updateUser(operator)
                    .password(passwordEncoder.encode(userInfo.getEmail()))
                    .build();
            mapper.insertSelective(user);
            userRoleRelationService.updateUserSystemGlobalRole(user, operator, request.getUserRoleIdList());
        });
        return request.getUserInfoList();
    }

    private Map<String, String> validateUserInfo(Collection<String> createEmails) {
        Map<String, String> errorMessage = new HashMap<>();
        String userEmailRepeatError = Translator.get("user.email.repeat");
        //判断参数内是否含有重复邮箱
        List<String> emailList = new ArrayList<>();
        Map<String, String> userInDbMap = queryChain().where(SYSTEM_USER.EMAIL.in(createEmails)).list()
                .stream().collect(Collectors.toMap(SystemUser::getEmail, SystemUser::getId));
        for (String createEmail : createEmails) {
            if (emailList.contains(createEmail)) {
                errorMessage.put(createEmail, userEmailRepeatError);
            } else {
                //判断邮箱是否已存在数据库中
                if (userInDbMap.containsKey(createEmail)) {
                    errorMessage.put(createEmail, userEmailRepeatError);
                } else {
                    emailList.add(createEmail);
                }
            }
        }
        return errorMessage;
    }
}
