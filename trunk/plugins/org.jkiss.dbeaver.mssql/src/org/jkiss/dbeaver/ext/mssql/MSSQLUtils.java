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

package org.jkiss.dbeaver.ext.mssql;

import org.jkiss.dbeaver.core.Log;
import org.jkiss.dbeaver.model.DBPClientHome;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.runtime.RuntimeUtils;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * MSSQL utils
 */
public class MSSQLUtils {

    static final Log log = Log.getLog(MSSQLUtils.class);

    private static Map<String, Integer> typeMap = new HashMap<String, Integer>();
    public static final String COLUMN_POSTFIX_PRIV = "_priv";

    static {
        typeMap.put("BIT", java.sql.Types.BIT);
        typeMap.put("TINYINT", java.sql.Types.TINYINT);
        typeMap.put("SMALLINT", java.sql.Types.SMALLINT);
        typeMap.put("MEDIUMINT", java.sql.Types.INTEGER);
        typeMap.put("INT", java.sql.Types.INTEGER);
        typeMap.put("INTEGER", java.sql.Types.INTEGER);
        typeMap.put("INT24", java.sql.Types.INTEGER);
        typeMap.put("BIGINT", java.sql.Types.BIGINT);
        typeMap.put("REAL", java.sql.Types.DOUBLE);
        typeMap.put("FLOAT", java.sql.Types.FLOAT);
        typeMap.put("DECIMAL", java.sql.Types.DECIMAL);
        typeMap.put("NUMERIC", java.sql.Types.DECIMAL);
        typeMap.put("DOUBLE", java.sql.Types.DOUBLE);
        typeMap.put("CHAR", java.sql.Types.CHAR);
        typeMap.put("VARCHAR", java.sql.Types.VARCHAR);
        typeMap.put("DATE", java.sql.Types.DATE);
        typeMap.put("TIME", java.sql.Types.TIME);
        typeMap.put("YEAR", java.sql.Types.DATE);
        typeMap.put("TIMESTAMP", java.sql.Types.TIMESTAMP);
        typeMap.put("DATETIME", java.sql.Types.TIMESTAMP);
        typeMap.put("TINYBLOB", java.sql.Types.BINARY);
        typeMap.put("BLOB", java.sql.Types.LONGVARBINARY);
        typeMap.put("MEDIUMBLOB", java.sql.Types.LONGVARBINARY);
        typeMap.put("LONGBLOB", java.sql.Types.LONGVARBINARY);
        typeMap.put("TINYTEXT", java.sql.Types.VARCHAR);
        typeMap.put("TEXT", java.sql.Types.LONGVARCHAR);
        typeMap.put("MEDIUMTEXT", java.sql.Types.LONGVARCHAR);
        typeMap.put("LONGTEXT", java.sql.Types.LONGVARCHAR);
        typeMap.put(MSSQLConstants.TYPE_NAME_ENUM, java.sql.Types.CHAR);
        typeMap.put(MSSQLConstants.TYPE_NAME_SET, java.sql.Types.CHAR);
        typeMap.put("GEOMETRY", java.sql.Types.BINARY);
        typeMap.put("BINARY", java.sql.Types.BINARY);
        typeMap.put("VARBINARY", java.sql.Types.VARBINARY);
    }

    public static int typeNameToValueType(String typeName)
    {
        Integer valueType = typeMap.get(typeName.toUpperCase());
        return valueType == null ? java.sql.Types.OTHER : valueType;
    }

    public static List<String> collectPrivilegeNames(ResultSet resultSet)
    {
        // Now collect all privileges columns
        try {
            List<String> privs = new ArrayList<String>();
            ResultSetMetaData rsMetaData = resultSet.getMetaData();
            int colCount = rsMetaData.getColumnCount();
            for (int i = 0; i < colCount; i++) {
                String colName = rsMetaData.getColumnName(i + 1);
                if (colName.toLowerCase().endsWith(COLUMN_POSTFIX_PRIV)) {
                    privs.add(colName.substring(0, colName.length() - COLUMN_POSTFIX_PRIV.length()));
                }
            }
            return privs;
        } catch (SQLException e) {
            log.debug(e);
            return Collections.emptyList();
        }
    }

    public static Map<String, Boolean> collectPrivileges(List<String> privNames, ResultSet resultSet)
    {
        // Now collect all privileges columns
        Map<String, Boolean> privs = new TreeMap<String, Boolean>();
        for (String privName : privNames) {
            privs.put(privName, "Y".equals(JDBCUtils.safeGetString(resultSet, privName + COLUMN_POSTFIX_PRIV)));
        }
        return privs;
    }


    public static String getMSSQLConsoleBinaryName()
    {
        return RuntimeUtils.getNativeBinaryName("mssql");
    }

    public static File getHomeBinary(DBPClientHome home, String binName) throws IOException
    {
        binName = RuntimeUtils.getNativeBinaryName(binName);
        File dumpBinary = new File(home.getHomePath(), "bin/" + binName);
        if (!dumpBinary.exists()) {
            dumpBinary = new File(home.getHomePath(), binName);
            if (!dumpBinary.exists()) {
                throw new IOException("Utility '" + binName + "' not found in MSSQL home '" + home.getDisplayName() + "'");
            }
        }
        return dumpBinary;
    }

}