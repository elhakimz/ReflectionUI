package xy.reflect.ui.info.method;

import java.util.List;
import java.util.Map;

import xy.reflect.ui.control.DefaultFieldControlData;
import xy.reflect.ui.control.DefaultMethodControlData;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.IInfoProxyFactory;
import xy.reflect.ui.undo.ControlDataValueModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.InvokeMethodModification;
import xy.reflect.ui.undo.IrreversibleModificationException;
import xy.reflect.ui.util.FututreActionBuilder;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class SubMethodInfo extends AbstractInfo implements IMethodInfo {

	protected IFieldInfo theField;
	protected IMethodInfo theSubMethod;
	protected FututreActionBuilder undoJobBuilder = new FututreActionBuilder();

	public SubMethodInfo(IFieldInfo theField, IMethodInfo theSubMethod) {
		super();
		this.theField = theField;
		this.theSubMethod = theSubMethod;
	}

	public SubMethodInfo(ITypeInfo type, String fieldName, String subMethodSignature) {
		this.theField = ReflectionUIUtils.findInfoByName(type.getFields(), fieldName);
		if (this.theField == null) {
			throw new ReflectionUIError("Field '" + fieldName + "' not found in type '" + type.getName() + "'");
		}
		this.theSubMethod = ReflectionUIUtils.findMethodBySignature(this.theField.getType().getMethods(),
				subMethodSignature);
		if (this.theSubMethod == null) {
			throw new ReflectionUIError("Sub-Method '" + subMethodSignature + "' not found in field type '"
					+ theField.getType().getName() + "'");
		}
	}

	@Override
	public String getSignature() {
		return ReflectionUIUtils.buildMethodSignature(this);
	}

	@Override
	public ITypeInfo getReturnValueType() {
		return theSubMethod.getReturnValueType();
	}

	@Override
	public IInfoProxyFactory getReturnValueTypeSpecificities() {
		return theSubMethod.getReturnValueTypeSpecificities();
	}

	@Override
	public String getName() {
		return theField.getName() + "." + theSubMethod.getName();
	}

	@Override
	public String getCaption() {
		return ReflectionUIUtils.composeMessage(theField.getCaption(), theSubMethod.getCaption());
	}

	protected Object getTheFieldValue(Object object) {
		Object result = theField.getValue(object);
		if (result == null) {
			throw new ReflectionUIError("Sub-method error: Parent field value is missing");
		}
		return result;
	}

	@Override
	public boolean isReadOnly() {
		if (!ReflectionUIUtils.canEditSeparateObjectValue(false, theField.getValueReturnMode(), !theField.isGetOnly())) {
			return true;
		}
		return theSubMethod.isReadOnly();
	}

	@Override
	public String getConfirmationMessage(Object object, InvocationData invocationData) {
		Object fieldValue = getTheFieldValue(object);
		return theSubMethod.getConfirmationMessage(fieldValue, invocationData);
	}

	@Override
	public Object invoke(Object object, InvocationData invocationData) {
		Object fieldValue = getTheFieldValue(object);
		Object result;
		IModification oppositeSubMethodModification;
		if (theSubMethod.getNextInvocationUndoJob(fieldValue, invocationData) == null) {
			result = theSubMethod.invoke(fieldValue, invocationData);
			oppositeSubMethodModification = null;
		} else {
			final Object[] resultHolder = new Object[1];
			oppositeSubMethodModification = ReflectionUIUtils.finalizeSeparateObjectValueEditSession(
					IModification.FAKE_MODIFICATION, true, theSubMethod.getValueReturnMode(), true,
					new InvokeMethodModification(
							new DefaultMethodControlData(fieldValue, new MethodInfoProxy(theSubMethod) {
								@Override
								public Object invoke(Object object, InvocationData invocationData) {
									resultHolder[0] = super.invoke(object, invocationData);
									return resultHolder[0];
								}
							}), invocationData, theSubMethod),
					theSubMethod, InvokeMethodModification.getTitle(theSubMethod));
			result = resultHolder[0];
		}
		IModification oppositeFieldModification = ReflectionUIUtils.finalizeSeparateObjectValueEditSession(
				IModification.FAKE_MODIFICATION, true, theField.getValueReturnMode(), true,
				new ControlDataValueModification(new DefaultFieldControlData(object, theField), fieldValue, theField),
				theField, ControlDataValueModification.getTitle(theField));

		undoJobBuilder.setOption("oppositeSubMethodModification", oppositeSubMethodModification);
		undoJobBuilder.setOption("oppositeFieldModification", oppositeFieldModification);
		undoJobBuilder.build();
		return result;

	}

	@Override
	public Runnable getNextInvocationUndoJob(Object object, final InvocationData invocationData) {
		Object fieldValue = getTheFieldValue(object);
		if (theSubMethod.getNextInvocationUndoJob(fieldValue, invocationData) == null) {
			return null;
		}
		return undoJobBuilder.will(new FututreActionBuilder.FuturePerformance() {

			@Override
			public void perform(Map<String, Object> options) {
				IModification oppositeSubMethodModification = (IModification) options
						.get("oppositeSubMethodModification");
				IModification oppositeFieldModification = (IModification) options.get("oppositeFieldModification");
				if (oppositeSubMethodModification == null) {
					throw new IrreversibleModificationException();
				}
				oppositeSubMethodModification.applyAndGetOpposite();
				oppositeFieldModification.applyAndGetOpposite();
			}
		});
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.combine(theField.getValueReturnMode(), theSubMethod.getValueReturnMode());
	}

	@Override
	public boolean isNullReturnValueDistinct() {
		return theSubMethod.isNullReturnValueDistinct();
	}

	@Override
	public String getNullReturnValueLabel() {
		return theSubMethod.getNullReturnValueLabel();
	}

	@Override
	public InfoCategory getCategory() {
		return theField.getCategory();
	}

	@Override
	public String getOnlineHelp() {
		return theSubMethod.getOnlineHelp();
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return theSubMethod.getSpecificProperties();
	}

	@Override
	public List<IParameterInfo> getParameters() {
		return theSubMethod.getParameters();
	}

	@Override
	public void validateParameters(Object object, InvocationData invocationData) throws Exception {
		Object fieldValue = getTheFieldValue(object);
		theSubMethod.validateParameters(fieldValue, invocationData);
	}

	@Override
	public boolean isReturnValueDetached() {
		return theSubMethod.isReturnValueDetached();
	}

	@Override
	public boolean isReturnValueIgnored() {
		return theSubMethod.isReturnValueIgnored();
	}

	public ResourcePath getIconImagePath() {
		return theSubMethod.getIconImagePath();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((theField == null) ? 0 : theField.hashCode());
		result = prime * result + ((theSubMethod == null) ? 0 : theSubMethod.hashCode());
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
		SubMethodInfo other = (SubMethodInfo) obj;
		if (theField == null) {
			if (other.theField != null)
				return false;
		} else if (!theField.equals(other.theField))
			return false;
		if (theSubMethod == null) {
			if (other.theSubMethod != null)
				return false;
		} else if (!theSubMethod.equals(other.theSubMethod))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SubMethodInfo [theField=" + theField + ", theSubMethod=" + theSubMethod + "]";
	}

}
