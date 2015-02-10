/*
 * Copyright (C) 2013      Denis Forveille titou10.titou10@gmail.com
 * Copyright (C) 2010-2014 Serge Rieder serge@jkiss.org
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
package org.jkiss.dbeaver.ext.db2.manager;

import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.ext.db2.model.DB2Bufferpool;
import org.jkiss.dbeaver.ext.db2.model.DB2DataSource;
import org.jkiss.dbeaver.model.impl.DBSObjectCache;

/**
 * DB2 Bufferpool Manager
 * 
 * @author Denis Forveille
 */
public class DB2BufferpoolManager extends DB2AbstractDropOnlyManager<DB2Bufferpool, DB2DataSource> {

    private static final String SQL_DROP = "DROP BUFFERPOOL %s";

    @Override
    public String buildDropStatement(DB2Bufferpool db2Bufferpool)
    {
        String name = db2Bufferpool.getName();
        return String.format(SQL_DROP, name);
    }

    @Nullable
    @Override
    public DBSObjectCache<DB2DataSource, DB2Bufferpool> getObjectsCache(DB2Bufferpool db2Bufferpool)
    {
        return db2Bufferpool.getDataSource().getBufferpoolCache();
    }

}