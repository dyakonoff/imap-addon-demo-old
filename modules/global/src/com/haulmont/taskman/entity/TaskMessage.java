package com.haulmont.taskman.entity;

import com.haulmont.addon.imap.entity.ImapMessage;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.Listeners;
import com.haulmont.cuba.core.entity.annotation.OnDeleteInverse;
import com.haulmont.cuba.core.global.DeletePolicy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@NamePattern("%s, %s|reporter,subject")
@Table(name = "TASKMAN_TASK_MESSAGE", indexes = {
        @Index(name = "INDX_TASKMAN_TASK_MESSAGE_ON_ORIGINAL_IMAP_MESSAGE_ID", columnList = "ORIGINAL_IMAP_MESSAGE_ID")
})
@Entity(name = "taskman_TaskMessage")
@Listeners({"taskman_TaskMessageListener"})
public class TaskMessage extends StandardEntity {
    @Column(name = "REPORTER")
    protected String reporter;

    @Column(name = "DIRECTION")
    protected String direction = MessageDirection.OUTBOX.getId();

    @NotNull
    @Column(name = "SUBJECT", nullable = false)
    protected String subject;

    @Lob
    @Column(name = "CONTENT")
    protected String content;

    @NotNull
    @OnDeleteInverse(DeletePolicy.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TASK_ID")
    protected Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IMAP_MESSAGE_ID")
    protected ImapMessage imapMessage;

    @Column(name = "ORIGINAL_IMAP_MESSAGE_ID")
    protected String originalImapMessageId;

    public String getOriginalImapMessageId() {
        return originalImapMessageId;
    }

    public void setOriginalImapMessageId(String originalImapMessageId) {
        this.originalImapMessageId = originalImapMessageId;
    }

    public ImapMessage getImapMessage() {
        return imapMessage;
    }

    public void setImapMessage(ImapMessage imapMessage) {
        this.imapMessage = imapMessage;
    }

    public MessageDirection getDirection() {
        return direction == null ? null : MessageDirection.fromId(direction);
    }

    public void setDirection(MessageDirection direction) {
        this.direction = direction == null ? null : direction.getId();
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }
}