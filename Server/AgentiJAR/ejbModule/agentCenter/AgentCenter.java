package agentCenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.AccessTimeout;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Remote;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;



import agent.AID;
import agent.AgentType;
import agent.IAgent;
import config.ReadConfig;
import util.JNDITreeParser;


@SuppressWarnings("serial")
@Startup
@Singleton
@Remote(IAgentCenter.class)
@LocalBean
public class AgentCenter implements IAgentCenter {

	@EJB
	private JNDITreeParser jndiTreeParser;
	
	private Node node;
	private Node masterNode;
	private ArrayList<Node> nodes = new ArrayList<Node>();
	ResteasyClient restClient = new ResteasyClientBuilder().build();
	
	private List<IAgent> agents = new ArrayList<IAgent>();
	private HashMap<String, List<AgentType>> supportedTypes = new HashMap<>();
	
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
			supportedTypes.put(node.getAlias(), getAvailableAgentClasses());

			System.out.println("Master node has been created.");
		}
		else
		{
			masterNode = new Node();
			masterNode.setAlias("master");
			masterNode.setAddress(params[2]);
			
			System.out.println("Slave node has been created.");
			registerNode();
		}
		
	}
	
	public boolean registerNode()
	{
		supportedTypes.put(node.getAlias(), getAvailableAgentClasses());

		String url = "http://" + masterNode.getAddress() + "/AgentiWAR/api/center/node";
		ResteasyWebTarget master = restClient.target(url);
		

		Response response = master.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(this, MediaType.APPLICATION_JSON));
		AgentCenter tmp = response.readEntity(new GenericType<AgentCenter>() {});
		this.nodes = tmp.getNodes();
		this.agents = tmp.getRunningAgents();
		this.supportedTypes = tmp.getSupportedTypes();
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
			
			agent.init(aid);
			agents.add(agent);
			System.out.println("Added agent " + agent.getAid().getStr());
		} catch (NamingException ex) {
			System.out.println("Context initialization error." + ex);
		}
		
	}

	@Override
	public AID startServerAgent(AgentType agType, String runtimeName) {
		AID aid = new AID(runtimeName, node, agType);
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
	
	@Override
	public String getAddress()
	{
		return node.getAddress();
	}
	
	@Override
	public IAgent findAgent(AID aid)
	{
		for(IAgent a : agents)
		{
			if( a.getAid().equals(aid))
			{
				return a;
			}			
		}
		return null;
	}

	@Override
	public void informNodes(AgentCenter newCenter)
	{
		if(!node.getAlias().equals("master"))
		{
			System.out.println("Only master can inform others");
			return;
		}
		
		restClient = new ResteasyClientBuilder().build();
		for(Node n : nodes)
		{
			if(n.getAlias().equals("master") || n.getAlias().equals(newCenter.getNode().getAlias()))
			{
				continue;
			}
			
			ResteasyWebTarget target = restClient.target("http://"+n.getAddress()+"/AgentiWAR/api/center/node");
			Response response = target.request(MediaType.APPLICATION_JSON)
					.post(Entity.entity(newCenter, MediaType.APPLICATION_JSON));
		}
		System.out.println("Nodes were informed about new member");
	}
	
	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public Node getMasterNode() {
		return masterNode;
	}

	public void setMasterNode(Node masterNode) {
		this.masterNode = masterNode;
	}

	@Override
	public ArrayList<Node> getNodes() {
		return nodes;
	}

	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;
	}

	@Override
	public HashMap<String, List<AgentType>> getSupportedTypes() {
		return supportedTypes;
	}
	
	@Override
	public void deleteNode(Node n)
	{
		this.getSupportedTypes().remove(n.getAlias());
		
		for(IAgent a :  new ArrayList<IAgent>(agents))
		{
			if(a.getAid().getHost().equals(n.getAlias()))
			{
				agents.remove(a);
			}
		}
		
		nodes.remove(n);
	}
	
	@Override
	public void deleteNodeFromAll(Node n)
	{
		if(!node.getAlias().equals("master"))
		{
			return;
		}
		
		for(Node tmp : nodes)
		{
			if(tmp.getAlias().equals("master") || n.getAlias().equals(n.getAlias()))
				continue;
			
			ResteasyWebTarget target = restClient.target("http://" + tmp.getAddress() +"/AgentiWAR/api/center/node/" + n.getAlias());
            Response response = target.request().delete();

		}
	
	}
	
	public List<AID> getAIDSFromRunningAgents()
	{
		List<AID> agentAIDS = new ArrayList<AID>();
		for(IAgent agent : this.agents)
		{
			System.out.println("- " + agent.getAid().getStr());
			agentAIDS.add(agent.getAid());
		}
		return agentAIDS;
	}
}
