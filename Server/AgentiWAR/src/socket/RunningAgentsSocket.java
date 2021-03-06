package socket;

import java.util.ArrayList;
import java.util.List;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/runningAgents")
public class RunningAgentsSocket {

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
    }
    
	public static void sendRunningAgents(String message)
	{
        for(Session s : sessions)
        {
        	if(s.isOpen())
        	{
                s.getAsyncRemote().sendText(message);
        	}
        }
	}
	
    @OnError
    public void onError(Throwable t) {
        System.out.println("onError::" + t.getMessage());
    }

}
