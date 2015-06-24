package org.alfresco.repomirror.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.alfresco.repomirror.Populator;

@Path("/v1")
public class RestAPI
{
    private final Populator populator;

    /**
     * @param testDAO                   low-level data service for tests
     * @param testService               test service for retrieving calculated data
     * @param logService                service to log basic crud for end user record
     * @param testRunServices           factory providing access to test run services
     */
    public RestAPI(Populator populator)
    {
        this.populator = populator;
    }

    @POST
    @Path("/populate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String populate()
    {
    	int count = populator.initialPopulate();
    	return "{\"count\" : " + count + ", \"status\":\"ok\"}";
    }
}