package jms;

import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import message.ACLMessage;

public class JMSQueue {

	public JMSQueue(ACLMessage message)
	{
		if(message == null)
		{
			return;
		}
		try
		{
			Context context = new InitialContext();
			ConnectionFactory cf = 
					(ConnectionFactory) context.lookup("java:/ConnectionFactory");
			Queue queue = (Queue) context.lookup("jms/queue/test");
			context.close();
			
			Connection conn;
			conn = cf.createConnection();
			Session session = conn.createSession();
			
			conn.start();
			ObjectMessage objMesage = session.createObjectMessage(message);
			MessageProducer messageProducer = session.createProducer(queue);
			messageProducer.send(objMesage);
			
			messageProducer.close();
			session.close();
			conn.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
