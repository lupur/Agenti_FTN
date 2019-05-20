package com.example.rest;

import java.util.List;

import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import messagemanager.ACLMessage;
import messagemanager.MessageManager;

@Path("/messages")
public class MessagesController {

	@EJB
	MessageManager msm;
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendMessage(ACLMessage  message)
	{
		msm.post(message);
		
		return Response.ok().build();		
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPerformatives()
	{
		List<String> perf = msm.getPerformatives();
		System.out.println("Size: " + perf.size());
		System.out.println(perf.toString());
		return Response.ok().build();
	}
}
