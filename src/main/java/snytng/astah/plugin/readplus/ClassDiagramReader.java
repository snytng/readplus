package snytng.astah.plugin.readplus;

import java.util.List;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.view.IDiagramViewManager;

/**
 * クラス図の関連名を読み上げる
 */
public class ClassDiagramReader {

	private IClassDiagram diagram = null;

	public ClassDiagramReader(IClassDiagram diagram){
		this.diagram = diagram;
	}

	/**
	 * クラス図に含まれるクラス数を返却する
	 * @return クラス数
	 */
	public int getNumberOfClasses() {
		int noc = 0;

		try {
			for(IPresentation p : diagram.getPresentations()){
				if(p.getModel() instanceof IClass){
					noc++;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return noc;
	}

	public static MessagePresentation readClassDiagramInProject(IDiagramViewManager dvm) throws ProjectNotFoundException,ClassNotFoundException {
		ProjectAccessor projectAccessor = AstahAPI.getAstahAPI().getProjectAccessor();
		IModel iCurrentProject = projectAccessor.getProject();
		return readClassDiagramInPackage(iCurrentProject, dvm);
	}

	private static MessagePresentation readClassDiagramInPackage(IPackage iPackage, IDiagramViewManager dvm) {
		MessagePresentation mps = new MessagePresentation();

		//パッケージ名を表示
		mps.add("##### [パッケージ名: " + iPackage.getFullName(".")+"]", null);

		// パッケージのダイアグラムを表示
		IDiagram[] iDiagrams = iPackage.getDiagrams();
		for (int i = 0; i < iDiagrams.length; i++){
			IDiagram iDiagram = iDiagrams[i];
			if(iDiagram instanceof IClassDiagram){
				mps.add("-----", null);
				mps.addAll(ClassDiagramReader.getMessagePresentation((IClassDiagram)iDiagram, dvm));
			}
		}

		// 子パッケージの要素（クラスやパッケージ）を表示
		INamedElement[] iNamedElements = iPackage.getOwnedElements();
		for (int i = 0; i < iNamedElements.length; i++) {
			if (iNamedElements[i] instanceof IPackage) {
				IPackage iChildPackage = (IPackage) iNamedElements[i];
				mps.addAll(readClassDiagramInPackage(iChildPackage, dvm));
			}
		}

		return mps;
	}

	private static final int LIMIT_NUMBER_OF_CLASSES_IN_CLASS_DIAGRAM = 10;

	public static MessagePresentation getMessagePresentation(IClassDiagram diagram, IDiagramViewManager dvm) {
		MessagePresentation mps = new MessagePresentation();

		ClassDiagramReader cdr = new ClassDiagramReader(diagram);

		// クラス数の表示
		int noc = cdr.getNumberOfClasses();
		mps.add("[" + diagram.getName() + "]クラス図には、「" + noc + "個」のクラスがあります", null);
		if(noc >= LIMIT_NUMBER_OF_CLASSES_IN_CLASS_DIAGRAM){
			mps.add("※警告: クラス数が" + LIMIT_NUMBER_OF_CLASSES_IN_CLASS_DIAGRAM + "以上です", null);
		}

		mps.add("=====", null);

		// 選択要素の表示
		IPresentation[] ps = dvm.getSelectedPresentations();
		if(ps.length > 0){
			mps.add("[" + diagram.getName() + "]クラス図で、" + ps.length + "個の要素が選択されています", null);

			for(IPresentation p : ps){
				// クラス
				if(p.getModel() instanceof IClass) {
					IClass c = (IClass)p.getModel();
					mps.add("クラス：" + ClassReader.printName(c), p);
					mps.add("  属性：" + ClassReader.printAttributes(c), p);
					// クラスに繋がる関連を読み上げ
					List<IElement> elements = ClassReader.getRelations(c);
					try {
						for(IPresentation dp : diagram.getPresentations()){
							IElement e = dp.getModel();
							if(elements.contains(e)){
								String r = RelationReader.printRelation(e);
								if(r != null){
									mps.add("  関連：" + r, dp);
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
						mps.add("関連：" + r, p);
					}
				} 
				// それ以外
				else {
					mps.add(p.getType(), p);
				}
			}
		} 
		// クラス図全体の読み上げ
		else {
			try {
				// 関連読み上げ
				for(IPresentation dp : diagram.getPresentations()){
					if(RelationReader.isSupportedRelation(dp.getModel())) {
						String r = RelationReader.printRelation(dp.getModel());
						if(r != null){
							mps.add(r, dp);
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
