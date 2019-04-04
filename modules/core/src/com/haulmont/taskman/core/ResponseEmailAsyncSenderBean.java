package com.haulmont.taskman.core;

import com.google.common.collect.ImmutableMap;
import com.haulmont.addon.imap.dto.ImapMessageDto;
import com.haulmont.cuba.core.app.EmailerAPI;
import com.haulmont.cuba.core.global.EmailInfo;
import com.haulmont.taskman.entity.Task;
import com.haulmont.taskman.entity.TaskMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Map;

@Component(ResponseEmailAsyncSenderBean.NAME)
public class ResponseEmailAsyncSenderBean {
    public static final String NAME = "taskman_ResponseEmailAsyncSenderBean";

    private static final Logger log = LoggerFactory.getLogger(ResponseEmailAsyncSenderBean.class);

    @Inject
    private EmailerAPI emailerAPI;

    protected final static String NEW_TASK_TEMPLATE = "com/haulmont/taskman/templates/new-task.txt";
    protected final static String UPDATE_TASK_TEMPLATE = "com/haulmont/taskman/templates/exist-task.txt";
    protected final static String REPLY_TASK_TEMPLATE = "com/haulmont/taskman/templates/task-replied.txt";

    public void sendNewTaskResponse(Task task, TaskMessage taskMessage, ImapMessageDto imapMsg) {
        sendResponseEmail(task, taskMessage, imapMsg, NEW_TASK_TEMPLATE);
    }

    public void sendUpdateTaskResponse(Task task, TaskMessage taskMessage, ImapMessageDto imapMsg) {
        sendResponseEmail(task, taskMessage, imapMsg, UPDATE_TASK_TEMPLATE);
    }

    public void sendReplyTaskResponse(Task task, TaskMessage taskMessage) {
        sendResponseEmail(task, taskMessage, null, REPLY_TASK_TEMPLATE);
    }

    protected void sendResponseEmail(Task task, TaskMessage message, @Nullable ImapMessageDto imapMessage, String template) {
        Map<String, Serializable> parameters = ImmutableMap.of("task", task, "message_content", message.getContent());
        EmailInfo emailInfo = new EmailInfo(
                task.getReporterEmail(),
                task.getSubject(),
                null,
                template,
                parameters);

        emailInfo.setBodyContentType("text/plain; charset=UTF-8");

        if (imapMessage != null) {
            String cc = imapMessage.getCc();
            if (!"[]".equals(cc)) {
                emailInfo.setCc(cc);
            }
        }

        log.info("Sending an email over SMTP with subject: " + task.getSubject());
        emailerAPI.sendEmailAsync(emailInfo);
    }
}