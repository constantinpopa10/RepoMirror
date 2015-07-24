/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repomirror.dao.mongo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.alfresco.bm.user.UserDataServiceImpl.Range;
import org.alfresco.repomirror.dao.NodesDataService;
import org.alfresco.repomirror.data.FileData;
import org.alfresco.repomirror.data.PathInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteConcern;

/**
 * 
 * @author sglover
 *
 */
public class MongoNodesDataService implements NodesDataService, InitializingBean
{
	private static Log logger = LogFactory.getLog(MongoNodesDataService.class);

	public static String FIELD_USERNAME = "username";
	public static String FIELD_RANDOMIZER = "randomizer";
	public static String FIELD_SUBSCRIBER_ID = "subscriberId";
	public static String FIELD_STATE = "state";

    /** The collection of users, which can be reused by derived extensions. */
    protected final DBCollection collection;
    
    public MongoNodesDataService(DB db, String collection)
    {
        this.collection = db.getCollection(collection);
    }
    
    @Override
    public void afterPropertiesSet() throws Exception
    {
        checkIndexes();
    }

    /**
     * Ensure that the MongoDB collection has the required indexes associated with
     * this user bean.
     */
    private void checkIndexes()
    {
        collection.setWriteConcern(WriteConcern.SAFE);

        DBObject uidxNodeId = BasicDBObjectBuilder
                .start("nodeId", 1)
                .get();
        DBObject optNodeId = BasicDBObjectBuilder
                .start("name", "uidxNodeId")
                .add("unique", Boolean.TRUE)
                .get();
        collection.createIndex(uidxNodeId, optNodeId);

        DBObject idxSiteId = BasicDBObjectBuilder
                .start("siteId", 1)
                .get();
        DBObject optSiteId = BasicDBObjectBuilder
                .start("name", "idxSiteId")
                .get();
        collection.createIndex(idxSiteId, optSiteId);

        DBObject idxPath = BasicDBObjectBuilder
                .start("path", 1)
                .add("nodeType", 2)
                .add("siteId", 3)
                .add("randomizer", 4)
                .get();
        DBObject optPath = BasicDBObjectBuilder
                .start("name", "idxPath")
                .get();
        collection.createIndex(idxPath, optPath);

        DBObject idxNodeType = BasicDBObjectBuilder
                .start("nodeType", 1)
                .add("randomizer", 2)
                .get();
        DBObject optNodeType = BasicDBObjectBuilder
                .start("name", "idxnodeType")
                .get();
        collection.createIndex(idxNodeType, optNodeType);
    }

    private Range getRandomizerRange(List<String> sites)
    {
    	QueryBuilder queryObjBuilder = QueryBuilder
        		.start();
        if(sites != null && sites.size() > 0)
        {
            queryObjBuilder.and("siteId").in(sites);
        }
        return getRandomizerRange(queryObjBuilder);
    }

    private Range getRandomizerRange(QueryBuilder queryObjBuilder)
    {
        DBObject queryObj = queryObjBuilder.get();

        DBObject fieldsObj = BasicDBObjectBuilder.start()
                .add("randomizer", Boolean.TRUE)
                .get();
        
        DBObject sortObj = BasicDBObjectBuilder.start()
                .add("randomizer", -1)
                .get();
        
        // Find max
        DBObject resultObj = collection.findOne(queryObj, fieldsObj, sortObj);
        int maxRandomizer = resultObj == null ? 0 : (Integer) resultObj.get("randomizer");
        
        // Find min
        sortObj.put("randomizer", +1);
        resultObj = collection.findOne(queryObj, fieldsObj, sortObj);
        int minRandomizer = resultObj == null ? 0 : (Integer) resultObj.get("randomizer");

        return new Range(minRandomizer, maxRandomizer);
    }

