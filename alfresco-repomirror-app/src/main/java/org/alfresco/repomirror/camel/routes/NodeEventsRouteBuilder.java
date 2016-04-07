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
import org.apache.camel.Exchange;
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
          @Qualifier("messagingTransactionManager") PlatformTransactionManager txnManager)
    {
        super(eventListener, dataFormat, txnManagerRef, sourceTopic + " -> bean", sourceTopic, getEventMetrics(), -1, -1,
                1000, ThreadAffinityTracking.ExceptionOnMismatch, null, txnManager, NodeEventsRouteBuilder.getRedeliveryConfig(),
                "");
    }

    private static RedeliveryConfig getRedeliveryConfig()
    {
        RedeliveryConfig redeliveryConfig = new RedeliveryConfig(-1, 2.0);
        return redeliveryConfig;
    }

    private static Processor getExceptionProcessor()
    {
        Processor processor = new Processor()
        {
            @Override
            public void process(Exchange exchange) throws Exception
            {
                // TODO Auto-generated method stub
                
            }
        };
        return processor;
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
