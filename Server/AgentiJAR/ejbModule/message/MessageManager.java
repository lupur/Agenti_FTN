package message;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import com.google.gson.Gson;

import agent.AID;
import agentCenter.IAgentCenter;
import jms.JMSQueue;

@Stateless
@Remote(IMessageManager.class)
@LocalBean
public class MessageManager implements IMessageManager {
	
	@EJB
	IAgentCenter agentCenter;
	
	@Override
	public List<String> getPerformatives() {
		final Performative[] arr = Performative.values();
		List<String> list = new ArrayList<>(arr.length);
		for (Performative p : arr)
			list.add(p.toString());
		return list;
	}

	@Override
	public void post(ACLMessage message) {
		for(AID rec : message.getReceivers())
		{
			if(rec.getHost().getAddress().equals(agentCenter.getNode().getAddress()))
			{
				new JMSQueue(message);
			}
			else
			{
				//TODO http zahtev ka recv nodu POST/ACL_MSG
				String URL = "http://" + rec.getHost().getAddress() + "/AgentiWAR/api/messages";
				ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget target = client.target(URL);
				Response r = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(new Gson().toJson(message), MediaType.APPLICATION_JSON));
			}
		}
	}	
}
