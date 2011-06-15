/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ext.oracle.model;

import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.DBSProcedureColumn;
import org.jkiss.dbeaver.model.struct.DBSProcedureColumnType;

import java.sql.ResultSet;

/**
 * OracleProcedureArgument
 */
public class OracleProcedureArgument implements DBSProcedureColumn
{
    private final OracleProcedure procedure;
    private String name;
    private OracleParameterMode mode;
    private OracleDataType type;
    private OracleDataType dataType;
    private int dataLength;
    private int dataScale;
    private int dataPrecision;

    public OracleProcedureArgument(
        DBRProgressMonitor monitor,
        OracleProcedure procedure,
        ResultSet dbResult)
    {
        this.procedure = procedure;
        this.name = JDBCUtils.safeGetString(dbResult, "ARGUMENT_NAME");
        this.mode = OracleParameterMode.getMode(JDBCUtils.safeGetString(dbResult, "IN_OUT"));
        this.type = OracleDataType.resolveDataType(
            monitor,
            procedure.getDataSource(),
            null,
            JDBCUtils.safeGetString(dbResult, "DATA_TYPE"));
        this.dataType = OracleDataType.resolveDataType(
            monitor,
            procedure.getDataSource(),
            JDBCUtils.safeGetString(dbResult, "TYPE_OWNER"),
            JDBCUtils.safeGetString(dbResult, "TYPE_NAME"));
        this.dataLength = JDBCUtils.safeGetInt(dbResult, "DATA_LENGTH");
        this.dataScale = JDBCUtils.safeGetInt(dbResult, "DATA_SCALE");
        this.dataPrecision = JDBCUtils.safeGetInt(dbResult, "DATA_PRECISION");
    }

    public String getDescription()
    {
        return null;
    }

    public DBSObject getParentObject()
    {
        return getProcedure();
    }

    public OracleDataSource getDataSource()
    {
        return procedure.getDataSource();
    }

    public OracleProcedure getProcedure()
    {
        return procedure;
    }

    public boolean isPersisted()
    {
        return true;
    }

    @Property(name = "Name", viewable = true, order = 10)
    public String getName()
    {
        return name;
    }

    @Property(name = "In/Out", viewable = true, order = 20)
    public DBSProcedureColumnType getColumnType()
    {
        return mode == null ? null : mode.getColumnType();
    }

    public boolean isNotNull()
    {
        return false;
    }

    @Property(name = "Length", viewable = true, order = 30)
    public long getMaxLength()
    {
        return dataLength;
    }

    public String getTypeName()
    {
        return type.getName();
    }

    public int getValueType()
    {
        return type.getValueType();
    }

    @Property(name = "Scale", viewable = true, order = 40)
    public int getScale()
    {
        return dataScale;
    }

    @Property(name = "Precision", viewable = true, order = 50)
    public int getPrecision()
    {
        return dataPrecision;
    }
}
