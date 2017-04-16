package xy.reflect.ui.info.field;

import java.util.Map;

import xy.reflect.ui.control.input.DefaultFieldControlData;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.util.ITypeInfoProxyFactory;
import xy.reflect.ui.undo.ControlDataValueModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class SubFieldInfo implements IFieldInfo {

	protected IFieldInfo theField;
	protected IFieldInfo theSubField;

	protected IModification oppositeSubFieldModification;
	protected IModification oppositeFieldModification;

	public SubFieldInfo(IFieldInfo theField, IFieldInfo theSubField) {
		super();
		this.theField = theField;
		this.theSubField = theSubField;
	}

	public SubFieldInfo(ITypeInfo type, String fieldName, String subFieldName) {
		this.theField = ReflectionUIUtils.findInfoByName(type.getFields(), fieldName);
		if (this.theField == null) {
			throw new ReflectionUIError("Field '" + fieldName + "' not found in type '" + type.getName() + "'");
		}
		this.theSubField = ReflectionUIUtils.findInfoByName(this.theField.getType().getFields(), subFieldName);
		if (this.theSubField == null) {
			throw new ReflectionUIError(
					"Sub-Field '" + subFieldName + "' not found in field type '" + theField.getType().getName() + "'");
		}
	}

	@Override
	public ITypeInfo getType() {
		return theSubField.getType();
	}

	@Override
	public ITypeInfoProxyFactory getTypeSpecificities() {
		return theSubField.getTypeSpecificities();
	}

	@Override
	public String getName() {
		return theField.getName() + "." + theSubField.getName();
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.composeMessage(theField.getCaption(), theSubField.getCaption());
	}

	@Override
	public Object getValue(Object object) {
		Object fieldValue = getTheSubFieldValue(object);
		return theSubField.getValue(fieldValue);
	}

	@Override
	public Object[] getValueOptions(Object object) {
		Object fieldValue = getTheSubFieldValue(object);
		return theSubField.getValueOptions(fieldValue);
	}

	@Override
	public boolean isGetOnly() {
		if (!ReflectionUIUtils.canCloseValueEditSession(false, theField.getValueReturnMode(),
				!theField.isGetOnly())) {
			return true;
		}
		return theSubField.isGetOnly();
	}

	@Override
	public void setValue(Object object, Object subFieldValue) {
		Object fieldValue = getTheSubFieldValue(object);

		oppositeSubFieldModification = new ControlDataValueModification(
				new DefaultFieldControlData(fieldValue, theSubField), subFieldValue, theSubField).applyAndGetOpposite();

		oppositeFieldModification = ReflectionUIUtils.closeValueEditSession(IModification.FAKE_MODIFICATION, true,
				theField.getValueReturnMode(), true,
				new ControlDataValueModification(new DefaultFieldControlData(object, theField), fieldValue, theField),
				theField, ControlDataValueModification.getTitle(theField));
	}

	protected Object getTheSubFieldValue(Object object) {
		Object result = theField.getValue(object);
		if (result == null) {
			try {
				result = ReflectionUIUtils.createDefaultInstance(theField.getType());
			} catch (Throwable t) {
				throw new ReflectionUIError(
						"Sub-field error: Parent field value is missing and cannot be constructed: " + t.toString(), t);
			}
		}
		return result;
	}

	@Override
	public Runnable getCustomUndoUpdateJob(Object object, Object value) {
		return new Runnable() {
			@Override
			public void run() {
				oppositeSubFieldModification.applyAndGetOpposite();
				oppositeSubFieldModification = null;
				oppositeFieldModification.applyAndGetOpposite();
				oppositeFieldModification = null;
			}
		};
	}

	@Override
	public boolean isValueNullable() {
		return theSubField.isValueNullable();
	}

	@Override
	public String getNullValueLabel() {
		return theSubField.getNullValueLabel();
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.combine(theField.getValueReturnMode(), theSubField.getValueReturnMode());
	}

	@Override
	public InfoCategory getCategory() {
		return theField.getCategory();
	}

	@Override
	public String getOnlineHelp() {
		return theSubField.getOnlineHelp();
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return theSubField.getSpecificProperties();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((theField == null) ? 0 : theField.hashCode());
		result = prime * result + ((theSubField == null) ? 0 : theSubField.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubFieldInfo other = (SubFieldInfo) obj;
		if (theField == null) {
			if (other.theField != null)
				return false;
		} else if (!theField.equals(other.theField))
			return false;
		if (theSubField == null) {
			if (other.theSubField != null)
				return false;
		} else if (!theSubField.equals(other.theSubField))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SubFieldInfo [theField=" + theField + ", theSubField=" + theSubField + "]";
	}

}
