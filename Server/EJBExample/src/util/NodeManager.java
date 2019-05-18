package util;

public class NodeManager {

	public static String getNodeName()
	{
		return System.getProperty("jboss.node.name");
	}
}
