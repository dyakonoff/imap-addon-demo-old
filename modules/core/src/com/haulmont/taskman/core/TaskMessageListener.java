package com.haulmont.taskman.core;

import com.google.common.collect.ImmutableMap;
import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.app.EmailerAPI;
import com.haulmont.cuba.core.global.EmailException;
import com.haulmont.cuba.core.global.EmailInfo;
import com.haulmont.cuba.core.listener.BeforeInsertEntityListener;
import com.haulmont.taskman.entity.MessageDirection;
import com.haulmont.taskman.entity.Task;
import com.haulmont.taskman.entity.TaskMessage;
import com.haulmont.taskman.entity.TaskState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Map;

@Component(TaskMessageListener.NAME)
public class TaskMessageListener implements BeforeInsertEntityListener<TaskMessage> {
    public static final String NAME = "taskman_TaskMessageListener";

    private static final Logger log = LoggerFactory.getLogger(TaskMessageListener.class);

    @Inject
    private EmailerAPI emailerAPI;

    @Override
    public void onBeforeInsert(TaskMessage message, EntityManager entityManager) {
        log.debug("=====> TaskMessageListener.onBeforeInsert");
        if (MessageDirection.OUTBOX == message.getDirection()) {
            Task task = entityManager.find(Task.class, message.getTask().getId(), "task-message-view");
            task.setState(TaskState.REPLIED);
            entityManager.persist(task);

            Map<String, Serializable> parameters = ImmutableMap.of("task", task, "message", message);
            EmailInfo emailInfo = new EmailInfo(
                    task.getReporterEmail(),
                    task.getSubject(),
                    null,
                    "com/haulmont/taskman/templates/task-replied.txt",
                    parameters);


            try {
                emailerAPI.sendEmail(emailInfo);
            } catch (EmailException e) {
                log.error(e.getMessage());
            }

            emailerAPI.sendEmailAsync(emailInfo);
        }
    }
}