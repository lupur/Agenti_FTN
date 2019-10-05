package socket;

import java.io.IOException;
import java.util.ArrayList;
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
	
	private static List<Session> sessions = new ArrayList<Session>();
	
	@OnOpen
	public void onOpen(Session session)
	{
        System.out.println("onOpen::" + session.getId());    
        sessions.add(session);
	}
	
	@OnClose
	public void onClose(Session session)
	{
		System.out.println("onClose::" +  session.getId());
		sessions.remove(session);
	}
	
	@OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("onMessage::From=" + session.getId() + " Message=" + message);
        
        List<AgentType> agents = agentCenter.getAvailableAgentClasses();
		session.getAsyncRemote().sendText(JSON.g.toJson(agents));
    }
    
	public static void sendAvailableAgentClasses()
	{
//        List<AgentType> agents = agentCenter.getAvailableAgentClasses();
//        for(Session s : sessions)
//        {
//        	if(s.isOpen())
//        	{
//                try {
//					s.getBasicRemote().sendText(JSON.g.toJson(agents));
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//        	}
//        }
	}
	
    @OnError
    public void onError(Throwable t) {
        System.out.println("onError::" + t.getMessage());
    }

}
