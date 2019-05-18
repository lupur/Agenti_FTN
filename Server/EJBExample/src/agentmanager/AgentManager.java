package agentmanager;

import java.io.Serializable;
import java.util.List;

public interface AgentManager extends Serializable {

	public List<AgentType> getAvailableAgentClasses();
}
