package agentmanager;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.NamingException;

import util.JNDITreeParser;

@Stateless
@Remote(AgentManager.class)
@LocalBean
public class AgentManagerBean implements AgentManager {

	private static final long serialVersionUID = 1L;
	
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

	
}
