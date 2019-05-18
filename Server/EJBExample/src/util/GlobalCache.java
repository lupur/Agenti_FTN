package util;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;

import agentmanager.AID;
import agentmanager.Agent;

public class GlobalCache {
	private static final String CACHE_CONTAINER = "java:jboss/infinispan/container/siebog-cache";
	private static GlobalCache instance;
	private CacheContainer cacheContainer;
	private static final String RUNNING_AGENTS = "running-agents";

	public static GlobalCache get() {
		if (instance == null) {
			synchronized (GlobalCache.class) {
				if (instance == null)
					instance = new GlobalCache();
			}
		}
		return instance;
	}

	private GlobalCache() {
		cacheContainer = ObjectFactory.lookup(CACHE_CONTAINER, CacheContainer.class);
	}

	public Cache<AID, Agent> getRunningAgents() {
		return cacheContainer.getCache(RUNNING_AGENTS);
	}

	public Cache<?, ?> getCache(String name) {
		return cacheContainer.getCache(name);
	}
}
