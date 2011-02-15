/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ext.generic.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSEntity;
import org.jkiss.dbeaver.model.struct.DBSEntityQualified;
import org.jkiss.dbeaver.model.struct.DBSObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * GenericPackage
 */
public class GenericPackage extends GenericEntityContainer implements DBSEntityQualified
{
    static final Log log = LogFactory.getLog(GenericPackage.class);

    private GenericEntityContainer container;
    private String packageName;
    private List<GenericProcedure> procedures = new ArrayList<GenericProcedure>();
    private boolean nameFromCatalog;

    public GenericPackage(
        GenericEntityContainer container,
        String packageName,
        boolean nameFromCatalog)
    {
        this.container = container;
        this.packageName = packageName;
        this.nameFromCatalog = nameFromCatalog;
    }

    @Property(name = "Package", viewable = true, order = 1)
    public String getName()
    {
        return packageName;
    }

    public String getDescription()
    {
        return null;
    }

    public DBSObject getParentObject()
    {
        return container;
    }

    @Override
    public GenericDataSource getDataSource()
    {
        return container.getDataSource();
    }

    @Property(name = "Catalog", viewable = true, order = 3)
    public GenericCatalog getCatalog()
    {
        return container.getCatalog();
    }

    @Property(name = "Schema", viewable = true, order = 4)
    public GenericSchema getSchema()
    {
        return container.getSchema();
    }

    @Override
    public DBSObject getObject()
    {
        return this;
    }

    public List<GenericProcedure> getProcedures()
    {
        return procedures;
    }

    public void addProcedure(GenericProcedure procedure)
    {
        procedures.add(procedure);
    }

    public String getFullQualifiedName()
    {
        return DBUtils.getFullQualifiedName(getDataSource(),
            getCatalog() == null ? null : getCatalog().getName(),
            getSchema() == null ? null : getSchema().getName(),
            getName());
    }

    public Collection<? extends DBSEntity> getChildren(DBRProgressMonitor monitor) throws DBException
    {
        return procedures;
    }

    public DBSEntity getChild(DBRProgressMonitor monitor, String childName) throws DBException
    {
        return DBUtils.findObject(procedures, childName);
    }

    public Class<? extends DBSEntity> getChildType(DBRProgressMonitor monitor) throws DBException
    {
        return GenericProcedure.class;
    }

    public void cacheStructure(DBRProgressMonitor monitor, int scope) throws DBException
    {
    }

    public boolean refreshEntity(DBRProgressMonitor monitor) throws DBException
    {
        return false;
    }

    public boolean isNameFromCatalog()
    {
        return nameFromCatalog;
    }
}
