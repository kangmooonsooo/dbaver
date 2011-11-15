/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.model;

import org.jkiss.dbeaver.model.runtime.DBRShellCommand;

import java.util.*;

/**
 * DBPConnectionInfo
 */
public class DBPConnectionInfo implements DBPObject
{

    //private DBPDriver driver;
    private String clientHomeId;
    private String hostName;
    private String hostPort;
    private String serverName;
    private String databaseName;
    private String userName;
    private String userPassword;
    private String url;
    private final Map<Object, Object> properties;
    private final Map<DBPConnectionEventType, DBRShellCommand> events;

    public DBPConnectionInfo()
    {
        this.properties = new HashMap<Object, Object>();
        this.events = new HashMap<DBPConnectionEventType, DBRShellCommand>();
    }

    public DBPConnectionInfo(DBPConnectionInfo info)
    {
        this.hostName = info.hostName;
        this.hostPort = info.hostPort;
        this.serverName = info.serverName;
        this.databaseName = info.databaseName;
        this.userName = info.userName;
        this.userPassword = info.userPassword;
        this.url = info.url;
        this.properties = new HashMap<Object, Object>(info.properties);
        this.events = new HashMap<DBPConnectionEventType, DBRShellCommand>(info.events);
    }

/*
	public DBPConnectionInfo(DBPDriver driver)
	{
		this.driver = driver;
	}

	public DBPDriver getDriver()
	{
		return driver;
	}
*/

    public String getClientHomeId()
    {
        return clientHomeId;
    }

    public void setClientHomeId(String clientHomeId)
    {
        this.clientHomeId = clientHomeId;
    }

    public String getHostName()
    {
        return hostName;
    }

    public void setHostName(String hostName)
    {
        this.hostName = hostName;
    }

    public String getHostPort()
    {
        return hostPort;
    }

    public void setHostPort(String hostPort)
    {
        this.hostPort = hostPort;
    }

    public String getServerName()
    {
        return serverName;
    }

    public void setServerName(String serverName)
    {
        this.serverName = serverName;
    }

    public String getDatabaseName()
    {
        return databaseName;
    }

    public void setDatabaseName(String databaseName)
    {
        this.databaseName = databaseName;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getUserPassword()
    {
        return userPassword;
    }

    public void setUserPassword(String userPassword)
    {
        this.userPassword = userPassword;
    }

    public Map<Object, Object> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<Object, Object> properties)
    {
        this.properties.clear();
        this.properties.putAll(properties);
    }

    public DBRShellCommand getEvent(DBPConnectionEventType eventType)
    {
        return events.get(eventType);
    }

    public void setEvent(DBPConnectionEventType eventType, DBRShellCommand command)
    {
        if (command == null) {
            events.remove(eventType);
        } else {
            events.put(eventType, command);
        }
    }

    public DBPConnectionEventType[] getDeclaredEvents()
    {
        Set<DBPConnectionEventType> eventTypes = events.keySet();
        return eventTypes.toArray(new DBPConnectionEventType[eventTypes.size()]);
    }
}
