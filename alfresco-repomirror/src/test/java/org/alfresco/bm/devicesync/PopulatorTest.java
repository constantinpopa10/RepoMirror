/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.bm.devicesync;

import java.util.List;

import org.alfresco.bm.data.DataCreationState;
import org.alfresco.bm.site.SiteData;
import org.alfresco.bm.site.SiteDataServiceImpl;
import org.alfresco.bm.user.UserDataServiceImpl;
import org.alfresco.repomirror.Populator;
import org.alfresco.repomirror.dao.MyUserDataServiceImpl;
import org.alfresco.repomirror.dao.mongo.MongoNodesDataService;
import org.alfresco.service.common.mongo.MongoDbFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.Mongo;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

/**
 * 
 * @author sglover
 *
 */
public class PopulatorTest
{
    private MongodForTestsFactory mongoFactory;

    @After
    public void afterClass()
    {
    	if(mongoFactory != null)
    	{
    		mongoFactory.shutdown();
    	}
    }

    private SiteDataServiceImpl sitesDataService;
    private UserDataServiceImpl userDataService;
	private MongoNodesDataService nodesDataService;
	private Populator populator;

	@Before
	public void before() throws Exception
	{
        final MongoDbFactory factory = new MongoDbFactory();
        boolean useEmbeddedMongo = ("true".equals(System.getProperty("useEmbeddedMongo")) ? true : false);
        if (useEmbeddedMongo)
        {
            this.mongoFactory = MongodForTestsFactory.with(Version.Main.PRODUCTION);
            final Mongo mongo = mongoFactory.newMongo();
            factory.setMongo(mongo);
        }
        else
        {
            factory.setMongoURI("mongodb://ec2-54-78-179-52.eu-west-1.compute.amazonaws.com:27017");
//            factory.setMongoURI("mongodb://127.0.0.1:27017");
            factory.setDbName("bm20-data");
        }
        final DB db = factory.createInstance();

//        this.userDataService = new MyUserDataServiceImpl(db, "mirrors.localhost.users");
//        this.userDataService.afterPropertiesSet();
//		this.nodesDataService = new MongoNodesDataService(db, "mirrors.localhost.nodes");
//		this.nodesDataService.afterPropertiesSet();
//		this.sitesDataService = new SiteDataServiceImpl(db, "mirrors.localhost.sites", "mirrors.localhost.siteMembers");
//		this.sitesDataService.afterPropertiesSet();
        this.userDataService = new MyUserDataServiceImpl(db, "mirrors.sync1.users");
        this.userDataService.afterPropertiesSet();
		this.nodesDataService = new MongoNodesDataService(db, "mirrors.sync1.nodes");
		this.nodesDataService.afterPropertiesSet();
		this.sitesDataService = new SiteDataServiceImpl(db, "mirrors.sync1.sites", "mirrors.sync1.siteMembers");
		this.sitesDataService.afterPropertiesSet();

		this.populator = new Populator(userDataService, sitesDataService, nodesDataService, -1, -1,
				"ec2-54-78-248-250.eu-west-1.compute.amazonaws.com", 8080, 5000);
//		this.populator = new Populator(userDataService, sitesDataService, nodesDataService, 10, 2,
//				"ec2-54-78-248-250.eu-west-1.compute.amazonaws.com", 8080, 1000);
	}

	@Test
	public void test1() throws Exception
	{
		populator.initialPopulate();
	}

//	@Test
	public void test2() throws Exception
	{
		int skip = 0;
		int maxItems = 100;
		int siteCount = 0;
		List<SiteData> sites = sitesDataService.getSites("default", DataCreationState.Created, skip, maxItems);
		while(sites != null && sites.size() > 0)
		{
			System.out.println();
			for(SiteData siteData : sites)
			{
				siteCount++;
			}
	
			skip += maxItems;
			sites = sitesDataService.getSites("default", DataCreationState.Created, skip, maxItems);
		};
		System.out.println(siteCount);
	}
}
