package agentCenter;

import java.io.Serializable;
import java.util.List;

import agent.AgentType;
import agent.IAgent;
import agent.AID;

public interface IAgentCenter extends Serializable {

	public List<AgentType> getAvailableAgentClasses();
	public List<IAgent> getRunningAgents();
	public void startServerAgent(AID aid, boolean replace);
	public AID startServerAgent(AgentType agType, String runtimeName);
	public boolean stopAgent(String agentID);
	public AgentType getAgentTypeByName(String name);

}
