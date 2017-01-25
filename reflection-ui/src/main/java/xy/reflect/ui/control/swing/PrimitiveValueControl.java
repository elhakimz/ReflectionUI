package xy.reflect.ui.control.swing;

import java.lang.reflect.InvocationTargetException;

import xy.reflect.ui.IReflectionUI;
import xy.reflect.ui.control.data.ControlDataProxy;
import xy.reflect.ui.control.data.IControlData;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReflectionUIError;

public class PrimitiveValueControl extends TextControl {

	private static final long serialVersionUID = 1L;

	public PrimitiveValueControl(SwingRenderer swingRenderer, IControlData data,
			Class<? extends Object> primitiveJavaType) {
		super(swingRenderer, 
				handleValueConversions(swingRenderer.getReflectionUI(), data, primitiveJavaType));
	}

	protected static IControlData handleValueConversions(final IReflectionUI reflectionUI, IControlData data,
			final Class<?> primitiveJavaType) {
		return new ControlDataProxy(data) {

			@Override
			public Object getValue() {
				Object result = super.getValue();
				if (result == null) {
					return result;
				}
				return toText(result);
			}

			@Override
			public void setValue(Object value) {
				if (value != null) {
					value = fromText((String) value, primitiveJavaType);
				}
				super.setValue(value);
			}

			@Override
			public ITypeInfo getType() {
				return new DefaultTypeInfo(reflectionUI, String.class);
			}

		};
	}

	public static String toText(Object object) {
		return object.toString();
	}

	public static Object fromText(String text, Class<?> javaType) {
		if (javaType.isPrimitive()) {
			javaType = ClassUtils.primitiveToWrapperType(javaType);
		}
		if (javaType == Character.class) {
			if (text.length() != 1) {
				throw new RuntimeException("Invalid value: '" + text + "'. 1 character is expected");
			}
			return text.charAt(0);
		} else {
			try {
				return javaType.getConstructor(new Class[] { String.class }).newInstance(text);
			} catch (IllegalArgumentException e) {
				throw new ReflectionUIError(e);
			} catch (SecurityException e) {
				throw new ReflectionUIError(e);
			} catch (InstantiationException e) {
				throw new ReflectionUIError(e);
			} catch (IllegalAccessException e) {
				throw new ReflectionUIError(e);
			} catch (InvocationTargetException e) {
				throw new ReflectionUIError(e.getTargetException());
			} catch (NoSuchMethodException e) {
				throw new ReflectionUIError(e);
			}
		}
	}
}
