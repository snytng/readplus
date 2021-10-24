package snytng.astah.plugin.readplus;

import java.util.ArrayList;
import java.util.List;

import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IPort;
import com.change_vision.jude.api.inf.model.IRequirement;

public class RequirementReader {

	private RequirementReader(){}

	public static String printName(IRequirement req){
		String message = "";
		message += req.getName();
		String alias1 = req.getAlias1();
		String alias2 = req.getAlias2();
		if(alias1 != null && !alias1.equals("")){
			message += String.format(View.getViewString("RequirementReader.alias1.message"), alias1);
		}
		if(alias2 != null && !alias2.equals("")){
			message += String.format(View.getViewString("RequirementReader.alias2.message"), alias2);
		}
		return message;
	}

	public static String printId(IRequirement req) {
		return req.getRequirementID();
	}

	public static String printText(IRequirement req) {
		return req.getRequirementText();
	}

	public static List<IElement> getRelations(IRequirement req){
		List<IElement> elements = new ArrayList<>();

		for (IElement iElement : req.getGeneralizations()) {
			if(RelationReader.isSupportedRelation(iElement)) {
				elements.add(iElement);
			}
		}

		for (IElement iElement : req.getSpecializations()) {
			if(RelationReader.isSupportedRelation(iElement)) {
				elements.add(iElement);
			}
		}

		for (IElement iElement : req.getSupplierRealizations()) {
			if(RelationReader.isSupportedRelation(iElement)) {
				elements.add(iElement);
			}
		}

		for (IElement iElement : req.getClientRealizations()) {
			if(RelationReader.isSupportedRelation(iElement)) {
				elements.add(iElement);
			}
		}

		for (IElement iElement : req.getSupplierDependencies()) {
			if(RelationReader.isSupportedRelation(iElement)) {
				elements.add(iElement);
			}
		}

		for (IElement iElement : req.getClientDependencies()) {
			if(RelationReader.isSupportedRelation(iElement)) {
				elements.add(iElement);
			}
		}

		for (IElement iElement : req.getSupplierUsages()) {
			if(RelationReader.isSupportedRelation(iElement)) {
				elements.add(iElement);
			}
		}

		for (IElement iElement : req.getClientUsages()) {
			if(RelationReader.isSupportedRelation(iElement)) {
				elements.add(iElement);
			}
		}

		for (IAttribute iAttr : req.getAttributes()) {
			if(RelationReader.isSupportedRelation(iAttr.getAssociation())) {
				IElement iElement = iAttr.getAssociation();
				elements.add(iElement);
			}
		}

		for (IPort iPort : req.getPorts()) {
			if(RelationReader.isSupportedRelation(iPort.getAssociation())) {
				IElement iElement = iPort.getAssociation();
				elements.add(iElement);
			}
		}

		for (IClass iClass : req.getNestedClasses()) {
			// TODO
		}


		return elements;
	}

}
