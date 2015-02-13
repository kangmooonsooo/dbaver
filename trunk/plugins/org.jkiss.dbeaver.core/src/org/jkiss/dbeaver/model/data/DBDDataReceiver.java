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

package org.jkiss.dbeaver.model.data;

import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCResultSet;
import org.jkiss.dbeaver.model.exec.DBCSession;

/**
 * Data receiver.
 * Used to receive some result set data.
 * Result set can be a result of some query execution, cursor returned from stored procedure, generated keys result set, etc.
 */
public interface DBDDataReceiver {

    void fetchStart(DBCSession session, DBCResultSet resultSet, long offset, long maxRows)
        throws DBCException;

    void fetchRow(DBCSession session, DBCResultSet resultSet)
        throws DBCException;

    /**
     * Called after entire result set if fetched.
     * @throws DBCException on error
     * @param session execution context
     */
    void fetchEnd(DBCSession session)
        throws DBCException;

    /**
     * Called after entire result set is fetched and closed.
     * This method is called even if fetchStart wasn't called in this data receiver (may occur if statement throws an error)
     */
    void close();

}