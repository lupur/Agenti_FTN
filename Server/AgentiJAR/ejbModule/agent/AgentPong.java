package agent;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateful;

import jms.JMSQueue;
import message.ACLMessage;
import message.Performative;

@SuppressWarnings("serial")
@Stateful
@Remote(IAgent.class)
public class AgentPong extends Agent {

	@Override
	public void handleMessage(ACLMessage msg)
	{
		if(msg.getPerformative() == Performative.REQUEST)
		{
			ACLMessage response = new ACLMessage();
			response.setPerformative(Performative.INFORM);
			response.setSender(this.getAid());
			List<AID> receivers = new ArrayList<AID>();
			receivers.add(msg.getSender());
			response.setReceivers(receivers);
			response.setConversationId(msg.getConversationId());
			System.out.println("["+this.getAid().getStr()+"] : Received message from: " + msg.getSender().getStr());
			
			new JMSQueue(response);
		}
	}
}
