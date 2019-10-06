package localSocket;

import java.io.Serializable;

public interface ILocalLogSocket extends Serializable {

	public void sendMessage(String message);
	
}
