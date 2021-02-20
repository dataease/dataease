package io.dataease.commons.user;

import io.dataease.dto.UserDTO;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

public class SessionUser extends UserDTO implements Serializable {

    private static final long serialVersionUID = -7149638440406959033L;

    public static SessionUser fromUser(UserDTO user) {
        SessionUser sessionUser = new SessionUser();
        BeanUtils.copyProperties(user, sessionUser);

        return sessionUser;
    }

}
