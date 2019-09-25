package agent;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import agentCenter.Node;

@SuppressWarnings("serial")
public class AID implements Serializable {

	private String name;
	private Node host;
	private AgentType type;
	private String str; //string representation

	public AID()
	{
		this.name = "";
		this.host = new Node();
		this.str = "";
		this.type = null;
	}

	public AID(String name, Node host, AgentType type) {
		this.name = name;
		this.type = type;
		this.host = host;
		str = name + "@" + host.getAlias();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Node getHost() {
		return host;
	}

	public void setHost(Node host) {
		this.host = host;
	}

	public AgentType getType() {
		return type;
	}

	public void setType(AgentType type) {
		this.type = type;
	}
	
	
	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
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
			obj.put("type", type);
			obj.put("str", str);
		} catch (Exception ex) {
		}
		return obj.toString();
	}
	
}
