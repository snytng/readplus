package snytng.astah.plugin.readplus;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.IAssociation;
import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IExtend;
import com.change_vision.jude.api.inf.model.IInclude;
import com.change_vision.jude.api.inf.model.IUseCase;
import com.change_vision.jude.api.inf.model.IUseCaseDiagram;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.view.IDiagramViewManager;

public class UseCaseDiagramReader {

	private IUseCaseDiagram diagram = null;

	public UseCaseDiagramReader(IUseCaseDiagram diagram){
		this.diagram = diagram;
	}

	/**
	 * ユースケース図に含まれるユースケースの数を取得する
	 * @return ユースケース数
	 */
	public int getUseCases(){
		IUseCase[] nodes = null;
		try {
			nodes = Arrays.stream(this.diagram.getPresentations())
					.map(IPresentation::getModel)
					.filter(IUseCase.class::isInstance)
					.toArray(IUseCase[]::new);
		} catch (InvalidUsingException e) {
			e.printStackTrace();
		}
		return nodes == null ? 0 : nodes.length;
	}

	public IPresentation[] supportedPresentation(IPresentation[] ps){
		return Arrays.stream(ps)
				.filter(p -> read(p) != null)
				.toArray(IPresentation[]::new);
	}

	public IPresentation[] unsupportedPresentation(IPresentation[] ps){
		return Arrays.stream(ps)
				.filter(p -> read(p) == null)
				.toArray(IPresentation[]::new);
	}


	public String read(IPresentation p){
		String message = "";

		// 関連を読み上げ
		if(p.getModel() instanceof IAssociation) {
			IAssociation as = (IAssociation)p.getModel();
			message = readAssociation(as);
		}
		// <<include>>を読み上げ
		else if(p.getModel() instanceof IInclude){
			IInclude in = (IInclude)p.getModel();
			message = readInclude(in);		
		}
		// <<extend>>を読み上げ
		else if(p.getModel() instanceof IExtend){
			IExtend ex = (IExtend)p.getModel();
			message = readExtend(ex);
		}
		// それ以外は未サポート
		else {
			//message = "*****読み上げをサポートしていません(" + p.getClass().get + ")";
			message = null;
		}

		return message;
	}

	private String readAssociation(IAssociation as){
		String message = null;
		IAttribute[] ats = as.getMemberEnds();
		if(ats[0].getType() instanceof IUseCase){
			message = ats[1].getType().getName() + "は、" + ats[0].getType().getName();				
		} else if(ats[1].getType() instanceof IUseCase){
			message = ats[0].getType().getName() + "は、" + ats[1].getType().getName();
		}
		return message;
	}

	private String readInclude(IInclude in){
		String message = null;
		StringBuilder actors = new StringBuilder();
		getActors(in.getIncludingCase(), new HashSet<>(), actors);
		message = actors + "は、" + in.getIncludingCase().getName() + " ときには、必ず " + in.getAddition().getName();
		return message;
	}

	private String readExtend(IExtend ex){
		String message = null;
		StringBuilder actors = new StringBuilder();
		getActors(ex.getExtendedCase(), new HashSet<>(), actors);
		message = actors + "は、" + ex.getExtendedCase().getName() + "ときには 、" + ex.getExtension().getName() + "ときもある";
		return message;
	}

