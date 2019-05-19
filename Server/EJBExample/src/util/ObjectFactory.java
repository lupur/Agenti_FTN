package util;

import javax.naming.NamingException;

import agentmanager.Agent;
import agentmanager.AgentManager;
import agentmanager.AgentManagerBean;
import messagemanager.JMSFactory;

public abstract class ObjectFactory {

	public static final String AgentManagerLookup = "ejb:" + Agent.EAR_MODULE + "/" + Agent.EJB_MODULE + "//"
			+ AgentManagerBean.class.getSimpleName() + "!" + AgentManager.class.getName();
//	public static final String MessageManagerLookup = "ejb:" + Agent.EAR_MODULE + "/" + Agent.EJB_MODULE + "//"
//			+ MessageManagerBean.class.getSimpleName() + "!" + MessageManager.class.getName();
//	public static final String WebClientManagerLookup = "ejb:" + Agent.EAR_MODULE + "/" + Agent.EJB_MODULE + "//"
//			+ SiebogRestBean.class.getSimpleName() + "!" + SiebogRest.class.getName()
//			+ "?stateful";
//	public static final String JMSFactoryLookup = "java:app/" + Agent.EJB_MODULE + "/"
//			+ JMSFactory.class.getSimpleName();

	public static AgentManager getAgentManager() {
		return lookup(AgentManagerLookup, AgentManager.class);
	}

//	public static MessageManager getMessageManager(SiebogNode remote) {
//		return lookup(MessageManagerLookup, MessageManager.class, remote);
//	}
//
//	public static SiebogRest getWebClientManager() {
//		return lookup(WebClientManagerLookup, SiebogRestBean.class, SiebogNode.LOCAL);
//	}
//
//	public static SessionContext getSessionContext() {
//		return lookup("java:comp/EJBContext", SessionContext.class, SiebogNode.LOCAL);
//	}

//	public static JMSFactory getJMSFactory() {
//		return lookup(JMSFactoryLookup, JMSFactory.class);
//	}

	@SuppressWarnings("unchecked")
	public static <T> T lookup(String name, Class<T> c) {
		try {
			return (T) ContextFactory.get().lookup(name);
		} catch (NamingException ex) {
			System.out.println("Failed to lookup: " + name);
//			throw new IllegalStateException("Failed to lookup " + name, ex);
			return null;
		}
	}
}
