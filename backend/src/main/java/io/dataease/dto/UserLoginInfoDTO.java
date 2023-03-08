package io.dataease.dto;

import io.dataease.auth.api.dto.CurrentUserDto;
import lombok.Data;

/**
 * Author: wangjiahao
 * Date: 2022/11/10
 * Description:
 */
@Data
public class UserLoginInfoDTO {

    private CurrentUserDto userInfo;

    private String ip;

    public UserLoginInfoDTO() {
    }

    public UserLoginInfoDTO(CurrentUserDto userInfo, String ip) {
        this.userInfo = userInfo;
        this.ip = ip;
    }

    public UserLoginInfoDTO(String username, String nickname, String ip) {
        this.userInfo = new CurrentUserDto(username, nickname);
        this.ip = ip;
    }
}
