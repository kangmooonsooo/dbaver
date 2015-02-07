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

package org.jkiss.dbeaver.model;

import java.util.List;
import java.util.Set;

/**
 * Keyword manager.
 * <p/>
 * Contains information about some concrete datasource underlying database syntax.
 * Support runtime change of datasource (reloads syntax information)
 */
public interface DBPKeywordManager extends DBPCommentsManager {

    Set<String> getReservedWords();

    Set<String> getFunctions();

    Set<String> getTypes();

    DBPKeywordType getKeywordType(String word);

    List<String> getMatchedKeywords(String word);

    boolean isKeywordStart(String word);

    boolean isEntityQueryWord(String word);

    boolean isAttributeQueryWord(String word);

}