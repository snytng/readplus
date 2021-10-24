package snytng.astah.plugin.readplus;

import java.util.Arrays;
import java.util.List;

import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.IState;
import com.change_vision.jude.api.inf.model.IStateMachine;
import com.change_vision.jude.api.inf.model.IStateMachineDiagram;
import com.change_vision.jude.api.inf.model.ITransition;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.view.IDiagramViewManager;

public class StateMachineDiagramReader {

	private IStateMachineDiagram diagram = null;

	public StateMachineDiagramReader(IStateMachineDiagram diagram){
		this.diagram = diagram;
	}

	/**
	 * 状態マシン図に含まれる状態数を取得する
	 * @return 状態数
	 */
	public int getNumberOfStates(){
		IStateMachine statemachine = this.diagram.getStateMachine();
		return statemachine == null ? 0 : statemachine.getVertexes().length;
	}

	/**
	 * 状態マシン図に含まれる遷移数を取得する
	 * @return 遷移数
	 */
	public int getNumberOfTransitions(){
		IStateMachine statemachine = this.diagram.getStateMachine();
		return statemachine == null ? 0 : statemachine.getTransitions().length;
	}

	public static MessagePresentation getMessagePresentation(IStateMachineDiagram diagram, IDiagramViewManager dvm) {
		MessagePresentation mps = new MessagePresentation();

		StateMachineDiagramReader smdr = new StateMachineDiagramReader(diagram);

		// ステート数を表示する
		mps.add(String.format(
				View.getViewString("StateMachineDiagramReader.numberOfStatesAndTransitions.meessage"),
				diagram.getName(), smdr.getNumberOfStates(),smdr.getNumberOfTransitions()
				),
				null);

		mps.add("=====", null);

		// 選択要素の表示
		IPresentation[] presentation = dvm.getSelectedPresentations();
		if(presentation.length > 0){
			mps.add(String.format(
					View.getViewString("StateMachineDiagramReader.selection.meessage"),
					presentation.length
					),
					null);

			for(int i = 0; i < presentation.length; i++){
				IPresentation p = presentation[i];
				// 状態
				if(p.getModel() instanceof IState) {
					IState s = (IState)p.getModel();
					// 状態名の表示
					mps.add(String.format(
							View.getViewString("StateMachineDiagramReader.state.message"),
							s.getName()
							),
							p);

					// 状態の内部属性を表示
					String attr = StateReader.printActions(s);
					mps.add(String.format(
							View.getViewString("StateMachineDiagramReader.state.action.message"),
							attr
							),
							p);

					// 状態から移る遷移を読み上げ
					IPresentation[] dps;
					try {
						dps = diagram.getPresentations();
						List<ITransition> ts = Arrays.asList(s.getOutgoings());
						for(IPresentation dp : dps){
							if(ts.contains(dp.getModel())){
								String tra = TransitionReader.read((ITransition)dp.getModel());
								mps.add(String.format(
										View.getViewString("StateMachineDiagramReader.state.transition.message"),
										tra
										),
										dp);

							}
						}
					} catch (InvalidUsingException e) {
						e.printStackTrace();
					}

				}
				// 遷移
				else if (p.getModel() instanceof ITransition){
					ITransition t = (ITransition)p.getModel();
					String tra = TransitionReader.read(t);
					if(! tra.equals("")){
						mps.add(String.format(
								View.getViewString("StateMachineDiagramReader.transition.message"),
								tra
								),
								p);

					}
				}
				// それ以外
				else {
					mps.add(p.getType(), p);
				}
			}
		}
		// 選択していない場合には、状態マシン図にある遷移のみを読み上げ
		else {
			IPresentation[] ps;
			try {
				ps = diagram.getPresentations();
				for (IPresentation p : ps){
					if (p.getModel() instanceof ITransition){
						ITransition t = (ITransition)p.getModel();
						String tra = TransitionReader.read(t);
						if(! tra.equals("")){
							mps.add("状態遷移：" + tra, p);
						}
					}
				}
			} catch (InvalidUsingException e) {
				e.printStackTrace();
			}
		}

		return mps;
	}

}
