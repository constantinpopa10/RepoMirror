/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repomirror.camel.routes;

import org.alfresco.repomirror.camel.NodeEventListener;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Route builder for registering a durable subscriber for repository node events
 * 
 * @author sglover
 */
@Component
public class NodeEventsRouteBuilder extends RouteBuilder
{
    private static Log logger = LogFactory.getLog(NodeEventsRouteBuilder.class);

    private NodeEventListener eventListener;
    private String dataFormat;
    private String sourceTopic = "activemq:topic:alfresco.events.repo.nodes";
    private String clientId;
    private String durableSubscriptionName;
    private String txnManager;

    @Autowired
    public NodeEventsRouteBuilder(@Qualifier("nodeEventListener") NodeEventListener eventListener,
    		@Value("${camel.node.events.dataFormat}") String dataFormat,
    		@Value("${camel.node.events.sourceTopic}") String sourceTopic,
    		@Value("${camel.node.events.clientId}") String clientId,
    		@Value("${camel.node.events.durableSubscriptionName}") String durableSubscriptionName,
    		@Value("${camel.node.events.txnManager}") String txnManager)
    {
    	this.eventListener = eventListener;
    	this.dataFormat = dataFormat;
    	this.sourceTopic = sourceTopic;
    	this.clientId = clientId;
    	this.durableSubscriptionName = durableSubscriptionName;
    	this.txnManager = txnManager;
    }

    private String getSourceTopic()
    {
        StringBuilder sb = new StringBuilder(sourceTopic);

        sb.append("?");
        sb.append("clientId=");
        sb.append(clientId);
        sb.append("&durableSubscriptionName=");
        sb.append(durableSubscriptionName);

        return sb.toString();
    }

    @Override
    public void configure()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Subscription service node events routes config: ");
            logger.debug("sourceTopic is "+sourceTopic);
            logger.debug("targetbean is "+eventListener);
        }

        from(getSourceTopic())
        .routeId("topic:alfresco.repo.events.nodes -> bean")
        .transacted().ref(txnManager)
        .unmarshal(dataFormat)
        .bean(eventListener, "onMessage")
        .end();
    }
}
