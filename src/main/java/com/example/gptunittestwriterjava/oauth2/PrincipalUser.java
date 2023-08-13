package com.example.gptunittestwriterjava.oauth2;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PrincipalUser {

    private final Integer id;
    private final String employeeId;
    private final String displayName;
    private final String email;
    private List<Integer> delegators;

    public PrincipalUser(Integer id, String employeeId, String displayName, String email) {
        this.id = id;
        this.employeeId = employeeId;
        this.displayName = displayName;
        this.email = email;
    }

    @Override
    public String toString() {
        return "PrincipalUser{" +
                "id=" + id +
                ", employeeId='" + employeeId + '\'' +
                ", displayName='" + displayName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
