package xy.reflect.ui.info.field;

import java.util.Arrays;
import java.util.Iterator;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.GenericEnumerationFactory;

public class ValueOptionsAsEnumerationFieldInfo extends FieldInfoProxy {

	protected Object object;

	protected GenericEnumerationFactory enumFactory;
	protected ITypeInfo enumType;
	private ReflectionUI reflectionUI;

	public ValueOptionsAsEnumerationFieldInfo(ReflectionUI reflectionUI, Object object, IFieldInfo base) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.enumFactory = createEnumerationFactory();
		this.enumType = reflectionUI.getTypeInfo(enumFactory.getInstanceTypeInfoSource());
	}

	protected GenericEnumerationFactory createEnumerationFactory() {
		ITypeInfo ownerType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		String enumTypeName = "ValueOptions [ownerType=" + ownerType.getName() + ", field=" + base.getName() + "]";
		Iterable<Object> iterable = new Iterable<Object>() {
			@Override
			public Iterator<Object> iterator() {
				Object[] valueOptions = base.getValueOptions(object);
				return Arrays.asList(valueOptions).iterator();
			}
		};
		return new GenericEnumerationFactory(reflectionUI, iterable, enumTypeName, "", false);
	}
	
	public static boolean hasValueOptions(Object object, IFieldInfo field) {
		Object[] valueOptions;
		try {
			valueOptions = field.getValueOptions(object);
			return valueOptions != null;
		} catch (Throwable t) {
			return true;
		}
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public Object getValue(Object object) {
		Object value = super.getValue(object);
		return enumFactory.getInstance(value);
	}

	@Override
	public void setValue(Object object, Object value) {
		value = enumFactory.unwrapInstance(value);
		super.setValue(object, value);
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
		value = enumFactory.unwrapInstance(value);
		return super.getNextUpdateCustomUndoJob(object, value);
	}

	@Override
	public ITypeInfo getType() {
		return enumType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ValueOptionsAsEnumerationFieldInfo other = (ValueOptionsAsEnumerationFieldInfo) obj;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		return true;
	}

}
