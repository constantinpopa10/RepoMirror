/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repomirror.dao;

import java.util.List;
import java.util.stream.Stream;

import org.alfresco.repomirror.data.FileData;
import org.alfresco.repomirror.data.PathInfo;

/**
 * 
 * @author sglover
 *
 */
public interface NodesDataService
{
	boolean nodeExists(String nodeId);
	void addNode(String siteId, String username, String nodeId, String nodePath, String name, String nodeType);
	void removeNode(String nodeId);
	void renameNode(String nodeId, String newName);
	void moveNode(String nodeId, String fromPath, String toPath);
	FileData randomFileInSite(String siteId);
	FileData randomNodeUnderFolder(String path, List<String> nodeTypes);
	String randomFolderUnderFolder(String path);
	void updateNode(String nodeId, Integer numChildren, Integer numChildFolders,
			Integer numSiblingsToProcess, Integer numChildrenToProcess);
	long countNodes(String siteId);
	long countNodesUnderFolder(String path);
	Stream<PathInfo> randomPathsWithContent(int max);
	Stream<PathInfo> randomPaths(int max);
}
