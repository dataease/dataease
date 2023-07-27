package io.dataease.xpack.permissions.user.manage;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.utils.Md5Utils;
import io.dataease.xpack.permissions.user.dao.auto.entity.PerUser;
import io.dataease.xpack.permissions.user.dao.auto.mapper.PerUserMapper;
import io.dataease.xpack.permissions.user.entity.ExcelCheckResult;
import io.dataease.xpack.permissions.user.entity.ExcelImportErrDto;
import io.dataease.xpack.permissions.user.entity.ExcelUserDTO;
import io.dataease.xpack.permissions.utils.PerConstant;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class ExcelCheckManage extends ServiceImpl<PerUserMapper, PerUser> {


    @Value("${dataease.init_password:DataEase123..}")
    private String DEFAULT_PWD;

    /**
     * 默认组织ID
     * 由系统管理或者application.yml中配置
     */
    private static final Long defaultOid = 1L;


    @Resource
    private PerUserMapper perUserMapper;

    @Resource
    private BatchUserRoleManage batchUserRoleManage;


    @Transactional
    public ExcelCheckResult<ExcelUserDTO> checkImportExcel(List<ExcelUserDTO> userList) {
        userList = userList.stream().filter(user -> !isEmptyUser(user)).collect(Collectors.toList());
        List<ExcelUserDTO> successList = new ArrayList<>();
        List<ExcelImportErrDto> errList = new ArrayList<>();
        for (ExcelUserDTO user : userList) {
            checkUser(user, successList, errList);
        }

        if (CollectionUtils.isNotEmpty(successList)) {
            batchSave(successList);
        }

        return new ExcelCheckResult<>(successList, errList);

    }

    public void validateHead(ExcelUserDTO head) {
        if (ObjectUtils.isEmpty(head)) throw new RuntimeException("template file error");
        Field[] declaredFields = head.getClass().getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (Modifier.isStatic(declaredField.getModifiers())) continue;
            Object o = null;
            try {
                declaredField.setAccessible(true);
                o = declaredField.get(head);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            ExcelProperty annotation = declaredField.getAnnotation(ExcelProperty.class);
            String[] values = annotation.value();
            if (ArrayUtils.isEmpty(values)) throw new RuntimeException("template file error");
            String value = values[0];
            if (StringUtils.isBlank(value)) throw new RuntimeException("template file error");
            if (!o.equals(value)) {
                throw new RuntimeException("template file error");
            }
        }
    }

    private Boolean isEmptyUser(ExcelUserDTO userExcel) {
        if (ObjectUtils.isEmpty(userExcel)) return true;
        boolean usernameBlank = StringUtils.isBlank(userExcel.getAccount());
        boolean nickNameBlank = StringUtils.isBlank(userExcel.getName());
        boolean emailBlank = StringUtils.isBlank(userExcel.getEmail());
        boolean phoneBlank = StringUtils.isBlank(userExcel.getPhone());
        boolean enableBlank = StringUtils.isBlank(userExcel.getEnable());
        return usernameBlank && nickNameBlank && emailBlank && phoneBlank && enableBlank;
    }


    @Transactional
    public void batchSave(List<ExcelUserDTO> successList) {
        List<PerUser> poUsers = new ArrayList<>();


        successList.forEach(excelUser -> {
            PerUser sysUser = BeanUtils.copyBean(new PerUser(), excelUser);
            long currentTimeMillis = System.currentTimeMillis();
            sysUser.setCreateTime(currentTimeMillis);
            sysUser.setEnable(StringUtils.equals("是", excelUser.getEnable()));
            sysUser.setLanguage("zh_CN");
            sysUser.setOrigin(0);
            sysUser.setPwd(Md5Utils.md5(DEFAULT_PWD));
            sysUser.setId(IDUtils.snowID());
            sysUser.setDefaultOid(defaultOid);
            sysUser.setCreatorId(AuthUtils.getUser().getUserId());
            poUsers.add(sysUser);
        });
        if (CollectionUtil.isNotEmpty(poUsers)) {
            saveBatch(poUsers);
            batchUserRoleManage.saveUserRole(poUsers.stream().map(PerUser::getId).collect(Collectors.toList()));
        }
    }

    private void checkUser(ExcelUserDTO user, List<ExcelUserDTO> successList, List<ExcelImportErrDto> errList) {
        ExcelUserDTO sourceUser = BeanUtils.copyBean(new ExcelUserDTO(), user);
        ExcelImportErrDto errDto = new ExcelImportErrDto(user, new HashMap<Integer, String>());
        boolean usernameValid = validateUserName(user, errDto);
        boolean nickNameValid = validateNickName(user, errDto);
        boolean emailValid = validateEmail(user, errDto);

        boolean phoneValid = validatePhone(user, errDto);
        boolean enableValid = validateEnable(user, errDto);

        if (usernameValid && nickNameValid && emailValid && phoneValid && enableValid) {
            successList.add(user);
            return;
        }
        errDto.setObject(sourceUser);
        errList.add(errDto);
    }

    private boolean validateUserName(ExcelUserDTO user, ExcelImportErrDto errDto) {
        String msg = null;
        String account = user.getAccount();
        if (StringUtils.isBlank(account)) {
            msg = "ID不能为空！";
        } else if (account.length() > 50) {
            msg = "ID长度必须在[1-50]之间！";
        } else if (!Pattern.matches(PerConstant.REGEX_USERNAME, account)) {
            msg = "ID只能包含字母数字下划线以及-并以字母开头！";
        } else if (userCount(new CountParam("account", account)) > 0) {
            msg = "系统中已存在此ID！";
        }
        if (StringUtils.isNotBlank(msg)) {
            errDto.getCellMap().put(0, msg);
            return false;
        }
        return true;
    }

    private boolean validateNickName(ExcelUserDTO user, ExcelImportErrDto errDto) {
        String msg = null;
        String nickName = user.getName();
        if (StringUtils.isBlank(nickName)) {
            msg = "姓名不能为空！";
        } else if (nickName.length() > 50 || nickName.length() < 2) {
            msg = "姓名长度必须在[1-50]之间！";
        } else if (Pattern.matches(PerConstant.REGEX_NICKNAME, nickName)) {
            msg = "姓名不支持特殊字符！";
        } else if (userCount(new CountParam("name", nickName)) > 0) {
            msg = "系统中已存在此姓名！";
        }
        if (StringUtils.isNotBlank(msg)) {
            errDto.getCellMap().put(1, msg);
            return false;
        }
        return true;
    }


    private boolean validateEmail(ExcelUserDTO user, ExcelImportErrDto errDto) {
        String msg = null;
        String email = user.getEmail();
        if (StringUtils.isBlank(email)) {
            msg = "邮箱不能为空！";
        } else if (!Pattern.matches(PerConstant.REGEX_EMAIL, email)) {
            msg = "邮箱格式错误！";
        } else if (userCount(new CountParam("email", email)) > 0) {
            msg = "系统中已存在此邮箱！";
        }
        if (StringUtils.isNotBlank(msg)) {
            errDto.getCellMap().put(3, msg);
            return false;
        }
        return true;
    }

    private boolean validatePhone(ExcelUserDTO user, ExcelImportErrDto errDto) {
        String msg = null;
        String phone = user.getPhone();
        if (StringUtils.isNotBlank(phone) && !Pattern.matches(PerConstant.REGEX_MOBILE, phone)) {
            msg = "手机号格式错误！";
        }
        if (StringUtils.isNotBlank(msg)) {
            errDto.getCellMap().put(4, msg);
            return false;
        }
        return true;
    }

    private boolean validateEnable(ExcelUserDTO user, ExcelImportErrDto errDto) {
        String msg = null;
        String enable = user.getEnable();
        if (StringUtils.isBlank(enable)) {
            msg = "用户状态不能为空！";
        } else if (!StringUtils.equals("是", enable) && !StringUtils.equals("否", enable)) {
            msg = "用户状态只支持[是,否]！";
        }
        if (StringUtils.isNotBlank(msg)) {
            errDto.getCellMap().put(7, msg);
            return false;
        }
        return true;
    }

    private int userCount(CountParam param) {
        QueryWrapper<PerUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(param.key), param.key, param.value);
        return perUserMapper.selectCount(queryWrapper).intValue();
    }

    private static class CountParam {
        private final String key;
        private final Object value;

        public CountParam(String key, Object value) {
            this.key = key;
            this.value = value;
        }
    }


}
