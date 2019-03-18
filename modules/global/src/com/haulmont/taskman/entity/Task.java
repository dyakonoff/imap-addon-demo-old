package com.haulmont.taskman.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.security.entity.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@NamePattern("[#%s] %s|number,subject")
@Table(name = "TASKMAN_TASK")
@Entity(name = "taskman_Task")
public class Task extends StandardEntity {
    @NotNull
    @Column(name = "NUMBER_", nullable = false, unique = true)
    protected Long number;

    @Column(name = "REPORTER_EMAIL")
    protected String reporterEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ASSIGNEE_ID")
    protected User assignee;

    @NotNull
    @Column(name = "STATE", nullable = false)
    protected String state = TaskState.OPEN.getId();

    @NotNull
    @Column(name = "SUBJECT", nullable = false)
    protected String subject;

    @Column(name = "CONTENT")
    protected String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public TaskState getState() {
        return state == null ? null : TaskState.fromId(state);
    }

    public void setState(TaskState state) {
        this.state = state == null ? null : state.getId();
    }

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public String getReporterEmail() {
        return reporterEmail;
    }

    public void setReporterEmail(String reporterEmail) {
        this.reporterEmail = reporterEmail;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }
}