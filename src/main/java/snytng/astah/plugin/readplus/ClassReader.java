package snytng.astah.plugin.readplus;

import java.util.ArrayList;
import java.util.List;

import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IElement;

public class ClassReader {

	private ClassReader(){}

	public static String printName(IClass iClass){
		String message = "";
		message += iClass.getName();
		String alias1 = iClass.getAlias1();
		String alias2 = iClass.getAlias2();
		if(alias1 != null && !alias1.equals("")){
			message += String.format(View.getViewString("ClassReader.alias1.message"), alias1);
		}
		if(alias2 != null && !alias2.equals("")){
			message += String.format(View.getViewString("ClassReader.alias2.message"), alias2);
		}
		return message;
	}

	public static String printAttributes(IClass iClass) {
		String message = "";

		String className = iClass.getName();
		IAttribute[] ias = iClass.getAttributes();
		for (int i = 0; i < ias.length; i++) {
			IAttribute ia = ias[i];
			String aName = ia.getName();
			if(aName != null && aName.length() != 0){
				message += String.format(View.getViewString("ClassReader.attribute.message"), className, ia.getName()) + " ";
			}
		}
		return message;
	}

	public static List<IElement> getRelations(IClass iClass){
		List<IElement> elements = new ArrayList<>();

		for (IElement iElement : iClass.getGeneralizations()) {
			if(RelationReader.isSupportedRelation(iElement)) {
				elements.add(iElement);
			}
		}

		for (IElement iElement : iClass.getSpecializations()) {
			if(RelationReader.isSupportedRelation(iElement)) {
				elements.add(iElement);
			}
		}

		for (IElement iElement : iClass.getSupplierRealizations()) {
			if(RelationReader.isSupportedRelation(iElement)) {
				elements.add(iElement);
			}
		}

		for (IElement iElement : iClass.getClientRealizations()) {
			if(RelationReader.isSupportedRelation(iElement)) {
				elements.add(iElement);
			}
		}

		for (IElement iElement : iClass.getSupplierDependencies()) {
			if(RelationReader.isSupportedRelation(iElement)) {
				elements.add(iElement);
			}
		}

		for (IElement iElement : iClass.getClientDependencies()) {
			if(RelationReader.isSupportedRelation(iElement)) {
				elements.add(iElement);
			}
		}

		for (IElement iElement : iClass.getSupplierUsages()) {
			if(RelationReader.isSupportedRelation(iElement)) {
				elements.add(iElement);
			}
		}

		for (IElement iElement : iClass.getClientUsages()) {
			if(RelationReader.isSupportedRelation(iElement)) {
				elements.add(iElement);
			}
		}

		for (IAttribute iAttr : iClass.getAttributes()) {
			if(RelationReader.isSupportedRelation(iAttr.getAssociation())) {
				IElement iElement = iAttr.getAssociation();
				elements.add(iElement);
			}
		}

		return elements;
	}

}
