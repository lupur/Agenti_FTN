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

import com.sun.org.apache.xerces.internal.util.Status;

import message.ACLMessage;
import message.IMessageManager;
import jms.JMSQueue;

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
		if(message == null)
		{
			return Response.status(400).build();
		}
		messageManager.post(message);
		return Response.ok().build();
	}
	
}
