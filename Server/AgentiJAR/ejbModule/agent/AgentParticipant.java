package agent;

import java.security.spec.MGF1ParameterSpec;
import java.text.SimpleDateFormat;
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
public class AgentParticipant extends Agent {

	@Override
	public String toString() {
		return "AgentParticipant [agentCenter=" + agentCenter + "]";
	}

	@EJB
	IAgentCenter agentCenter;
		
	@Override
	public void handleMessage(ACLMessage msg)
	{
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		switch (msg.getPerformative()) {
		case CALL_FOR_PROPOSAL:
			handleProposal(msg);
			break;
		case ACCEPT_PROPOSAL:
			doWork(msg);
			break;
		case REJECT_PROPOSAL:
			handleRejectProposal(msg);
			break;

		default:
			System.out.println("Unkwnown performative");
			break;
		}
	}
	
	private void handleProposal(ACLMessage msg)
	{
		String message = "[" + this.getAid().getStr() +"]: \n" +
					"Call for proposal from: " + msg.getSender().getStr();
		super.sendLogs(message, agentCenter.getNodes());
		ACLMessage response = new ACLMessage();
		response.setSender(this.getAid());
		
		List<AID> recv = new ArrayList<AID>();
		recv.add(msg.getSender());
		response.setReceivers(recv);
		
		Date date = new Date();
		
		if(date.getTime() % 3 == 0)
		{
			message = "[" + this.getAid().getStr() +"]: \n" +
								"Decided not to participate.";
			
			response.setPerformative(Performative.REFUSE);
		}
		else
		{
			message = "[" + this.getAid().getStr() +"]: \n"
						+ "Decided to participate.";
			response.setPerformative(Performative.PROPOSE);
		}
		super.sendLogs(message, agentCenter.getNodes());
		
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
	
	private void handleRejectProposal(ACLMessage msg) {
		String message = "[" + this.getAid().getStr() +"]: \n" +
						"Proposal rejected by " + msg.getSender().getStr();
		
		super.sendLogs(message, agentCenter.getNodes());
	}
	
	private void doWork(ACLMessage msg)
	{
		String message = "[" + this.getAid().getStr() +"]: \n" +
						"Doing some work for: " + msg.getSender().getStr();
		super.sendLogs(message, agentCenter.getNodes());
		ACLMessage response = new ACLMessage();
		response.setSender(this.getAid());
		
		List<AID> recv = new ArrayList<AID>();
		recv.add(msg.getSender());
		response.setReceivers(recv);
		
		Date date = new Date();
		
		if(date.getTime() % 5 == 0)
		{
			message = "[" + this.getAid().getStr() +"]: \n" +
							"Failed to finish the job for: " + msg.getSender().getStr();
			super.sendLogs(message, agentCenter.getNodes());
			response.setPerformative(Performative.FAILURE);
			response.setContent("Could not finish the task.");
		}
		else
		{
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			message = "[" + this.getAid().getStr() +"]: \n" +
					"Finished the job for: " + msg.getSender().getStr();
			super.sendLogs(message, agentCenter.getNodes());
			response.setPerformative(Performative.INFORM);
			response.setContent("Finish the task at: " + formatter.format(date));
			
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
}
