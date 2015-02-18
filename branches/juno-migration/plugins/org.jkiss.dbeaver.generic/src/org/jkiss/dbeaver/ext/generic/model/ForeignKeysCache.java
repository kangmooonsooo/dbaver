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
package org.jkiss.dbeaver.ext.generic.model;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCExecutionContext;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCConstants;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.cache.JDBCCompositeCache;
import org.jkiss.dbeaver.model.struct.rdb.DBSForeignKeyDefferability;
import org.jkiss.dbeaver.model.struct.rdb.DBSForeignKeyModifyRule;
import org.jkiss.dbeaver.model.struct.DBSEntityConstraintType;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* Foreign key cache
*/
class ForeignKeysCache extends JDBCCompositeCache<GenericStructContainer, GenericTable, GenericTableForeignKey, GenericTableForeignKeyColumnTable> {

    Map<String, GenericPrimaryKey> pkMap = new HashMap<String, GenericPrimaryKey>();

    ForeignKeysCache(TableCache tableCache)
    {
        super(tableCache, GenericTable.class, JDBCConstants.FKTABLE_NAME, JDBCConstants.FK_NAME);
    }

    @Override
    public void clearCache()
    {
        pkMap.clear();
        super.clearCache();
    }

    @Override
    protected JDBCStatement prepareObjectsStatement(JDBCExecutionContext context, GenericStructContainer owner, GenericTable forParent)
        throws SQLException
    {
        return context.getMetaData().getImportedKeys(
            owner.getCatalog() == null ? null : owner.getCatalog().getName(),
            owner.getSchema() == null ? null : owner.getSchema().getName(),
            forParent == null ? null : forParent.getName())
            .getSource();
    }

