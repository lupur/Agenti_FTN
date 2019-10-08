package jms;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import agent.AID;
import agent.IAgent;
import agentCenter.IAgentCenter;
import message.ACLMessage;

@MessageDriven(activationConfig = 
{
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:jboss/exported/jms/queue/test")
})												  
public class JMSQueueListener implements MessageListener {

	@EJB
	IAgentCenter agentCenter;
	
	@Override
	public void onMessage(Message msg) {
		ObjectMessage objMessage = (ObjectMessage) msg;
		try
		{
			ACLMessage message = (ACLMessage) objMessage.getObject();
			
			if(message.getReceivers() == null || message.getReceivers().isEmpty())
			{
				System.out.println("No receivers in the message");
				return;
			}
			
			for(AID receiverAID : message.getReceivers())
			{
				if(agentCenter.getAddress().equals(receiverAID.getHost().getAddress()))
				{
					IAgent agent = agentCenter.findAgent(receiverAID);
						
					if(agent == null)
					{
						return;
					}
					System.out.println("Sending message to: " + agent.getAid().getStr());
					agent.handleMessage(message);	
				}
			}
		}
		catch (JMSException e)
		{
			e.printStackTrace();
		}
	}

	
}
