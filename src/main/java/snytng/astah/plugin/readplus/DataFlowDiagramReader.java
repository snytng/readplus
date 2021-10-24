package snytng.astah.plugin.readplus;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.IDataFlow;
import com.change_vision.jude.api.inf.model.IDataFlowDiagram;
import com.change_vision.jude.api.inf.model.IDataFlowNode;
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
		// プロセスの入力の読み上げ
		String inputs = "";
		IDataFlow[] inFlows = processBox.getIncomings();
		for(IDataFlow inf : inFlows){
			IDataFlowNode source = inf.getSource();

			String infName = inf.getName();
			System.out.println("infName=" + infName);
			// [dataflow]の[]を削除する
			if(infName != null && infName.startsWith("[")){
				infName = infName.substring(1, infName.length()-1);
			}
			// dataflowがあれば読み上げる
			if(infName != null && ! infName.equals("")){
				inputs += String.format(
						View.getViewString("DataFlowDiagramReader.process.inputFlowWithData.message"),
						source.getName(), infName);
			} else {
				inputs += String.format(
						View.getViewString("DataFlowDiagramReader.process.inputFlow.message"),
						source.getName());
			}
		}

		// プロセスの出力の読み上げ
		String outputs = "";
		IDataFlow[] outFlows = processBox.getOutgoings();
		for(IDataFlow outf : outFlows){
			IDataFlowNode target = outf.getTarget();

			String outfName = outf.getName();

			System.out.println("outfName=" + outfName);
			// [dataflow]の[]を削除する
			if(outfName != null && outfName.startsWith("[")){
				outfName = outfName.substring(1, outfName.length()-1);
			}
			// dataflowがあれば読み上げる
			if(outfName != null && ! outfName.equals("")){
				outputs += String.format(
						View.getViewString("DataFlowDiagramReader.process.outputFlowWithData.message"),
						target.getName(), outfName );
			} else {
				outputs += String.format(
						View.getViewString("DataFlowDiagramReader.process.outputFlow.message"),
						target.getName());
			}

		}

		return String.format(
				View.getViewString("DataFlowDiagramReader.process.message"),
				inputs, processBox.getName(), outputs
				);
	}

	public static MessagePresentation getMessagePresentation(IDataFlowDiagram diagram, IDiagramViewManager dvm) {
		MessagePresentation mps = new MessagePresentation();

		DataFlowDiagramReader dfdr = new DataFlowDiagramReader(diagram);

		// データフロー図のプロセス数を表示する
		mps.add(String.format(
				View.getViewString("DataFlowDiagramReader.diagram.message"),
				diagram.getName(),dfdr.getDataFlowNodes()
				),
				null);

		mps.add("=====", null);

		try {
			// 選択要素の表示
			IPresentation[] ps = dvm.getSelectedPresentations();
			if(ps.length > 0){
				mps.add(String.format(
						View.getViewString("DataFlowDiagramReader.selection.message"),
						ps.length
						),
						null);

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
