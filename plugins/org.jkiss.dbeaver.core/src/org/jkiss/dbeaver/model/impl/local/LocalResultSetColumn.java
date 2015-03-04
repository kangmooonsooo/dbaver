/*
 * Copyright (C) 2010-2015 Serge Rieder
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

package org.jkiss.dbeaver.model.impl.local;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.DBPDataKind;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.data.DBDPseudoAttribute;
import org.jkiss.dbeaver.model.exec.DBCAttributeMetaData;
import org.jkiss.dbeaver.model.exec.DBCEntityMetaData;

/**
 * LocalResultSetColumn
 */
public class LocalResultSetColumn implements DBCAttributeMetaData
{
    private final LocalResultSet resultSet;
    private final int index;
    private final String label;
    private final DBPDataKind dataKind;

    public LocalResultSetColumn(LocalResultSet resultSet, int index, String label, DBPDataKind dataKind)
    {
        this.resultSet = resultSet;
        this.index = index;
        this.label = label;
        this.dataKind = dataKind;
    }

    @Override
    public int getOrdinalPosition()
    {
        return index;
    }

    @Nullable
    @Override
    public Object getSource() {
        return null;
    }

    @NotNull
    @Override
    public String getLabel()
    {
        return label;
    }

    @Nullable
    @Override
    public String getEntityName()
    {
        return null;
    }

    @Override
    public boolean isReadOnly()
    {
        return true;
    }

    @Nullable
    @Override
    public DBDPseudoAttribute getPseudoAttribute()
    {
        return null;
    }

    @Nullable
    @Override
    public DBCEntityMetaData getEntityMetaData()
    {
        return null;
    }

    @Override
    public boolean isRequired()
    {
        return false;
    }

    @Override
    public boolean isAutoGenerated() {
        return false;
    }

    @Override
    public boolean isPseudoAttribute() {
        return false;
    }

    @Override
    public String getName()
    {
        return label;
    }

    @Override
    public String getTypeName()
    {
        return DBUtils.getDefaultDataTypeName(resultSet.getSession().getDataSource(), dataKind);
    }

    @Override
    public int getTypeID()
    {
        return 0;
    }

    @Override
    public DBPDataKind getDataKind()
    {
        return dataKind;
    }

    @Override
    public int getScale()
    {
        return 0;
    }

    @Override
    public int getPrecision()
    {
        return 0;
    }

    @Override
    public long getMaxLength()
    {
        return 0;
    }
}