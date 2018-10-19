package xy.reflect.ui.info.type.source;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;

public class TypeInfoSourceProxy implements ITypeInfoSource {

	protected ITypeInfoSource base;

	public TypeInfoSourceProxy(ITypeInfoSource base) {
		super();
		this.base = base;
	}

	public ITypeInfo getTypeInfo(ReflectionUI reflectionUI) {
		return new InfoProxyFactory() {

			@Override
			protected ITypeInfoSource getSource(ITypeInfo type) {
				return TypeInfoSourceProxy.this;
			}

			@Override
			public String getIdentifier() {
				return TypeInfoSourceProxy.this.toString();
			}

		}.wrapTypeInfo(base.getTypeInfo(reflectionUI));
	}

	public SpecificitiesIdentifier getSpecificitiesIdentifier() {
		return base.getSpecificitiesIdentifier();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((base == null) ? 0 : base.hashCode());
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
		TypeInfoSourceProxy other = (TypeInfoSourceProxy) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TypeInfoSourceProxy [base=" + base + "]";
	}

}
