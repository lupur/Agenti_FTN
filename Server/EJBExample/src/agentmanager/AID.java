package agentmanager;

import java.io.Serializable;

import util.NodeManager;

public class AID implements Serializable {

	private static final long serialVersionUID = 1L;
	private final String name;
	private final String host;
	private final AgentType agType;
	private String str; //string representation
	
	public static final String HOST_NAME = NodeManager.getNodeName();
	
	public AID()
	{
		this.name = "";
		this.host = "";
		this.agType = null;
	}
	
	public AID(String name, AgentType agType) {
		this(name, HOST_NAME, agType);
	}

	public AID(String name, String host, AgentType agType) {
		this.name = name;
		this.host = host != null ? host : HOST_NAME;
		this.agType = agType;
		str = name + "@" + host;
	}

	public String getName() {
		return name;
	}

	public String getHost() {
		return host;
	}

	public AgentType getAgType() {
		return agType;
	}

	public String getStr() {
		return str;
	}
	
}
