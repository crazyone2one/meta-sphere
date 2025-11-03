package com.master.meta.service.impl;

import com.master.meta.dto.BasePageRequest;
import com.master.meta.dto.UserInfoDTO;
import com.master.meta.dto.system.*;
import com.master.meta.entity.SystemUser;
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
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.master.meta.entity.table.SystemUserTableDef.SYSTEM_USER;

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

    public SystemUserServiceImpl(BaseUserRoleRelationService userRoleRelationService,
                                 @Qualifier("baseUserRoleService") BaseUserRoleService userRoleService,
                                 PasswordEncoder passwordEncoder) {
        this.userRoleRelationService = userRoleRelationService;
        this.userRoleService = userRoleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserInfoDTO getUserInfo() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return queryChain().where(SYSTEM_USER.NAME.eq(name)).oneAs(UserInfoDTO.class);
    }

    @Override
    public UserDTO getUserDTOByKeyword(String keyword) {
        UserDTO userDTO = queryChain().where(SYSTEM_USER.EMAIL.like(keyword).or(SYSTEM_USER.NAME.like(keyword))).oneAs(UserDTO.class);
        if (userDTO != null) {
            userDTO.setUserRoleRelations(
                    userRoleRelationService.selectByUserId(userDTO.getId())
            );
            userDTO.setUserRoles(
                    userRoleService.selectByUserRoleRelations(userDTO.getUserRoleRelations())
            );
        }
        return userDTO;
    }

    @Override
    public UserBatchCreateResponse addUser(UserBatchCreateRequest request, String source, String operator) {
        userRoleService.checkRoleIsGlobalAndHaveMember(request.getUserRoleIdList(), true);
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
    public Page<UserTableResponse> pageUserTable(BasePageRequest request) {
        Page<UserTableResponse> responsePage = queryChain().pageAs(new Page<>(request.getPage(), request.getPageSize()), UserTableResponse.class);
        List<UserTableResponse> records = responsePage.getRecords();
        List<String> userIdList = records.stream().map(SystemUser::getId).toList();
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
    public TableBatchProcessResponse updateUserEnable(UserChangeEnableRequest request, String userName) {
        checkUserInDb(request.getSelectIds());
        boolean update = updateChain().set(SYSTEM_USER.ENABLE, request.isEnable()).update();
        if (update) {
            TableBatchProcessResponse response = new TableBatchProcessResponse();
            response.setTotalCount(request.getSelectIds().size());
            response.setSuccessCount(request.getSelectIds().size());
            return response;
        }
        return null;
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
