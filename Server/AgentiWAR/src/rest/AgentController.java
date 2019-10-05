package rest;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;

import agent.AID;
import agent.AgentType;
import agent.IAgent;
import agentCenter.AgentCenter;
import socket.RunningAgentsSocket;
import util.JSON;

@Path("/agents")
public class AgentController {
	
	@EJB
	AgentCenter agentCenter;
	
	@GET
	@Path("/classes")
	@Produces(MediaType.APPLICATION_JSON)
	public Response GetAgentClasses()
	{
		List<AgentType> agents = agentCenter.getAvailableAgentClasses();
		System.out.println("No of agents: " +  agents.size());
		for(int i=0; i<agents.size(); i++)
		{
			System.out.println("AGENT["+i+": "
					+ agents.get(i).getModule() + " : " 
					+ agents.get(i).getName() + "]");
		}
		
		return Response.ok(agents).build();
	}
	
	@GET
	@Path("/running")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRunningAgents()
	{
		
		List<AID> aids = agentCenter.getAIDSFromRunningAgents();
		System.out.println("*****************");
		System.out.println(aids.size());
		System.out.println(aids.size());
		System.out.println(aids.size());
		return Response.ok(aids).build();
	}
	
	@PUT
	@Path("/running/{type}/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response startAgent(@PathParam("type") String type, @PathParam("name") String name)
	{
		AgentType agentType = agentCenter.getAgentTypeByName(type);
		agentCenter.startServerAgent(agentType, name);
		
		RunningAgentsSocket.sendRunningAgents(JSON.g.toJson(agentCenter.getAIDSFromRunningAgents()));
		return Response.ok().build();
	}
	
	@DELETE
	@Path("/running/{aid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response stopAgent(@PathParam("aid") String agentID)
	{
		if(agentCenter.stopAgent(agentID))
		{
			RunningAgentsSocket.sendRunningAgents(JSON.g.toJson(agentCenter.getAIDSFromRunningAgents()));
			return Response.ok().build();
		}
		return Response.status(404).build();
	}
}
