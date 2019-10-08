package agentCenter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import agent.AgentType;
import agent.IAgent;
import agent.AID;

public interface IAgentCenter extends Serializable {

	public List<AgentType> getAvailableAgentClasses();
	public List<IAgent> getRunningAgents();
	public void setRunningAgents(List<IAgent> runningAgents);
	public void startServerAgent(AID aid, boolean replace);
	public AID startServerAgent(AgentType agType, String runtimeName);
	public boolean stopAgent(String agentID);
	public AgentType getAgentTypeByName(String name);
	public String getAddress();
	public IAgent findAgent(AID aid);
//	public void informNodes(AgentCenter newCenter);
	public ArrayList<Node> getNodes();
	public void putNode(Node node);
	public void addSupportedType(String key, List<AgentType> value);
	public void setNodes(ArrayList<Node> nodes);
	public HashMap<String, List<AgentType>> getSupportedTypes();
	public void setSupportedTypes(HashMap<String, List<AgentType>> supportedTypes);
	public Node getNode();
	public void setNode(Node node);
	public void deleteNodeFromAll(Node n);
	public void deleteNode(Node n);
	public List<AID> getAIDSFromRunningAgents();

}
