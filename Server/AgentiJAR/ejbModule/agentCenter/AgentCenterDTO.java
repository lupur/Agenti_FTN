package agentCenter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import agent.AID;
import agent.AgentType;
import agent.IAgent;

@SuppressWarnings("serial")
public class AgentCenterDTO implements Serializable {
	
	private ArrayList<Node> nodes = new ArrayList<Node>();
	private List<AID> agents = new ArrayList<AID>();
	private HashMap<String, List<AgentType>> supportedTypes = new HashMap<>();
	private List<AgentType> availableClasses = new ArrayList<>();
	private Node node = new Node();
	
	public AgentCenterDTO(IAgentCenter agentCenter)
	{
		this.nodes = agentCenter.getNodes();
		this.agents = agentCenter.getRunningAgents();
		this.supportedTypes = agentCenter.getSupportedTypes();
		this.node = agentCenter.getNode();
		this.availableClasses = agentCenter.getAvailableAgentClasses();
	}
	public ArrayList<Node> getNodes() {
		return nodes;
	}
	public List<AID> getAgents() {
		return agents;
	}
	public HashMap<String, List<AgentType>> getSupportedTypes() {
		return supportedTypes;
	}
	
	public List<AgentType> getAvailableAgentClasses()
	{
		return availableClasses;
	}
	
	public Node getNode()
	{
		return node;
	}
	@Override
	public String toString() {
		return "AgentCenterDTO [nodes=" + nodes + ", agents=" + agents + ", supportedTypes=" + supportedTypes
				+ ", availableClasses=" + availableClasses + ", node=" + node + "]";
	}

	public boolean isValid()
	{
		if(this.getAgents() == null)
			return false;
		
		if(this.getAvailableAgentClasses() == null)
			return false;
		
		if(this.getNode() == null)
			return false;
		
		if(this.getNodes() == null)
			return false;
		
		if(this.getSupportedTypes() == null)
			return false;
		
		return true;
		
	}
}
