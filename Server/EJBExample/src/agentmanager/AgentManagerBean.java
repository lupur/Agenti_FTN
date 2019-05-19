package agentmanager;

import java.util.ArrayList;
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
import util.ObjectFactory;

@Stateless
@Remote(AgentManager.class)
@LocalBean
public class AgentManagerBean implements AgentManager {

	private static final long serialVersionUID = 1L;
	
	private Cache<AID, Agent> agents;
	
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
	
	private Cache<AID, Agent> getCache() {
		if (agents == null)
			agents = GlobalCache.get().getRunningAgents();
		return agents;
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


	
}
