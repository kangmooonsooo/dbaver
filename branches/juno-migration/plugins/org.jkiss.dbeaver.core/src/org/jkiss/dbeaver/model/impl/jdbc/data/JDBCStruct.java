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
package org.jkiss.dbeaver.model.impl.jdbc.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jkiss.dbeaver.model.DBConstants;
import org.jkiss.dbeaver.model.data.DBDValue;
import org.jkiss.dbeaver.model.data.DBDValueCloneable;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;

import java.sql.SQLException;
import java.sql.Struct;

/**
 * Struct holder
 */
public class JDBCStruct implements DBDValue, DBDValueCloneable {

    static final Log log = LogFactory.getLog(JDBCStruct.class);

    private Struct contents;
    private JDBCArrayType type;

    public JDBCStruct(Struct contents)
    {
        this.contents = contents;
    }

    public Struct getValue() throws DBCException
    {
        return contents;
    }

    @Override
    public DBDValueCloneable cloneValue(DBRProgressMonitor monitor)
    {
        return new JDBCStruct(contents);
    }

    @Override
    public boolean isNull()
    {
        return contents == null;
    }

    @Override
    public DBDValue makeNull()
    {
        return new JDBCStruct(null);
    }

    @Override
    public void release()
    {
    }

    public String toString()
    {
        if (isNull()) {
            return DBConstants.NULL_VALUE_LABEL;
        } else {
            try {
                return makeStructString();
            } catch (SQLException e) {
                log.error(e);
                return contents.toString();
            }
        }
    }

    public String getTypeName()
    {
        try {
            return contents == null ? null : contents.getSQLTypeName();
        } catch (SQLException e) {
            log.error(e);
            return null;
        }
    }

    public String makeStructString() throws SQLException
    {
        if (isNull()) {
            return null;
        }
        if (contents == null) {
            return null;
        }
        StringBuilder str = new StringBuilder(200);
        String typeName = getTypeName();
        if (typeName != null) {
            str.append(typeName);
        }
        str.append("(");
        final Object[] attributes = contents.getAttributes();
        for (int i = 0, attributesLength = attributes.length; i < attributesLength; i++) {
            if (i > 0) str.append(',');
            str.append('\'');
            Object item = attributes[i];
            str.append(item == null ? DBConstants.NULL_VALUE_LABEL : item.toString());
            str.append('\'');
        }
        str.append(")");
        return str.toString();
    }

}