    @Override
    protected GenericTableForeignKey fetchObject(JDBCExecutionContext context, GenericStructContainer owner, GenericTable parent, String fkName, ResultSet dbResult)
        throws SQLException, DBException
    {
        String pkTableCatalog = JDBCUtils.safeGetStringTrimmed(dbResult, JDBCConstants.PKTABLE_CAT);
        String pkTableSchema = JDBCUtils.safeGetStringTrimmed(dbResult, JDBCConstants.PKTABLE_SCHEM);
        String pkTableName = JDBCUtils.safeGetStringTrimmed(dbResult, JDBCConstants.PKTABLE_NAME);

        int keySeq = JDBCUtils.safeGetInt(dbResult, JDBCConstants.KEY_SEQ);
        int updateRuleNum = JDBCUtils.safeGetInt(dbResult, JDBCConstants.UPDATE_RULE);
        int deleteRuleNum = JDBCUtils.safeGetInt(dbResult, JDBCConstants.DELETE_RULE);
        String pkName = JDBCUtils.safeGetStringTrimmed(dbResult, JDBCConstants.PK_NAME);
        int defferabilityNum = JDBCUtils.safeGetInt(dbResult, JDBCConstants.DEFERRABILITY);

        DBSForeignKeyModifyRule deleteRule = JDBCUtils.getCascadeFromNum(deleteRuleNum);
        DBSForeignKeyModifyRule updateRule = JDBCUtils.getCascadeFromNum(updateRuleNum);
        DBSForeignKeyDefferability defferability;
        switch (defferabilityNum) {
            case DatabaseMetaData.importedKeyInitiallyDeferred: defferability = DBSForeignKeyDefferability.INITIALLY_DEFERRED; break;
            case DatabaseMetaData.importedKeyInitiallyImmediate: defferability = DBSForeignKeyDefferability.INITIALLY_IMMEDIATE; break;
            case DatabaseMetaData.importedKeyNotDeferrable: defferability = DBSForeignKeyDefferability.NOT_DEFERRABLE; break;
            default: defferability = DBSForeignKeyDefferability.UNKNOWN; break;
        }

        if (pkTableName == null) {
            log.debug("Null PK table name");
            return null;
        }
        //String pkTableFullName = DBUtils.getFullQualifiedName(getDataSource(), pkTableCatalog, pkTableSchema, pkTableName);
        GenericTable pkTable = parent.getDataSource().findTable(context.getProgressMonitor(), pkTableCatalog, pkTableSchema, pkTableName);
        if (pkTable == null) {
            log.warn("Can't find PK table " + pkTableName);
            return null;
        }

        // Find PK
        GenericPrimaryKey pk = null;
        if (pkName != null) {
            pk = DBUtils.findObject(pkTable.getConstraints(context.getProgressMonitor()), pkName);
            if (pk == null) {
                log.warn("Unique key '" + pkName + "' not found in table " + pkTable.getFullQualifiedName());
            }
        }
        if (pk == null) {
            String pkColumnName = JDBCUtils.safeGetStringTrimmed(dbResult, JDBCConstants.PKCOLUMN_NAME);
            GenericTableColumn pkColumn = pkTable.getAttribute(context.getProgressMonitor(), pkColumnName);
            if (pkColumn == null) {
                log.warn("Can't find PK table " + pkTable.getFullQualifiedName() + " column " + pkColumnName);
                return null;
            }

            Collection<GenericPrimaryKey> uniqueKeys = pkTable.getConstraints(context.getProgressMonitor());
            if (uniqueKeys != null) {
                for (GenericPrimaryKey pkConstraint : uniqueKeys) {
                    if (pkConstraint.getConstraintType().isUnique() && DBUtils.getConstraintColumn(context.getProgressMonitor(), pkConstraint, pkColumn) != null) {
                        pk = pkConstraint;
                        break;
                    }
                }
            }
            if (pk == null) {
                log.warn("Could not find unique key for table " + pkTable.getFullQualifiedName() + " column " + pkColumn.getName());
                // Too bad. But we have to create new fake PK for this FK
                String pkFullName = pkTable.getFullQualifiedName() + "." + pkName;
                pk = pkMap.get(pkFullName);
                if (pk == null) {
                    pk = new GenericPrimaryKey(pkTable, pkName, null, DBSEntityConstraintType.PRIMARY_KEY, true);
                    pkMap.put(pkFullName, pk);
                    // Add this fake constraint to it's owner
                    pk.getTable().addUniqueKey(pk);
                }
                pk.addColumn(new GenericTableConstraintColumn(pk, pkColumn, keySeq));
            }
        }

        return new GenericTableForeignKey(parent, fkName, null, pk, deleteRule, updateRule, defferability, true);
    }

    @Override
    protected GenericTableForeignKeyColumnTable fetchObjectRow(
        JDBCExecutionContext context,
        GenericTable parent, GenericTableForeignKey foreignKey, ResultSet dbResult)
        throws SQLException, DBException
    {
        String pkColumnName = JDBCUtils.safeGetStringTrimmed(dbResult, JDBCConstants.PKCOLUMN_NAME);
        GenericTableConstraintColumn pkColumn = (GenericTableConstraintColumn)DBUtils.getConstraintColumn(context.getProgressMonitor(), foreignKey.getReferencedConstraint(), pkColumnName);
        if (pkColumn == null) {
            log.warn("Can't find PK table " + foreignKey.getReferencedConstraint().getTable().getFullQualifiedName() + " column " + pkColumnName);
            return null;
        }
        int keySeq = JDBCUtils.safeGetInt(dbResult, JDBCConstants.KEY_SEQ);

        String fkColumnName = JDBCUtils.safeGetStringTrimmed(dbResult, JDBCConstants.FKCOLUMN_NAME);
        GenericTableColumn fkColumn = foreignKey.getTable().getAttribute(context.getProgressMonitor(), fkColumnName);
        if (fkColumn == null) {
            log.warn("Can't find FK table " + foreignKey.getTable().getFullQualifiedName() + " column " + fkColumnName);
            return null;
        }

        return new GenericTableForeignKeyColumnTable(foreignKey, fkColumn, keySeq, pkColumn.getAttribute());
    }

    @Override
    protected void cacheChildren(GenericTableForeignKey foreignKey, List<GenericTableForeignKeyColumnTable> rows)
    {
        foreignKey.setColumns(rows);
    }

}