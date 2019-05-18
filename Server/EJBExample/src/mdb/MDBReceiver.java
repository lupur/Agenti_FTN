package mdb;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType",
				propertyValue = "javax.jms.Topic"),
		@ActivationConfigProperty(propertyName = "destination",
				propertyValue="testTopic")
})
public class MDBReceiver implements MessageListener {

	public MDBReceiver() {
		System.out.println("MDBReceiver created");
	}
	@Resource 
	MessageDrivenContext ctx;
	
	@Override
	public void onMessage(Message arg0) {
		TextMessage txt = (TextMessage) arg0;
		// TODO Auto-generated method stub
		try {
			System.out.println("Received message: " + txt.getText());
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@PreDestroy
	public void remove()
	{
		System.out.println("MDBReceiver removed");
	}
}
