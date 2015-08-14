/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repomirror;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.site.SiteData;
import org.alfresco.bm.site.SiteDataService;
import org.alfresco.bm.user.UserData;
import org.alfresco.bm.user.UserDataService;
import org.alfresco.repomirror.dao.NodesDataService;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sglover
 *
 */
public class Populator
{
    private static Log logger = LogFactory.getLog(Populator.class);

	public final static String REPOSITORY_ID_USE_FIRST = "---";

	private UserDataService userDataService;
	private SiteDataService sitesDataService;
	private NodesDataService nodesDataService;
	private OperationContext opContext;
	private int maxChildrenPerFolder = -1;
	private int maxChildFolders = -1;
	private int maxInitialNodes = 0;

	private final String alfrescoHost;
	private final int alfrescoPort;

    public Populator(UserDataService userDataService,
            SiteDataService sitesDataService, NodesDataService nodesDataService, int maxChildrenPerFolder,
            int maxChildFolders, String alfrescoHost, int alfrescoPort, int maxInitialNodes)
    {
	    super();
	    this.userDataService = userDataService;
	    this.sitesDataService = sitesDataService;
	    this.nodesDataService = nodesDataService;
	    this.maxChildFolders = maxChildFolders;
	    this.maxChildrenPerFolder = maxChildrenPerFolder;
	    this.alfrescoHost = alfrescoHost;
	    this.alfrescoPort = alfrescoPort;
	    this.maxInitialNodes = maxInitialNodes;
    }

	private Session getCMISSession(String username, BindingType bindingType, String bindingUrl, String repositoryId)
    {
        UserData user = userDataService.findUserByUsername(username);
        if (user == null)
        {
            throw new RuntimeException("Unable to start CMIS session; user no longer exists: " + username);
        }
        String password = user.getPassword();

        // Build session parameters
        Map<String, String> parameters = new HashMap<String, String>();
        if (bindingType != null && bindingType.equals(BindingType.ATOMPUB))
        {
            parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
            parameters.put(SessionParameter.ATOMPUB_URL, bindingUrl);
        }
        else if (bindingType != null && bindingType.equals(BindingType.BROWSER))
        {
            parameters.put(SessionParameter.BINDING_TYPE, BindingType.BROWSER.value());
            parameters.put(SessionParameter.BROWSER_URL, bindingUrl);
        }
        else
        {
            throw new RuntimeException("Unsupported CMIS binding type: " + bindingType);
        }
        parameters.put(SessionParameter.USER, username);
        parameters.put(SessionParameter.PASSWORD, password);
        
        // First check if we need to choose a repository
        SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
        List<Repository> repositories = sessionFactory.getRepositories(parameters);
        if (repositories.size() == 0)
        {
        	throw new RuntimeException("Unable to find any repositories at " + bindingUrl + " with user " + username);
        }
        if (repositoryId.equals(REPOSITORY_ID_USE_FIRST))
        {
            String repositoryIdFirst = repositories.get(0).getId();
            parameters.put(SessionParameter.REPOSITORY_ID, repositoryIdFirst);
        }
        else
        {
            parameters.put(SessionParameter.REPOSITORY_ID, repositoryId);
        }

        // Create the session
        Session session = SessionFactoryImpl.newInstance().createSession(parameters);
        if(opContext != null)
        {
        	session.setDefaultContext(opContext);
        }

        return session;
    }

	private String normalizeNodeId(String nodeId)
	{
		int idx = nodeId.indexOf(";");
		if(idx != -1)
		{
			nodeId = nodeId.substring(0, idx);
		}
		return nodeId;
	}

	private String normalizeNodeType(String nodeType)
	{
		if(nodeType.equals("cmis:document"))
		{
			nodeType = "cm:document";
		}
		if(nodeType.equals("cmis:folder"))
		{
			nodeType = "cm:folder";
		}
		
		return nodeType;
	}

