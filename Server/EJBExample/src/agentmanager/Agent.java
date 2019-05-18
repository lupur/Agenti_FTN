package agentmanager;

import java.io.Serializable;
import java.util.List;

import connectionmanager.ObjectField;
import messagemanager.ACLMessage;

public interface Agent extends Serializable {
	
	String SIEBOG_MODULE = "siebog-jar";
	String SIEBOG_EAR = "siebog-ear";
	String SIEBOG_WAR = "siebog-war";

	void init(AID aid, AgentInitArgs args);

	void stop();

	void handleMessage(ACLMessage msg);

	String ping();
	
	AID getAid();
	
	void move(String host);
	
	void reconstruct(List<ObjectField> agent);
	
	List<ObjectField> deconstruct();
}
