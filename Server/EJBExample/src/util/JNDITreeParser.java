package util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;

import javax.naming.InitialContext;

import agentmanager.Agent;
import agentmanager.AgentType;

@Stateless
@LocalBean
public class JNDITreeParser {

	private static final String INTF = "!" + Agent.class.getName();
	private static final String EXP = "java:jboss/exported/";
	private Context context;
	
	@PostConstruct
	public void postConstruct() {
		Hashtable<String, Object> jndiProps = new Hashtable<>();
		jndiProps.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
		
		try {
			context =  new InitialContext(jndiProps);
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			context = null;
		}
	}
	
	public List<AgentType> parse() throws NamingException {
		List<AgentType> result = new ArrayList<>();
		NamingEnumeration<NameClassPair> moduleList = context.list(EXP);
		while(moduleList.hasMore())
		{
			NameClassPair ncp = moduleList.next();
			String module = ncp.getName();
			processModule("", module, result);
			
		}
		return result;
	}
	
	public AgentType getTypeByName(String name) throws NamingException 
	{
		List<AgentType> agentTypes = new ArrayList<>();
		NamingEnumeration<NameClassPair> moduleList = context.list(EXP);
		while(moduleList.hasMore())
		{
			NameClassPair ncp = moduleList.next();
			String module = ncp.getName();
			processModule("", module, agentTypes);
			
		}
		
		for(AgentType agType : agentTypes)
		{
			if(agType.getEjbName().equals(name))
			{
				return agType;
			}
		}
		
		return null;
	}
	
	private void processModule(String parentModule, String module, List<AgentType> result) throws NamingException {
		NamingEnumeration<NameClassPair> agentList;
		if (parentModule.equals("")) {
			agentList = context.list(EXP + "/" + module);
		} else {
			try {
				agentList = context.list(EXP + "/" + parentModule + "/" + module);
			} catch (NotContextException ex) {
				return;
			}
		}
		
		while (agentList.hasMore()) {
			NameClassPair ncp = agentList.next();
			String ejbName = ncp.getName();
			if (ejbName.contains("!")) {
				AgentType agType = parseEjbNameIfValid(parentModule, module, ejbName);
				if (agType != null) {
					result.add(agType);
				}
			} else {
				// perhaps a nested module (jar inside ear)?
				processModule(module, ejbName, result);
			}
		}
	}
	
	private AgentType parseEjbNameIfValid(String parentModule, String module, String ejbName) {
		if (ejbName != null && ejbName.endsWith(INTF)) {
			return parseEjbName(parentModule, module, ejbName);
		}
		return null;
	}

	private AgentType parseEjbName(String parentModule, String module, String ejbName) {
		ejbName = extractAgentName(ejbName);
		
		String path;
		if (parentModule.equals("")) {
			path = String.format("/%s/agents/xjaf", module);
			return new AgentType(module, ejbName, path);
		} else {
			path = String.format("/%s/%s/agents/xjaf", parentModule, module);
			return new AgentType(parentModule + "/" + module, ejbName, path);
		}
	}

	private String extractAgentName(String ejbName) {
		int n = ejbName.lastIndexOf(INTF);
		return ejbName.substring(0, n);
	}
}
