package agent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;

import agentCenter.IAgentCenter;
import localSocket.LocalLogSocket;
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
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		
		ArrayList<AID> participants = getParticipants();
		if(participants.isEmpty())
		{
			String message = "[" + this.getAid().getStr() +"]: ";
			message += "\nNo active participants to initiate contract.";
			System.out.print(message);
			LocalLogSocket socket = new LocalLogSocket(agentCenter.getAddress());
			socket.sendMessage(message);
			return;
		}
		String message = "[" + this.getAid().getStr() +"]: ";
		message += "Sending proposal to participants:\n" + participants.toString();
		System.out.print(message);
		LocalLogSocket socket = new LocalLogSocket(agentCenter.getAddress());
		socket.sendMessage(message);
		ACLMessage cfp = new ACLMessage();
		cfp.setPerformative(Performative.CALL_FOR_PROPOSAL);
		cfp.setReceivers(participants);
		cfp.setSender(this.getAid());
		messageManager.post(cfp);
	}
	
	private void handlePropose(ACLMessage msg)
	{
		String message = "[" + this.getAid().getStr() +"]: \n" + 
						"Received proposal from: " + msg.getSender().getStr();
		System.out.print(message);
		LocalLogSocket socket = new LocalLogSocket(agentCenter.getAddress());
		socket.sendMessage(message);
		
		ACLMessage response = new ACLMessage();
		List<AID> recv = new ArrayList<AID>();
		recv.add(msg.getSender());
		response.setReceivers(recv);
		response.setSender(this.getAid());
		
		Date date = new Date();
		
		if(date.getTime() % 3 == 0)
		{
			message = "[" + this.getAid().getStr() +"]: \n" +
					"Rejecting proposal from: " + msg.getSender().getStr();
			System.out.print(message);
			socket.sendMessage(message);
			response.setPerformative(Performative.REJECT_PROPOSAL);
		}
		else
		{
			message = "[" + this.getAid().getStr() +"]: " +
						"Acceptiong proposal from: " + msg.getSender().getStr();
			System.out.println(message);
			socket.sendMessage(message);
			response.setPerformative(Performative.ACCEPT_PROPOSAL);
		}
		
		messageManager.post(response);
		
	}
	
	private void handleRefuse(ACLMessage msg)
	{
		String message = "[" + this.getAid().getStr() +"]: \n" +
					msg.getSender().getStr() + " refused proposal.";
		LocalLogSocket socket = new LocalLogSocket(agentCenter.getAddress());
		socket.sendMessage(message);
	}
	
	private void handleInform(ACLMessage msg)
	{
		String message = "[" + this.getAid().getStr() +"]: \n"+
					"From: " + msg.getSender().getStr() + " received info: "+ msg.getContent();
		LocalLogSocket socket = new LocalLogSocket(agentCenter.getAddress());
		socket.sendMessage(message);
	}
	
	public void handleFailure(ACLMessage msg)
	{
		String message = "[" + this.getAid().getStr() +"]: \n" +
					msg.getSender().getStr() + " failed.";
		LocalLogSocket socket = new LocalLogSocket(agentCenter.getAddress());
		socket.sendMessage(message);
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
