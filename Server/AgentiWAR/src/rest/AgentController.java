package rest;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import agent.AID;
import agent.AgentType;
import agent.IAgent;
import agentCenter.AgentCenter;
import sun.management.resources.agent_zh_CN;

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
		
		return Response.ok().build();
	}
	
	@GET
	@Path("/running")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRunningAgents()
	{
		List<IAgent> aids = agentCenter.getRunningAgents();
		
		System.out.println("AIDS: " + aids.size());
		
		return Response.ok().build();
	}
	
	@PUT
	@Path("/running/{type}/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response startAgent(@PathParam("type") String type, @PathParam("name") String name)
	{
		AgentType agentType = agentCenter.getAgentTypeByName(type);
		agentCenter.startServerAgent(agentType, name);
		return Response.ok().build();
	}
	
	@DELETE
	@Path("/running/{aid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response stopAgent(@PathParam("aid") String agentID)
	{
		if(agentCenter.stopAgent(agentID))
		{
			return Response.ok().build();
		}
		return Response.serverError().build();
	}
}
