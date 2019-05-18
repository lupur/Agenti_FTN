package agentmanager;

import java.io.Serializable;
import java.util.List;

import connectionmanager.ObjectField;
import messagemanager.ACLMessage;

public interface Agent extends Serializable {
	
	String EJB_MODULE = "EJBExample";
	String EAR_MODULE = "WebExample";
	String WAR_MODULE = "WebExample";

	void init(AID aid, AgentInitArgs args);

	void stop();

	void handleMessage(ACLMessage msg);

	String ping();
	
	AID getAid();
	
	void move(String host);
	
	void reconstruct(List<ObjectField> agent);
	
	List<ObjectField> deconstruct();
}
