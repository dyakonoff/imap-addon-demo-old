package com.haulmont.taskman.web.task;

import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.ScreenBuilders;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.model.DataContext;
import com.haulmont.cuba.gui.model.InstanceContainer;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.gui.util.OperationResult;
import com.haulmont.taskman.entity.Task;
import com.haulmont.taskman.entity.TaskMessage;

import javax.inject.Inject;

@UiController("taskman_Task.edit")
@UiDescriptor("task-edit.xml")
@EditedEntityContainer("taskDc")
@LoadDataBeforeShow
public class TaskEdit extends StandardEditor<Task> {
    @Inject
    private Table<TaskMessage> messagesTable;
    @Inject
    private ScreenBuilders screenBuilders;
    @Inject
    private Metadata metadata;
    @Inject
    private InstanceContainer<Task> taskDc;

    @Subscribe("messagesTable.edit")
    private void onMessagesTableEdit(Action.ActionPerformedEvent event) {
        screenBuilders.editor(messagesTable)
                .newEntity()
                .withScreenId(metadata.getClass(TaskMessage.class).getName() + ".create")
                .withLaunchMode(OpenMode.DIALOG)
                .build()
                .show()
                .addAfterCloseListener(e ->
                        getEditedEntityLoader().load()
                );
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    private void onPreCommit(DataContext.PreCommitEvent event) {
        if (taskDc.getItem().getNumber() == null) {
            // set it to 0, to get it finally assigned in the LIstener
            taskDc.getItem().setNumber(0L);
        }
    }




}