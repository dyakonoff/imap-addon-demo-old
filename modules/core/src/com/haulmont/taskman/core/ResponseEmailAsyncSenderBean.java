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
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Map;

@Component(ResponseEmailAsyncSenderBean.NAME)
public class ResponseEmailAsyncSenderBean {
    public static final String NAME = "taskman_ResponseEmailAsyncSenderBean";

    private static final Logger log = LoggerFactory.getLogger(ResponseEmailAsyncSenderBean.class);

    private EmailerAPI emailerAPI;

    protected final static String NEW_TASK_TEMPLATE = "com/haulmont/taskman/templates/new-task.txt";
    protected final static String UPDATE_TASK_TEMPLATE = "com/haulmont/taskman/templates/exist-task.txt";
    protected final static String REPLY_TASK_TEMPLATE = "com/haulmont/taskman/templates/task-replied.txt";

    protected class Html2Text extends HTMLEditorKit.ParserCallback {
        StringBuffer s;

        public Html2Text() {
        }

        public void parse(Reader in) throws IOException {
            s = new StringBuffer();
            ParserDelegator delegator = new ParserDelegator();
            // the third parameter is TRUE to ignore charset directive
            delegator.parse(in, this, Boolean.TRUE);
        }

        public void handleText(char[] text, int pos) {
            s.append(text);
        }

        public String getText() {
            return s.toString();
        }
    }


    public void sendNewTaskResponse(Task task, TaskMessage taskMessage, ImapMessageDto imapMsg) {
        sendResponseEmail(task, taskMessage, imapMsg, NEW_TASK_TEMPLATE);
    }

    public void sendUpdateTaskResponse(Task task, TaskMessage taskMessage, ImapMessageDto imapMsg) {
        sendResponseEmail(task, taskMessage, imapMsg, UPDATE_TASK_TEMPLATE);
    }

    public void sendReplyTaskResponse(Task task, TaskMessage taskMessage) {
        sendResponseEmail(task, taskMessage, null, REPLY_TASK_TEMPLATE);
    }

    protected String htmlToText(String htmlText) {
        Html2Text parser = new Html2Text();
        try (StringReader reader = new StringReader(htmlText)) {
            parser.parse(reader);
            return parser.getText();
        } catch (IOException e) {
            log.error("Error converting html to text: " + e.getMessage());
        }

        return htmlText;
    }

    protected void sendResponseEmail(Task task, TaskMessage taskMessage, @Nullable ImapMessageDto imapMessageDto, String templateName) {
        String messageContent = htmlToText(taskMessage.getContent());
        Map<String, Serializable> parameters = ImmutableMap.of("task", task, "message_content", messageContent);
        EmailInfo emailInfo = new EmailInfo(
                task.getReporterEmail(),
                String.format("#%d %s", task.getNumber(), task.getSubject()),
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

        emailerAPI.sendEmailAsync(emailInfo);
    }
}