package com.example.rest;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Session;
import javax.jms.Topic;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import agentmanager.AID;
import agentmanager.AgentManager;
import agentmanager.AgentType;
import sun.management.resources.agent;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;


@Stateless
@Path("/agents")
public class AgentsController {

	@EJB
	AgentManager agentManager;
	
	
	
	@GET
	@Path("/classes")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAgentClasses()
	{	
		
		List<AgentType> agents = agentManager.getAvailableAgentClasses();
		
		System.out.println("SIZE: " + agents.get(0).getEjbName());
		return Response.ok().build();
	}
	
	@GET
	@Path("/running")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRunningAgents()
	{
		List<AID> aids = agentManager.getRunningAgents();
		
		System.out.println("AIDS: " + aids.size());
				
		
		return Response.ok().build();
	}
	
	@PUT
	@Path("/running/{type}/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response startAgent(@PathParam("type") String type, @PathParam("name") String name)
	{
		return Response.ok().build();
	}
	
	@DELETE
	@Path("/running/{aid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response stopAgent(@PathParam("aid") int agentID)
	{
		return Response.ok().build();
	}

}
