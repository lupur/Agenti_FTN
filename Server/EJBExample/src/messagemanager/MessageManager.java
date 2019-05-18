package messagemanager;

import java.util.List;

public interface MessageManager {
	public static final String REPLY_WITH_TEST = "siebog-test";
	
	List<String> getPerformatives();

	void post(ACLMessage message);

	void post(ACLMessage message, long delayMillisec);

	String ping();
}