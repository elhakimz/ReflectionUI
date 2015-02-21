package xy.reflect.ui.info.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.HiddenNullableFacetFieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MultiSubListField;
import xy.reflect.ui.info.field.MultiSubListField.VirtualItem;
import xy.reflect.ui.info.type.IListTypeInfo.IItemPosition;
import xy.reflect.ui.info.type.IListTypeInfo.IListStructuralInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class DefaultListStructuralInfo implements IListStructuralInfo {

	protected ReflectionUI reflectionUI;
	protected ITypeInfo rootItemType;

	public DefaultListStructuralInfo(ReflectionUI reflectionUI,
			ITypeInfo rootItemType) {
		this.reflectionUI = reflectionUI;
		this.rootItemType = rootItemType;
	}

	@Override
	public IFieldInfo getItemSubListField(IItemPosition itemPosition) {
		List<IFieldInfo> candidateFields = getItemSubListCandidateFields(itemPosition);
		for (int i = 0; i < candidateFields.size(); i++) {
			candidateFields.set(i, new HiddenNullableFacetFieldInfoProxy(
					reflectionUI, candidateFields.get(i)));
		}
		if (candidateFields.size() == 0) {
			return null;
		} else if (candidateFields.size() == 1) {
			return candidateFields.get(0);
		} else {
			return new MultiSubListField(reflectionUI, candidateFields);
		}
	}

	protected List<IFieldInfo> getItemSubListCandidateFields(
			IItemPosition itemPosition) {
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		ITypeInfo itemType = itemPosition.getContainingListType().getItemType();
		Object item = itemPosition.getItem();
		ITypeInfo actualItemType = reflectionUI.getTypeInfo(reflectionUI
				.getTypeInfoSource(item));
		if ((actualItemType instanceof IMapEntryTypeInfo)
				&& (itemPosition.getParentItemPosition() != null)) {
			IMapEntryTypeInfo entryType = (IMapEntryTypeInfo) actualItemType;
			IFieldInfo entryValueField = entryType.getValueField();
			ITypeInfo entryValueType = entryValueField.getType();
			if (entryValueType instanceof IListTypeInfo) {
				ITypeInfo entryValuListItemType = ((IListTypeInfo) entryValueType)
						.getItemType();
				ITypeInfo parentListItemType = itemPosition
						.getParentItemPosition().getContainingListType()
						.getItemType();
				if (ReflectionUIUtils.equalsOrBothNull(parentListItemType,
						entryValuListItemType)) {
					result.add(entryValueField);
				}
			}
		} else {
			List<IFieldInfo> itemFields = actualItemType.getFields();
			for (IFieldInfo field : itemFields) {
				ITypeInfo fieldType = field.getType();
				if (fieldType instanceof IListTypeInfo) {
					ITypeInfo subListItemType = ((IListTypeInfo) fieldType)
							.getItemType();
					if (subListItemType instanceof IMapEntryTypeInfo) {
						IMapEntryTypeInfo entryType = (IMapEntryTypeInfo) subListItemType;
						ITypeInfo entryValueType = entryType.getValueField()
								.getType();
						if (entryValueType instanceof IListTypeInfo) {
							ITypeInfo entryValuListItemType = ((IListTypeInfo) entryValueType)
									.getItemType();
							if (ReflectionUIUtils.equalsOrBothNull(itemType,
									entryValuListItemType)) {
								result.add(field);
							}
						}
					} else {
						if (ReflectionUIUtils.equalsOrBothNull(itemType,
								subListItemType)) {
							result.add(field);
						}
					}
				}
			}
		}
		return result;
	}

	@Override
	public List<IFieldInfo> getItemSubListFieldsToExcludeFromDetailsView(
			IItemPosition itemPosition) {
		Object item = itemPosition.getItem();
		if (item instanceof VirtualItem) {
			return ((IListTypeInfo) new MultiSubListField(reflectionUI,
					Collections.<IFieldInfo> emptyList()).getType())
					.getStructuralInfo()
					.getItemSubListFieldsToExcludeFromDetailsView(itemPosition);
		}
		return getItemSubListCandidateFields(itemPosition);
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public String getColumnCaption(int columnIndex) {
		return "";
	}

	@Override
	public String getCellValue(IItemPosition itemPosition, int columnIndex) {
		if (columnIndex != 0) {
			throw new ReflectionUIError();
		}
		return reflectionUI.toString(itemPosition.getItem());
	}

}
