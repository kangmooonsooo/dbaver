/*
 * Copyright (C) 2010-2015 Serge Rieder
 * serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jkiss.dbeaver.ext.generic.edit;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.ext.generic.model.*;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.impl.DBObjectNameCaseTransformer;
import org.jkiss.dbeaver.model.impl.DBSObjectCache;
import org.jkiss.dbeaver.model.impl.jdbc.edit.struct.JDBCForeignKeyManager;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.rdb.DBSForeignKeyDefferability;
import org.jkiss.dbeaver.model.struct.rdb.DBSForeignKeyModifyRule;
import org.jkiss.dbeaver.ui.dialogs.struct.EditForeignKeyDialog;
import org.jkiss.utils.CommonUtils;

/**
 * Generic foreign manager
 */
public class GenericForeignKeyManager extends JDBCForeignKeyManager<GenericTableForeignKey, GenericTable> {

    @Nullable
    @Override
    public DBSObjectCache<? extends DBSObject, GenericTableForeignKey> getObjectsCache(GenericTableForeignKey object)
    {
        return object.getParentObject().getContainer().getForeignKeysCache();
    }

    @Override
    protected GenericTableForeignKey createDatabaseObject(IWorkbenchWindow workbenchWindow, DBECommandContext context, GenericTable table, Object from)
    {
        EditForeignKeyDialog editDialog = new EditForeignKeyDialog(
            workbenchWindow.getShell(),
            "Create foreign key",
            table,
            new DBSForeignKeyModifyRule[] {
                DBSForeignKeyModifyRule.NO_ACTION,
                DBSForeignKeyModifyRule.CASCADE, DBSForeignKeyModifyRule.RESTRICT,
                DBSForeignKeyModifyRule.SET_NULL,
                DBSForeignKeyModifyRule.SET_DEFAULT });
        if (editDialog.open() != IDialogConstants.OK_ID) {
            return null;
        }

        final GenericTableForeignKey foreignKey = new GenericTableForeignKey(
            table,
            null,
            null,
            (GenericPrimaryKey) editDialog.getUniqueConstraint(),
            editDialog.getOnDeleteRule(),
            editDialog.getOnUpdateRule(),
            DBSForeignKeyDefferability.NOT_DEFERRABLE,
            false);
        foreignKey.setName(DBObjectNameCaseTransformer.transformName(foreignKey,
                CommonUtils.escapeIdentifier(table.getName()) + "_" +
                        CommonUtils.escapeIdentifier(editDialog.getUniqueConstraint().getParentObject().getName()) + "_FK"));
        int colIndex = 1;
        for (EditForeignKeyDialog.FKColumnInfo tableColumn : editDialog.getColumns()) {
            foreignKey.addColumn(
                new GenericTableForeignKeyColumnTable(
                    foreignKey,
                    (GenericTableColumn) tableColumn.getOwnColumn(),
                    colIndex++,
                    (GenericTableColumn) tableColumn.getRefColumn()));
        }
        return foreignKey;
    }

}