/*
 * Copyright (C) 2010-2015 Serge Rieder serge@jkiss.org
 * Copyright (C) 2011-2012 Eugene Fradkin eugene.fradkin@gmail.com
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
package org.jkiss.dbeaver.ext.mssql.edit;

import org.eclipse.ui.IWorkbenchWindow;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.edit.DBEPersistAction;
import org.jkiss.dbeaver.ext.mssql.MSSQLMessages;
import org.jkiss.dbeaver.ext.mssql.model.MSSQLCatalog;
import org.jkiss.dbeaver.ext.mssql.model.MSSQLDataSource;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEObjectRenamer;
import org.jkiss.dbeaver.model.impl.DBSObjectCache;
import org.jkiss.dbeaver.model.impl.edit.SQLDatabasePersistAction;
import org.jkiss.dbeaver.model.impl.sql.edit.SQLObjectEditor;
import org.jkiss.dbeaver.ui.dialogs.EnterNameDialog;
import org.jkiss.utils.CommonUtils;

/**
 * MSSQLCatalogManager
 */
public class MSSQLCatalogManager extends SQLObjectEditor<MSSQLCatalog, MSSQLDataSource> implements DBEObjectRenamer<MSSQLCatalog> {

    @Override
    public long getMakerOptions()
    {
        return FEATURE_SAVE_IMMEDIATELY;
    }

    @Nullable
    @Override
    public DBSObjectCache<MSSQLDataSource, MSSQLCatalog> getObjectsCache(MSSQLCatalog object)
    {
        return object.getDataSource().getCatalogCache();
    }

    @Override
    protected MSSQLCatalog createDatabaseObject(IWorkbenchWindow workbenchWindow, DBECommandContext context, MSSQLDataSource parent, Object copyFrom)
    {
        String schemaName = EnterNameDialog.chooseName(workbenchWindow.getShell(), MSSQLMessages.edit_catalog_manager_dialog_schema_name);
        if (CommonUtils.isEmpty(schemaName)) {
            return null;
        }
        MSSQLCatalog newCatalog = new MSSQLCatalog(parent, null);
        newCatalog.setName(schemaName);
        return newCatalog;
    }

    @Override
    protected DBEPersistAction[] makeObjectCreateActions(ObjectCreateCommand command)
    {
        return new DBEPersistAction[] {
            new SQLDatabasePersistAction("Create schema", "CREATE SCHEMA `" + command.getObject().getName() + "`") //$NON-NLS-2$
        };
    }

    @Override
    protected DBEPersistAction[] makeObjectDeleteActions(ObjectDeleteCommand command)
    {
        return new DBEPersistAction[] {
            new SQLDatabasePersistAction("Drop schema", "DROP SCHEMA `" + command.getObject().getName() + "`") //$NON-NLS-2$
        };
    }

    @Override
    public void renameObject(DBECommandContext commandContext, MSSQLCatalog catalog, String newName) throws DBException
    {
        throw new DBException("Direct database rename is not yet implemented in MSSQL. You should use export/import functions for that.");
        //super.addCommand(new CommandRenameCatalog(newName), null);
        //saveChanges(monitor);
    }

/*
    private class CommandRenameCatalog extends DBECommandAbstract<MSSQLCatalog> {
        private String newName;

        protected CommandRenameCatalog(MSSQLCatalog catalog, String newName)
        {
            super(catalog, "Rename catalog");
            this.newName = newName;
        }
        public DBEPersistAction[] getPersistActions()
        {
            return new DBEPersistAction[] {
                new SQLDatabasePersistAction("Rename catalog", "RENAME SCHEMA " + getObject().getName() + " TO " + newName)
            };
        }

        @Override
        public void updateModel()
        {
            getObject().setName(newName);
            getObject().getDataSource().getContainer().fireEvent(
                new DBPEvent(DBPEvent.Action.OBJECT_UPDATE, getObject()));
        }
    }
*/

    /*
http://www.artfulsoftware.com/infotree/queries.php#112
Rename Database
It's sometimes necessary to rename a database. MSSQL 5.0 has no command for it. Simply bringing down the server to rename a database directory is not safe. MSSQL 5.1.7 introduced a RENAME DATABASE command, but the command left several unchanged database objects behind, and was found to lose data, so it was dropped in 5.1.23.

It seems a natural for a stored procedure using dynamic (prepared) statements. PREPARE supports CREATE | RENAME TABLE. As precautions:

    Before calling the sproc, the new database must have been created.
    The procedure refuses to rename the mssql database.
    The old database is left behind, minus what was moved.


DROP PROCEDURE IF EXISTS RenameDatabase;
DELIMITER go
CREATE PROCEDURE RenameDatabase( oldname CHAR (64), newname CHAR(64) )
BEGIN
  DECLARE version CHAR(32);
  DECLARE sname CHAR(64) DEFAULT NULL;
  DECLARE rows INT DEFAULT 1;
  DECLARE changed INT DEFAULT 0;
  IF STRCMP( oldname, 'mssql' ) <> 0 THEN
    REPEAT
      SELECT table_name INTO sname
      FROM information_schema.tables AS t
      WHERE t.table_type='BASE TABLE' AND t.table_schema = oldname
      LIMIT 1;
      SET rows = FOUND_ROWS();
      IF rows = 1 THEN
        SET @scmd = CONCAT( 'RENAME TABLE `', oldname, '`.`', sname,
                            '` TO `', newname, '`.`', sname, '`' );
        PREPARE cmd FROM @scmd;
        EXECUTE cmd;
        DEALLOCATE PREPARE cmd;
        SET changed = 1;
      END IF;
    UNTIL rows = 0 END REPEAT;
    IF changed > 0 THEN
      SET @scmd = CONCAT( "UPDATE mssql.db SET Db = '",
                          newname,
                          "' WHERE Db = '", oldname, "'" );
      PREPARE cmd FROM @scmd;
      EXECUTE cmd;
      DROP PREPARE cmd;
      SET @scmd = CONCAT( "UPDATE mssql.proc SET Db = '",
                          newname,
                          "' WHERE Db = '", oldname, "'" );
      PREPARE cmd FROM @scmd;
      EXECUTE cmd;
      DROP PREPARE cmd;
      SELECT version() INTO version;
      IF version >= '5.1.7' THEN
        SET @scmd = CONCAT( "UPDATE mssql.event SET db = '",
                            newname,
                            "' WHERE db = '", oldname, "'" );
        PREPARE cmd FROM @scmd;
        EXECUTE cmd;
        DROP PREPARE cmd;
      END IF;
      SET @scmd = CONCAT( "UPDATE mssql.columns_priv SET Db = '",
                          newname,
                          "' WHERE Db = '", oldname, "'" );
      PREPARE cmd FROM @scmd;
      EXECUTE cmd;
      DROP PREPARE cmd;
      FLUSH PRIVILEGES;
    END IF;
  END IF;
END;
go
DELIMITER ;

     */

}
