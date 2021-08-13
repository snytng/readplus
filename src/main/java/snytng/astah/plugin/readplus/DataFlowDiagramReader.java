package snytng.astah.plugin.readplus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.IDataFlow;
import com.change_vision.jude.api.inf.model.IDataFlowDiagram;
import com.change_vision.jude.api.inf.model.IDataFlowNode;
import com.change_vision.jude.api.inf.model.IDataStore;
import com.change_vision.jude.api.inf.model.IExternalEntity;
import com.change_vision.jude.api.inf.model.IProcessBox;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.view.IDiagramViewManager;

public class DataFlowDiagramReader {

	private IDataFlowDiagram diagram = null;

	public DataFlowDiagramReader(IDataFlowDiagram diagram){
		this.diagram = diagram;
	}

	/**
	 * データフロー図に含まれるデータフローノードの数を取得する
	 * @return データフローノード数
	 */
	public int getDataFlowNodes(){
		IDataFlowNode[] nodes = this.diagram.getDataFlowNodes();
		return nodes == null ? 0 : nodes.length;
	}

	public IPresentation[] supportedPresentation(IPresentation[] ps){
		return Arrays.stream(ps)
				.filter(p -> p.getModel() instanceof IProcessBox)
				.toArray(IPresentation[]::new);
	}

	public IPresentation[] unsupportedPresentation(IPresentation[] ps){
		IPresentation[] supports = supportedPresentation(ps);
		return Arrays.stream(ps)
				.filter(p -> ! Arrays.asList(supports).contains(p))
				.toArray(IPresentation[]::new);
	}


	public IPresentation[] getRelatedPresentations(IPresentation[] ps) throws InvalidUsingException{
		Set<IPresentation> ret = new HashSet<>();

		List<IPresentation> presentaions = Arrays.asList(this.diagram.getPresentations());

		for(IPresentation p : ps){

			// プロセスを読み上げ
			if(p.getModel() instanceof IProcessBox) {
				IProcessBox pb = (IProcessBox)p.getModel();
				presentaions.stream().filter(pr -> pr.getModel() == pb).forEach(pr -> ret.add(pr));
			}
			// データ（データストア、外部エンティティ、アンカー）につながるプロセスを読み上げ
			else if(p.getModel() instanceof IDataFlowNode) {
				IDataFlowNode ds = (IDataFlowNode)p.getModel();

				for(IDataFlow df : ds.getIncomings()){
					IDataFlowNode dfn = df.getSource();
					if(dfn instanceof IProcessBox){
						IProcessBox pb = (IProcessBox)dfn;
						presentaions.stream().filter(pr -> pr.getModel() == pb).forEach(pr -> ret.add(pr));
					}				
				}

				for(IDataFlow df : ds.getOutgoings()){
					IDataFlowNode dfn = df.getTarget();
					if(dfn instanceof IProcessBox){
						IProcessBox pb = (IProcessBox)dfn;
						presentaions.stream().filter(pr -> pr.getModel() == pb).forEach(pr -> ret.add(pr));
					}				
				}
			}
			// データフローにつながるプロセスを読み上げ
			else if(p.getModel() instanceof IDataFlow) {
				IDataFlow df = (IDataFlow)p.getModel();
				{	
					IDataFlowNode dfn = df.getSource();
					if(dfn instanceof IProcessBox){
						IProcessBox pb = (IProcessBox)dfn;
						presentaions.stream().filter(pr -> pr.getModel() == pb).forEach(pr -> ret.add(pr));
					}
				}
				{
					IDataFlowNode dfn = df.getTarget();
					if(dfn instanceof IProcessBox){
						IProcessBox pb = (IProcessBox)dfn;
						presentaions.stream().filter(pr -> pr.getModel() == pb).forEach(pr -> ret.add(pr));
					}				
				}
			}
		}
		
		return ret.toArray(new IPresentation[ret.size()]);
	}

	public String read(IPresentation p){
		String message = "";

		// プロセスを読み上げ
		if(p.getModel() instanceof IProcessBox) {
			IProcessBox pb = (IProcessBox)p.getModel();
			message += read(pb);
		}

		return message;
	}


	private String read(IProcessBox processBox){
		String message = "";

		// プロセスの入力の読み上げ
		IDataFlow[] inFlows = processBox.getIncomings();
		for(IDataFlow inf : inFlows){
			IDataFlowNode source = inf.getSource();

			message += source.getName();
			String infName = inf.getName();
			System.out.println("infName=" + infName);
			if(infName != null && infName.startsWith("[")){
				infName = infName.substring(1, infName.length()-1);
			}
			if(infName != null && ! infName.equals("")){
				message += "の" + infName;
			}
			message += "、";

		}

		// プロセス
		message += "を用いて、「" + processBox.getName() + "」ことで、";

		// プロセスの出力の読み上げ
		IDataFlow[] outFlows = processBox.getOutgoings();
		for(IDataFlow outf : outFlows){
			IDataFlowNode target = outf.getTarget();

			String outfName = outf.getName();
			System.out.println("outfName=" + outfName);
			if(outfName != null && outfName.startsWith("[")){
				outfName = outfName.substring(1, outfName.length()-1);
			}
			if(outfName != null && ! outfName.equals("")){
				message += outfName + "が入った";
			}
			message += target.getName() + "、";

		}
		message += "を作りだす。";

		return message;
	}

	public static MessagePresentation getMessagePresentation(IDataFlowDiagram diagram, IDiagramViewManager dvm) {
		MessagePresentation mps = new MessagePresentation();

		DataFlowDiagramReader dfdr = new DataFlowDiagramReader(diagram);

		// データフロー図のプロセス数を表示する
		mps.add("[" + diagram.getName() + "]データフロー図には、" + 
				dfdr.getDataFlowNodes() + "個のデータフローノードがあります",
				null);

		mps.add("=====", null);

		try {
			// 選択要素の表示
			IPresentation[] ps = dvm.getSelectedPresentations();
			if(ps.length > 0){
				mps.add("[" + diagram.getName() + "]データフロー図で、" + ps.length + "個の要素が選択されています", null);

				IPresentation[] dps = dfdr.getRelatedPresentations(ps);
				// 選択された要素のコミュニケーション手順を表示する
				for(IPresentation p : dfdr.supportedPresentation(dps)){
					String m = dfdr.read(p);
					if(! m.equals("")){
						mps.add(m, p);	
					}
				}
				for(IPresentation p : dfdr.unsupportedPresentation(dps)){
					String m = dfdr.read(p);
					if(! m.equals("")){
						mps.add(m, p);	
					}
				}
			}
			else 
			{	
				// 全てのコミュニケーション手順を表示する
				IPresentation[] dps = diagram.getPresentations(); 
				for(IPresentation p : dfdr.supportedPresentation(dps)){
					String m = dfdr.read(p);
					if(! m.equals("")){
						mps.add(m, p);	
					}
				}
				/*
				for(IPresentation p : dfdr.unsupportedPresentation(dps)){
					String m = dfdr.read(p);
					if(! m.equals("")){
						mps.add(m, p);	
					}
				}
				 */
			}
		} catch (InvalidUsingException e) {
			e.printStackTrace();
		}

		return mps;
	}

}
