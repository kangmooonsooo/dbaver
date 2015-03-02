/*
 * Copyright (C) 2013      Denis Forveille titou10.titou10@gmail.com
 * Copyright (C) 2010-2015 Serge Rieder serge@jkiss.org
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
import org.jkiss.dbeaver.ext.db2.model.DB2Schema;
import org.jkiss.dbeaver.ext.db2.model.fed.DB2Nickname;
import org.jkiss.dbeaver.model.impl.DBSObjectCache;

/**
 * DB2 Federated Nickname Manager
 * 
 * @author Denis Forveille
 */
public class DB2NicknameManager extends DB2AbstractDropOnlyManager<DB2Nickname, DB2Schema> {

    private static final String SQL_DROP = "DROP NICKNAME %s";

    @Override
    public String buildDropStatement(DB2Nickname db2Nickname)
    {
        String fullyQualifiedName = db2Nickname.getFullQualifiedName();
        return String.format(SQL_DROP, fullyQualifiedName);
    }

    @Nullable
    @Override
    public DBSObjectCache<DB2Schema, DB2Nickname> getObjectsCache(DB2Nickname db2Nickname)
    {
        return db2Nickname.getSchema().getNicknameCache();
    }

}