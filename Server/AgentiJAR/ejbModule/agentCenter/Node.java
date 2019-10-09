package agentCenter;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Node implements Serializable {

	private String alias;
	private String address;
	
	public String getAlias() {
		return alias;
	}
			
	public Node() {
		super();
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null)
		{
			return false;
		}
		if(obj.getClass() != getClass())
		{
			return false;
		}
		Node node = (Node) obj;
		
		return this.alias.equals(node.getAlias()) &&
			this.address.equals(node.getAddress());
	}
	
	
	
	
}
