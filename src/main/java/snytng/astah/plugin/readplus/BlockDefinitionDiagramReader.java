package snytng.astah.plugin.readplus;

import java.util.List;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
import com.change_vision.jude.api.inf.model.IBlockDefinitionDiagram;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.view.IDiagramViewManager;

/**
 * ブロック定義図の関連名を読み上げる
 */
public class BlockDefinitionDiagramReader {

	private IBlockDefinitionDiagram diagram = null;

	public BlockDefinitionDiagramReader(IBlockDefinitionDiagram diagram){
		this.diagram = diagram;
	}

	/**
	 * ブロック定義図に含まれるブロック数を返却する
	 * @return クラス数
	 */
	public int getNumberOfBlocks() {
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

	public static MessagePresentation readDiagramInProject(IDiagramViewManager dvm) throws ProjectNotFoundException,ClassNotFoundException {
		ProjectAccessor projectAccessor = AstahAPI.getAstahAPI().getProjectAccessor();
		IModel iCurrentProject = projectAccessor.getProject();
		return readDiagramInPackage(iCurrentProject, dvm);
	}

	private static MessagePresentation readDiagramInPackage(IPackage iPackage, IDiagramViewManager dvm) {
		MessagePresentation mps = new MessagePresentation();

		//パッケージ名を表示
		//mps.add("##### [パッケージ名: " + iPackage.getFullName(".")+"]", null);
		mps.add("##### [Package name: " + iPackage.getFullName(".")+"]", null);

		// パッケージのダイアグラムを表示
		IDiagram[] iDiagrams = iPackage.getDiagrams();
		for (int i = 0; i < iDiagrams.length; i++){
			IDiagram iDiagram = iDiagrams[i];
			if(iDiagram instanceof IBlockDefinitionDiagram){
				mps.add("-----", null);
				mps.addAll(BlockDefinitionDiagramReader.getMessagePresentation((IBlockDefinitionDiagram)iDiagram, dvm));
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

	private static final int LIMIT_NUMBER_OF_BLOCKS = 10;

	public static MessagePresentation getMessagePresentation(IBlockDefinitionDiagram diagram, IDiagramViewManager dvm) {
		MessagePresentation mps = new MessagePresentation();

		BlockDefinitionDiagramReader cdr = new BlockDefinitionDiagramReader(diagram);

		// ブロック数の表示
		int noc = cdr.getNumberOfBlocks();
		//mps.add("[" + diagram.getName() + "]ブロック定義図には、「" + noc + "個」のブロックがあります", null);
		mps.add("[" + diagram.getName() + "] Block Definition Diagram has " + noc + " block(s).", null);
		if(noc >= LIMIT_NUMBER_OF_BLOCKS){
			//mps.add("※警告: ブロック数が" + LIMIT_NUMBER_OF_BLOCKS + "以上です", null);
			mps.add("*warning: block number is greater than the limit number of blocks: " + LIMIT_NUMBER_OF_BLOCKS + ".", null);
		}

		mps.add("=====", null);

		// 選択要素の表示
		IPresentation[] ps = dvm.getSelectedPresentations();
		if(ps.length > 0){
			//mps.add("[" + diagram.getName() + "]ブロック定義図で、" + ps.length + "個の要素が選択されています", null);
			mps.add("There is(are) " + ps.length + " selected block(s).", null);

			for(IPresentation p : ps){
				// クラス
				if(p.getModel() instanceof IClass) {
					IClass c = (IClass)p.getModel();
					//mps.add("ブロック：" + ClassReader.printName(c), p);
					//mps.add("    属性：" + ClassReader.printAttributes(c), p);
					mps.add("Block:        " + ClassReader.printName(c), p);
					mps.add("    Attribute:" + ClassReader.printAttributes(c), p);
					// クラスに繋がる関連を読み上げ
					List<IElement> elements = ClassReader.getRelations(c);
					try {
						for(IPresentation dp : diagram.getPresentations()){
							IElement e = dp.getModel();
							if(elements.contains(e)){
								String r = RelationReader.printRelation(e);
								if(r != null){
									//mps.add("    関連：" + r, dp);
									mps.add("    Realation:" + r, dp);
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
						//mps.add("関連：" + r, p);
						mps.add("Realation:" + r, p);
					}
				}
				// それ以外
				else {
					mps.add(p.getType(), p);
				}
			}
		}
		// 図全体の読み上げ
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
