package org.alfresco.repomirror.data;


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

	public PathInfo(String siteId, String path, Integer numChildren,
			Integer numChildFolders)
    {
	    super();
	    this.siteId = siteId;
	    this.path = path;
	    this.numChildren = numChildren;
	    this.numChildFolders = numChildFolders;
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

	
}
