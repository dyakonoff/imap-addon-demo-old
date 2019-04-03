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

    @Inject
    private HtmlToTextConverterService htmlToTextConverterService;

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

    protected void sendResponseEmail(Task task, TaskMessage taskMessage, @Nullable ImapMessageDto imapMessageDto, String templateName) {
        String messageContent = htmlToTextConverterService.convert(taskMessage.getContent());
        Map<String, Serializable> parameters = ImmutableMap.of("task", task, "message_content", messageContent);
        String emailSubject = String.format("#%d %s", task.getNumber(), task.getSubject());
        EmailInfo emailInfo = new EmailInfo(
                task.getReporterEmail(),
                emailSubject,
                null,
                templateName,
                parameters);

        emailInfo.setBodyContentType("text/plain; charset=UTF-8");

        if (imapMessageDto != null) {
            String cc = imapMessageDto.getCc();
            if (!"[]".equals(cc)) {
                emailInfo.setCc(cc);
            }
        }

        log.info("Sending an email over SMTP with subject: " + emailSubject);
        emailerAPI.sendEmailAsync(emailInfo);
    }
}