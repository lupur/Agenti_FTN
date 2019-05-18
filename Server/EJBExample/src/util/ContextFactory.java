package util;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public abstract class ContextFactory {

	private static Context context;
	private static Context remoteContext;

	/*
	 * java.naming.factory.url.pkgs=org.jboss.ejb.client.naming
	 * java.naming.factory.initial=org.jboss.naming.remote.client.
	 * InitialContextFactory java.naming.provider.url=http-remoting://maja:8080
	 */

	static {
		try {
			Hashtable<String, Object> jndiProps = new Hashtable<>();
			jndiProps.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
			context = new InitialContext(jndiProps);
		} catch (NamingException ex) {
			System.out.println("Context initialization error." + ex);
		}
	}

	public static Context get() {
		return context;
	}
}