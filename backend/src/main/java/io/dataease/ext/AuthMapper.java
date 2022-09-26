package io.dataease.ext;



import io.dataease.auth.api.dto.CurrentRoleDto;
import io.dataease.auth.entity.SysUserEntity;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface AuthMapper {



    List<String> roleCodes(@Param("userId") Long userId);


    List<String> permissions(@Param("userId") Long userId);

    List<String> permissionsAll();

    List<Long> userMenuIds(@Param("userId") Long userId);


    SysUserEntity findUser(@Param("userId") Long userId);

    SysUserEntity findUserByName(@Param("username") String username);

    SysUserEntity findLdapUserByName(@Param("username") String username);

    SysUserEntity findCasUserByName(@Param("username") String username);

    SysUserEntity findUserBySub(@Param("sub") String sub, @Param("userFrom") Integer userFrom);


    List<CurrentRoleDto> roles(@Param("userId") Long userId);

    SysUserEntity findWecomUser(@Param("wecomId") String wecomId);
    SysUserEntity findDingtalkUser(@Param("dingtalkId") String dingtalkId);
    SysUserEntity findLarkUser(@Param("larkId") String larkId);

}
