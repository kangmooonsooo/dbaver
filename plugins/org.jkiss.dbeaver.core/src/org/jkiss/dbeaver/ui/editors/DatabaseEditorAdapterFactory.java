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
package org.jkiss.dbeaver.ui.editors;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.jkiss.dbeaver.ext.IDataSourceContainerProvider;
import org.jkiss.dbeaver.ext.IDatabaseEditorInput;
import org.jkiss.dbeaver.model.DBPObject;
import org.jkiss.dbeaver.model.navigator.DBNNode;
import org.jkiss.dbeaver.model.struct.DBSDataContainer;
import org.jkiss.dbeaver.model.struct.DBSDataSourceContainer;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.DBSWrapper;

/**
 * DatabaseEditorAdapterFactory
 */
public class DatabaseEditorAdapterFactory implements IAdapterFactory
{
    private static final Class<?>[] ADAPTER_LIST = { DBSObject.class, DBSDataContainer.class, DBSDataSourceContainer.class };

    @Override
    public Object getAdapter(Object adaptableObject, Class adapterType)
    {
        if (adapterType == DBSDataSourceContainer.class) {
            if (adaptableObject instanceof IEditorPart) {
                adaptableObject = ((IEditorPart) adaptableObject) .getEditorInput();
            }
            if (adaptableObject instanceof DBSDataSourceContainer) {
                return adaptableObject;
            }
            if (adaptableObject instanceof IDataSourceContainerProvider) {
                return ((IDataSourceContainerProvider)adaptableObject).getDataSourceContainer();
            }
            return null;
        } else if (DBPObject.class.isAssignableFrom(adapterType)) {
            if (adaptableObject instanceof IEditorPart) {
                IEditorInput editorInput = ((IEditorPart) adaptableObject).getEditorInput();
                if (editorInput instanceof IDatabaseEditorInput) {
                    DBNNode node = ((IDatabaseEditorInput) editorInput).getTreeNode();
                    if (node instanceof DBSWrapper) {
                        DBSObject object = ((DBSWrapper)node).getObject();
                        if (object != null && adapterType.isAssignableFrom(object.getClass())) {
                            return object;
                        }
                    }
                }
            }
        }/* else if (adaptableObject instanceof EntityEditor) {
            IEditorPart activeEditor = ((EntityEditor) adaptableObject).getActiveEditor();
            if (activeEditor != null) {
                if (adapterType.isAssignableFrom(activeEditor.getClass())) {
                    return activeEditor;
                }
                if (activeEditor instanceof IFolderedPart) {
                    Object activeFolder = ((IFolderedPart) activeEditor).getActiveFolder();
                    if (activeFolder != null) {
                        if (adapterType.isAssignableFrom(activeFolder.getClass())) {
                            return activeEditor;
                        }
                        if (activeFolder instanceof IAdaptable) {
                            return ((IAdaptable) activeFolder).getAdapter(adapterType);
                        }
                    }
                }
            }
        }*/

        return null;
    }

    @Override
    public Class[] getAdapterList()
    {
        return ADAPTER_LIST;
    }
}