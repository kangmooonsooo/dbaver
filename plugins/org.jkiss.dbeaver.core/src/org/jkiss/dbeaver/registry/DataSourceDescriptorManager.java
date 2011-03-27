/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.registry;

import org.eclipse.ui.IWorkbenchWindow;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.model.DBPConnectionInfo;
import org.jkiss.dbeaver.model.edit.DBEObjectMaker;
import org.jkiss.dbeaver.model.impl.edit.DBEObjectManagerImpl;
import org.jkiss.dbeaver.ui.actions.datasource.DataSourceDisconnectHandler;
import org.jkiss.dbeaver.ui.dialogs.connection.ConnectionDialog;
import org.jkiss.dbeaver.ui.dialogs.connection.NewConnectionWizard;

import java.util.Map;

/**
 * DataSourceDescriptorManager
 */
public class DataSourceDescriptorManager extends DBEObjectManagerImpl<DataSourceDescriptor> implements DBEObjectMaker<DataSourceDescriptor> {

    public CreateResult createNewObject(IWorkbenchWindow workbenchWindow, Object parent, DataSourceDescriptor copyFrom)
    {
        if (copyFrom != null) {
            DataSourceRegistry registry = parent instanceof DataSourceRegistry ? (DataSourceRegistry)parent : copyFrom.getRegistry();
            DataSourceDescriptor dataSource = new DataSourceDescriptor(
                registry,
                DataSourceDescriptor.generateNewId(copyFrom.getDriver()),
                copyFrom.getDriver(),
                new DBPConnectionInfo(copyFrom.getConnectionInfo()));
            dataSource.setSchemaFilter(copyFrom.getSchemaFilter());
            dataSource.setCatalogFilter(copyFrom.getCatalogFilter());
            dataSource.setDescription(copyFrom.getDescription());
            dataSource.setSavePassword(copyFrom.isSavePassword());
            dataSource.setShowSystemObjects(copyFrom.isShowSystemObjects());
            // Generate new name
            String origName = copyFrom.getName();
            String newName = origName;
            for (int i = 0; ; i++) {
                if (registry.findDataSourceByName(newName) == null) {
                    break;
                }
                newName = origName + " " + (i + 1);
            }
            dataSource.setName(newName);
            registry.addDataSource(dataSource);
        } else {
            DataSourceRegistry registry;
            if (parent instanceof DataSourceRegistry) {
                registry = (DataSourceRegistry)parent;
            } else {
                registry = DBeaverCore.getInstance().getProjectRegistry().getActiveDataSourceRegistry();
            }
            ConnectionDialog dialog = new ConnectionDialog(workbenchWindow,
                new NewConnectionWizard(registry));
            dialog.open();
        }
        return CreateResult.CANCEL;
    }

    public void deleteObject(Map<String, Object> options)
    {
        Runnable remover = new Runnable() {
            public void run()
            {
                getObject().getRegistry().removeDataSource(getObject());
            }
        };
        if (getObject().isConnected()) {
            DataSourceDisconnectHandler.execute(getObject(), remover);
        } else {
            remover.run();
        }
    }

}