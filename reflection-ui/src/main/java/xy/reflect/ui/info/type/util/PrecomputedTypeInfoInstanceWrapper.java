package xy.reflect.ui.info.type.util;

import java.util.Map;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class PrecomputedTypeInfoInstanceWrapper {

	protected Object instance;
	protected ITypeInfo precomputedType;
	protected StackTraceElement[] instanciationTrace = ReflectionUIUtils
			.createDebugTrace();

	public PrecomputedTypeInfoInstanceWrapper(Object instance,
			ITypeInfo precomputedType) {
		this.instance = instance;
		this.precomputedType = precomputedType;
	}

	public PrecomputedTypeInfoSource getPrecomputedTypeInfoSource() {
		return new PrecomputedTypeInfoSource(
				adaptPrecomputedType(precomputedType));
	}

	public static ITypeInfo adaptPrecomputedType(final ITypeInfo precomputedType) {
		return new TypeInfoProxyConfiguration() {

			protected Object unwrap(Object object) {
				PrecomputedTypeInfoInstanceWrapper wrapper = (PrecomputedTypeInfoInstanceWrapper) object;
				if (!wrapper.precomputedType.equals(precomputedType)) {
					throw new ReflectionUIError(
							PrecomputedTypeInfoInstanceWrapper.class
									.getSimpleName()
									+ " Error: "
									+ "\nExpected precomputed type: "
									+ precomputedType
									+ " ("
									+ precomputedType.getClass()
									+ ")"
									+ ";"
									+ "\nFound precomputed type: "
									+ wrapper.precomputedType
									+ " ("
									+ wrapper.precomputedType.getClass()
									+ ")"
									+ ";" +

									"\nInstance: " + wrapper.instance);
				}
				return wrapper.getInstance();
			}

			@Override
			protected Object getValue(Object object, IFieldInfo field,
					ITypeInfo containingType) {
				object = unwrap(object);
				return super.getValue(object, field, containingType);
			}

			@Override
			protected void setValue(Object object, Object value,
					IFieldInfo field, ITypeInfo containingType) {
				object = unwrap(object);
				super.setValue(object, value, field, containingType);
			}

			@Override
			protected Object invoke(Object object,
					Map<Integer, Object> valueByParameterPosition,
					IMethodInfo method, ITypeInfo containingType) {
				object = unwrap(object);
				return super.invoke(object, valueByParameterPosition, method,
						containingType);
			}

			@Override
			protected void validate(ITypeInfo type, Object object)
					throws Exception {
				object = unwrap(object);
				super.validate(type, object);
			}

			@Override
			protected String toString(ITypeInfo type, Object object) {
				object = unwrap(object);
				return super.toString(type, object);
			}

			@Override
			protected Object fromListValue(IListTypeInfo type,
					Object[] listValue) {
				Object result = super.fromListValue(type, listValue);
				return new PrecomputedTypeInfoInstanceWrapper(result,
						precomputedType);
			}

			@Override
			protected String formatEnumerationItem(Object object,
					IEnumerationTypeInfo type) {
				object = unwrap(object);
				return super.formatEnumerationItem(object, type);
			}

			@Override
			protected Object[] toListValue(IListTypeInfo type, Object object) {
				object = unwrap(object);
				return super.toListValue(type, object);
			}

			@Override
			protected boolean supportsInstance(ITypeInfo type, Object object) {
				object = unwrap(object);
				return super.supportsInstance(type, object);
			}

			@Override
			protected void validateParameters(IMethodInfo method,
					ITypeInfo containingType, Object object,
					Map<Integer, Object> valueByParameterPosition)
					throws Exception {
				object = unwrap(object);
				super.validateParameters(method, containingType, object,
						valueByParameterPosition);
			}

			@Override
			protected IModification getUndoModification(IMethodInfo method,
					ITypeInfo containingType, Object object,
					Map<Integer, Object> valueByParameterPosition) {
				object = unwrap(object);
				return super.getUndoModification(method, containingType,
						object, valueByParameterPosition);
			}

		}.get(precomputedType);
	}

	public Object getInstance() {
		return instance;
	}

	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((instance == null) ? 0 : instance.hashCode());
		result = prime * result
				+ ((precomputedType == null) ? 0 : precomputedType.hashCode());
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
		PrecomputedTypeInfoInstanceWrapper other = (PrecomputedTypeInfoInstanceWrapper) obj;
		if (instance == null) {
			if (other.instance != null)
				return false;
		} else if (!instance.equals(other.instance))
			return false;
		if (precomputedType == null) {
			if (other.precomputedType != null)
				return false;
		} else if (!precomputedType.equals(other.precomputedType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return instance.toString();
	}

}
