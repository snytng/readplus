package snytng.astah.plugin.readplus;

import com.change_vision.jude.api.inf.model.IActivityDiagram;
import com.change_vision.jude.api.inf.model.IActivityNode;
import com.change_vision.jude.api.inf.model.IFlow;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.view.IDiagramViewManager;

public class ActivityDiagramReader {

	private IActivityDiagram diagram = null;

	public ActivityDiagramReader(IActivityDiagram diagram){
		this.diagram = diagram;
	}

	/**
	 * アクティビティ図に含まれるノードの数を取得する
	 * @return ノード数
	 */
	public int getActivityNodes(){
		IActivityNode[] nodes = this.diagram.getActivity().getActivityNodes();
		return nodes == null ? 0 : nodes.length;
	}

	/**
	 * アクティビティ図に含まれるフローの数を取得する
	 * @return フロー数
	 */
	public int getFlows(){
		IFlow[] flows = this.diagram.getActivity().getFlows();
		return flows == null ? 0 : flows.length;
	}


	public static MessagePresentation getMessagePresentation(IActivityDiagram diagram, IDiagramViewManager dvm) {
		MessagePresentation mps = new MessagePresentation();
	
		ActivityDiagramReader adr = new ActivityDiagramReader(diagram);
	
		// アクティビティ図のノード数を表示する
		mps.add("[" + diagram.getName() + "]アクティビティ図には、" + adr.getActivityNodes() + "個のノードがあります", null);
	
		mps.add("=====", null);

		// 選択要素の表示
		IPresentation[] ps = dvm.getSelectedPresentations();
		if(ps.length > 0){
			mps.add("[" + diagram.getName() + "]アクティビティ図で、" + ps.length + "個の要素が選択されています", null);
		}
		
		return mps;
	}

}
