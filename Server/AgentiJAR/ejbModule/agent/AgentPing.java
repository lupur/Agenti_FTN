package agent;

import javax.ejb.Remote;
import javax.ejb.Stateful;

@SuppressWarnings("serial")
@Stateful
@Remote(IAgent.class)
public class AgentPing extends Agent {

	
}
