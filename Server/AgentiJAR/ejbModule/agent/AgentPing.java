package agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;

import agentCenter.IAgentCenter;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(msg.getPerformative() == Performative.REQUEST)
		{
			AID receiver = getRandomPong();
			if(receiver == null)
			{
				String message = "[" + this.getAid().getStr() +"]: " + "No pongs to receive a message, exiting.";
				LocalLogSocket logger = new LocalLogSocket(agentCenter.getAddress());
				logger.sendMessage(message);
				return;
			}
			
			String message = "[" + this.getAid().getStr() +"]: " +
					"Sending PING to: " +receiver.getStr();
			LocalLogSocket logger = new LocalLogSocket(agentCenter.getAddress());
			logger.sendMessage(message);
			ACLMessage response = new ACLMessage();
			response.setPerformative(Performative.REQUEST);
			response.setSender(this.getAid());
			List<AID> receivers = new ArrayList<AID>();
			receivers.add(receiver);
			response.setReceivers(receivers);
			response.setConversationId(msg.getConversationId());
			
			new JMSQueue(response);
		}
		else if(msg.getPerformative() == Performative.INFORM)
		{
			String message = "["+this.getAid().getStr()+"] : Received response from: " + msg.getSender().getStr();
			LocalLogSocket logger = new LocalLogSocket(agentCenter.getAddress());
			logger.sendMessage(message);
		}
	}
	
	private AID getRandomPong()
	{
		ArrayList<AID> pongs = new ArrayList<AID>();
		for(IAgent agent : agentCenter.getRunningAgents())
		{
			if(agent.getAid().getType().getName().equals(AgentPong.class.getSimpleName()))
			{
				pongs.add(agent.getAid());
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
