package agent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import com.google.gson.Gson;

import agentCenter.IAgentCenter;
import jms.JMSQueue;
import localSocket.LocalLogSocket;
import message.ACLMessage;
import message.Performative;


@SuppressWarnings("serial")
@Stateful
@Remote(IAgent.class)
public class AgentInitiator extends Agent {

	@Override
	public String toString() {
		return "AgentInitiator [agentCenter=" + agentCenter + "]";
	}

	@EJB
	IAgentCenter agentCenter;
	
	
	@Override
	public void handleMessage(ACLMessage msg)
	{
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		switch(msg.getPerformative())
		{
		case REQUEST:
			initiateProtocol();
			break;
		case PROPOSE:
			handlePropose(msg);
			break;
		case REFUSE:
			handleRefuse(msg);
			break;
		case INFORM:
			handleInform(msg);
			break;
		case FAILURE:
			handleFailure(msg);
			break;
		default:
			System.out.println("Unkwnown performative: " + msg.getPerformative());
			break;
			
		}
	}
	
	private void initiateProtocol()
	{
		
		ArrayList<AID> participants = getParticipants();
		if(participants.isEmpty())
		{
			String message = "[" + this.getAid().getStr() +"]: ";
			message += "\nNo active participants to initiate contract.";
			System.out.print(message);
			LocalLogSocket socket = new LocalLogSocket(agentCenter.getAddress());
			socket.sendMessage(message);
			return;
		}
		String message = "[" + this.getAid().getStr() +"]: ";
		message += "Sending proposal to participants:\n" + participants.toString();
		System.out.print(message);
		LocalLogSocket socket = new LocalLogSocket(agentCenter.getAddress());
		socket.sendMessage(message);
		ACLMessage response = new ACLMessage();
		response.setPerformative(Performative.CALL_FOR_PROPOSAL);
		response.setReceivers(participants);
		response.setSender(this.getAid());
		
		for(AID rec : response.getReceivers())
		{
			if(rec.getHost().getAddress().equals(agentCenter.getNode().getAddress()))
			{
				new JMSQueue(response);
			}
			else
			{
				//TODO http zahtev ka recv nodu POST/ACL_MSG
				String URL = "http://" + rec.getHost().getAddress() + "/AgentiWAR/api/messages";
				ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget target = client.target(URL);
				Response r = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(new Gson().toJson(response), MediaType.APPLICATION_JSON));
			}
		}
	}
	
	private void handlePropose(ACLMessage msg)
	{
		String message = "[" + this.getAid().getStr() +"]: \n" + 
						"Received proposal from: " + msg.getSender().getStr();
		System.out.print(message);
		LocalLogSocket socket = new LocalLogSocket(agentCenter.getAddress());
		socket.sendMessage(message);
		
		ACLMessage response = new ACLMessage();
		List<AID> recv = new ArrayList<AID>();
		recv.add(msg.getSender());
		response.setReceivers(recv);
		response.setSender(this.getAid());
		
		Date date = new Date();
		
		if(date.getTime() % 3 == 0)
		{
			message = "[" + this.getAid().getStr() +"]: \n" +
					"Rejecting proposal from: " + msg.getSender().getStr();
			System.out.print(message);
			socket.sendMessage(message);
			response.setPerformative(Performative.REJECT_PROPOSAL);
		}
		else
		{
			message = "[" + this.getAid().getStr() +"]: " +
						"Acceptiong proposal from: " + msg.getSender().getStr();
			System.out.println(message);
			socket.sendMessage(message);
			response.setPerformative(Performative.ACCEPT_PROPOSAL);
		}
		
		for(AID rec : response.getReceivers())
		{
			if(rec.getHost().getAddress().equals(agentCenter.getNode().getAddress()))
			{
				new JMSQueue(response);
			}
			else
			{
				//TODO http zahtev ka recv nodu POST/ACL_MSG
				String URL = "http://" + rec.getHost().getAddress() + "/AgentiWAR/api/messages";
				ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget target = client.target(URL);
				Response r = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(new Gson().toJson(response), MediaType.APPLICATION_JSON));
			}
		}
		
	}
	
	private void handleRefuse(ACLMessage msg)
	{
		String message = "[" + this.getAid().getStr() +"]: \n" +
					msg.getSender().getStr() + " refused proposal.";
		LocalLogSocket socket = new LocalLogSocket(agentCenter.getAddress());
		socket.sendMessage(message);
	}
	
	private void handleInform(ACLMessage msg)
	{
		String message = "[" + this.getAid().getStr() +"]: \n"+
					"From: " + msg.getSender().getStr() + " received info: "+ msg.getContent();
		LocalLogSocket socket = new LocalLogSocket(agentCenter.getAddress());
		socket.sendMessage(message);
	}
	
	public void handleFailure(ACLMessage msg)
	{
		String message = "[" + this.getAid().getStr() +"]: \n" +
					msg.getSender().getStr() + " failed.";
		LocalLogSocket socket = new LocalLogSocket(agentCenter.getAddress());
		socket.sendMessage(message);
	}
	
	private ArrayList<AID> getParticipants()
	{
		ArrayList<AID> participants = new ArrayList<AID>();
		for(AID agent: agentCenter.getRunningAgents())
		{
			String participantName = AgentParticipant.class.getSimpleName();
			if(agent.getType().getName().equals(participantName))
			{
				participants.add(agent);	
			}
					}
		return participants;
	}
}
