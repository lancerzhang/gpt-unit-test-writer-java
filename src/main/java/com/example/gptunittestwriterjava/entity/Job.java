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
@NamedEntityGraph(name = "graph.Job.user",
        attributeNodes = @NamedAttributeNode("user"))
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String projectId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
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
