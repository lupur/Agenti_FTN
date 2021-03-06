package agent;

import java.io.Serializable;

@SuppressWarnings("serial")
public class AgentType implements Serializable {

	private String name;
	private String module;
		
	public AgentType() {
		super();
	}

	public AgentType(String module, String name) {
		super();
		this.name = name;
		this.module = module;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getModule()
	{
		return module;
	}
	
	public void setModule(String module)
	{
		this.module = module;
	}
	
}
