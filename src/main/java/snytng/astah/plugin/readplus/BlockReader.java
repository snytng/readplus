package snytng.astah.plugin.readplus;

import java.util.ArrayList;
import java.util.List;

import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IBlock;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IPort;

public class BlockReader {

	private BlockReader(){}

	public static String printName(IBlock iBlock){
		String message = "";
		message += iBlock.getName();
		String alias1 = iBlock.getAlias1();
		String alias2 = iBlock.getAlias2();
		if(alias1 != null && !alias1.equals("")){
			message += String.format(View.getViewString("BlockReader.alias1.message"), alias1);
		}
		if(alias2 != null && !alias2.equals("")){
			message += String.format(View.getViewString("BlockReader.alias2.message"), alias2);
		}
		return message;
	}

	public static String printAttributes(IBlock iBlock) {
		String message = "";

		String blockName = iBlock.getName();
		IAttribute[] ias = iBlock.getAttributes();
		for (int i = 0; i < ias.length; i++) {
			IAttribute ia = ias[i];
			String aName = ia.getName();
			if(aName != null && aName.length() != 0){
				message += String.format(View.getViewString("BlockReader.attribute.message"), blockName, ia.getName()) + " ";
			}
		}
		return message;
	}

	public static List<IElement> getRelations(IBlock iBlock){
		List<IElement> elements = new ArrayList<>();

		for (IElement iElement : iBlock.getGeneralizations()) {
			if(RelationReader.isSupportedRelation(iElement)) {
				elements.add(iElement);
			}
		}

		for (IElement iElement : iBlock.getSpecializations()) {
			if(RelationReader.isSupportedRelation(iElement)) {
				elements.add(iElement);
			}
		}

		for (IElement iElement : iBlock.getSupplierRealizations()) {
			if(RelationReader.isSupportedRelation(iElement)) {
				elements.add(iElement);
			}
		}

		for (IElement iElement : iBlock.getClientRealizations()) {
			if(RelationReader.isSupportedRelation(iElement)) {
				elements.add(iElement);
			}
		}

		for (IElement iElement : iBlock.getSupplierDependencies()) {
			if(RelationReader.isSupportedRelation(iElement)) {
				elements.add(iElement);
			}
		}

		for (IElement iElement : iBlock.getClientDependencies()) {
			if(RelationReader.isSupportedRelation(iElement)) {
				elements.add(iElement);
			}
		}

		for (IElement iElement : iBlock.getSupplierUsages()) {
			if(RelationReader.isSupportedRelation(iElement)) {
				elements.add(iElement);
			}
		}

		for (IElement iElement : iBlock.getClientUsages()) {
			if(RelationReader.isSupportedRelation(iElement)) {
				elements.add(iElement);
			}
		}

		for (IAttribute iAttr : iBlock.getAttributes()) {
			if(RelationReader.isSupportedRelation(iAttr.getAssociation())) {
				IElement iElement = iAttr.getAssociation();
				elements.add(iElement);
			}
		}

		for (IAttribute iAttr : iBlock.getParts()) {
			if(RelationReader.isSupportedRelation(iAttr.getAssociation())) {
				IElement iElement = iAttr.getAssociation();
				elements.add(iElement);
			}
		}

		for (IPort iPort : iBlock.getPorts()) {
			if(RelationReader.isSupportedRelation(iPort.getAssociation())) {
				IElement iElement = iPort.getAssociation();
				elements.add(iElement);
			}
		}

		for (IPort iPort : iBlock.getFullPorts()) {
			if(RelationReader.isSupportedRelation(iPort.getAssociation())) {
				IElement iElement = iPort.getAssociation();
				elements.add(iElement);
			}
		}

		for (IPort iPort : iBlock.getProxyPorts()) {
			if(RelationReader.isSupportedRelation(iPort.getAssociation())) {
				IElement iElement = iPort.getAssociation();
				elements.add(iElement);
			}
		}

		return elements;
	}

}
