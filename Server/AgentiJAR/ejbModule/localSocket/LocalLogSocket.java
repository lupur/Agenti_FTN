package localSocket;

import java.io.IOException;
import java.net.URI;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

@ClientEndpoint
public class LocalLogSocket {

	Session userSession = null;
	
	public LocalLogSocket(String address)
	{
		final URI endpoint = URI.create("ws://" + address + "/AgentiWAR/log");
		try {
			WebSocketContainer client = ContainerProvider.getWebSocketContainer();
			client.connectToServer(this, endpoint);
		} catch (DeploymentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@OnOpen
    public void onOpen(Session userSession) {
        System.out.println("Opening local websocket");
        this.userSession = userSession;
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        System.out.println("Closing local websocket");
        this.userSession = null;
    }
    
    @OnMessage
    public void onMessage(String message)
    {
    	
    }
    
    
    public void sendMessage(String message) {
        this.userSession.getAsyncRemote().sendText(message);
    }

}
