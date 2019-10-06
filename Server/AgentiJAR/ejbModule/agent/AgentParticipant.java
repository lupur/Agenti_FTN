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
import localSocket.LocalLogSocket;
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
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		String message = "[" + this.getAid().getStr() +"]: \n" +
					"Call for proposal from: " + msg.getSender().getStr();
		LocalLogSocket logger = new LocalLogSocket(agentCenter.getAddress());
		logger.sendMessage(message);
		ACLMessage response = new ACLMessage();
		response.setSender(this.getAid());
		
		List<AID> recv = new ArrayList<AID>();
		recv.add(msg.getSender());
		response.setReceivers(recv);
		
		Date date = new Date();
		
		if(date.getTime() % 3 == 0)
		{
			message = "[" + this.getAid().getStr() +"]: \n" +
								"Decided not to participate.";
			
			response.setPerformative(Performative.REFUSE);
		}
		else
		{
			message = "[" + this.getAid().getStr() +"]: \n"
						+ "Decided to participate.";
			response.setPerformative(Performative.PROPOSE);
		}
		logger.sendMessage(message);
		messageManager.post(response);
	}
	
	private void handleRejectProposal(ACLMessage msg) {
		String message = "[" + this.getAid().getStr() +"]: \n" +
						"Proposal rejected by " + msg.getSender().getStr();
		
		LocalLogSocket logger = new LocalLogSocket(agentCenter.getAddress());
		logger.sendMessage(message);
		
	}
	
	private void doWork(ACLMessage msg)
	{
		String message = "[" + this.getAid().getStr() +"]: \n" +
						"Doing some work for: " + msg.getSender().getStr();
		LocalLogSocket logger = new LocalLogSocket(agentCenter.getAddress());
		logger.sendMessage(message);
		ACLMessage response = new ACLMessage();
		response.setSender(this.getAid());
		
		List<AID> recv = new ArrayList<AID>();
		recv.add(msg.getSender());
		response.setReceivers(recv);
		
		Date date = new Date();
		
		if(date.getTime() % 5 == 0)
		{
			message = "[" + this.getAid().getStr() +"]: \n" +
							"Failed to finish the job for: " + msg.getSender().getStr();
			logger.sendMessage(message);
			response.setPerformative(Performative.FAILURE);
			response.setContent("Could not finish the task.");
		}
		else
		{
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			message = "[" + this.getAid().getStr() +"]: \n" +
					"Finished the job for: " + msg.getSender().getStr();
			logger.sendMessage(message);
			response.setPerformative(Performative.INFORM);
			response.setContent("Finish the task at: " + formatter.format(date));
			
		}
		
		messageManager.post(response);
	}
}
