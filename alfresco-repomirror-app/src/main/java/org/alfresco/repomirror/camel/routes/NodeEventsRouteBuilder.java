/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repomirror.camel.routes;

import javax.jms.Message;

import org.alfresco.repomirror.camel.NodeEventListener;
import org.alfresco.service.common.events.EventMetrics;
import org.alfresco.service.common.events.RedeliveryConfig;
import org.alfresco.service.common.events.ThreadAffinityTracking;
import org.alfresco.service.common.events.VirtualTopicRouteBuilderImpl;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Route builder for registering a durable subscriber for repository node events
 * 
 * @author sglover
 */
@Component
public class NodeEventsRouteBuilder extends VirtualTopicRouteBuilderImpl
{
    @Autowired
    public NodeEventsRouteBuilder(
          @Qualifier("nodeEventListener") NodeEventListener eventListener,
          @Value("${camel.node.events.dataFormat}") String dataFormat,
          @Value("${camel.node.events.sourceTopic}") String sourceTopic,
          @Value("${camel.node.events.txnManager}") String txnManagerRef,
          @Qualifier("repoMirrorMessagingTransactionManager") PlatformTransactionManager txnManager,
          @Value("${messaging.events.repo.node.deadletterUri}") String deadLetterUri,
          @Qualifier("messagingExceptionProcessor") Processor messagingExceptionProcessor)
    {
        super(eventListener, dataFormat, txnManagerRef, sourceTopic + " -> bean", sourceTopic, getEventMetrics(), -1, -1,
                1000, ThreadAffinityTracking.ExceptionOnMismatch, messagingExceptionProcessor, txnManager,
                NodeEventsRouteBuilder.getRedeliveryConfig(), deadLetterUri);
    }

    private static RedeliveryConfig getRedeliveryConfig()
    {
        RedeliveryConfig redeliveryConfig = new RedeliveryConfig(-1, 2.0);
        return redeliveryConfig;
    }

    private static EventMetrics getEventMetrics()
    {
        EventMetrics metrics = new EventMetrics()
        {
            @Override
            public void onEvent(Message jmsMessage, Object event)
            {
            }
        };
        return metrics;
    }
}