    private DBObject toDBObject(FileData fileData)
    {
    	DBObject dbObject = BasicDBObjectBuilder
    			.start("username", fileData.getUsername())
    			.add("path", fileData.getNodePath())
    			.add("siteId", fileData.getSiteId())
    			.add("nodeId", fileData.getNodeId())
    			.add("name", fileData.getName())
    			.add("nodeType", fileData.getNodeType())
    			.add("randomizer", fileData.getRandomizer())
    			.get();
    	return dbObject;
    }

    private FileData toFileData(DBObject dbObject)
    {
		String username = (String)dbObject.get("username");
		String nodePath = (String)dbObject.get("path");
		String siteId = (String)dbObject.get("siteId");
		String nodeId = (String)dbObject.get("nodeId");
		String name = (String)dbObject.get("name");
		String nodeType = (String)dbObject.get("nodeType");
		int randomizer = (Integer)dbObject.get("randomizer");
		FileData fileData = new FileData(randomizer, siteId, username, nodeId, nodePath, name, nodeType);
		return fileData;
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

    @Override
    public boolean nodeExists(String nodeId)
    {
		nodeId = normalizeNodeId(nodeId);
    	DBObject query = QueryBuilder
    			.start("nodeId").is(nodeId)
    			.get();
    	DBObject dbObject = collection.findOne(query);
    	return dbObject != null;
    }

	@Override
    public void addNode(String siteId, String username, String nodeId, String nodePath,
            String name, String nodeType)
    {
		nodeId = normalizeNodeId(nodeId);

		if(nodeExists(nodeId))
		{
			logger.warn("Found dup nodeId " + nodeId + ", " + nodePath);
		}
		else
		{
			FileData fileData = new FileData(siteId, username, nodeId, nodePath, name, nodeType);
	    	DBObject insert = toDBObject(fileData);
	    	collection.insert(insert);
		}
    }

	@Override
    public void removeNode(String nodeId)
    {
		nodeId = normalizeNodeId(nodeId);

    	DBObject query = QueryBuilder
    			.start("nodeId").is(nodeId)
    			.get();
    	collection.remove(query);
    }

	@Override
    public void renameNode(String nodeId, String newName)
    {
		nodeId = normalizeNodeId(nodeId);

    	DBObject query = QueryBuilder
    			.start("nodeId").is(nodeId)
    			.get();
    	DBObject update = BasicDBObjectBuilder
    			.start("$set", BasicDBObjectBuilder
    					.start("name", newName)
    					.get())
    			.get();
    	collection.update(query, update, false, false);
    }

	@Override
    public void moveNode(String nodeId, String fromPath, String toPath)
    {
	    // TODO Auto-generated method stub
	    
    }

    @Override
	public FileData randomFileInSite(String siteId)
	{
        Range range = getRandomizerRange(Arrays.asList(siteId));
        int upper = range.getMax();
        int lower = range.getMin();
        int random = lower + (int) (Math.random() * (double) (upper - lower));
 
		DBObject query = BasicDBObjectBuilder
				.start("siteId", siteId)
				.push("randomizer")
					.add("$gte", random)
					.pop()
				.get();
		DBObject dbObject = collection.findOne(query);
		FileData fileData = toFileData(dbObject);
		return fileData;
	}

    @Override
	public FileData randomNodeUnderFolder(String path, List<String> nodeTypes)
	{
    	Pattern regex = Pattern.compile("^" + path);
    	QueryBuilder rangeQueryObjBuilder = QueryBuilder
        		.start("path").regex(regex);
        if(nodeTypes != null && nodeTypes.size() > 0)
        {
    		rangeQueryObjBuilder.and("nodeType").in(nodeTypes);
        }

        Range range = getRandomizerRange(rangeQueryObjBuilder);
        int upper = range.getMax();
        int lower = range.getMin();
        int random = lower + (int) (Math.random() * (double) (upper - lower));
 
    	QueryBuilder queryObjBuilder = QueryBuilder
        		.start("path").regex(regex);
        if(nodeTypes != null && nodeTypes.size() > 0)
        {
    		queryObjBuilder.and("nodeType").in(nodeTypes);
        }
        queryObjBuilder.and("randomizer").greaterThanEquals(random);
		DBObject query = queryObjBuilder.get();
		DBObject dbObject = collection.findOne(query);
		FileData fileData = (dbObject != null ? toFileData(dbObject) : null);
		return fileData;
	}

    @Override
	public String randomFolderUnderFolder(String path)
	{
    	String nodePath = null;

    	FileData fileData = randomNodeUnderFolder(path, Arrays.asList("cm:content", "cm:folder"));
    	if(fileData != null)
    	{
	    	String nodeType = fileData.getNodeType();
	    	nodePath = fileData.getNodePath();
	    	if(nodeType.equals("cm:content"))
	    	{
	    		int idx = nodePath.lastIndexOf("/");
	    		nodePath = nodePath.substring(0, idx);
	    	}
    	}

		return nodePath;
	}

	@Override
    public void updateNode(String nodeId, Integer numChildren, Integer numChildFolders,
            Integer numSiblingsToProcess, Integer numChildrenToProcess)
    {
		nodeId = normalizeNodeId(nodeId);

    	QueryBuilder queryObjBuilder = QueryBuilder
        		.start("nodeId").is(nodeId);
		DBObject query = queryObjBuilder.get();
		DBObject update = BasicDBObjectBuilder
				.start()
				.push("$set")
					.add("numChildren", numChildren)
					.add("numSiblingsToProcess", numSiblingsToProcess)
					.add("numChildrenToProcess", numChildrenToProcess)
					.add("numChildFolders", numChildFolders)
				.pop()
				.get();
		collection.update(query, update, false, false);
    }

	@Override
	public long countNodes(String siteId)
	{
    	QueryBuilder queryObjBuilder = QueryBuilder
        		.start("siteId").is(siteId);
		DBObject query = queryObjBuilder.get();
		long count = collection.count(query);
		return count;
	}

	@Override
    public long countNodesUnderFolder(String path)
    {
		path = "^" + path;
		if(!path.endsWith("/"))
		{
			path = path + "/";
		}
    	Pattern regex = Pattern.compile(path);
    	QueryBuilder queryObjBuilder = QueryBuilder
        		.start("path").regex(regex);
		DBObject query = queryObjBuilder.get();
		long count = collection.count(query);
		return count;
    }
	
	private PathInfo toPathInfo(DBObject dbObject)
	{
		PathInfo pathInfo = null;

		if(dbObject != null)
		{
			String siteId = (String)dbObject.get("siteId");
			String path = (String)dbObject.get("path");
			Integer numChildren = (Integer)dbObject.get("numChildren");
			Integer numChildFolders = (Integer)dbObject.get("numChildFolders");
			pathInfo = new PathInfo(siteId, path, numChildren, numChildFolders);
		}

		return pathInfo;
	}

	@Override
	public PathInfo randomFolderInSite(String siteId)
	{
    	Pattern regex = Pattern.compile("documentLibrary");

        QueryBuilder rangeBuilder = QueryBuilder
        		.start("path").regex(regex)
        		.and("nodeType").in(Arrays.asList("cm:content", "cm:folder"))
        		.and("siteId").is(siteId);
        Range range = getRandomizerRange(rangeBuilder);
        int upper = range.getMax();
        int lower = range.getMin();
        int random = lower + (int) (Math.random() * (double) (upper - lower));

        QueryBuilder builder = QueryBuilder
        		.start("path").regex(regex)
        		.and("nodeType").in(Arrays.asList("cm:content", "cm:folder"))
        		.and("siteId").is(siteId)
        		.and(FIELD_RANDOMIZER).greaterThanEquals(Integer.valueOf(random));
        DBObject queryObj = builder.get();

        DBObject dbObject = collection.findOne(queryObj);
        if(dbObject == null)
        {
            queryObj.put(FIELD_RANDOMIZER, new BasicDBObject("$lt", random));
        }

        dbObject = collection.findOne(queryObj);

    	PathInfo pathInfo = toPathInfo(dbObject);
        return pathInfo;
	}

	@Override
	public Stream<PathInfo> randomPathsWithContent(int max)
	{
    	Pattern regex = Pattern.compile("[^documentLibrary]$");

        QueryBuilder rangeBuilder = QueryBuilder
        		.start("path").regex(regex)
        		.and("nodeType").in(Arrays.asList("cm:content", "cm:folder"))
        		.and("siteId").exists(true)
        		.and("siteId").notEquals(null);
        Range range = getRandomizerRange(rangeBuilder);
        int upper = range.getMax();
        int lower = range.getMin();
        int random = lower + (int) (Math.random() * (double) (upper - lower));

        QueryBuilder builder = QueryBuilder
        		.start("path").regex(regex)
        		.and("nodeType").in(Arrays.asList("cm:content", "cm:folder"))
        		.and("siteId").exists(true)
        		.and("siteId").notEquals(null)
        		.and(FIELD_RANDOMIZER).greaterThanEquals(Integer.valueOf(random));
        DBObject queryObj = builder.get();

        boolean ascending = true;

        DBObject dbObject = collection.findOne(queryObj);
        if(dbObject == null)
        {
        	ascending = false;
            queryObj.put(FIELD_RANDOMIZER, new BasicDBObject("$lt", random));
        }

        DBObject orderBy = BasicDBObjectBuilder
        		.start(FIELD_RANDOMIZER, (ascending ? 1 : -1))
        		.get();

    	DBCursor cur = collection.find(queryObj).sort(orderBy).limit(max);

    	Stream<PathInfo> stream = StreamSupport.stream(cur.spliterator(), false)
    		.onClose(() -> cur.close())  // need to close cursor;
//    		.filter(dbo -> dbo.get("siteId") != null)
    		.map(dbo -> toPathInfo(dbo))
    		.filter(pi -> pi != null);

        return stream;
	}

	@Override
	public Stream<PathInfo> randomPaths(int max)
	{
        QueryBuilder rangeBuilder = QueryBuilder
        		.start("nodeType").in(Arrays.asList("cm:content", "cm:folder"))
        		.and("siteId").exists(true)
        		.and("siteId").notEquals(null);
        Range range = getRandomizerRange(rangeBuilder);
        int upper = range.getMax();
        int lower = range.getMin();
        int random = lower + (int) (Math.random() * (double) (upper - lower));

        QueryBuilder builder = QueryBuilder
        		.start("nodeType").in(Arrays.asList("cm:content", "cm:folder"))
        		.and("siteId").exists(true)
        		.and("siteId").notEquals(null)
        		.and(FIELD_RANDOMIZER).greaterThanEquals(Integer.valueOf(random));
        DBObject queryObj = builder.get();

        boolean ascending = true;

        DBObject dbObject = collection.findOne(queryObj);
        if(dbObject == null)
        {
        	ascending = false;
            queryObj.put(FIELD_RANDOMIZER, new BasicDBObject("$lt", random));
        }

        DBObject orderBy = BasicDBObjectBuilder
        		.start(FIELD_RANDOMIZER, (ascending ? 1 : -1))
        		.get();

        Stream<PathInfo> stream = null;
        if(max >= 0)
        {
        	DBCursor cur = collection.find(queryObj).sort(orderBy).limit(max);
        	stream = StreamSupport.stream(cur.spliterator(), false)
            		.onClose(() -> cur.close())  // need to close cursor;
            		.map(dbo -> toPathInfo(dbo))
            		.filter(pi -> pi != null);
        }
        else
        {
        	stream = Collections.<PathInfo>emptyList().stream();
        }

        return stream;
	}
}
