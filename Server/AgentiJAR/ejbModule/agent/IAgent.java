package agent;

import java.io.Serializable;

import message.ACLMessage;

public interface IAgent extends Serializable {
	
	String EJB_MODULE = "AgentiEJB";
	String EAR_MODULE = "AgentiEAR";
	String WAR_MODULE = "AgentiWAR";
	
	void init(AID aid);
	void stop();
	void handleMessage(ACLMessage msg);
	String ping();
	AID getAid();
	void move(String host);
}