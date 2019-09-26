package message;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import jms.JMSQueue;

@Stateless
@Remote(IMessageManager.class)
@LocalBean
public class MessageManager implements IMessageManager {
	
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
		new JMSQueue(message); 
	}	
}