	private void getActors(IUseCase uc, Set<IUseCase> ucs, StringBuilder actors) {

		System.out.println("getActors: <--" + uc.getName());
		if(! ucs.contains(uc)){
			ucs.add(uc);
		}

		while(true){

			int count = 0;

			IInclude[] ins = uc.getAdditionInvs();
			for(IInclude in : ins){
				IUseCase u = in.getIncludingCase();
				System.out.println("getActors: <<include>> " + in);

				if(! ucs.contains(u)){
					ucs.add(u);
					getActors(u, ucs, actors);
					count++;
				}
			}

			IExtend[] exs = uc.getExtends();
			for(IExtend ex : exs){
				IUseCase u = ex.getExtendedCase();
				System.out.println("getActors: <<extend>> " + ex);
				if(! ucs.contains(u)){
					ucs.add(u);
					getActors(u, ucs, actors);
					count++;
				}
			}

			IAttribute[] ats = uc.getAttributes();
			for(IAttribute at : ats){
				System.out.println("getActors: <<attribute>> " + at);
				if(! (at.getType() instanceof IUseCase)){
					IClass c = at.getType();
					try {
						IPresentation[] pind = diagram.getPresentations();
						IPresentation[] pofc = c.getPresentations();

						if(Arrays.stream(pind)
								.filter(p -> Arrays.asList(pofc).contains(p))
								.count() > 0){
							if(! actors.toString().isEmpty()){
								actors.append(", ");
							}
							actors.append(at.getType().getName());
							System.out.println("getActors: append" + at.getType().getName());
						}

					} catch (InvalidUsingException e) {
						e.printStackTrace();
					}						
				}
			}

			if(count == 0){
				break;
			}
		}

		System.out.println("getActors: -->" + uc.getName());

	}

	public static MessagePresentation getMessagePresentation(IUseCaseDiagram diagram, IDiagramViewManager dvm) {
		MessagePresentation mps = new MessagePresentation();

		UseCaseDiagramReader ucdr = new UseCaseDiagramReader(diagram);

		// ユースケース図のプロセス数を表示する
		mps.add("[" + diagram.getName() + "]ユースケース図には、" + 
				ucdr.getUseCases() + "個のユースケースがあります",
				null);

		mps.add("=====", null);

		IPresentation[] ps = dvm.getSelectedPresentations();

		// 選択要素を表示する
		if(ps.length > 0){
			mps.add("[" + diagram.getName() + "]ユースケース図で、" + ps.length + "個の要素が選択されています", null);

			for(IPresentation p : ucdr.supportedPresentation(ps)){
				String m = ucdr.read(p);
				if(m != null && ! m.isEmpty()){
					mps.add(m, p);
				}
			}
			for(IPresentation p : ucdr.unsupportedPresentation(ps)){
				String m = ucdr.read(p);
				if(m != null && ! m.isEmpty()){
					mps.add(m, p);
				}
			}

			try {
				IPresentation[] allps = diagram.getPresentations();

				Arrays.stream(ps)
				.filter(p -> p.getModel() instanceof IUseCase)
				.forEach(p -> {
					IUseCase uc = (IUseCase)p.getModel();

					Arrays.stream(uc.getAttributes())
					.map(IAttribute::getAssociation)
					.forEach(as -> {
						for(IPresentation allp : allps){
							if(allp.getModel() == as){
								String m = ucdr.read(allp);
								if(m != null && ! m.isEmpty()){
									mps.add(m, allp);
								}
							}
						}
					});
					Arrays.stream(uc.getAdditionInvs())
					.forEach(in -> {
						for(IPresentation allp : allps){
							if(allp.getModel() == in){
								String m = ucdr.read(allp);
								if(m != null && ! m.isEmpty()){
									mps.add(m, allp);
								}
							}
						}
					});
					Arrays.stream(uc.getExtends())
					.forEach(ex -> {
						for(IPresentation allp : allps){
							if(allp.getModel() == ex){
								String m = ucdr.read(allp);
								if(m != null && ! m.isEmpty()){
									mps.add(m, allp);
								}
							}
						}
					});
				});
			}catch(InvalidUsingException e){
				e.printStackTrace();
			}

		}
		// 全ての要素をを表示する
		else 
		{	
			try {
				ps = diagram.getPresentations();

				for(IPresentation p : ucdr.supportedPresentation(ps)){
					String m = ucdr.read(p);
					if(m != null && ! m.isEmpty()){
						mps.add(m, p);	
					}
				}
				for(IPresentation p : ucdr.unsupportedPresentation(ps)){
					String m = ucdr.read(p);
					if(m != null && ! m.isEmpty()){
						mps.add(m, p);	
					}
				}

			} catch (InvalidUsingException e) {
				e.printStackTrace();
			}
		}


		return mps;
	}

}
