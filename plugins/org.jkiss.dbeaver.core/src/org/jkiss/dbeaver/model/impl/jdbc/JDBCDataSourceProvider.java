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
package org.jkiss.dbeaver.model.impl.jdbc;

import org.jkiss.dbeaver.core.Log;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.model.*;
import org.jkiss.dbeaver.ui.properties.PropertyDescriptorEx;

import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * JDBCDataSourceProvider
 */
public abstract class JDBCDataSourceProvider implements DBPDataSourceProvider {
    static final protected Log log = Log.getLog(JDBCDataSourceProvider.class);

    @Override
    public void close() {

    }

    @Override
    public void init(DBPApplication application) {

    }

    @Override
    public IPropertyDescriptor[] getConnectionProperties(
        IRunnableContext runnableContext,
        DBPDriver driver,
        DBPConnectionInfo connectionInfo)
        throws DBException {
        Collection<IPropertyDescriptor> props = null;
        Object driverInstance = driver.getDriverInstance(runnableContext);
        if (driverInstance instanceof Driver) {
            props = readDriverProperties(connectionInfo, (Driver) driverInstance);
        }
        if (props == null) {
            return null;
        }
        return props.toArray(new IPropertyDescriptor[props.size()]);
    }

    private Collection<IPropertyDescriptor> readDriverProperties(
        DBPConnectionInfo connectionInfo,
        Driver driver)
        throws DBException {
        Properties driverProps = new Properties();
        //driverProps.putAll(connectionInfo.getProperties());
        DriverPropertyInfo[] propDescs;
        try {
            propDescs = driver.getPropertyInfo(connectionInfo.getUrl(), driverProps);
        } catch (Throwable e) {
            log.debug("Cannot obtain driver's properties", e); //$NON-NLS-1$
            return null;
        }
        if (propDescs == null) {
            return null;
        }

        List<IPropertyDescriptor> properties = new ArrayList<IPropertyDescriptor>();
        for (DriverPropertyInfo desc : propDescs) {
            if (DBConstants.DATA_SOURCE_PROPERTY_USER.equals(desc.name) || DBConstants.DATA_SOURCE_PROPERTY_PASSWORD.equals(desc.name)) {
                // Skip user/password properties
                continue;
            }
            desc.value = getConnectionPropertyDefaultValue(desc.name, desc.value);
            properties.add(new PropertyDescriptorEx(
                CoreMessages.model_jdbc_driver_properties,
                desc.name,
                desc.name,
                desc.description,
                String.class,
                desc.required,
                desc.value,
                desc.choices,
                true));
        }
        return properties;
    }

    protected String getConnectionPropertyDefaultValue(String name, String value) {
        return value;
    }
}