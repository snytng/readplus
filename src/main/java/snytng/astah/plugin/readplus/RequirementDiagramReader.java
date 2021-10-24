package snytng.astah.plugin.readplus;

import java.util.List;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.model.IRequirement;
import com.change_vision.jude.api.inf.model.IRequirementDiagram;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.view.IDiagramViewManager;

/**
 * 要求図の関連名を読み上げる
 */
public class RequirementDiagramReader {

	private IRequirementDiagram diagram = null;

	public RequirementDiagramReader(IRequirementDiagram diagram){
		this.diagram = diagram;
	}

	/**
	 * ブロック定義図に含まれるブロック数を返却する
	 * @return クラス数
	 */
	public int getNumberOfRequirements() {
		int noreq = 0;

		try {
			for(IPresentation p : diagram.getPresentations()){
				if(p.getModel() instanceof IRequirement){
					noreq++;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return noreq;
	}

	public static MessagePresentation readDiagramInProject(IDiagramViewManager dvm) throws ProjectNotFoundException,ClassNotFoundException {
		ProjectAccessor projectAccessor = AstahAPI.getAstahAPI().getProjectAccessor();
		IModel iCurrentProject = projectAccessor.getProject();
		return readDiagramInPackage(iCurrentProject, dvm);
	}

	private static MessagePresentation readDiagramInPackage(IPackage iPackage, IDiagramViewManager dvm) {
		MessagePresentation mps = new MessagePresentation();

		//パッケージ名を表示
		mps.add(String.format(
				View.getViewString("RequirementDiagramReader.packageName.meessage"),
				iPackage.getFullName(".")),
				null);

		// パッケージのダイアグラムを表示
		IDiagram[] iDiagrams = iPackage.getDiagrams();
		for (int i = 0; i < iDiagrams.length; i++){
			IDiagram iDiagram = iDiagrams[i];
			if(iDiagram instanceof IRequirementDiagram){
				mps.add("-----", null);
				mps.addAll(RequirementDiagramReader.getMessagePresentation((IRequirementDiagram)iDiagram, dvm));
			}
		}

		// 子パッケージの要素（クラスやパッケージ）を表示
		INamedElement[] iNamedElements = iPackage.getOwnedElements();
		for (int i = 0; i < iNamedElements.length; i++) {
			if (iNamedElements[i] instanceof IPackage) {
				IPackage iChildPackage = (IPackage) iNamedElements[i];
				mps.addAll(readDiagramInPackage(iChildPackage, dvm));
			}
		}

		return mps;
	}

	private static final int LIMIT_NUMBER_OF_REQUIREMENTS = 10;

	public static MessagePresentation getMessagePresentation(IRequirementDiagram diagram, IDiagramViewManager dvm) {
		MessagePresentation mps = new MessagePresentation();

		RequirementDiagramReader cdr = new RequirementDiagramReader(diagram);

		// 要求数の表示
		int nor = cdr.getNumberOfRequirements();
		mps.add(String.format(
				View.getViewString("RequirementDiagramReader.numberOfBlocks.meessage"),
				diagram.getName(), nor),
				null);
		if(nor >= LIMIT_NUMBER_OF_REQUIREMENTS){
			mps.add(String.format(
					View.getViewString("RequirementDiagramReader.warningNumberOfBlocks.meessage"),
					LIMIT_NUMBER_OF_REQUIREMENTS),
					null);
		}

		mps.add("=====", null);

		// 選択要素の表示
		IPresentation[] ps = dvm.getSelectedPresentations();
		if(ps.length > 0){
			mps.add(String.format(
					View.getViewString("RequirementDiagramReader.selection.meessage"),
					ps.length),
					null);

			for(IPresentation p : ps){
				// ブロック
				if(p.getModel() instanceof IRequirement) {
					IRequirement req = (IRequirement)p.getModel();
					mps.add(String.format(
							View.getViewString("RequirementDiagramReader.requirement.message"),
							RequirementReader.printName(req)),
							p);
					mps.add(String.format(
							View.getViewString("RequirementDiagramReader.requirement.id.message"),
							RequirementReader.printId(req)),
							p);
					mps.add(String.format(
							View.getViewString("RequirementDiagramReader.requirement.text.message"),
							RequirementReader.printText(req)),
							p);
					// クラスに繋がる関連を読み上げ
					List<IElement> elements = RequirementReader.getRelations(req);
					try {
						for(IPresentation dp : diagram.getPresentations()){
							IElement e = dp.getModel();
							if(elements.contains(e)){
								String r = RelationReader.printRelation(e);
								if(r != null){
									mps.add(String.format(
											View.getViewString("RequirementDiagramReader.relation.message"),
											r),
											dp);
								}
							}
						}
					} catch (InvalidUsingException e) {
						e.printStackTrace();
					}
				}
				// 関連
				else if(RelationReader.isSupportedRelation(p.getModel())) {
					String r = RelationReader.printRelation(p.getModel());
					if(r != null){
						mps.add(String.format(
								View.getViewString("RequirementDiagramReader.relation.message"),
								r),
								p);
					}
				}
				// タイプ
				else if(RelationReader.isSupportedType(p.getType())){
					String t = RelationReader.printType(p);
					if(t != null){
						mps.add(String.format(
								View.getViewString("RequirementDiagramReader.type.message"),
								t),
								p);
					}
				}
				// それ以外
				else {
					mps.add("model:" + p.getModel() + "class:" + p.getClass().getName() + ", type:" + p.getType(), p);
					System.out.println("unsupported element=model:" + p.getModel() + "class:" + p.getClass().getName() + ", type:" + p.getType());
					for (Object key : p.getProperties().keySet()) {
						System.out.println("\tprop:" + key + "=" + p.getProperties().get(key));
					}
				}
			}
		}
		// 図全体の読み上げ
		else {
			try {
				for(IPresentation dp : diagram.getPresentations()){
					// 関連
					if(RelationReader.isSupportedRelation(dp.getModel())) {
						String r = RelationReader.printRelation(dp.getModel());
						if(r != null){
							mps.add(r, dp);
						}
					}
					// タイプ
					else if(RelationReader.isSupportedType(dp.getType())){
						String t = RelationReader.printType(dp);
						if(t != null){
							mps.add(t, dp);
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		return mps;
	}

}
