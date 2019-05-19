package agentmanager;

import java.io.Serializable;
import java.util.List;

public interface AgentManager extends Serializable {

	public List<AgentType> getAvailableAgentClasses();
	
	public List<AID> getRunningAgents();
	
	public void startServerAgent(AID aid, AgentInitArgs args, boolean replace);
	
	public AID startServerAgent(AgentType agType, String runtimeName);

	public void stopAgent(AID aid);
	
	public AgentType getAgentTypeByName(String name);
}