	private int populate(Session session, String siteId, Folder folder)
	{
		int count = 0;

		String folderNodeId = normalizeNodeId(folder.getId());
		int numChildren = 0;
		int numChildFolders = 0;

    	String folderNodePath = folder.getPath();
    	if(!folderNodePath.startsWith("/Company Home"))
    	{
    		folderNodePath = "/Company Home" + folderNodePath;
    	}
    	String folderName = folder.getName();
		ObjectType folderType = folder.getType();
		String folderNodeType = normalizeNodeType(folderType.getId());
		List<List<String>> parentNodeIds = getParentNodeIds(session, folder);

		if(!nodesDataService.nodeExists(folderNodeId))
		{
			logger.debug("Processing " + siteId + ", " + folderNodeId + ", " + folderNodePath);
        	nodesDataService.addNode(siteId, null, folderNodeId, folderNodePath, folderName, folderNodeType,
        			parentNodeIds);
        	count++;
		}

		for(CmisObject child : folder.getChildren())
		{
			ObjectType childType = child.getType();
			String nodeType = childType.getId();
			if(nodeType.equals("cmis:document"))
			{
				nodeType = "cm:document";
			}
			if(nodeType.equals("cmis:folder"))
			{
				nodeType = "cm:folder";
			}
	        BaseTypeId baseTypeId = childType.getBaseTypeId();
			String nodeId = child.getId();

	        if(baseTypeId.equals(BaseTypeId.CMIS_FOLDER))
	        {
	        	if(maxChildFolders == -1 || numChildFolders < maxChildFolders)
	        	{
		        	Folder childFolder = (Folder)child;
		        	populate(session, siteId, childFolder);
	        	}
	        	numChildFolders++;
				numChildren++;
	        }
	        else if(baseTypeId.equals(BaseTypeId.CMIS_DOCUMENT))
	        {
				if(maxChildrenPerFolder == -1 || numChildren < maxChildrenPerFolder)
				{
		        	Document document = (Document)child;
					String name = document.getName();
					if(nodeId.indexOf(";") != -1)
					{
						int idx = nodeId.indexOf(";");
						nodeId = nodeId.substring(0, idx);
					}
		        	List<String> paths = document.getPaths();
		        	List<List<String>> docParentNodeIds = getParentNodeIds(session, document);
		        	String childNodePath = (paths != null && paths.size() > 0 ? paths.get(0) : null);
		        	if(!childNodePath.startsWith("/Company Home"))
		        	{
		        		childNodePath = "/Company Home" + childNodePath;
		        	}
					if(!nodesDataService.nodeExists(nodeId))
					{
						logger.debug("Processing " + siteId + ", " + nodeId + ", " + childNodePath);
			        	nodesDataService.addNode(siteId, null, nodeId, childNodePath, name, nodeType, docParentNodeIds);
			        	count++;
					}
				}
				numChildren++;
	        }
	        else
	        {
	        	logger.debug("Skipping node " + nodeId + " with type " + nodeType + ", not a document or folder");
	        }
		}

		int numSiblingsToProcess = (maxChildrenPerFolder < 0 ? 0 :
			((numChildren > maxChildrenPerFolder) ? numChildren - maxChildrenPerFolder : 0));
		int numChildrenToProcess = (maxChildFolders < 0 ? 0 :
			((numChildFolders > maxChildFolders) ? numChildFolders - maxChildFolders : 0));
		nodesDataService.updateNode(folderNodeId, numChildren, numChildFolders,
				numSiblingsToProcess, numChildrenToProcess);

		return count;
	}

	/**
	 * Note: primary parents only.
	 * 
	 * @param session
	 * @param cmisObject
	 * @return
	 */
	private List<List<String>> getParentNodeIds(Session session, FileableCmisObject cmisObject)
	{
		List<List<String>> parentNodeIds = new LinkedList<>();

		List<String> primaryParentNodeIds = new LinkedList<>();
		parentNodeIds.add(primaryParentNodeIds);
		for(Folder parent : cmisObject.getParents())
		{
			String parentNodeId = normalizeNodeId(parent.getId());
			primaryParentNodeIds.add(parentNodeId);
		}

		return parentNodeIds;
	}

	private int populate(Session session, String siteId, String nodePath)
	{
		Folder documentLibrary = (Folder)session.getObjectByPath(nodePath);
		return populate(session, siteId, documentLibrary);
	}
	
	private String nodePath(String nodePath)
	{
		if(nodePath.startsWith("/Company Home"))
		{
			int idx = "/Company Home".length();
			nodePath = nodePath.substring(idx);
		}
		return nodePath;
	}

	public int initialPopulate()
	{
		Session session = getCMISSession("admin", BindingType.BROWSER,
				"http://" + alfrescoHost + ":" + alfrescoPort + "/alfresco/api/-default-/public/cmis/versions/1.1/browser",
				"-default-");

		int count = 0;

		int skip = 0;
		int maxItems = 100;

		List<SiteData> sites = sitesDataService.getSites("default", DataCreationState.Created, skip, maxItems);
		while(sites != null && sites.size() > 0 && count < maxInitialNodes)
		{
			for(SiteData siteData : sites)
			{
				String siteId = siteData.getSiteId();
				String sitePath = siteData.getPath();
				if(sitePath == null)
				{
					sitePath = "/Company Home/Sites/" + siteId + "/documentLibrary";
				}
				String nodePath = nodePath(sitePath);
				int siteCount = populate(session, siteId, nodePath);
				count += siteCount;
			}

			skip += maxItems;
			sites = sitesDataService.getSites("default", DataCreationState.Created, skip, maxItems);
		};

		return count;
	}
}
