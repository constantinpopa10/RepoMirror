/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repomirror.camel;

import java.io.IOException;
import java.util.List;

import org.alfresco.events.types.NodeAddedEvent;
import org.alfresco.events.types.NodeEvent;
import org.alfresco.events.types.NodeMovedEvent;
import org.alfresco.events.types.NodeRemovedEvent;
import org.alfresco.events.types.NodeRenamedEvent;
import org.alfresco.repomirror.dao.NodesDataService;

/**
 * 
 * @author sglover
 *
 */
public class NodeEventListener
{
	private NodesDataService filesService;

	public NodeEventListener(NodesDataService filesService)
	{
		super();
		this.filesService = filesService;
	}

	private String getNodePath(NodeEvent nodeEvent)
	{
		List<String> paths = nodeEvent.getPaths();
		String nodePath = null;
		if(paths != null && paths.size() > 0)
		{
			nodePath = paths.get(0);
		}

		return nodePath;
	}

	private void nodeAdded(NodeAddedEvent event) throws IOException
	{
		String siteId = event.getSiteId();
		String nodeId = event.getNodeId();
		String username = event.getUsername();
		String nodePath = getNodePath(event);
		String name = event.getName();
		String nodeType = event.getNodeType();
		filesService.addNode(siteId, username, nodeId, nodePath, name, nodeType);
	}

	private void nodeRemoved(NodeRemovedEvent event)
	{
		String nodeId = event.getNodeId();
		filesService.removeNode(nodeId);
	}

	private void nodeRenamed(NodeRenamedEvent event)
	{
		String nodeId = event.getNodeId();
		String newName = event.getNewName();
		filesService.renameNode(nodeId, newName);
	}

	private void nodeMoved(NodeMovedEvent event)
	{
		String nodeId = event.getNodeId();
		String fromPath = getNodePath(event);
		String toPath = event.getToPaths().get(0);
		filesService.moveNode(nodeId, fromPath, toPath);
	}

	public void onMessage(Object message) throws IOException
	{
		if(message instanceof NodeAddedEvent)
		{
			NodeAddedEvent nodeAddedEvent = (NodeAddedEvent)message;
			nodeAdded(nodeAddedEvent);
		}
		else if(message instanceof NodeRemovedEvent)
		{
			NodeRemovedEvent nodeRemovedEvent = (NodeRemovedEvent)message;
			nodeRemoved(nodeRemovedEvent);
		}
		else if(message instanceof NodeRenamedEvent)
		{
			NodeRenamedEvent nodeRenamedEvent = (NodeRenamedEvent)message;
			nodeRenamed(nodeRenamedEvent );
		}
		else if(message instanceof NodeMovedEvent)
		{
			NodeMovedEvent nodeMovedEvent = (NodeMovedEvent)message;
			nodeMoved(nodeMovedEvent );
		}
		else
		{
			// TODO
		}

	}
}
