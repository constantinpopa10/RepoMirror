/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repomirror.camel;

import java.util.List;
import java.util.Map;

import org.alfresco.events.types.NodeAddedEvent;
import org.alfresco.events.types.NodeEvent;
import org.alfresco.events.types.NodeMovedEvent;
import org.alfresco.events.types.NodeRemovedEvent;
import org.alfresco.events.types.NodeRenamedEvent;
import org.alfresco.repomirror.dao.NodesDataService;
import org.alfresco.service.common.events.EventListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author sglover
 *
 */
public class NodeEventListener implements EventListener
{
    protected static Log logger = LogFactory.getLog(NodeEventListener.class);

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
        if (paths != null && paths.size() > 0)
        {
            nodePath = paths.get(0);
        }

        return nodePath;
    }

    private void nodeAdded(NodeAddedEvent event)
    {
        String siteId = event.getSiteId();
        String nodeId = event.getNodeId();
        String username = event.getUsername();
        String nodePath = getNodePath(event);
        String name = event.getName();
        String nodeType = event.getNodeType();
        List<List<String>> parentNodeIds = event.getParentNodeIds();
        filesService.addNode(siteId, username, nodeId, nodePath, name, nodeType,
                parentNodeIds);
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

    private void onMessage(Object message)
    {
        if (message instanceof NodeAddedEvent)
        {
            NodeAddedEvent nodeAddedEvent = (NodeAddedEvent) message;
            nodeAdded(nodeAddedEvent);
        }
        else if (message instanceof NodeRemovedEvent)
        {
            NodeRemovedEvent nodeRemovedEvent = (NodeRemovedEvent) message;
            nodeRemoved(nodeRemovedEvent);
        }
        else if (message instanceof NodeRenamedEvent)
        {
            NodeRenamedEvent nodeRenamedEvent = (NodeRenamedEvent) message;
            nodeRenamed(nodeRenamedEvent);
        }
        else if (message instanceof NodeMovedEvent)
        {
            NodeMovedEvent nodeMovedEvent = (NodeMovedEvent) message;
            nodeMoved(nodeMovedEvent);
        }
        else
        {
            logger.warn("Unhandled event " + message);
        }
    }

    @Override
    public void onEvent(Object message)
    {
        onMessage(message);
    }

    @Override
    public void onEvent(Map<String, Object> headers, Object message)
    {
        onMessage(message);
    }
}
