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
import javax.json.Json;
import javax.ejb.Remote;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import com.google.gson.Gson;

import javax.ejb.Lock;
import javax.ejb.LockType;

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
@Lock(LockType.READ)
@AccessTimeout(-1)
public class AgentCenter implements IAgentCenter {

	@EJB
	private JNDITreeParser jndiTreeParser;
	
	private Node node;
	private Node masterNode;
	private ArrayList<Node> nodes = new ArrayList<Node>();
	private List<AID> agents = new ArrayList<AID>();
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
		
		javax.ws.rs.client.Client client = ClientBuilder.newClient();
		
		ResteasyClient restClient = new ResteasyClientBuilder().build();
		AgentCenterDTO acDto = new AgentCenterDTO(this);
		String url = "http://" + masterNode.getAddress() + "/AgentiWAR/api/center/node";
		ResteasyWebTarget master = restClient.target(url);
		
		System.out.println("Sending registartion to master");
		
		Response response = null;
		
		
		
		Thread t1 = new Thread(new Runnable() {
		    @Override
		    public void run() {
		        // code goes here.
		    	Response response = master.request(MediaType.APPLICATION_JSON)
						.post(Entity.entity(new Gson().toJson(acDto), MediaType.APPLICATION_JSON));
		    	
		    	return;		    	
		    }
		});  
		t1.start();
		
//		AgentCenterDTO responseAC = new Gson().fromJson((String) response.readEntity(String.class), AgentCenterDTO.class);
//		
//		if(responseAC.isValid())
//		{
//			agents = responseAC.getAgents();
//			nodes = responseAC.getNodes();
//			responseAC.getAvailableAgentClasses();
//			supportedTypes = responseAC.getSupportedTypes();
//			
//		}
		
//		System.out.println("Response from registration: "+ responseAC.toString());
		System.out.println("Finished registartion");
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
	public List<AID> getRunningAgents() {
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
		for(AID a : agents)
		{
			if(a.equals(aid))
			{
				if(!replace)
				{
					throw new IllegalStateException("Agent already running: " + aid);
				}
				stopAgent(a.getStr());
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
			agents.add(agent.getAid());
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
		for(AID aid : agents)
		{
			if(aid.getStr().equals(agentID))
			{
				IAgent agent = null;
				try {
					Context context = new InitialContext();
					agent = (IAgent) context.lookup("java:global/AgentiEAR/AgentiJAR/" + aid.getType().getName());
					agent.stop();
					agents.remove(aid);
				} catch (NamingException ex) {
					System.out.println("Context initialization error." + ex);
				}
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
		for(AID a : agents)
		{
			if( a.equals(aid))
			{
				IAgent agent = null;
				try {
					Context context = new InitialContext();
					agent = (IAgent) context.lookup("java:global/AgentiEAR/AgentiJAR/" + aid.getType().getName());
					return agent;
				} catch (NamingException ex) {
					System.out.println("Context initialization error." + ex);
				}
			}			
		}
		return null;
	}

//	@Override
//	public void informNodes(AgentCenter newCenter)
//	{
//		if(!node.getAlias().equals("master"))
//		{
//			System.out.println("Only master can inform others");
//			return;
//		}
//		
//		restClient = new ResteasyClientBuilder().build();
//		for(Node n : nodes)
//		{
//			if(n.getAlias().equals("master") || n.getAlias().equals(newCenter.getNode().getAlias()))
//			{
//				continue;
//			}
//			
//			ResteasyWebTarget target = restClient.target("http://"+n.getAddress()+"/AgentiWAR/api/center/node");
//			Response response = target.request(MediaType.APPLICATION_JSON)
//					.post(Entity.entity(newCenter, MediaType.APPLICATION_JSON));
//		}
//		System.out.println("Nodes were informed about new member");
//	}
	
	public Node getNode() {
		return node;
	}

	@Override
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

	@Override
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
		
		for(AID a :  new ArrayList<AID>(agents))
		{
			if(a.getHost().equals(n.getAlias()))
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
			ResteasyClient restClient = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = restClient.target("http://" + tmp.getAddress() +"/AgentiWAR/api/center/node/" + n.getAlias());
            Response response = target.request().delete();

		}
	
	}
	

	@Override
	public void setRunningAgents(List<AID> runningAgents) {
		this.agents = runningAgents;
	}

	@Override
	public void setSupportedTypes(HashMap<String, List<AgentType>> supportedTypes) {
		this.supportedTypes = supportedTypes;
	}

	@Override
	public void putNode(Node node) {
		// TODO Auto-generated method stub
		this.nodes.add(node);
	}

	@Override
	public void addSupportedType(String key, List<AgentType> value) {
		// TODO Auto-generated method stub
		supportedTypes.put(key, value);
	}

}
