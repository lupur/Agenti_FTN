package agent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;

import agentCenter.IAgentCenter;
import message.ACLMessage;
import message.IMessageManager;
import message.Performative;

@SuppressWarnings("serial")
@Stateful
@Remote(IAgent.class)
public class AgentInitiator extends Agent {

	@EJB
	IAgentCenter agentCenter;
	
	@EJB 
	IMessageManager messageManager;
	
	@Override
	public void handleMessage(ACLMessage msg)
	{
		switch(msg.getPerformative())
		{
		case REQUEST:
			initiateProtocol();
			break;
		case PROPOSE:
			handlePropose(msg);
			break;
		case REFUSE:
			handleRefuse(msg);
			break;
		case INFORM:
			handleInform(msg);
			break;
		case FAILURE:
			handleFailure(msg);
			break;
		default:
			System.out.println("Unkwnown performative: " + msg.getPerformative());
			break;
			
		}
	}
	
	private void initiateProtocol()
	{
		System.out.print("[" + this.getAid().getStr() +"]: ");
		ArrayList<AID> participants = getParticipants();
		if(participants.isEmpty())
		{
			System.out.print("[" + this.getAid().getStr() +"]: ");
			System.out.println("No active participants to initiate contract.");
			return;
		}
		System.out.print("Sending proposal to participants:");
		System.out.println(participants.toString());
		ACLMessage cfp = new ACLMessage();
		cfp.setPerformative(Performative.CALL_FOR_PROPOSAL);
		cfp.setReceivers(participants);
		cfp.setSender(this.getAid());
		messageManager.post(cfp);
	}
	
	private void handlePropose(ACLMessage msg)
	{
		System.out.print("[" + this.getAid().getStr() +"]: ");
		System.out.println("Received proposal from: " + msg.getSender().getStr());
		
		ACLMessage response = new ACLMessage();
		List<AID> recv = new ArrayList<AID>();
		recv.add(msg.getSender());
		response.setReceivers(recv);
		response.setSender(this.getAid());
		
		Date date = new Date();
		
		if(date.getTime() % 3 == 0)
		{
			System.out.print("[" + this.getAid().getStr() +"]: ");
			System.out.println("Rejecting proposal from: " + msg.getSender().getStr());
			response.setPerformative(Performative.REJECT_PROPOSAL);
		}
		else
		{
			System.out.print("[" + this.getAid().getStr() +"]: ");
			System.out.println("Acceptiong proposal from: " + msg.getSender().getStr());
			response.setPerformative(Performative.ACCEPT_PROPOSAL);
		}
		
		messageManager.post(response);
		
	}
	
	private void handleRefuse(ACLMessage msg)
	{
		System.out.print("[" + this.getAid().getStr() +"]: ");
		System.out.println(msg.getSender().getStr() + " refused proposal.");
	}
	
	private void handleInform(ACLMessage msg)
	{
		System.out.print("[" + this.getAid().getStr() +"]: ");
		System.out.println("From: " + msg.getSender().getStr() + " received info: "+ msg.getContent());
	}
	
	public void handleFailure(ACLMessage msg)
	{
		System.out.print("[" + this.getAid().getStr() +"]: ");
		System.out.println(msg.getSender().getStr() + " failed.");
	}
	
	private ArrayList<AID> getParticipants()
	{
		ArrayList<AID> participants = new ArrayList<AID>();
		for(IAgent agent: agentCenter.getRunningAgents())
		{
			String participantName = AgentParticipant.class.getSimpleName();
			if(agent.getAid().getType().getName().equals(participantName))
			{
				participants.add(agent.getAid());	
			}
					}
		return participants;
	}
}
