package socket;

import java.io.IOException;
import java.util.List;

import javax.ejb.EJB;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import agent.AgentType;
import agentCenter.AgentCenter;
import util.JSON;

@ServerEndpoint(value = "/agentClasses")
public class AgentClassesSocket {

	@EJB
	AgentCenter agentCenter;
	
	@OnOpen
	public void onOpen(Session session)
	{
        System.out.println("onOpen::" + session.getId());        
	}
	
	@OnClose
	public void onClose(Session session)
	{
		System.out.println("onClose::" +  session.getId());
	}
	
	@OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("onMessage::From=" + session.getId() + " Message=" + message);
        
        try {
        	List<AgentType> agents = agentCenter.getAvailableAgentClasses();
            session.getBasicRemote().sendText(JSON.g.toJson(agents));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @OnError
    public void onError(Throwable t) {
        System.out.println("onError::" + t.getMessage());
    }

}
