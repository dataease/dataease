package com.fit2cloud.commons.auth.bean;


import com.fit2cloud.commons.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfo  implements Serializable {

    private List<String> roles;

    private String introduction;

    private String name;

    private String avatar;
}
