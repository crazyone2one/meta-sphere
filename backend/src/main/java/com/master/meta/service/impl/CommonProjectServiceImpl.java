package com.master.meta.service.impl;

import com.master.meta.constants.HttpMethodConstants;
import com.master.meta.constants.InternalUserRole;
import com.master.meta.constants.OperationLogConstants;
import com.master.meta.dto.system.project.*;
import com.master.meta.dto.system.user.UserExtendDTO;
import com.master.meta.dto.system.user.UserRoleOptionDto;
import com.master.meta.dto.system.user.UserVO;
import com.master.meta.entity.SystemProject;
import com.master.meta.entity.SystemUser;
import com.master.meta.entity.UserRoleRelation;
import com.master.meta.handle.Translator;
import com.master.meta.handle.exception.CustomException;
import com.master.meta.handle.log.LogDTO;
import com.master.meta.handle.log.constants.OperationLogModule;
import com.master.meta.handle.log.constants.OperationLogType;
import com.master.meta.mapper.OrganizationMapper;
import com.master.meta.mapper.SystemProjectMapper;
import com.master.meta.mapper.SystemUserMapper;
import com.master.meta.mapper.UserRoleRelationMapper;
import com.master.meta.service.CommonProjectService;
import com.master.meta.service.OperationLogService;
import com.master.meta.utils.JSON;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.master.meta.entity.table.SystemProjectTableDef.SYSTEM_PROJECT;
import static com.master.meta.entity.table.SystemUserTableDef.SYSTEM_USER;
import static com.master.meta.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
import static com.master.meta.entity.table.UserRoleTableDef.USER_ROLE;

/**
 * @author Created by 11's papa on 2025/11/14
 */
@Service("commonProjectService")
@RequiredArgsConstructor
public class CommonProjectServiceImpl extends ServiceImpl<SystemProjectMapper, SystemProject> implements CommonProjectService {
    private final OrganizationMapper organizationMapper;
    protected final UserRoleRelationMapper userRoleRelationMapper;
    private final OperationLogService operationLogService;
    protected final SystemUserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectDTO add(AddProjectRequest addProjectDTO, String createUser, String path, String module) {
        SystemProject project = new SystemProject();
        ProjectDTO projectDTO = new ProjectDTO();
        project.setName(addProjectDTO.getName());
        project.setNum(addProjectDTO.getNum());
        project.setOrganizationId(addProjectDTO.getOrganizationId());
        checkProjectExistByName(project);
        project.setUpdateUser(createUser);
        project.setCreateUser(createUser);
        project.setEnable(addProjectDTO.getEnable());
        project.setAllResourcePool(addProjectDTO.isAllResourcePool());
        project.setDescription(addProjectDTO.getDescription());
        BeanUtils.copyProperties(project, projectDTO);
        projectDTO.setOrganizationName(organizationMapper.selectOneById(project.getOrganizationId()).getName());
        //判断是否有模块设置
        if (CollectionUtils.isNotEmpty(addProjectDTO.getModuleIds())) {
            project.setModuleSetting(addProjectDTO.getModuleIds());
            projectDTO.setModuleIds(addProjectDTO.getModuleIds());
        }
        mapper.insertSelective(project);
        ProjectAddMemberBatchRequest memberRequest = new ProjectAddMemberBatchRequest();
        memberRequest.setProjectIds(List.of(project.getId()));
        memberRequest.setUserIds(addProjectDTO.getUserIds());
        addProjectAdmin(memberRequest, createUser, path, OperationLogType.ADD.name(), Translator.get("add"), module);
        return projectDTO;
    }

    @Override
    public void checkProjectNotExist(String id) {
        if (mapper.selectOneById(id) == null) {
            throw new CustomException(Translator.get("project_is_not_exist"));
        }
    }

