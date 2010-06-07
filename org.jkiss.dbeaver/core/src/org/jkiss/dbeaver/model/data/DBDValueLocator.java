/*
 * Copyright (c) 2010, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.model.data;

import net.sf.jkiss.utils.CommonUtils;
import org.jkiss.dbeaver.model.DBPObject;
import org.jkiss.dbeaver.model.anno.Property;
import org.jkiss.dbeaver.model.dbc.DBCColumnMetaData;
import org.jkiss.dbeaver.model.dbc.DBCTableIdentifier;
import org.jkiss.dbeaver.model.struct.DBSConstraint;
import org.jkiss.dbeaver.model.struct.DBSTable;
import org.jkiss.dbeaver.model.struct.DBSIndex;
import org.jkiss.dbeaver.model.struct.DBSStructureObject;

import java.util.List;

/**
 * Value locator.
 * Unique identifier of row in certain table.
 */
public class DBDValueLocator implements DBPObject {

    private DBSTable table;
    private DBCTableIdentifier tableIdentifier;

    public DBDValueLocator(DBSTable table, DBCTableIdentifier tableIdentifier)
    {
        this.table = table;
        this.tableIdentifier = tableIdentifier;
    }

    public String getKeyId(DBDRowController rowController)
    {
        StringBuilder keyId = new StringBuilder();
        List<? extends DBCColumnMetaData> keyColumns = getKeyColumns();
        for (DBCColumnMetaData keyColumn : keyColumns) {
            keyId.append('.').append(CommonUtils.escapeIdentifier(keyColumn.getColumnName()));
            Object keyValue = rowController.getColumnValue(keyColumn);
            keyId.append('-');
            keyId.append(CommonUtils.escapeIdentifier(keyValue == null ? "NULL" : keyValue.toString()));
        }
        return keyId.toString();
    }

    @Property(name = "Table", viewable = true, order = 1)
    public DBSTable getTable() {
        return table;
    }

    @Property(name = "Key", viewable = true, order = 2)
    public DBSStructureObject getUniqueKey() {
        return tableIdentifier.getConstraint() != null ? tableIdentifier.getConstraint() : tableIdentifier.getIndex();
    }

    public DBCTableIdentifier getTableIdentifier()
    {
        return tableIdentifier;
    }

    public DBSConstraint getUniqueConstraint()
    {
        return tableIdentifier.getConstraint();
    }

    public DBSIndex getUniqueIndex()
    {
        return tableIdentifier.getIndex();
    }

    public String getKeyKind()
    {
        if (tableIdentifier.getConstraint() != null) {
            return "CONSTRAINT";
        } else {
            return "INDEX";
        }
    }

    public String getKeyType()
    {
        if (tableIdentifier.getConstraint() != null) {
            return tableIdentifier.getConstraint().getConstraintType().name();
        } else {
            return tableIdentifier.getIndex().getIndexType().name();
        }
    }

    public List<? extends DBCColumnMetaData> getKeyColumns()
    {
        return tableIdentifier.getResultSetColumns();
    }

/*
    public Object[] getKeyValues(Object[] row) {
        Object[] keyValues = new Object[keyColumns.size()];
        for (DBSTableColumn column : keyColumns) {
            keyColumns
        }
        return keyValues;
    }
*/

}
