package com.haulmont.taskman.web.task;

import com.haulmont.cuba.gui.screen.*;
import com.haulmont.taskman.entity.Task;

@UiController("taskman_Task.edit")
@UiDescriptor("task-edit.xml")
@EditedEntityContainer("taskDc")
@LoadDataBeforeShow
public class TaskEdit extends StandardEditor<Task> {
}