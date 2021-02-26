package io.dataease.auth.filter;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class F2CPermissionsFilter extends PermissionsAuthorizationFilter {

    @Override
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws IOException {
        Subject subject = getSubject(request, response);
        String[] perms = (String[]) mappedValue;
        if (perms != null && perms.length > 0) {
            for (String str : perms) {
                // 判断访问的用户是否拥有mappedValue权限
                if (subject.isPermitted(str)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

}
