package com.example.rest;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;

public class JMSHandler implements MessageListener{
	public JMSHandler()
	{
		try {
			Context context = new InitialContext();
			ConnectionFactory cf = (ConnectionFactory) context
					.lookup("java:/ConnectionFactory");
			final Topic topic = (Topic) context
					.lookup("jms/topic/test");
			context.close();
			Connection connection = cf.createConnection();//"guest", "guestguest");
			final Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			connection.start();

			MessageConsumer consumer = session.createConsumer(topic);
			consumer.setMessageListener(this);
			
			// create and publish a message
			TextMessage msg = session.createTextMessage();
			msg.setText("Text!!!");
			MessageProducer producer = session.createProducer(topic);
			producer.send(msg);
			Thread.sleep(1000);
			System.out
					.println("Message published. Please check application server's console to see the response from MDB.");
			
			producer.close();
			consumer.close();
			connection.stop();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void onMessage(Message arg0) {
		// TODO Auto-generated method stub
		
	}
	
}
