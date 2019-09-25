package rest;

import java.util.List;

import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import message.ACLMessage;
import message.IMessageManager;

@Path("/messages")
public class MessageController {

	@EJB 
	IMessageManager messageManager;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getPerformatives()
	{
		return messageManager.getPerformatives();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendMessage(ACLMessage  message)
	{
		messageManager.post(message);
		
		return Response.ok().build();		
	}
	
}
