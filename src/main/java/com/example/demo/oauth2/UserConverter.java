package com.example.demo.oauth2;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserConverter extends DefaultUserAuthenticationConverter {

    @Autowired
    private UserService userService;

    @Override
    public Authentication extractAuthentication(Map<String, ?> map) {
        Authentication authentication = super.extractAuthentication(map);
        String employeeId = (String) map.get("employeeId");
        String displayName = (String) map.get("displayName");
        String email = (String) map.get("email");
        User dbuser = userService.findEmployeeId(employeeId);
        if (dbuser == null) {
            dbuser = new User();
            dbuser.setEmployeeId(employeeId);
            dbuser.setDisplayName(displayName);
            dbuser.setEmail(email);
            dbuser = userService.createUser(dbuser);
        }
        Integer myId = dbuser.getId();
        PrincipalUser principalUser = new PrincipalUser(myId, employeeId, displayName, email);
        return new UsernamePasswordAuthenticationToken(principalUser, authentication.getCredentials(), authentication.getAuthorities());
    }

}
