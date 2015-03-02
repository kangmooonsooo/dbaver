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
package org.jkiss.dbeaver.ext.wmi;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.wmi.model.WMIDataSource;
import org.jkiss.dbeaver.model.*;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSDataSourceContainer;
import org.jkiss.dbeaver.runtime.RuntimeUtils;
import org.jkiss.wmi.service.WMIService;

public class WMIDataSourceProvider implements DBPDataSourceProvider {


    private boolean libLoaded = false;

    @Override
    public void init(DBPApplication application)
    {
    }

    @Override
    public long getFeatures()
    {
        return FEATURE_SCHEMAS;
    }

    @Override
    public IPropertyDescriptor[] getConnectionProperties(
        IRunnableContext runnableContext,
        DBPDriver driver,
        DBPConnectionInfo connectionInfo) throws DBException
    {
        driver.validateFilesPresence(runnableContext);
        return null;
    }

    @Override
    public String getConnectionURL(DBPDriver driver, DBPConnectionInfo connectionInfo)
    {
        return
            "wmi://" + connectionInfo.getServerName() +
                "/" + connectionInfo.getHostName() +
                "/" + connectionInfo.getDatabaseName();
    }

    @Override
    public DBPDataSource openDataSource(DBRProgressMonitor monitor, DBSDataSourceContainer container) throws DBException
    {
        if (!libLoaded) {
            DBPDriver driver = container.getDriver();
            driver.loadDriver(RuntimeUtils.makeContext(monitor));
            loadNativeLib(driver);
            libLoaded = true;
        }
        return new WMIDataSource(container);
    }

    private void loadNativeLib(DBPDriver driver) throws DBException
    {
        for (DBPDriverFile libFile : driver.getFiles()) {
            if (libFile.matchesCurrentPlatform() && libFile.getType() == DBPDriverFileType.lib) {
                try {
                    WMIService.linkNative(libFile.getFile().getAbsolutePath());
                } catch (UnsatisfiedLinkError e) {
                    throw new DBException("Can't load native library '" + libFile.getFile().getAbsolutePath() + "'", e);
                }
            }
        }
    }

    @Override
    public void close()
    {
        //WMIService.unInitializeThread();
    }

}