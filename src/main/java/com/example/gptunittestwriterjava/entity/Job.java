package com.example.gptunittestwriterjava.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "job",
        indexes = {
                @Index(name = "idx_github_repo_branch_jobtype_status", columnList = "githubRepo,branch,jobType,status"),
                @Index(name = "idx_status_id", columnList = "status, id")
        })
@NamedEntityGraph(name = "graph.Job.user",
        attributeNodes = @NamedAttributeNode("user"))
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String githubRepo;
    @NotBlank
    private String branch;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @Enumerated(EnumType.STRING)
    private JobType jobType;
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
