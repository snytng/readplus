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
					message += View.getViewString("TransitionReader.fromInitialStateOfDiagram");
				}
				// ある状態の疑似開始状態の場合
				else {
					message += String.format(
							View.getViewString("TransitionReader.fromInitialStateOfState"),
							initialState.getContainer());

				}
			}
			// 疑似開始状態以外の場合
			else {
				message += String.format(
						View.getViewString("TransitionReader.fromPseudoState"),
						source);

			}
		}
		//元状態が疑似状態以外の場合
		else {
			if(doActivity != null && ! doActivity.equals("")){
				message += String.format(
						View.getViewString("TransitionReader.fromStateWhileDoing"),
						source, doActivity);
			} else {
				message += String.format(
						View.getViewString("TransitionReader.fromState"),
						source);

			}

			if(event != null && ! event.equals("")){
				message += String.format(
						View.getViewString("TransitionReader.event"),
						event);
			} else {
				message += View.getViewString("TransitionReader.nullEvent");
			}
		}

		if(guard != null && ! guard.equals("")){
			message += String.format(
					View.getViewString("TransitionReader.transitionWithGuard"),
					guard);
		} else {
			message += View.getViewString("TransitionReader.transitionWithoutGuard");
		}

		if(action != null && ! action.equals("")){
			message += String.format(
					View.getViewString("TransitionReader.toStateWithactionOfTransition"),
					action, target);
		} else {
			message += String.format(
					View.getViewString("TransitionReader.toState"),
					target);
		}

		return message;
	}
}
