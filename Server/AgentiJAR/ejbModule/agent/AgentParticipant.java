package agent;

import java.security.spec.MGF1ParameterSpec;
import java.text.SimpleDateFormat;
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
public class AgentParticipant extends Agent {

	@EJB
	IAgentCenter agentCenter;
	
	@EJB 
	IMessageManager messageManager;
	
	@Override
	public void handleMessage(ACLMessage msg)
	{
		switch (msg.getPerformative()) {
		case CALL_FOR_PROPOSAL:
			handleProposal(msg);
			break;
		case ACCEPT_PROPOSAL:
			doWork(msg);
			break;
		case REJECT_PROPOSAL:
			handleRejectProposal(msg);
			break;

		default:
			System.out.println("Unkwnown performative");
			break;
		}
	}
	
	private void handleProposal(ACLMessage msg)
	{
		System.out.print("[" + this.getAid().getStr() +"]: ");
		System.out.println("Call for proposal from: " + msg.getSender().getStr());
		ACLMessage response = new ACLMessage();
		response.setSender(this.getAid());
		
		List<AID> recv = new ArrayList<AID>();
		recv.add(msg.getSender());
		response.setReceivers(recv);
		
		Date date = new Date();
		
		if(date.getTime() % 3 == 0)
		{
			System.out.print("[" + this.getAid().getStr() +"]: ");
			System.out.println("Decided not to participate.");
			response.setPerformative(Performative.REFUSE);
		}
		else
		{
			System.out.print("[" + this.getAid().getStr() +"]: ");
			System.out.print("Decided to participate.");
			response.setPerformative(Performative.PROPOSE);
		}
		messageManager.post(response);
	}
	
	private void handleRejectProposal(ACLMessage msg) {
		System.out.print("[" + this.getAid().getStr() +"]: ");
		System.out.println("Proposal rejected by " + msg.getSender().getStr());
	}
	
	private void doWork(ACLMessage msg)
	{
		System.out.print("[" + this.getAid().getStr() +"]: ");
		System.out.println("Doing some work for: " + msg.getSender().getStr());
		ACLMessage response = new ACLMessage();
		response.setSender(this.getAid());
		
		List<AID> recv = new ArrayList<AID>();
		recv.add(msg.getSender());
		response.setReceivers(recv);
		
		Date date = new Date();
		
		if(date.getTime() % 5 == 0)
		{
			System.out.print("[" + this.getAid().getStr() +"]: ");
			System.out.println("Failed to finish the job for: " + msg.getSender().getStr());
			response.setPerformative(Performative.FAILURE);
			response.setContent("Could not finish the task.");
		}
		else
		{
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			System.out.print("[" + this.getAid().getStr() +"]: ");
			System.out.println("Finished the job for: " + msg.getSender().getStr());
			response.setPerformative(Performative.INFORM);
			response.setContent("Finish the task at: " + formatter.format(date));
			
		}
		
		messageManager.post(response);
	}
}
