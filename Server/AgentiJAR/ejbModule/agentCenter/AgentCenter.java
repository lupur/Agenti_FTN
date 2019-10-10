package agentCenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.AccessTimeout;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.json.Json;
import javax.ejb.Remote;
import javax.ejb.Schedule;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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
	private List<AID> allAids = new ArrayList<AID>();
	private List<IAgent> agents = new ArrayList<IAgent>();
	private HashMap<String, List<AgentType>> supportedTypes = new HashMap<>();
	
	public AgentCenter() {};
	
	private Thread heartBeatThread = null;
	private final ReentrantLock nodesLock = new ReentrantLock();
	
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
		heartBeatThread = new Thread(new Runnable() {
		    @Override
		    public void run() {
		        // code goes here.
		    	while(true) {
		    		try {
		    			nodesLock.lock();
		    			heartBeat();
		    			nodesLock.unlock();
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    	}	    	
		    }
		});  
		heartBeatThread.start();
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
		return allAids;
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
		for(AID a : allAids)
		{
			if(a.equals(aid))
			{
				if(!replace)
				{
					throw new IllegalStateException("Agent already running: " + aid);
				}
				stopAgent(a.getStr());
				allAids.remove(a);
				
				System.out.println("Same agent deleted");
				break;
			}
		}
		
		for(IAgent ag : agents)
		{
			if(ag.getAid().equals(aid))
			{
				agents.remove(ag);
				break;
			}
		}
		
		IAgent agent = null;
		try {
			Context context = new InitialContext();
			agent = (IAgent) context.lookup("java:global/AgentiEAR/AgentiJAR/" + aid.getType().getName());
			
			agent.init(aid);
			allAids.add(agent.getAid());
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
		for(AID aid : allAids)
		{
			if(aid.getStr().equals(agentID))
			{
				IAgent agent = null;
				try {
					Context context = new InitialContext();
					agent = (IAgent) context.lookup("java:global/AgentiEAR/AgentiJAR/" + aid.getType().getName());
					agent.stop();
					allAids.remove(aid);
					
					agents.remove(agent);
					
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
		for(IAgent a : agents)
		{
			if( a.getAid().getStr().equals(aid.getStr()))
			{
				return a;
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
		nodesLock.lock();
		this.node = node;
		nodesLock.unlock();
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
		System.out.print("Starting deleting node : " + n.getAlias());
		this.getSupportedTypes().remove(n.getAlias());
		
		for(AID a : new ArrayList<AID>(allAids))
		{
			if(a.getHost().getAlias().equals(n.getAlias()))
			{
				allAids.remove(a);
			}
		}
		for(IAgent a: new ArrayList<IAgent>(agents))
		{
			if(a.getAid().getHost().getAlias().equals(n.getAlias()))
			{
				agents.remove(a);
			}
		}
		nodesLock.lock();
		nodes.remove(n);
		nodesLock.unlock();
		System.out.print("Finished deleting node : " + n.getAlias());
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
			if(tmp.getAlias().equals("master") || n.getAlias().equals(tmp.getAlias()))
				continue;
			ResteasyClient restClient = new ResteasyClientBuilder().build();
			String url = "http://" + tmp.getAddress() +"/AgentiWAR/api/center/node/" + n.getAlias();
			ResteasyWebTarget target = restClient.target(url);
           System.out.println("SHOULD INFORM : " + url);
			Response response = target.request().delete();
		}
	}
	
	@PreDestroy
	public void onDestroy()
	{
		System.out.println("Starting node deletion");
		
    	ResteasyClient restClient = new ResteasyClientBuilder().build();
    	String url = "http://" + masterNode.getAddress() +"/AgentiWAR/api/center/node/" + node.getAlias();
		ResteasyWebTarget target = restClient.target(url);
		System.out.println("Deleting request: "+ url);
        Response response = target.request().delete();
		System.out.println("Result of the deletion repsonse: " + response.getStatus());
		
		if(heartBeatThread != null)
		{
			heartBeatThread.stop();
		}
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void setRunningAgents(List<AID> runningAgents) {
		this.allAids = runningAgents;
	}

	@Override
	public void setSupportedTypes(HashMap<String, List<AgentType>> supportedTypes) {
		this.supportedTypes = supportedTypes;
	}

	@Override
	public void putNode(Node node) {
		this.nodes.add(node);
	}

	@Override
	public void addSupportedType(String key, List<AgentType> value) {
		supportedTypes.put(key, value);
	}
	
	@Override
	public boolean registerRunningAgents() {
		
		Client client = ClientBuilder.newClient();
		ResteasyClient restClient = new ResteasyClientBuilder().build();
		List<AID> runningAgents = getRunningAgents();
		
		for(Node node : nodes) {
			if(node.equals(this.getNode()))
			{
				continue;
			}
			System.out.println("SHOULD send running agent on node: " + node.getAlias());
			String url = "http://" + node.getAddress() + "/AgentiWAR/api/center/agents/running";
			ResteasyWebTarget target = restClient.target(url);
			Response response = target.request(MediaType.APPLICATION_JSON)
					.post(Entity.entity(new Gson().toJson(runningAgents), MediaType.APPLICATION_JSON));
			System.out.println("GOT response: "+response.getStatus());
		}
		
		return true;
	}

	@Override
	public void heartBeat() {
		if(node == null)
		{
			return;
		}
		Client client = ClientBuilder.newClient();
		ResteasyClient restClient = new ResteasyClientBuilder().build();
		for(Node iteratedNode : nodes) {
			String url = "http://" + iteratedNode.getAddress() + "/AgentiWAR/api/center/node";
			ResteasyWebTarget target = restClient.target(url);
			Response response = target.request(MediaType.APPLICATION_JSON).get();
			if(response.getStatus()== 500) {
				System.out.println("GOT 500 First time for " + iteratedNode.getAlias());
				response = target.request(MediaType.APPLICATION_JSON).get();
				if(response.getStatus()== 500) {
					System.out.println("GOT 500 Second time for " + iteratedNode.getAlias());
					url = "http://" + masterNode.getAddress() + "/AgentiWAR/api/center/node/" + iteratedNode.getAlias();
					target = restClient.target(url);
					response = target.request().delete();
				} 
			}
		}
	}
}
