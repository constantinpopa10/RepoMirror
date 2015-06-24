/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repomirror.data;

/**
 * 
 * @author sglover
 *
 */
public class FileData
{
	private int randomizer;
	private String siteId;
	private String username;
	private String nodeId;
	private String nodePath;
	private String name;
	private String nodeType;

	public FileData(String siteId, String username, String nodeId,
            String nodePath, String name, String nodeType)
    {
        this((int)(Math.random() * 1E6), siteId, username, nodeId, nodePath, name, nodeType);
    }

	public FileData(int randomizer, String siteId, String username, String nodeId,
            String nodePath, String name, String nodeType)
    {
	    super();
        this.randomizer = randomizer;
	    this.siteId = siteId;
	    this.username = username;
	    this.nodeId = nodeId;
	    this.nodePath = nodePath;
	    this.name = name;
	    this.nodeType = nodeType;
    }

	public String getNodeType()
	{
		return nodeType;
	}

	public int getRandomizer()
	{
		return randomizer;
	}

	public String getSiteId()
	{
		return siteId;
	}
	public String getUsername()
	{
		return username;
	}
	public String getNodeId()
	{
		return nodeId;
	}
	public String getNodePath()
	{
		return nodePath;
	}
	public String getName()
	{
		return name;
	}

	@Override
    public String toString()
    {
	    return "FileData [randomizer=" + randomizer + ", siteId=" + siteId
	            + ", username=" + username + ", nodeId=" + nodeId
	            + ", nodePath=" + nodePath + ", name=" + name + ", nodeType="
	            + nodeType + "]";
    }
}
