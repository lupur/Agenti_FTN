package agent;

import javax.ejb.Stateful;

import agentCenter.AgentInitArgs;
import message.ACLMessage;

@SuppressWarnings("serial")
@Stateful
public class Agent implements IAgent {

	private AID aid;

	@Override
	public void init(AID aid) {
		// TODO Auto-generated method stub
		this.aid = aid;
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
		return aid;
	}

	@Override
	public void move(String host) {
		// TODO Auto-generated method stub
		
	}
	
	
}