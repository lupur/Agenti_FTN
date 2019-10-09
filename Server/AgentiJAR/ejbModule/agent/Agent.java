package agent;

import java.util.ArrayList;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import com.google.gson.Gson;

import agentCenter.AgentInitArgs;
import agentCenter.Node;
import message.ACLMessage;

@SuppressWarnings("serial")
@Stateful
@Remote(IAgent.class)
public class Agent implements IAgent {

	private AID aid;

	@Override
	public void init(AID aid) {
		this.aid = aid;
	}

	@Override
	public void stop() {
		
	}

	@Override
	public void handleMessage(ACLMessage msg) {
		
	}

	@Override
	public String ping() {
		return null;
	}

	@Override
	public AID getAid() {
		return aid;
	}

	@Override
	public void setAid(AID aid) {
		this.aid = aid;
	}

	@Override
	public String toString() {
		return "Agent [aid=" + aid + "]";
	}
	
	protected void sendLogs(String message, ArrayList<Node> nodes)
	{
		for(Node node : nodes) {

			String URL = "http://" + node.getAddress() + "/AgentiWAR/api/center/log";
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = client.target(URL);
			Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(new Gson().toJson(message), MediaType.APPLICATION_JSON));
		}	
	}
	
	
}
