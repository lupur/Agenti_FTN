package agent;

import java.util.ArrayList;
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
import message.ACLMessage;
import message.Performative;

@SuppressWarnings("serial")
@Stateful
@Remote(IAgent.class)
public class AgentPong extends Agent {

	@Override
	public String toString() {
		return "AgentPong [agentCenter=" + agentCenter + "]";
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
			ACLMessage response = new ACLMessage();
			response.setPerformative(Performative.INFORM);
			response.setSender(this.getAid());
			List<AID> receivers = new ArrayList<AID>();
			receivers.add(msg.getSender());
			response.setReceivers(receivers);
			response.setConversationId(msg.getConversationId());
			String message = "["+this.getAid().getStr()+"] : Received message from: " + msg.getSender().getStr();
			message +=". Sending a response...";
			super.sendLogs(message, agentCenter.getNodes());
			for(AID rec : response.getReceivers())
			{
				if(rec.getHost().getAddress().equals(agentCenter.getNode().getAddress()))
				{
					new JMSQueue(response);
				}
				else
				{
					String URL = "http://" + rec.getHost().getAddress() + "/AgentiWAR/api/messages";
					ResteasyClient client = new ResteasyClientBuilder().build();
					ResteasyWebTarget target = client.target(URL);
					Response r = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(new Gson().toJson(response), MediaType.APPLICATION_JSON));
				}
			}
		}
	}
}
