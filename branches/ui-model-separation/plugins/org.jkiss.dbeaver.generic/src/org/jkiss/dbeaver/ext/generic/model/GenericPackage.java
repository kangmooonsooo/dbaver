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
import org.jkiss.dbeaver.model.DBPQualifiedObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;

import java.util.ArrayList;
import java.util.Collection;

/**
 * GenericPackage
 */
public class GenericPackage extends GenericObjectContainer implements DBPQualifiedObject, GenericStoredCode
{

    private GenericStructContainer container;
    private String packageName;
    private boolean nameFromCatalog;

    public GenericPackage(
        GenericStructContainer container,
        String packageName,
        boolean nameFromCatalog)
    {
        super(container.getDataSource());
        this.container = container;
        this.packageName = packageName;
        this.nameFromCatalog = nameFromCatalog;
        this.procedures = new ArrayList<GenericProcedure>();
    }

    @Override
    @Property(viewable = true, order = 1)
    public String getName()
    {
        return packageName;
    }

    @Override
    public String getDescription()
    {
        return null;
    }

    @Override
    public DBSObject getParentObject()
    {
        return container;
    }

    @Override
    @Property(viewable = true, order = 3)
    public GenericCatalog getCatalog()
    {
        return container.getCatalog();
    }

    @Override
    @Property(viewable = true, order = 4)
    public GenericSchema getSchema()
    {
        return container.getSchema();
    }

    @Override
    public DBSObject getObject()
    {
        return this;
    }

    @Override
    public Collection<GenericProcedure> getProcedures(DBRProgressMonitor monitor)
        throws DBException
    {
        return procedures;
    }

    @Override
    public String getFullQualifiedName()
    {
        return DBUtils.getFullQualifiedName(getDataSource(),
            getCatalog(),
            getSchema(),
            this);
    }

    @Override
    public Collection<? extends DBSObject> getChildren(DBRProgressMonitor monitor) throws DBException
    {
        return procedures;
    }

    @Override
    public DBSObject getChild(DBRProgressMonitor monitor, String childName) throws DBException
    {
        return DBUtils.findObject(procedures, childName);
    }

    @Override
    public Class<? extends DBSObject> getChildType(DBRProgressMonitor monitor) throws DBException
    {
        return GenericProcedure.class;
    }

    @Override
    public void cacheStructure(DBRProgressMonitor monitor, int scope) throws DBException
    {
    }

    @Override
    public boolean refreshObject(DBRProgressMonitor monitor) throws DBException
    {
        procedures.clear();
        return false;
    }

    public boolean isNameFromCatalog()
    {
        return nameFromCatalog;
    }

    void addProcedure(GenericProcedure procedure)
    {
        procedures.add(procedure);
    }

    void orderProcedures()
    {
        DBUtils.orderObjects(procedures);
    }
}