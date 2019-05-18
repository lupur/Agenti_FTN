package agents;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import agentmanager.AID;
import agentmanager.Agent;
import agentmanager.AgentInitArgs;
import connectionmanager.ObjectField;
import messagemanager.ACLMessage;
import javax.ejb.Stateless;

@Stateless
@Remote(Agent.class)
public class AgentPing implements Agent {


	private static final long serialVersionUID = 1L;

	@Override
	public void init(AID aid, AgentInitArgs args) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleMessage(ACLMessage msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String ping() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AID getAid() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void move(String host) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reconstruct(List<ObjectField> agent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<ObjectField> deconstruct() {
		// TODO Auto-generated method stub
		return null;
	}

}
