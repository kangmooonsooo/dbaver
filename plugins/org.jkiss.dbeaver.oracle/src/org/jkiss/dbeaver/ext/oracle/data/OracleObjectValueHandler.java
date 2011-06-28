/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ext.oracle.data;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.data.DBDValue;
import org.jkiss.dbeaver.model.data.DBDValueController;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCExecutionContext;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.impl.jdbc.data.JDBCAbstractValueHandler;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;

import java.sql.SQLException;

/**
 * Object type support
 */
public class OracleObjectValueHandler extends JDBCAbstractValueHandler {

    public static final OracleObjectValueHandler INSTANCE = new OracleObjectValueHandler();

    @Override
    public String getValueDisplayString(DBSTypedObject column, Object value)
    {
        if (value != null) {
            return "[OBJECT]";
        } else {
            return super.getValueDisplayString(column, value);
        }
    }

    protected DBDValue getColumnValue(DBCExecutionContext context, JDBCResultSet resultSet, DBSTypedObject column, int columnIndex) throws DBCException, SQLException
    {
        //final Object object = resultSet.getObject(columnIndex);
        Object object = resultSet.getObject(columnIndex);
        return new OracleObjectValue(object);
    }

    @Override
    protected void bindParameter(JDBCExecutionContext context, JDBCPreparedStatement statement, DBSTypedObject paramType, int paramIndex, Object value) throws DBCException, SQLException
    {
        throw new DBCException("Parameter bind is not implemented");
    }

    public Class getValueObjectType()
    {
        return java.lang.Object.class;
    }

    public Object copyValueObject(DBCExecutionContext context, DBSTypedObject column, Object value) throws DBCException
    {
        return null;
    }

    public boolean editValue(DBDValueController controller) throws DBException
    {
        return false;
    }

}
