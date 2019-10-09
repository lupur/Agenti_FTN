package rest;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import com.google.gson.Gson;

import agent.AID;
import agent.AgentType;
import agent.IAgent;
import agentCenter.AgentCenter;
import agentCenter.AgentCenterDTO;
import agentCenter.IAgentCenter;
import agentCenter.Node;
import socket.RunningAgentsSocket;
import util.JSON;

@Path("/agents")
@LocalBean
public class AgentController {
	
	@EJB
	IAgentCenter agentCenter;
	
	@GET
	@Path("/classes")
	@Produces(MediaType.APPLICATION_JSON)
	public Response GetAgentClasses()
	{
		List<AgentType> allAgentTypes = new ArrayList<>();
		for(List<AgentType> supportedTypes : agentCenter.getSupportedTypes().values()) {
			for(int i=0; i<supportedTypes.size(); i++) {
				AgentType supportedType = supportedTypes.get(i);
				if(!allAgentTypes.stream().filter(
						agentType -> agentType.getModule().equals(supportedType.getModule()) && agentType.getName().equals(supportedType.getName()))
						.findFirst().isPresent()) {
					allAgentTypes.add(supportedTypes.get(i));
				}
			}
		}
	
		
		
//		List<AgentType> agents = agentCenter.getAvailableAgentClasses();
		System.out.println("No of agents: " +  allAgentTypes.size());
		for(int i=0; i<allAgentTypes.size(); i++)
		{
			System.out.println("AGENT["+i+": "
					+ allAgentTypes.get(i).getModule() + " : " 
					+ allAgentTypes.get(i).getName() + "]");
		}
		
		return Response.ok(allAgentTypes).build();
	}
	
	@GET
	@Path("/running")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRunningAgents()
	{
		System.out.println("*****************");
		List<AID> aids = agentCenter.getRunningAgents();
		System.out.println(aids.size());

		return Response.ok(aids).build();
	}
	
	@PUT
	@Path("/running/{type}/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response startAgent(@PathParam("type") String type, @PathParam("name") String name)
	{
		
		AgentType agentType = agentCenter.getAgentTypeByName(type);
		if(agentType == null) {
			
			for(Map.Entry<String, List<AgentType>> entry : agentCenter.getSupportedTypes().entrySet()) {
				for(AgentType supportedType : entry.getValue()) {
					if(supportedType.getName().equals(type)) {
						for(Node node : agentCenter.getNodes()) {
							if(node.getAlias().equals(entry.getKey())) {
								System.out.println(entry.getKey());
								
								Client client = ClientBuilder.newClient();
								
								ResteasyClient restClient = new ResteasyClientBuilder().build();
								String url = "http://" + node.getAddress() + "/running/" + type + "/" + name;
								ResteasyWebTarget target = restClient.target(url);
								
								// TODO: Fix this
								Response response = target.request().put(Entity.json(""));
								
								return response;
							}
						}
					}
				}
			}
		} else {
			System.out.println(agentType.getName());
			agentCenter.startServerAgent(agentType, name);
			
			RunningAgentsSocket.sendRunningAgents(JSON.g.toJson(agentCenter.getRunningAgents()));
			agentCenter.registerRunningAgents();			
		}
		return Response.ok().build();
	}
	
	@DELETE
	@Path("/running/{aid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response stopAgent(@PathParam("aid") String agentID)
	{
		if(agentCenter.stopAgent(agentID))
		{
			RunningAgentsSocket.sendRunningAgents(JSON.g.toJson(agentCenter.getRunningAgents()));
			return Response.ok().build();
		}
		return Response.status(404).build();
	}
}
