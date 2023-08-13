package com.example.gptunittestwriterjava.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Date;

@Getter
@Setter
@Entity
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String projectId;
    @NotBlank
    private String employeeId;
    @NotBlank
    private String username;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String jobType;
    private Instant startTime;
    private int originScore;
    @Enumerated(EnumType.STRING)
    private JobStatus status;
    private Instant endTime;
    private int endScore;
    private int duration;
    private double cost;
    private int linesOfGenTests;

    private Date createdAt;

    @Column
    private Date lastModified;

    @PrePersist
    public void prePersist() {
        this.createdAt = new Date();
        this.lastModified = new Date();
    }
}
