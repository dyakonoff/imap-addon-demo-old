
package com.haulmont.taskman.core;

import com.google.common.collect.ImmutableMap;
import com.haulmont.addon.imap.api.ImapAPI;
import com.haulmont.addon.imap.dto.ImapMessageDto;
import com.haulmont.addon.imap.entity.ImapMessage;
import com.haulmont.addon.imap.events.NewEmailImapEvent;
import com.haulmont.cuba.core.app.EmailerAPI;
import com.haulmont.cuba.core.app.UniqueNumbersAPI;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.EmailInfo;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.taskman.entity.MessageDirection;
import com.haulmont.taskman.entity.Task;
import com.haulmont.taskman.entity.TaskMessage;
import com.haulmont.taskman.entity.TaskState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component(ImapMessageHandler.NAME)
public class ImapMessageHandler {
    public static final String NAME = "taskman_ImapMessageHandler";

    protected final static Pattern TASK_NUMBER_PATTERN = Pattern.compile("^#(\\d+)\\s");

    private static final Logger log = LoggerFactory.getLogger(ImapMessageHandler.class);

    @Inject
    private ImapAPI imapAPI;

    @Inject
    private Metadata metadata;

    @Inject
    private DataManager dataManager;

    @Inject
    private UniqueNumbersAPI uniqueNumbersAPI;

    @Inject
    private EmailerAPI emailerAPI;

    @EventListener
    @Transactional
    public void handleMessage(NewEmailImapEvent imapEvent) {
        log.debug("=====> ImapMessageHandler.handleMessage");
        ImapMessage imapMessage = imapEvent.getMessage();
        ImapMessageDto imapMessageDto = imapAPI.fetchMessage(imapMessage);
        String subject = imapMessageDto.getSubject();

        TaskMessage message = metadata.create(TaskMessage.class);
        message.setContent(imapMessageDto.getBody());
        message.setSubject(imapMessageDto.getSubject());
        message.setReporter(imapMessageDto.getFrom());
        message.setImapMessage(imapMessage);
        message.setDirection(MessageDirection.INBOX);

        boolean isNewTask = false;

        Matcher matcher = TASK_NUMBER_PATTERN.matcher(subject);
        Task task = null;

        if (matcher.find()) {
            String taskNumber = matcher.group(1);
            task = findTaskByNumber(Integer.valueOf(taskNumber));
            if (task != null && TaskState.REPLIED == task.getState()) {
                task.setState(TaskState.ASSIGNED);
            }
        }

        if (task == null) {
            task = metadata.create(Task.class);
            task.setState(TaskState.OPEN);
            task.setContent(imapMessageDto.getBody());
            task.setSubject(imapMessageDto.getSubject());
            task.setReporterEmail(imapMessageDto.getFrom());
            task.setNumber(uniqueNumbersAPI.getNextNumber(Task.class.getSimpleName()));
            isNewTask = true;
        }

        message.setTask(task);

        dataManager.commit(task, message);

        sendResponse(imapMessageDto, message, isNewTask, task);
    }

    private void sendResponse(ImapMessageDto imapMessageDto, TaskMessage message, boolean isNewTask, Task task) {
        String templateName;
        if (isNewTask) {
            templateName = "com/haulmont/taskman/templates/new-task.txt";
        } else {
            templateName = "com/haulmont/taskman/templates/exist-task.txt";
        }

        Map<String, Serializable> parameters = ImmutableMap.of("task", task, "message", message);
        EmailInfo emailInfo = new EmailInfo(
                task.getReporterEmail(),
                task.getSubject(),
                null,
                templateName,
                parameters);

        String cc = imapMessageDto.getCc();
        if (!"[]".equals(cc)) {
            emailInfo.setCc(cc);
        }

        emailerAPI.sendEmailAsync(emailInfo);
    }

    private Task findTaskByNumber(Integer taskNumber) {
        return dataManager.load(Task.class)
                .view("task-view")
                .query("select t from taskman_Task t where t.number = :number")
                .parameter("number", taskNumber)
                .optional()
                .orElse(null);
    }
}