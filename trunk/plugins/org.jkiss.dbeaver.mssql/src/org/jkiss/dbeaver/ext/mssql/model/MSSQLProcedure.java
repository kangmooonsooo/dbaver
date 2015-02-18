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
package org.jkiss.dbeaver.ext.mssql.model;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.mssql.MSSQLConstants;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.struct.AbstractProcedure;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureParameterType;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureType;
import org.jkiss.dbeaver.utils.ContentUtils;
import org.jkiss.utils.CommonUtils;

import java.sql.ResultSet;
import java.util.Collection;

/**
 * GenericProcedure
 */
public class MSSQLProcedure extends AbstractProcedure<MSSQLDataSource, MSSQLCatalog> implements MSSQLSourceObject
{
    //static final Log log = Log.getLog(MSSQLProcedure.class);

    private DBSProcedureType procedureType;
    private String resultType;
    private String bodyType;
    private String body;
    private boolean deterministic;
    private transient String clientBody;
    private String charset;

    public MSSQLProcedure(MSSQLCatalog catalog)
    {
        super(catalog, false);
        this.procedureType = DBSProcedureType.PROCEDURE;
        this.bodyType = "SQL";
        this.resultType = "";
        this.deterministic = false;
    }

    public MSSQLProcedure(
        MSSQLCatalog catalog,
        ResultSet dbResult)
    {
        super(catalog, true);
        loadInfo(dbResult);
    }

    private void loadInfo(ResultSet dbResult)
    {
        setName(JDBCUtils.safeGetString(dbResult, MSSQLConstants.COL_ROUTINE_NAME));
        setDescription(JDBCUtils.safeGetString(dbResult, MSSQLConstants.COL_ROUTINE_COMMENT));
        this.procedureType = DBSProcedureType.valueOf(JDBCUtils.safeGetString(dbResult, MSSQLConstants.COL_ROUTINE_TYPE).toUpperCase());
        this.resultType = JDBCUtils.safeGetString(dbResult, MSSQLConstants.COL_DTD_IDENTIFIER);
        this.bodyType = JDBCUtils.safeGetString(dbResult, MSSQLConstants.COL_ROUTINE_BODY);
        this.body = JDBCUtils.safeGetString(dbResult, MSSQLConstants.COL_ROUTINE_DEFINITION);
        this.charset = JDBCUtils.safeGetString(dbResult, MSSQLConstants.COL_CHARACTER_SET_CLIENT);
        this.deterministic = JDBCUtils.safeGetBoolean(dbResult, MSSQLConstants.COL_IS_DETERMINISTIC, "YES");
        this.description = JDBCUtils.safeGetString(dbResult, MSSQLConstants.COL_ROUTINE_COMMENT);
    }

    @Override
    @Property(order = 2)
    public DBSProcedureType getProcedureType()
    {
        return procedureType ;
    }

    public void setProcedureType(DBSProcedureType procedureType)
    {
        this.procedureType = procedureType;
    }

    @Property(order = 2)
    public String getResultType()
    {
        return resultType;
    }

    @Property(order = 3)
    public String getBodyType()
    {
        return bodyType;
    }

    @Property(hidden = true, editable = true, updatable = true, order = -1)
    public String getBody()
    {
        if (this.body == null && !persisted) {
            this.body = "BEGIN" + ContentUtils.getDefaultLineSeparator() + "END";
            if (procedureType == DBSProcedureType.FUNCTION) {
                body = "RETURNS INT" + ContentUtils.getDefaultLineSeparator() + body;
            }
        }
        return body;
    }

    @Property(hidden = true, editable = true, updatable = true, order = -1)
    public String getClientBody(DBRProgressMonitor monitor)
        throws DBException
    {
        if (clientBody == null) {
            StringBuilder cb = new StringBuilder(getBody().length() + 100);
            cb.append(procedureType).append(' ').append(getFullQualifiedName()).append(" (");

            int colIndex = 0;
            for (MSSQLProcedureParameter column : CommonUtils.safeCollection(getParameters(monitor))) {
                if (column.getParameterType() == DBSProcedureParameterType.RETURN) {
                    continue;
                }
                if (colIndex > 0) {
                    cb.append(", ");
                }
                if (getProcedureType() == DBSProcedureType.PROCEDURE) {
                    cb.append(column.getParameterType()).append(' ');
                }
                cb.append(column.getName()).append(' ');
                appendParameterType(cb, column);
                colIndex++;
            }
            cb.append(")").append(ContentUtils.getDefaultLineSeparator());
            for (MSSQLProcedureParameter column : CommonUtils.safeCollection(getParameters(monitor))) {
                if (column.getParameterType() == DBSProcedureParameterType.RETURN) {
                    cb.append("RETURNS ");
                    appendParameterType(cb, column);
                    cb.append(ContentUtils.getDefaultLineSeparator());
                }
            }
            if (deterministic) {
                cb.append("DETERMINISTIC").append(ContentUtils.getDefaultLineSeparator());
            }
            cb.append(getBody());
            clientBody = cb.toString();
        }
        return clientBody;
    }

    @Property(editable = true, updatable = true, order = 3)
    public boolean isDeterministic()
    {
        return deterministic;
    }

    public void setDeterministic(boolean deterministic)
    {
        this.deterministic = deterministic;
    }

    private static void appendParameterType(StringBuilder cb, MSSQLProcedureParameter column)
    {
        cb.append(column.getTypeName());
        if (column.getMaxLength() > 0) {
            cb.append('(').append(column.getMaxLength()).append(')');
        }
    }

    public String getClientBody()
    {
        return clientBody;
    }

    public void setClientBody(String clientBody)
    {
        this.clientBody = clientBody;
    }

    //@Property(name = "Client Charset", order = 4)
    public String getCharset()
    {
        return charset;
    }

    @Override
    public Collection<MSSQLProcedureParameter> getParameters(DBRProgressMonitor monitor)
        throws DBException
    {
        return getContainer().proceduresCache.getChildren(monitor, getContainer(), this);
    }

    @Override
    public String getFullQualifiedName()
    {
        return DBUtils.getFullQualifiedName(getDataSource(),
            getContainer(),
            this);
    }

    @Override
    @Property(hidden = true, editable = true, updatable = true, order = -1)
    public String getSourceText(DBRProgressMonitor monitor) throws DBException
    {
        return getClientBody(monitor);
    }

    @Override
    public void setSourceText(String sourceText) throws DBException
    {
        setClientBody(sourceText);
    }
}