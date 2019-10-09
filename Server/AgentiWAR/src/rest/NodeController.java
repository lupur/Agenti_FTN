package rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import agent.AID;
import agent.AgentType;
import agentCenter.AgentCenterDTO;
import agentCenter.IAgentCenter;
import agentCenter.Node;
import socket.AgentClassesSocket;
import socket.LogSocket;
import socket.RunningAgentsSocket;
import util.JSON;

@Path("/center")
@Stateless
public class NodeController {
 
	@EJB
	IAgentCenter agentCenter;
	
	@POST
	@Path("/node")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response registerNode(String center)
	{	
		System.out.println("Into POST/center/node method");
		if(center.isEmpty())
		{
			System.out.println("POST/center/node AgentCenterDTO is null");
			return Response.status(400).build();
		}
		Gson gson = new Gson();
		AgentCenterDTO acDto = null;
		try
		{
			acDto = gson.fromJson(center, AgentCenterDTO.class);
		} catch (Exception e) {
			System.out.println("Wrong body format.");
			return Response.status(400).build();
		}
		
		if(!acDto.isValid())
		{
			System.out.println("Wrong body format.");
			return Response.status(400).build();
		}
		
		if(agentCenter.getNode().getAlias().equals("master")) {
			System.out.println("Master node should register new slave");
			agentCenter.addSupportedType(acDto.getNode().getAlias(), acDto.getAvailableAgentClasses());
			
			updateNewNode(acDto.getNode().getAddress());
			
			agentCenter.putNode(acDto.getNode());
			updateNodes();
//			return Response.ok().entity(new Gson().toJson(new AgentCenterDTO(agentCenter))).build();

		} else {
			System.out.println("Slave node is adding new data");
			agentCenter.setSupportedTypes(acDto.getSupportedTypes());
			agentCenter.setNodes(acDto.getNodes());
			
		}

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
		AgentClassesSocket.sendAvailableAgentClasses(JSON.g.toJson(allAgentTypes));
		
		return Response.ok().build();

	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/agents/running")
	public Response updateRunningAgents(String runningAgents) {
		System.out.println("Got request from master to update running agents");
		if(runningAgents.isEmpty())
		{
			return Response.status(400).build();
		}
		
		Gson gson = new Gson();
		List<AID> runningAgentsList = new ArrayList<AID>();
		try
		{
			runningAgentsList = gson.fromJson(runningAgents, new TypeToken<List<AID>>(){}.getType());
		} catch (Exception e) {
			System.out.println("[POST/agents/running/]Wrong body format.");
			return Response.status(400).build();
		}
		if(runningAgentsList == null)
		{
			System.out.println("Wrong body format.");
			return Response.status(400).build();
		}
		System.out.println("Node is updating all running agents");
		agentCenter.setRunningAgents(runningAgentsList);
		RunningAgentsSocket.sendRunningAgents(JSON.g.toJson(agentCenter.getRunningAgents()));
		return Response.ok().build();		
	}
	
	@DELETE
	@Path("/node/{alias}")
	public Response deleteNode(@PathParam("alias") String alias)
	{
		System.out.println("Got request to delete");
		Node del = null;

		for(Node n : agentCenter.getNodes())
		{
			if(n.getAlias().equals(alias))
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
		AgentClassesSocket.sendAvailableAgentClasses(JSON.g.toJson(allAgentTypes));
		
		RunningAgentsSocket.sendRunningAgents(JSON.g.toJson(agentCenter.getRunningAgents()));
		
		return Response.status(200).build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/log")
	public Response updateLogSocket(String message) {
		
		LogSocket.sendLog(message);
		return Response.ok().build();		
	}
	
	@GET
	@Path("/node")
	public Response checkHealth() {
		if(agentCenter.getNode() != null && agentCenter.getNode().getAlias().equals("slave1")) {
			Random r = new Random();
			if(r.nextInt(19)%13==0) {
				agentCenter.setNode(null);
			}
		}
		try {
			agentCenter.getNode().getAddress();
			return Response.ok().build();
		} catch(Exception e) {
			return Response.serverError().build();
		}	
	}
	
	private void updateNodes() {
		System.out.println("[updateNodes] Should inform " + (agentCenter.getNodes().size() - 1) + " nodes.");
		for(Node node : agentCenter.getNodes()) {
			if(node.getAlias().equals("master"))
			{
				continue;
			}
			System.out.println("Node["+node.getAlias() + " - " + node.getAddress() + " will get info about new node");
			String URL = "http://" + node.getAddress() + "/AgentiWAR/api/center/node";
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = client.target(URL);
			Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(new Gson().toJson(new AgentCenterDTO(agentCenter)), MediaType.APPLICATION_JSON));
			System.out.println("Node["+node.getAlias() + "] : " + response.getStatus());
		}
		return;
	}
	
	private void updateNewNode(String address) {
		System.out.println("New node will get a list of all running");
		String URL = "http://" + address + "/AgentiWAR/api/center/agents/running";
		
		Gson gson = new Gson();
				
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(URL);
		Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(gson.toJson(agentCenter.getRunningAgents()), MediaType.APPLICATION_JSON));
	}
}
