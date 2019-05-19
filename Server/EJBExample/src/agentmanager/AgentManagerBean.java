package agentmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.NamingException;

import org.infinispan.Cache;

import util.GlobalCache;
import util.JNDITreeParser;
import util.NodeManager;
import util.ObjectFactory;

@Stateless
@Remote(AgentManager.class)
@LocalBean
public class AgentManagerBean implements AgentManager {

	private static final long serialVersionUID = 1L;
	
//	private HashMap<AID, Agent> agents;
	
	@EJB
	private JNDITreeParser jndiTreeParser;
	
	@Override
	public List<AgentType> getAvailableAgentClasses() {
		try {
			return jndiTreeParser.parse();
		} catch (NamingException ex) {
			throw new IllegalStateException(ex);
		}
	}

	@Override
	public List<AID> getRunningAgents() {
		Set<AID> set = getCache().keySet();
		if (set.size() > 0) {
			try {
				AID aid = set.iterator().next();
				try {
					ObjectFactory.lookup(getAgentLookup(aid.getAgType(), true), Agent.class);
				} catch (Exception ex) {
					ObjectFactory.lookup(getAgentLookup(aid.getAgType(), false), Agent.class);
				}
			} catch (Exception ex) {
				getCache().clear();
				return new ArrayList<AID>();
			}
		}
		return new ArrayList<AID>(set);
	}
	
	private HashMap<AID, Agent> getCache() {
//		if (agents == null)
//			agents = GlobalCache.get().getRunningAgents();
		return GlobalCache.get().getRunningAgents();
	}
	
	private String getAgentLookup(AgentType agType, boolean stateful) {
		if (inEar(agType)) {
			// in ear file
			if (stateful)
				return String.format("ejb:%s//%s!%s?stateful", agType.getModule(),
						agType.getEjbName(), Agent.class.getName());
			else
				return String.format("ejb:%s//%s!%s", agType.getModule(), agType.getEjbName(),
						Agent.class.getName());
		} else {
			// in jar file
			if (stateful)
				return String.format("ejb:/%s//%s!%s?stateful", agType.getModule(),
						agType.getEjbName(), Agent.class.getName());
			else
				return String.format("ejb:/%s//%s!%s", agType.getModule(), agType.getEjbName(),
						Agent.class.getName());
		}
	}
	
	private boolean inEar(AgentType agType) {
		if (agType.getModule().contains("/"))
			return true;
		return false;
	}

	
	@Override
	public void startServerAgent(AID aid, AgentInitArgs args, boolean replace) {
		if (getCache().containsKey(aid)) {
			if (!replace) {
				throw new IllegalStateException("Agent already running: " + aid);
			}
			stopAgent(aid);
		}
		Agent agent = null;
		try {
			agent = ObjectFactory.lookup(getAgentLookup(aid.getAgType(), true), Agent.class);
		} catch (IllegalStateException ex) {
			agent = ObjectFactory.lookup(getAgentLookup(aid.getAgType(), false), Agent.class);
		}
		initAgent(agent, aid, args);
		System.out.println("Agent started");
	}

	/**
	 * Starts a server-side agent.
	 */
	@Override
	public AID startServerAgent(AgentType agType, String runtimeName) {
		String host = NodeManager.getNodeName();

		if (host == null) host = AID.HOST_NAME;
		if (agType.args != null) {
			host = agType.args.get("host", AID.HOST_NAME);
		}
		AID aid = new AID(runtimeName, host, agType);
		startServerAgent(aid, agType.args, true);
		return aid;
	}

	@Override
	public void stopAgent(AID aid) {
		Agent agent = getCache().get(aid);
		if (agent != null) {
			agent.stop();
			getCache().remove(aid);

			System.out.println("Agent " + aid.toString() + " stopped");
		}
	}
	
	private void initAgent(Agent agent, AID aid, AgentInitArgs args) {
		// the order of the next two statements matters. if we call init first and the agent
		// sends a message from there, it sometimes happens that the reply arrives before we
		// register the AID. also some agents might wish to terminate themselves inside init.
		getCache().put(aid, agent);
		agent.init(aid, args);
	}

	@Override
	public AgentType getAgentTypeByName(String name) {
		try {
			return jndiTreeParser.getTypeByName(name);
		} catch (NamingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
