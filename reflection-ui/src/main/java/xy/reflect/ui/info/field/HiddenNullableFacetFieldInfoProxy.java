package xy.reflect.ui.info.field;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.factory.HiddenNullableFacetsTypeInfoProxyFactory;

public class HiddenNullableFacetFieldInfoProxy extends FieldInfoProxy {

	protected ReflectionUI reflectionUI;
	protected IFieldInfo base;

	public HiddenNullableFacetFieldInfoProxy(final ReflectionUI reflectionUI, final IFieldInfo base) {
		super(base);
		this.reflectionUI = reflectionUI;
		this.base = base;
	}

	@SuppressWarnings("unused")
	@Override
	public Object getValue(final Object object) {
		final Object[] result = new Object[1];
		new HiddenNullableFacetsTypeInfoProxyFactory(reflectionUI) {
			{
				result[0] = getValue(object, base, null);
			}
		};
		return result[0];
	}

	@Override
	public boolean isValueNullable() {
		return false;
	}

	@Override
	public int hashCode() {
		return base.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		return base.equals(((HiddenNullableFacetFieldInfoProxy) obj).base);
	}

	@Override
	public String toString() {
		return "HiddenNullableFacetFieldInfoProxy [base=" + base + "]";
	}

}
