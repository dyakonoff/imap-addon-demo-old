package com.haulmont.taskman.web.taskmessage;

import com.haulmont.cuba.gui.screen.*;
import com.haulmont.taskman.entity.TaskMessage;

@UiController("taskman_TaskMessage.edit")
@UiDescriptor("task-message-edit.xml")
@EditedEntityContainer("taskMessageDc")
@LoadDataBeforeShow
public class TaskMessageEdit extends StandardEditor<TaskMessage> {
}