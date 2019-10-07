package rest;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import agent.IAgent;
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
	public Response registerNode(AgentCenter newCenter)
	{	
		if(agentCenter.getNode().getAlias().equals("master")) {
			agentCenter.getSupportedTypes().put(newCenter.getNode().getAlias(), newCenter.getAvailableAgentClasses());
			agentCenter.getNodes().add(newCenter.getNode());
			
			updateNewNode(newCenter.getAddress());
			updateNodes();
		} else {
			agentCenter.setSupportedTypes(newCenter.getSupportedTypes());
			agentCenter.setNodes(newCenter.getNodes());
			
		}

		return Response.ok().build();

	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/agents/running")
	public Response updateRunningAgents(List<IAgent> runningAgents) {
		agentCenter.setRunningAgents(runningAgents);
		return Response.ok().build();		
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
	
	private void updateNodes() {
		for(Node node : agentCenter.getNodes()) {
			String URL = "http://" + node.getAddress() + "/AgentiWAR/api/center/node";
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = client.target(URL);
			Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(agentCenter, MediaType.APPLICATION_JSON));
		}
	}
	
	private void updateNewNode(String address) {
		String URL = "http://" + address + "/AgentiWAR/api/center/agents/running";
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(URL);
		Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(agentCenter.getRunningAgents(), MediaType.APPLICATION_JSON));
	}
}
