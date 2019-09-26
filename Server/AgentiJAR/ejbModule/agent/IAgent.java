package agent;

import java.io.Serializable;

import message.ACLMessage;

public interface IAgent extends Serializable {
	
	String EJB_MODULE = "AgentiEJB";
	String EAR_MODULE = "AgentiEAR";
	String WAR_MODULE = "AgentiWAR";
	
	public void init(AID aid);
	public void stop();
	public void handleMessage(ACLMessage msg);
	public String ping();
	public void setAid(AID aid);
	public AID getAid();
}