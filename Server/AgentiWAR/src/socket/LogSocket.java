package socket;

import java.util.ArrayList;
import java.util.List;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/log")
public class LogSocket {
	
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
        sendLog(message);
    }
    
	public static void sendLog(String message)
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