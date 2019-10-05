package rest;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import agentCenter.AgentCenter;
import agentCenter.IAgentCenter;
import agentCenter.Node;

@Path("/center")
@Stateless
public class NodeController {
 
	@EJB
	IAgentCenter agentCenter;
	
	@POST
	@Path("/node")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object registerNode(AgentCenter newCenter)
	{
		
		agentCenter.getNodes().add(newCenter.getNode());
		agentCenter.getSupportedTypes()
			.put(newCenter.getNode().getAlias(), newCenter.getSupportedTypes().get(newCenter.getNode().getAlias()));
		if(agentCenter.getNode().getAlias().equals("master"))
		{
			agentCenter.informNodes(newCenter);
			return agentCenter;
		}

		return true;

	}
	
	@DELETE
	@Path("/node/{alias}")
	public Response deleteNode(@PathParam("alias") String alias)
	{
		Node del = null;

		for(Node n : agentCenter.getNodes())
		{
			if(n.getAddress().equals(alias))
			{
				del = n;
			}
		}
		
		if(del == null)
		{
			return Response.status(404).build();
		}
		
		if(agentCenter.getNode().getAlias().equals("master"))
		{
			agentCenter.deleteNodeFromAll(del);
		}
		agentCenter.deleteNode(del);
		
		return Response.status(200).build();
	}
	
}
