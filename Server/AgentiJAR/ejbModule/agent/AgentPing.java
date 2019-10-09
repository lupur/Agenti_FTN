package agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

import agentCenter.AgentCenterDTO;
import agentCenter.IAgentCenter;
import agentCenter.Node;
import jms.JMSQueue;
import localSocket.LocalLogSocket;
import message.ACLMessage;
import message.Performative;
import sun.management.resources.agent_zh_CN;

@SuppressWarnings("serial")
@Stateful
@Remote(IAgent.class)
public class AgentPing extends Agent {

	@Override
	public String toString() {
		return "AgentPing [agentCenter=" + agentCenter + "]";
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
		if(msg.getPerformative() == Performative.REQUEST)
		{
			AID receiver = getRandomPong();
			if(receiver == null)
			{
				String message = "[" + this.getAid().getStr() +"]: " + "No pongs to receive a message, exiting.";
				super.sendLogs(message, agentCenter.getNodes());
				return;
			}
			
			String message = "[" + this.getAid().getStr() +"]: " +
					"Sending PING to: " +receiver.getStr();
			super.sendLogs(message, agentCenter.getNodes());
			ACLMessage response = new ACLMessage();
			response.setPerformative(Performative.REQUEST);
			response.setSender(this.getAid());
			List<AID> receivers = new ArrayList<AID>();
			receivers.add(receiver);
			response.setReceivers(receivers);
			response.setConversationId(msg.getConversationId());
			
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
		else if(msg.getPerformative() == Performative.INFORM)
		{
			String message = "["+this.getAid().getStr()+"] : Received response from: " + msg.getSender().getStr();
			super.sendLogs(message, agentCenter.getNodes());
		}
	}
	
	private AID getRandomPong()
	{
		ArrayList<AID> pongs = new ArrayList<AID>();
		for(AID agent : agentCenter.getRunningAgents())
		{
			if(agent.getType().getName().equals(AgentPong.class.getSimpleName()))
			{
				pongs.add(agent);
			}
		}
		if(pongs.size() == 0)
		{
			return null;
		}
		Random rand = new Random();
		
		return pongs.get(rand.nextInt(pongs.size()));
		
	}
}
