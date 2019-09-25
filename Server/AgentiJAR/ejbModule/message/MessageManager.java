package message;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import agent.AID;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

@Stateless
@Remote(IMessageManager.class)
@LocalBean
public class MessageManager implements IMessageManager {

	@EJB
	private JMSFactory factory;
	
	private Session session;
	private MessageProducer defaultProducer;
	private MessageProducer testProducer;
	
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
		// TODO Auto-generated method stub
		for(int i=0; i<message.receivers.size(); i++)
		{
			if(message.receivers.get(i) == null)
			{
				throw new IllegalArgumentException("AID cannot be null");
			}
			postToReceiver(message, i);
		}
	}
	
	private void postToReceiver(ACLMessage msg, int index)
	{
		AID aid = msg.receivers.get(index);
		try
		{
			ObjectMessage jmsMsg = session.createObjectMessage(msg);
			setupJmsMsg(jmsMsg, aid, index);
			getProducer(msg).send(jmsMsg);
		}
		catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	private void setupJmsMsg(ObjectMessage jmsMsg, AID aid, int index) throws JMSException {
		// TODO See message grouping in a cluster
		// http://docs.jboss.org/hornetq/2.2.5.Final/user-manual/en/html/message-grouping.html
		jmsMsg.setStringProperty("JMSXGroupID", aid.getStr());
		jmsMsg.setIntProperty("AIDIndex", index);
		jmsMsg.setStringProperty("_HQ_DUPL_ID", UUID.randomUUID().toString());

	}
	
	private MessageProducer getProducer(ACLMessage msg) {
		if (MessageManager.REPLY_WITH_TEST.equals(msg.inReplyTo)) {
			return getTestProducer();
		}
		return defaultProducer;
	}
	
	private MessageProducer getTestProducer() {
		if (testProducer == null) {
			testProducer = factory.getTestProducer(session);
		}
		return testProducer;
	}
	
	@Override
	public String ping() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