    @Override
    public ProjectDTO get(String id) {
        List<SystemProject> projects = QueryChain.of(SystemProject.class).where(SYSTEM_PROJECT.ID.eq(id).and(SYSTEM_PROJECT.ENABLE.eq(true))).list();
        ProjectDTO projectDTO = new ProjectDTO();
        if (CollectionUtils.isNotEmpty(projects)) {
            BeanUtils.copyProperties(projects.getFirst(), projectDTO);
            projectDTO.setOrganizationName(organizationMapper.selectOneById(projectDTO.getOrganizationId()).getName());
            List<ProjectDTO> projectDTOS = buildUserInfo(List.of(projectDTO));
            projectDTO = projectDTOS.getFirst();
        } else {
            return null;
        }
        return projectDTO;
    }

    @Override
    public List<ProjectDTO> buildUserInfo(List<ProjectDTO> projectList) {
        List<String> projectIds = projectList.stream().map(ProjectDTO::getId).toList();

        QueryWrapper queryWrapper = QueryWrapper.create()
                .select("`system_user`.id,`system_user`.name,user_role_relation.source_id")
                .from(USER_ROLE_RELATION).leftJoin(SYSTEM_USER).on(USER_ROLE_RELATION.USER_ID.eq(SYSTEM_USER.ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.in(projectIds).and(USER_ROLE_RELATION.ROLE_CODE.eq("project_admin")));
        QueryWrapper wrapper = queryWrapper.groupBy(USER_ROLE_RELATION.SOURCE_ID, SYSTEM_USER.ID);
        List<UserVO> users = userRoleRelationMapper.selectListByQueryAs(wrapper, UserVO.class);
        //根据sourceId分组
        Map<String, List<UserVO>> userMapList = users.stream().collect(Collectors.groupingBy(UserVO::getSourceId));
        List<ProjectDTO> projectDTOList = getProjectExtendDTOList(projectIds);
        Map<String, ProjectDTO> projectMap = projectDTOList.stream().collect(Collectors.toMap(ProjectDTO::getId, projectDTO -> projectDTO));
        if (CollectionUtils.isNotEmpty(projectList)) {
            projectList.forEach(project -> {
                if (CollectionUtils.isNotEmpty(project.getModuleSetting())) {
                    project.setModuleIds(project.getModuleSetting());
                }
                project.setMemberCount(projectMap.get(project.getId()).getMemberCount());
                List<UserVO> userExtendDTOS = userMapList.get(project.getId());
                if (CollectionUtils.isNotEmpty(userExtendDTOS)) {
                    project.setAdminList(userExtendDTOS);
                    List<String> userIdList = userExtendDTOS.stream().map(UserVO::getName).collect(Collectors.toList());
                    project.setProjectCreateUserIsAdmin(CollectionUtils.isNotEmpty(userIdList) && userIdList.contains(project.getCreateUser()));
                } else {
                    project.setAdminList(new ArrayList<>());
                }
            });
        }
        return projectList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(UpdateProjectRequest updateProjectDto, String updateUser, String path, String module) {
        SystemProject project = new SystemProject();
        project.setId(updateProjectDto.getId());
        project.setName(updateProjectDto.getName());
        project.setDescription(updateProjectDto.getDescription());
        project.setOrganizationId(updateProjectDto.getOrganizationId());
        project.setEnable(updateProjectDto.getEnable());
        project.setAllResourcePool(updateProjectDto.isAllResourcePool());
        project.setUpdateUser(updateUser);
        checkProjectExistByName(project);
        checkProjectNotExist(project.getId());
        List<UserRoleRelation> userRoleRelations = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.SOURCE_ID.eq(project.getId())
                .and(USER_ROLE_RELATION.ROLE_CODE.eq(InternalUserRole.PROJECT_ADMIN.getValue()))).list();
        List<String> orgUserIds = userRoleRelations.stream().map(UserRoleRelation::getUserId).toList();
        List<LogDTO> logDTOList = new ArrayList<>();
        List<String> deleteIds = orgUserIds.stream()
                .filter(item -> !updateProjectDto.getUserIds().contains(item)).toList();
        List<String> insertIds = updateProjectDto.getUserIds().stream()
                .filter(item -> !orgUserIds.contains(item))
                .toList();
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            QueryChain<UserRoleRelation> userRoleRelationQueryChain = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.SOURCE_ID.eq(project.getId())
                    .and(USER_ROLE_RELATION.ROLE_CODE.eq(InternalUserRole.PROJECT_ADMIN.getValue()))
                    .and(USER_ROLE_RELATION.USER_ID.in(deleteIds)));
            userRoleRelationQueryChain.list().forEach(userRoleRelation -> {
                SystemUser user = userMapper.selectOneById(userRoleRelation.getUserId());
                String logProjectId = OperationLogConstants.SYSTEM;
                if (OperationLogModule.SETTING_ORGANIZATION_PROJECT.equals(module)) {
                    logProjectId = OperationLogConstants.ORGANIZATION;
                }
                LogDTO logDTO = new LogDTO(logProjectId, project.getOrganizationId(), userRoleRelation.getId(), updateUser, OperationLogType.DELETE.name(), module, Translator.get("delete") + Translator.get("project_admin") + ": " + user.getName());
                setLog(logDTO, path, HttpMethodConstants.POST.name(), logDTOList);
            });
            userRoleRelationMapper.deleteByQuery(userRoleRelationQueryChain);
        }
        if (CollectionUtils.isNotEmpty(insertIds)) {
            ProjectAddMemberBatchRequest memberRequest = new ProjectAddMemberBatchRequest();
            memberRequest.setProjectIds(List.of(project.getId()));
            memberRequest.setUserIds(insertIds);
            addProjectAdmin(memberRequest, updateUser, path, OperationLogType.ADD.name(), Translator.get("add"), module);
        }
        if (CollectionUtils.isNotEmpty(logDTOList)) {
            operationLogService.batchAdd(logDTOList);
        }
        //判断是否有模块设置
        if (CollectionUtils.isNotEmpty(updateProjectDto.getModuleIds())) {
            project.setModuleSetting(updateProjectDto.getModuleIds());

        } else {
            project.setModuleSetting(new ArrayList<>());
        }
        mapper.update(project);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id, String deleteUser) {
        // 删除项目删除全部资源 这里的删除只是假删除
        checkProjectNotExist(id);
        updateChain().set(SYSTEM_PROJECT.DELETE_USER, deleteUser).set(SYSTEM_PROJECT.DELETED, true)
                .where(SYSTEM_PROJECT.ID.eq(id)).update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enable(String id, String updateUser) {
        updateChain().set(SYSTEM_PROJECT.ENABLE, true).set(SYSTEM_PROJECT.UPDATE_USER, updateUser)
                .where(SYSTEM_PROJECT.ID.eq(id)).update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(String id, String updateUser) {
        updateChain().set(SYSTEM_PROJECT.ENABLE, false).set(SYSTEM_PROJECT.UPDATE_USER, updateUser)
                .where(SYSTEM_PROJECT.ID.eq(id)).update();
    }

    @Override
    public Page<UserExtendDTO> getProjectMemberList(ProjectMemberRequest request) {
        QueryChain<UserRoleRelation> tempQueryChain = QueryChain.of(UserRoleRelation.class)
                .select(USER_ROLE_RELATION.ROLE_CODE, SYSTEM_USER.ID, SYSTEM_USER.NAME, SYSTEM_USER.EMAIL, SYSTEM_USER.PHONE,
                        USER_ROLE_RELATION.CREATE_TIME.as("memberTime"))
                .from(USER_ROLE_RELATION).leftJoin(SYSTEM_USER).on(USER_ROLE_RELATION.USER_ID.eq(SYSTEM_USER.ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.eq(request.getProjectId())
                        .and(SYSTEM_USER.NAME.like(request.getKeyword())
                                .or(SYSTEM_USER.EMAIL.like(request.getKeyword()))
                                .or(SYSTEM_USER.PHONE.like(request.getKeyword()))))
                .orderBy(USER_ROLE_RELATION.CREATE_TIME.desc());
        return queryChain()
                .select(" temp.id,temp.name,temp.email,temp.phone , MAX( if (temp.role_code = 'project_admin', true, false)) as adminFlag, MIN(temp.memberTime) as groupTime")
                .from(tempQueryChain).as("temp")
                .groupBy("temp.id")
                .orderBy("adminFlag", false)
                .orderBy("groupTime", false)
                .pageAs(new Page<>(request.getPage(), request.getPageSize()), UserExtendDTO.class);
    }

    @Override
    public List<UserRoleOptionDto> selectProjectUserRoleByUserIds(List<String> userIds, String projectId) {
        return QueryChain.of(UserRoleRelation.class)
                .select(QueryMethods.distinct(USER_ROLE_RELATION.ROLE_CODE.as("id"), USER_ROLE_RELATION.USER_ID))
                .select(USER_ROLE.NAME)
                .from(USER_ROLE_RELATION).leftJoin(USER_ROLE).on(USER_ROLE_RELATION.ROLE_CODE.eq(USER_ROLE.CODE))
                .where(USER_ROLE_RELATION.USER_ID.in(userIds).and(USER_ROLE_RELATION.SOURCE_ID.eq(projectId)))
                .listAs(UserRoleOptionDto.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addProjectUser(ProjectAddMemberRequest request, String createUser, String path, String type, String content, String module) {
        List<LogDTO> logDTOList = new ArrayList<>();
        List<UserRoleRelation> userRoleRelations = new ArrayList<>();
        SystemProject project = mapper.selectOneById(request.getProjectId());
        Map<String, String> userMap = addUserPre(request.getUserIds(), createUser, path, module, request.getProjectId(), project);
        request.getUserIds().forEach(userId -> {
            request.getUserRoleIds().forEach(roleId -> {
                UserRoleRelation userRoleRelation = new UserRoleRelation();
                userRoleRelation.setUserId(userId);
                userRoleRelation.setRoleCode(roleId);
                userRoleRelation.setSourceId(request.getProjectId());
                userRoleRelation.setCreateUser(createUser);
                userRoleRelation.setOrganizationId(project.getOrganizationId());
                userRoleRelations.add(userRoleRelation);
                String logProjectId = OperationLogConstants.SYSTEM;
                if (OperationLogModule.SETTING_ORGANIZATION_PROJECT.equals(module)) {
                    logProjectId = OperationLogConstants.ORGANIZATION;
                }
                LogDTO logDTO = new LogDTO(logProjectId, OperationLogConstants.SYSTEM, userRoleRelation.getId(), createUser, type, module, content + Translator.get("project_member") + ": " + userMap.get(userId));
                setLog(logDTO, path, HttpMethodConstants.POST.name(), logDTOList);
            });
        });
        if (CollectionUtils.isNotEmpty(userRoleRelations)) {
            userRoleRelationMapper.insertBatch(userRoleRelations);
        }
        operationLogService.batchAdd(logDTOList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removeProjectMember(String projectId, String userId, String createUser, String module, String path) {
        checkProjectNotExist(projectId);
        SystemUser user = QueryChain.of(userMapper).where(SYSTEM_USER.ID.eq(userId)).oneOpt()
                .orElseThrow(() -> new CustomException(Translator.get("user_not_exist")));
        long count = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.ne(userId)
                .and(USER_ROLE_RELATION.SOURCE_ID.eq(projectId))
                .and(USER_ROLE_RELATION.ROLE_CODE.eq("project_admin"))).count();
        if (count == 0) {
            throw new CustomException(Translator.get("keep_at_least_one_administrator"));
        }
        if (projectId.equals(user.getLastProjectId())) {
            user.setLastProjectId(StringUtils.EMPTY);
            userMapper.update(user);
        }
        List<LogDTO> logDTOList = new ArrayList<>();
        QueryChain<UserRoleRelation> userRoleRelationQueryChain = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.eq(userId)
                .and(USER_ROLE_RELATION.SOURCE_ID.eq(projectId)));
        userRoleRelationQueryChain.list().forEach(userRoleRelation -> {
            String logProjectId = OperationLogConstants.SYSTEM;
            if (OperationLogModule.SETTING_ORGANIZATION_PROJECT.equals(module)) {
                logProjectId = OperationLogConstants.ORGANIZATION;
            }
            LogDTO logDTO = new LogDTO(logProjectId, OperationLogConstants.SYSTEM, userRoleRelation.getId(), createUser, OperationLogType.DELETE.name(), module, Translator.get("delete") + Translator.get("project_member") + ": " + user.getName());
            setLog(logDTO, path, HttpMethodConstants.GET.name(), logDTOList);
        });
        operationLogService.batchAdd(logDTOList);
        return userRoleRelationMapper.deleteByQuery(userRoleRelationQueryChain);
    }

    @Override
    public void rename(UpdateProjectNameRequest request, String userName) {
        checkProjectNotExist(request.getId());
        SystemProject project = new SystemProject();
        project.setId(request.getId());
        project.setName(request.getName());
        project.setOrganizationId(request.getOrganizationId());
        checkProjectExistByName(project);
        project.setUpdateUser(userName);
        mapper.update(project);
    }

    @Override
    public void revoke(String id, String updateUser) {
        LogicDeleteManager.execWithoutLogicDelete(()->{
            checkProjectNotExist(id);
            updateChain().set(SYSTEM_PROJECT.UPDATE_USER, updateUser).set(SYSTEM_PROJECT.DELETED, false)
                    .where(SYSTEM_PROJECT.ID.eq(id)).update();
        });
    }

    private List<ProjectDTO> getProjectExtendDTOList(List<String> projectIds) {
        QueryChain<UserRoleRelation> tempQueryChain = QueryChain.of(UserRoleRelation.class)
                .select(USER_ROLE_RELATION.SOURCE_ID, SYSTEM_USER.ID)
                .from(USER_ROLE_RELATION).leftJoin(SYSTEM_USER).on(USER_ROLE_RELATION.USER_ID.eq(SYSTEM_USER.ID))
                .where(USER_ROLE_RELATION.SOURCE_ID.in(projectIds));
        return QueryChain.of(SystemProject.class)
                .select(SYSTEM_PROJECT.ID)
                .select("count(distinct temp.id) as memberCount")
                .from(SYSTEM_PROJECT).as("p")
                .leftJoin(tempQueryChain).as("temp").on("p.id = temp.source_id")
                .groupBy(SYSTEM_PROJECT.ID)
                .listAs(ProjectDTO.class);
    }

    private void checkProjectExistByName(SystemProject project) {
        boolean exists = QueryChain.of(SystemProject.class).where(SYSTEM_PROJECT.NAME.eq(project.getName())
                .and(SYSTEM_PROJECT.ORGANIZATION_ID.eq(project.getOrganizationId()))
                .and(SYSTEM_PROJECT.ID.ne(project.getId()))
                .and(SYSTEM_PROJECT.NUM.eq(project.getNum()))).exists();
        if (exists) {
            throw new CustomException(Translator.get("project_name_already_exists"));
        }
    }

    private void addProjectAdmin(ProjectAddMemberBatchRequest request, String createUser, String path, String type, String content, String module) {
        List<LogDTO> logDTOList = new ArrayList<>();
        List<UserRoleRelation> userRoleRelations = new ArrayList<>();
        request.getProjectIds().forEach(projectId -> {
            SystemProject project = mapper.selectOneById(projectId);
            Map<String, String> nameMap = addUserPre(request.getUserIds(), createUser, path, module, projectId, project);
            request.getUserIds().forEach(userId -> {
                boolean exists = QueryChain.of(UserRoleRelation.class)
                        .where(USER_ROLE_RELATION.USER_ID.eq(userId).and(USER_ROLE_RELATION.SOURCE_ID.eq(projectId))
                                .and(USER_ROLE_RELATION.ROLE_CODE.eq(InternalUserRole.PROJECT_ADMIN.getValue())))
                        .exists();
                if (!exists) {
                    UserRoleRelation adminRole = new UserRoleRelation();
                    adminRole.setUserId(userId);
                    adminRole.setRoleCode(InternalUserRole.PROJECT_ADMIN.getValue());
                    adminRole.setSourceId(projectId);
                    adminRole.setCreateUser(createUser);
                    adminRole.setOrganizationId(project.getOrganizationId());
                    userRoleRelations.add(adminRole);
                    String logProjectId = OperationLogConstants.SYSTEM;
                    if (OperationLogModule.SETTING_ORGANIZATION_PROJECT.equals(module)) {
                        logProjectId = OperationLogConstants.ORGANIZATION;
                    }
                    LogDTO logDTO = new LogDTO(logProjectId, project.getOrganizationId(), adminRole.getId(), createUser, type, module, content + Translator.get("project_admin") + ": " + nameMap.get(userId));
                    setLog(logDTO, path, HttpMethodConstants.POST.name(), logDTOList);
                }
            });
        });
        if (CollectionUtils.isNotEmpty(userRoleRelations)) {
            userRoleRelationMapper.insertBatch(userRoleRelations);
        }
        operationLogService.batchAdd(logDTOList);
    }

    private Map<String, String> addUserPre(List<String> userIds, String createUser, String path, String module, String projectId, SystemProject project) {
        checkProjectNotExist(projectId);
        List<SystemUser> users = QueryChain.of(SystemUser.class).where(SYSTEM_USER.ID.in(userIds)).list();
        if (userIds.size() != users.size()) {
            throw new CustomException(Translator.get("user_not_exist"));
        }
        Map<String, String> userMap = users.stream().collect(Collectors.toMap(SystemUser::getId, SystemUser::getName));
        this.checkOrgRoleExit(userIds, project.getOrganizationId(), createUser, userMap, path, module);
        return userMap;
    }

    private void checkOrgRoleExit(List<String> userIds, String orgId, String createUser, Map<String, String> nameMap, String path, String module) {
        List<LogDTO> logDTOList = new ArrayList<>();
        List<UserRoleRelation> userRoleRelations = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.in(userIds)
                .and(USER_ROLE_RELATION.SOURCE_ID.eq(orgId))
        ).list();
        //把用户id放到一个新的list
        List<String> orgUserIds = userRoleRelations.stream().map(UserRoleRelation::getUserId).toList();
        if (!userIds.isEmpty()) {
            List<UserRoleRelation> userRoleRelation = new ArrayList<>();
            userIds.forEach(userId -> {
                if (!orgUserIds.contains(userId)) {
                    UserRoleRelation memberRole = new UserRoleRelation();
                    memberRole.setUserId(userId);
                    memberRole.setRoleCode(InternalUserRole.ORG_MEMBER.getValue());
                    memberRole.setSourceId(orgId);
                    memberRole.setCreateUser(createUser);
                    memberRole.setOrganizationId(orgId);
                    userRoleRelation.add(memberRole);
                    LogDTO logDTO = new LogDTO(orgId, orgId, memberRole.getId(), createUser, OperationLogType.ADD.name(), module, Translator.get("add") + Translator.get("organization_member") + ": " + nameMap.get(userId));
                    setLog(logDTO, path, HttpMethodConstants.POST.name(), logDTOList);
                }
            });
            if (CollectionUtils.isNotEmpty(userRoleRelation)) {
                userRoleRelationMapper.insertBatch(userRoleRelation);
            }
        }
        operationLogService.batchAdd(logDTOList);
    }

    public void setLog(LogDTO dto, String path, String method, List<LogDTO> logDTOList) {
        dto.setPath(path);
        dto.setMethod(method);
        dto.setOriginalValue(JSON.toJSONBytes(StringUtils.EMPTY));
        logDTOList.add(dto);
    }
}
