package agentmanager;

import java.io.Serializable;

public class AgentType implements Serializable {
	
	private static final long serialVersionUID = -90743052629163920L;
	
	public static final char SEPARATOR = '$';
	private final String module;
	private final String ejbName;
	private final String path;
	public  final AgentInitArgs args;
	
	public AgentType() {
		this("", "");
	}

	public AgentType(String module, String ejbName) {
		this(module, ejbName, "");
	}

	public AgentType(String module, String ejbName, String path) {
		this.module = module;
		this.ejbName = ejbName;
		this.path = path;
		args = null;
	}

	public String getName() {
		return ejbName;
	}
	
	public String getModule() {
		return module;
	}
	
	
}
