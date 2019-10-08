package agent;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;

import agentCenter.IAgentCenter;
import jms.JMSQueue;
import localSocket.LocalLogSocket;
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
			// TODO Auto-generated catch block
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
			LocalLogSocket logger = new LocalLogSocket(agentCenter.getAddress());
			logger.sendMessage(message);
			new JMSQueue(response);
		}
	}
}
