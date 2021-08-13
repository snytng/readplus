package snytng.astah.plugin.readplus;

import com.change_vision.jude.api.inf.model.IState;

public class StateReader {
	
	private StateReader(){}
	
	public static String printActions(IState iState) {
		String message = "";
		
		String entryAction = iState.getEntry();
		String exitAction  = iState.getExit();
		String doActivity  = iState.getDoActivity();

		message += "entry/" + entryAction + ", exit/" + exitAction + ", do/" + doActivity;
		
		return message;
	}
	
}
