package com.haulmont.taskman.web.task;

import com.haulmont.cuba.gui.screen.*;
import com.haulmont.taskman.entity.Task;

@UiController("taskman_Task.browse")
@UiDescriptor("task-browse.xml")
@LookupComponent("tasksTable")
@LoadDataBeforeShow
public class TaskBrowse extends StandardLookup<Task> {
}