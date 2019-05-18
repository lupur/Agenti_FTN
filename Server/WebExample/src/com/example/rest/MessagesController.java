package com.example.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/messages")
public class MessagesController {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response sendMessage(String message)
	{
		return Response.ok().build();		
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMessages()
	{
		return Response.ok().build();
	}
}
