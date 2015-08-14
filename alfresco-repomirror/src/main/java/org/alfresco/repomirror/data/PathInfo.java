package org.alfresco.repomirror.data;

import java.util.List;


/**
 * 
 * @author sglover
 *
 */
public class PathInfo
{
	private String siteId;
	private String path;
	private Integer numChildren;
	private Integer numChildFolders;
	private String nodeType;
	private String nodeId;
	private List<List<String>> parentNodeIds;

	public PathInfo(String siteId, String path, Integer numChildren,
			Integer numChildFolders, String nodeType, String nodeId, List<List<String>> parentNodeIds)
    {
	    super();
	    this.siteId = siteId;
	    this.path = path;
	    this.numChildren = numChildren;
	    this.numChildFolders = numChildFolders;
	    this.nodeType = nodeType;
	    this.nodeId = nodeId;
	    this.parentNodeIds = parentNodeIds;
    }

	public List<List<String>> getParentNodeIds()
	{
		return parentNodeIds;
	}


	public String getNodeId()
	{
		return nodeId;
	}

	public String getNodeType()
	{
		return nodeType;
	}

	public String getSiteId()
	{
		return siteId;
	}
	public String getPath()
	{
		return path;
	}
	public Integer getNumChildren()
	{
		return numChildren;
	}
	public Integer getNumChildFolders()
	{
		return numChildFolders;
	}

	@Override
    public String toString()
    {
	    return "PathInfo [siteId=" + siteId + ", path=" + path
	            + ", numChildren=" + numChildren + ", numChildFolders="
	            + numChildFolders + ", nodeType=" + nodeType + ", nodeId="
	            + nodeId + ", parentNodeIds=" + parentNodeIds + "]";
    }
}
