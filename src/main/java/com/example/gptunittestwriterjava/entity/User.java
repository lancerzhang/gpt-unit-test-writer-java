package com.example.gptunittestwriterjava.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_display_name_employee_id", columnList = "display_name,employee_id")
}, uniqueConstraints = {@UniqueConstraint(name = "uc_users_employee_id", columnNames = "employee_id")})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {
    @Id
    @GeneratedValue
    private Integer id;

    @Column(name = "display_name", nullable = false)
    @Size(max = 255)
    private String displayName;

    @Column(name = "employee_id")
    @Size(max = 255)
    private String employeeId;

    @Size(max = 255)
    private String email;

    private double budget;

    private Date createdAt;

    @Column
    private Date lastModified;

    @PrePersist
    public void prePersist() {
        this.createdAt = new Date();
        this.lastModified = new Date();
    }
}
