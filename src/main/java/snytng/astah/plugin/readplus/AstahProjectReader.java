package snytng.astah.plugin.readplus;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.view.IDiagramViewManager;

public class AstahProjectReader {
	
	private AstahProjectReader(){}

	public static int getNumberOfDiagrams() throws ProjectNotFoundException,ClassNotFoundException {
		return getNumberOfDiagrams(IDiagram.class).length;
	}

	public static int getNumberOfClasses() throws ProjectNotFoundException,ClassNotFoundException {
		return getNumberOfDiagrams(IClass.class).length;
	}

	private static INamedElement[] getNumberOfDiagrams(Class<?> c) throws ProjectNotFoundException,ClassNotFoundException {
		ProjectAccessor projectAccessor = AstahAPI.getAstahAPI().getProjectAccessor();
		return projectAccessor.findElements(c);
	}
	
	public static IDiagram findDiagramsByID(String id) throws ProjectNotFoundException, ClassNotFoundException{
		ProjectAccessor projectAccessor = AstahAPI.getAstahAPI().getProjectAccessor();
		INamedElement[] diagrams  = projectAccessor.findElements(IDiagram.class);
		for(INamedElement diagram: diagrams){
			if(diagram.getId().equals(id)){
				return (IDiagram)diagram;
			}
		}
		return null;
	}

	public static MessagePresentation getMessagePresentation(IDiagramViewManager dvm){
		MessagePresentation mps = new MessagePresentation();
		
		try {
			mps.add("このプロジェクトには、" + AstahProjectReader.getNumberOfClasses() + "個のクラスがあります", null);
			mps.add("このプロジェクトには、" + AstahProjectReader.getNumberOfDiagrams() + "個の図があります", null);
	
			// プロジェクトの中のクラス図の読み上げ
			mps.addAll(ClassDiagramReader.readClassDiagramInProject(dvm));
			
	
		} catch (ProjectNotFoundException e) {
			mps.clear();
			mps.add("プロジェクトを開いてください", null);
		} catch (Exception e) {
			mps.clear();
			mps.add("プロジェクトの読み取りに失敗しました", null);
		}
		
		return mps;
	}
}
