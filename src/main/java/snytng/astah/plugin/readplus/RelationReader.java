package snytng.astah.plugin.readplus;

import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.IAssociation;
import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IDependency;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IGeneralization;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IRealization;
import com.change_vision.jude.api.inf.model.IUsage;
import com.change_vision.jude.api.inf.presentation.ILinkPresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;

public class RelationReader {

	private RelationReader(){}

	/**
	 * 読み上げをサポートされている関連かどうかを返却する
	 * @param e モデル要素
	 * @return サポートされている場合はtrue、サポートされていない場合にはfalse
	 */
	public static boolean isSupportedRelation(IElement e){
		if(e == null) return false;

		return
				(e instanceof IAssociation) ||
				(e instanceof IUsage)       ||
				(e instanceof IDependency)  ||
				(e instanceof IRealization) ||
				(e instanceof IGeneralization);
	}

	/**
	 * 関連を読み上げる
	 * @param e モデル要素
	 * @return 関連を読み上げた文字列、読み上げられない場合にはnull
	 */
	public static String printRelation(IElement e){
		// 関連
		if(e instanceof IAssociation){
			IAssociation ia = (IAssociation)e;
			return printAssotication(ia);
		}
		// 利用
		else if(e instanceof IUsage){
			IUsage id = (IUsage)e;
			return printUsage(id);
		}
		// 依存
		else if(e instanceof IDependency){
			IDependency id = (IDependency)e;
			return printDependency(id);
		}
		// 継承
		else if(e instanceof IGeneralization){
			IGeneralization ig = (IGeneralization)e;
			return printGeneralization(ig);
		}
		// 実現
		else if(e instanceof IRealization){
			IRealization ir = (IRealization)e;
			return printRealization(ir);
		}
		// それ以外
		else {
			return null;
		}
	}

	public static String printUsage(IUsage iu) {
		String[] oxData = View.getViewString("RelationReader.ox").split(",");
		String ox = oxData[0];
		INamedElement client = iu.getClient();
		INamedElement supplier = iu.getSupplier();
		return String.format(View.getViewString("RelationReader.usage.meesage"), ox, client, supplier);
	}

	public static String printDependency(IDependency id) {
		String[] oxData = View.getViewString("RelationReader.ox").split(",");
		String ox = oxData[0];
		INamedElement client = id.getClient();
		INamedElement supplier = id.getSupplier();
		return String.format(View.getViewString("RelationReader.dependency.meesage"), ox, client, supplier);
	}

	public static String printGeneralization(IGeneralization ig) {
		String[] oxData = View.getViewString("RelationReader.ox").split(",");
		String ox = oxData[0];
		IClass subType = ig.getSubType();
		IClass superType = ig.getSuperType();
		return String.format(View.getViewString("RelationReader.generalization.meesage"), ox, subType, superType);
	}

	public static String printRealization(IRealization iRealization) {
		String[] oxData = View.getViewString("RelationReader.ox").split(",");
		String ox = oxData[0];
		INamedElement client = iRealization.getClient();
		INamedElement supplier = iRealization.getSupplier();
		return String.format(View.getViewString("RelationReader.realization.meesage"), ox, client, supplier);
	}

	public static String printAssotication(IAssociation iAssociation) {
		String[] oxData = View.getViewString("RelationReader.ox").split(",");
		String ox = "";

		// 関連名の読み方の方向＝▲の方向
		// IPresentationのname_direction_reverseが0なら関連の方向と同じ、1ながら関連の方向と反対
		boolean direction = true;
		try {
			IPresentation[] ips = iAssociation.getPresentations();
			direction = ips[0].getProperty("name_direction_reverse").equals("0");
		}catch(InvalidUsingException e){
			direction = false;
		}

		IAttribute[] iAttributes = iAssociation.getMemberEnds();
		IAttribute fromAttribute = iAttributes[0];
		IAttribute toAttribute = iAttributes[1];

		// 関連名
		String verb = iAssociation.getName();

		// 関連名がない場合
		if(verb.isEmpty()){
			ox = oxData[1];
			// 集約
			if (iAttributes[0].isAggregate() || iAttributes[0].isComposite()) {
				fromAttribute = iAttributes[1];
				toAttribute = iAttributes[0];
				ox = oxData[0];
				verb = View.getViewString("RelationReader.association.aggregate.verb");

			}
			// 集約
			else if(iAttributes[1].isAggregate() || iAttributes[1].isComposite()){
				fromAttribute = iAttributes[0];
				toAttribute = iAttributes[1];
				ox = oxData[0];
				verb = View.getViewString("RelationReader.association.aggregate.verb");
			}
		}
		// 関連名がある場合
		else {
			ox = oxData[0];
			// 順方向
			if(direction){
				fromAttribute = iAttributes[0];
				toAttribute = iAttributes[1];
			}
			// 逆方向
			else {
				fromAttribute = iAttributes[1];
				toAttribute = iAttributes[0];
			}
		}

		// fromとtoのクラスを決める
		IClass fromClass = fromAttribute.getType();
		IClass toClass = toAttribute.getType();

		// 関連端のロールを取得する
		String fromRole = fromAttribute.getName();
		String toRole = toAttribute.getName();

		// 読み上げの名前を決める
		String fromName = fromClass.toString();
		if(fromRole != null && !fromRole.isEmpty()){
			fromName += "(" + fromRole + ")";
		}
		String toName = toClass.toString();
		if(toRole != null && !toRole.isEmpty()){
			toName += "(" + toRole + ")";
		}

		// 読み上げ文章を作成
		return String.format(
				View.getViewString("RelationReader.association.meesage"),
				ox, fromName, toName, verb);
	}

	public static boolean isSupportedType(String type) {
		if (type == null) return false;

		if (type.equals("Containment")){
			return true;
		}
		return false;
	}

	public static String printType(IPresentation p) {
		String[] oxData = View.getViewString("RelationReader.ox").split(",");
		String message = "";

		if (p.getType().equals("Containment")){
			ILinkPresentation lp = (ILinkPresentation)p;
			message = String.format(View.getViewString("RelationReader.containment.meesage"),
					oxData[0], lp.getSource().getLabel(), lp.getTarget().getLabel());
		}

		return message;
	}
}
