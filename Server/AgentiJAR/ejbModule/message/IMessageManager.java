package message;

import java.util.List;

public interface IMessageManager {
	
	List<String> getPerformatives();
	void post(ACLMessage message);
}
