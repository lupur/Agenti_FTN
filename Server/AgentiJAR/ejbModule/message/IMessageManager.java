package message;

import java.util.List;

public interface IMessageManager {
	
	public static final String REPLY_WITH_TEST = "siebog-test";
	List<String> getPerformatives();
	void post(ACLMessage message);
	String ping();
}
