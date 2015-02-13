/*
 * Copyright (C) 2010-2012 Serge Rieder
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
package org.jkiss.dbeaver.model.impl.jdbc.edit.struct;

import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.ext.IDatabasePersistAction;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.edit.prop.DBECommandComposite;
import org.jkiss.dbeaver.model.impl.edit.AbstractDatabasePersistAction;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTable;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTableConstraint;
import org.jkiss.dbeaver.model.struct.DBSEntityAttributeRef;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableForeignKey;
import org.jkiss.dbeaver.runtime.VoidProgressMonitor;
import org.jkiss.utils.CommonUtils;

import java.util.Collection;

/**
 * JDBC foreign key manager
 */
public abstract class JDBCForeignKeyManager<OBJECT_TYPE extends JDBCTableConstraint<TABLE_TYPE> & DBSTableForeignKey, TABLE_TYPE extends JDBCTable>
    extends JDBCObjectEditor<OBJECT_TYPE, TABLE_TYPE>
{

    @Override
    public long getMakerOptions()
    {
        return FEATURE_EDITOR_ON_CREATE;
    }

    @Override
    protected IDatabasePersistAction[] makeObjectCreateActions(ObjectCreateCommand command)
    {
        final TABLE_TYPE table = command.getObject().getTable();
        return new IDatabasePersistAction[] {
            new AbstractDatabasePersistAction(
                CoreMessages.model_jdbc_create_new_foreign_key,
                "ALTER TABLE " + table.getFullQualifiedName() + " ADD " + getNestedDeclaration(table, command)) //$NON-NLS-1$ //$NON-NLS-2$
        };
    }

    @Override
    protected IDatabasePersistAction[] makeObjectDeleteActions(ObjectDeleteCommand command)
    {
        return new IDatabasePersistAction[] {
            new AbstractDatabasePersistAction(
                CoreMessages.model_jdbc_drop_foreign_key,
                getDropForeignKeyPattern(command.getObject())
                    .replace(PATTERN_ITEM_TABLE, command.getObject().getTable().getFullQualifiedName())
                    .replace(PATTERN_ITEM_CONSTRAINT, command.getObject().getName()))
        };
    }

    @Override
    protected StringBuilder getNestedDeclaration(TABLE_TYPE owner, DBECommandComposite<OBJECT_TYPE, PropertyHandler> command)
    {
        OBJECT_TYPE foreignKey = command.getObject();

        // Create column
        String constraintName = DBUtils.getQuotedIdentifier(foreignKey.getDataSource(), foreignKey.getName());

        StringBuilder decl = new StringBuilder(40);
        decl
            .append("CONSTRAINT ").append(constraintName) //$NON-NLS-1$
            .append(" ").append(foreignKey.getConstraintType().getName().toUpperCase()) //$NON-NLS-1$
            .append(" ("); //$NON-NLS-1$
        // Get columns using void monitor
        final Collection<? extends DBSEntityAttributeRef> columns = command.getObject().getAttributeReferences(VoidProgressMonitor.INSTANCE);
        boolean firstColumn = true;
        for (DBSEntityAttributeRef constraintColumn : columns) {
            if (!firstColumn) decl.append(","); //$NON-NLS-1$
            firstColumn = false;
            decl.append(constraintColumn.getAttribute().getName());
        }
        decl.append(") REFERENCES ").append(foreignKey.getReferencedConstraint().getParentObject().getFullQualifiedName()).append("("); //$NON-NLS-1$ //$NON-NLS-2$
        firstColumn = true;
        for (DBSEntityAttributeRef constraintColumn : foreignKey.getReferencedConstraint().getAttributeReferences(VoidProgressMonitor.INSTANCE)) {
            if (!firstColumn) decl.append(","); //$NON-NLS-1$
            firstColumn = false;
            decl.append(constraintColumn.getAttribute().getName());
        }
        decl.append(")"); //$NON-NLS-1$
        if (foreignKey.getDeleteRule() != null && !CommonUtils.isEmpty(foreignKey.getDeleteRule().getClause())) {
            decl.append(" ON DELETE ").append(foreignKey.getDeleteRule().getClause()); //$NON-NLS-1$
        }
        if (foreignKey.getUpdateRule() != null && !CommonUtils.isEmpty(foreignKey.getUpdateRule().getClause())) {
            decl.append(" ON UPDATE ").append(foreignKey.getUpdateRule().getClause()); //$NON-NLS-1$
        }
        return decl;
    }

    protected String getDropForeignKeyPattern(OBJECT_TYPE constraint)
    {
        return "ALTER TABLE " + PATTERN_ITEM_TABLE + " DROP CONSTRAINT " + PATTERN_ITEM_CONSTRAINT; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
