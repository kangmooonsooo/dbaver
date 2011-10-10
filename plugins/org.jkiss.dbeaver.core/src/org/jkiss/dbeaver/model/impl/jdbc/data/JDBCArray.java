/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.model.impl.jdbc.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.model.DBConstants;
import org.jkiss.dbeaver.model.data.DBDArray;
import org.jkiss.dbeaver.model.data.DBDValue;
import org.jkiss.dbeaver.model.data.DBDValueCloneable;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCExecutionContext;
import org.jkiss.dbeaver.model.impl.jdbc.api.JDBCResultSetImpl;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;
import org.jkiss.dbeaver.utils.ContentUtils;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Array holder
 */
public class JDBCArray implements DBDArray, DBDValueCloneable {

    static final Log log = LogFactory.getLog(JDBCArray.class);

    private Object[] contents;
    private JDBCArrayType type;

    public static Object makeArray(JDBCExecutionContext context, Array array)
    {
        if (array == null) {
            return null;
        }
        JDBCArrayType type;
        try {
            type = new JDBCArrayType(array.getBaseTypeName(), array.getBaseType());
        } catch (SQLException e) {
            type = new JDBCArrayType(CoreMessages.model_jdbc_unknown, java.sql.Types.OTHER);
        }
        if (!type.resolveHandler(context)) {
            // Could not resolve element type handler
            // So we just won't use array wrapper here
            return array;
        }

        Object[] contents = null;
        try {
            contents = extractDataFromArray(array);
        } catch (Exception e) {
            try {
                contents = extractDataFromResultSet(context, array, type);
            } catch (Exception e1) {
                log.warn("Could not extract array data from JDBC array"); //$NON-NLS-1$
            }
        }
        return new JDBCArray(contents, type);
    }

    private static Object[] extractDataFromResultSet(JDBCExecutionContext context, Array array, JDBCArrayType type) throws SQLException, DBCException
    {
        ResultSet dbResult = array.getResultSet();
        if (dbResult == null) {
            return null;
        }
        try {
            DBCResultSet resultSet = JDBCResultSetImpl.makeResultSet(context, dbResult, CoreMessages.model_jdbc_array_result_set);
            List<Object> data = new ArrayList<Object>();
            while (dbResult.next()) {
                data.add(type.getValueHandler().getValueObject(context, resultSet, type, 0));
            }
            return data.toArray();
        }
        finally {
            try {
                dbResult.close();
            } catch (SQLException e) {
                log.debug("Could not close array result set", e); //$NON-NLS-1$
            }
        }
    }

    private static Object[] extractDataFromArray(Array array) throws SQLException
    {
        Object arrObject = array.getArray();
        if (arrObject == null) {
            return null;
        }
        int arrLength = java.lang.reflect.Array.getLength(arrObject);
        Object[] contents = new Object[arrLength];
        for (int i = 0; i < arrLength; i++) {
            contents[i] = java.lang.reflect.Array.get(arrObject, i);
        }
        return contents;
    }

    public JDBCArray(Object[] contents, JDBCArrayType type)
    {
        this.contents = contents;
        this.type = type;
    }

    private Object[] getContents() throws DBCException
    {
        return contents;
    }

    public DBSTypedObject getElementType()
    {
        return type;
    }

    public Object[] getValue() throws DBCException
    {
        return getContents();
    }

    public DBDValueCloneable cloneValue(DBRProgressMonitor monitor)
    {
        return new JDBCArray(contents, type);
    }

    public boolean isNull()
    {
        return contents == null;
    }

    public DBDValue makeNull()
    {
        return new JDBCArray(null, type);
    }

    public void release()
    {
    }

    public String toString()
    {
        if (isNull()) {
            return DBConstants.NULL_VALUE_LABEL;
        } else {
            return makeArrayString();
        }
    }

    public String makeArrayString()
    {
        if (isNull()) {
            return null;
        }
        if (contents == null) {
            return null;
        }
        StringBuilder str = new StringBuilder();
        for (Object item : contents) {
            String itemString = type.getValueHandler().getValueDisplayString(type, item);
            if (str.length() > 0) {
                //str.append(ContentUtils.getDefaultLineSeparator());
                str.append(","); //$NON-NLS-1$
            }
            str.append(itemString);
        }
        return str.toString();
    }
}
