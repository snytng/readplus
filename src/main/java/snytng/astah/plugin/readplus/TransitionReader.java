package snytng.astah.plugin.readplus;

import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IPseudostate;
import com.change_vision.jude.api.inf.model.IState;
import com.change_vision.jude.api.inf.model.ITransition;
import com.change_vision.jude.api.inf.model.IVertex;

public class TransitionReader {
	
	private TransitionReader(){}

	public static boolean isSupportedTransition(IElement e){
		if(e == null) return false;

		return 
				(e instanceof ITransition);
	}


	public static String read(ITransition iTransition){
		String message ="";

		IVertex sourceState = iTransition.getSource();
		String source       = sourceState.getName();
		IVertex targetState = iTransition.getTarget();
		String target       = targetState.getName();
		
		String event = iTransition.getEvent();
		String action = iTransition.getAction();
		String guard = iTransition.getGuard();
		String doActivity = null;
		
		if(iTransition.getSource() instanceof IState){
			doActivity = ((IState)iTransition.getSource()).getDoActivity();
		}

		// 元状態が疑似状態の場合
		if(sourceState instanceof IPseudostate){
			// 疑似開始状態の場合
			if(((IPseudostate) sourceState).isInitialPseudostate()){
				IPseudostate initialState = (IPseudostate)sourceState;
				// 状態遷移図の疑似開始状態の場合
				if(initialState.getContainer() == null){
					message += "状態マシンが起動";			
				} 
				// ある状態の疑似開始状態の場合
				else {
					message += initialState.getContainer() + "状態が開始";
				}
			}
			// 疑似開始状態以外の場合
			else {
				message += "「" + source + "」状態に到達";			
			}
		}
		//元状態が疑似状態以外の場合
		else {
			message += "「" + source + "」状態で、";
			if(doActivity != null && ! doActivity.equals("")){
				message += "「" + doActivity + "」を実行中の時に、";
			}

			if(event != null && ! event.equals("")){
				message += "「" + event + "」イベントが発生";
			} else {
				message += "ヌルイベントが発生";
			}
		}
		
		if(guard != null && ! guard.equals("")){
			message += "して、「" + guard + "」という条件を満たすならば、"; 
		} else {
			message += "したら、";
		}
		if(action != null && ! action.equals("")){
			message += "「" + action + "」を実行して、";
		}
		message += "「" + target + "」状態に遷移する。";

		return message;
	}
}
