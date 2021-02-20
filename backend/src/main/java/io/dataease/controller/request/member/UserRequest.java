package io.dataease.controller.request.member;

import io.dataease.base.domain.User;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class UserRequest extends User {

    private List<Map<String, Object>> roles = new ArrayList<>();

}
