package agentmanager;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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
		this.str = "";
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
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AID other = (AID) obj;
		return str.equals(other.str);
	}
	
	@Override
	public String toString() {
		Map<String, Object> obj = new HashMap<String, Object>();
		try {
			obj.put("name", name);
			obj.put("host", host);
			obj.put("agType", agType);
			obj.put("str", str);
		} catch (Exception ex) {
		}
		return obj.toString();
	}
	
	@Override
	public int hashCode() {
		return str.hashCode();
	}
}
