/*
 * Copyright (C) 2010-2014 Serge Rieder
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
package org.jkiss.dbeaver.ext.postgresql.model;

import org.jkiss.dbeaver.core.Log;
import org.eclipse.core.runtime.IConfigurationElement;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.postgresql.PostgreUtils;
import org.jkiss.dbeaver.ext.generic.model.GenericProcedure;
import org.jkiss.dbeaver.ext.generic.model.GenericStructContainer;
import org.jkiss.dbeaver.ext.generic.model.GenericTable;
import org.jkiss.dbeaver.ext.generic.model.meta.GenericMetaModel;
import org.jkiss.dbeaver.model.exec.DBCExecutionPurpose;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.sql.SQLUtils;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureType;

import java.sql.SQLException;

/**
 * PostgreMetaModel
 */
public class PostgreMetaModel extends GenericMetaModel
{
    static final Log log = Log.getLog(PostgreMetaModel.class);

    public PostgreMetaModel(IConfigurationElement cfg) {
        super(cfg);
    }

    public String getViewDDL(DBRProgressMonitor monitor, GenericTable sourceObject) throws DBException {
        JDBCSession session = sourceObject.getDataSource().openSession(monitor, DBCExecutionPurpose.META, "Read view definition");
        try {
            return JDBCUtils.queryString(session, "SELECT definition FROM PG_CATALOG.PG_VIEWS WHERE SchemaName=? and ViewName=?", sourceObject.getContainer().getName(), sourceObject.getName());
        } catch (SQLException e) {
            throw new DBException(e, sourceObject.getDataSource());
        } finally {
            session.close();
        }
    }

    @Override
    public String getProcedureDDL(DBRProgressMonitor monitor, GenericProcedure sourceObject) throws DBException {
        JDBCSession session = sourceObject.getDataSource().openSession(monitor, DBCExecutionPurpose.META, "Read procedure definition");
        try {
            return JDBCUtils.queryString(session, "SELECT p.prosrc FROM PG_CATALOG.PG_PROC P, PG_CATALOG.PG_NAMESPACE NS\n" +
                "WHERE ns.oid=p.pronamespace and ns.nspname=? AND p.proname=?", sourceObject.getContainer().getName(), sourceObject.getName());
        } catch (SQLException e) {
            throw new DBException(e, sourceObject.getDataSource());
        } finally {
            session.close();
        }
    }
}