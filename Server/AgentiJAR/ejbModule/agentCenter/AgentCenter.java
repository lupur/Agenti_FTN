package agentCenter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import com.sun.xml.internal.bind.v2.ContextFactory;

import agent.AID;
import agent.Agent;
import agent.AgentType;
import agent.IAgent;
import config.ReadConfig;
import sun.management.resources.agent;
import util.JNDITreeParser;

import java.util.Hashtable;


@SuppressWarnings("serial")
@Stateless
@Remote(IAgentCenter.class)
@LocalBean
public class AgentCenter implements IAgentCenter {

	@EJB
	private JNDITreeParser jndiTreeParser;
	
	private Node node;
	private Node masterNode;
	private ArrayList<Node> nodes = new ArrayList<Node>();
	ResteasyClient restClient = new ResteasyClientBuilder().build();
	
	private ArrayList<IAgent> agents = new ArrayList<IAgent>();
	
	public AgentCenter() {};
	
	@PostConstruct
	public void Init()
	{
		ReadConfig readConfig = new ReadConfig();
		String[] params = readConfig.GetConfigParams();
		
		if(params == null)
		{
			System.out.print("Invalid config file... Bailing out...");
			System.exit(-1);
		}
		
		node = new Node();
		node.setAlias(params[0]);
		node.setAddress(params[1]);
		
		if(node.getAlias().equals("master"))
		{
			masterNode = new Node();
			masterNode = node;
			nodes = new ArrayList<Node>();
			nodes.add(node);
			System.out.println("Master node has been created.");
		}
		else
		{
			masterNode = new Node();
			masterNode.setAlias("mater");
			masterNode.setAddress(params[2]);
			
			System.out.println("Slave node has been created.");
			
			//TODO: SHOULD IMPLEMENT REGISTRATION METHOD;
		}
	}
	
	public boolean registerNode()
	{
		String url = "http://" + masterNode.getAddress() + "Agenti/api/agentCenter/node";
		ResteasyWebTarget master = restClient.target(url);
		
		return true;
	}
	
	@Override
	public List<AgentType> getAvailableAgentClasses() {
		try {
			return jndiTreeParser.parse();
		} catch (NamingException ex) {
			throw new IllegalStateException(ex);
		}
	}

	@Override
	public List<IAgent> getRunningAgents() {
		return agents;
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

	@Override
	public void startServerAgent(AID aid, boolean replace) {
		for(IAgent a : agents)
		{
			if(a.getAid().equals(aid))
			{
				if(!replace)
				{
					throw new IllegalStateException("Agent already running: " + aid);
				}
				stopAgent(a.getAid().getStr());
				agents.remove(a);
				System.out.println("Same agent deleted");
				break;
			}
		}
		IAgent agent = null;
		try {
			Context context = new InitialContext();
			agent = (IAgent) context.lookup("java:global/AgentiEAR/AgentiJAR/" + aid.getType().getName());
			agents.add(agent);
			agent.init(aid);
			System.out.println("Added agent " + agent.getAid().getName());
		} catch (NamingException ex) {
			System.out.println("Context initialization error." + ex);
		}
		
	}

	@Override
	public AID startServerAgent(AgentType agType, String runtimeName) {
		Node host = new Node();
		AID aid = new AID(runtimeName, host, agType);
		startServerAgent(aid, true);
		return aid;
	}

	@Override
	public boolean stopAgent(String agentID) {
		for(IAgent agent : agents)
		{
			if(agent.getAid().getStr().equals(agentID))
			{
				agent.stop();
				agents.remove(agent);
				return true;
			}
		}
		return false;
	}
	
	
}
