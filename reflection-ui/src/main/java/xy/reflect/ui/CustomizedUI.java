package xy.reflect.ui;

import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.custom.InfoCustomizations.FieldCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.TypeCustomization;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoCustomizationsFactory;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * This is a subclass of ReflectionUI supporting declarative customizations of
 * the generated abstract UI models.
 * 
 * @author olitank
 *
 */
public class CustomizedUI extends ReflectionUI {

	protected static CustomizedUI defaultInstance;

	protected InfoCustomizations infoCustomizations;

	/**
	 * @return the default instance of this class. This instance is constructed with
	 *         the {@link InfoCustomizations#getDefault()} return value.
	 */
	public static CustomizedUI getDefault() {
		if (defaultInstance == null) {
			defaultInstance = new CustomizedUI(InfoCustomizations.getDefault());
		}
		return defaultInstance;
	}

	/**
	 * Constructs an instance of this class that will use the given customizations.
	 * 
	 * @param infoCustomizations
	 *            The abstract UI model customizations specification object.
	 */
	public CustomizedUI(InfoCustomizations infoCustomizations) {
		super();
		this.infoCustomizations = infoCustomizations;
	}

	/**
	 * Constructs an instance of this class with empty customizations.
	 */
	public CustomizedUI() {
		this(new InfoCustomizations());
	}

	/**
	 * @return abstract UI model customizations specification.
	 */
	public InfoCustomizations getInfoCustomizations() {
		return infoCustomizations;
	}

	@Override
	public final ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
		ITypeInfo result = super.getTypeInfo(typeSource);
		result = getInfoCustomizationsSetupFactory().wrapTypeInfo(result);
		result = getTypeInfoBeforeCustomizations(result);
		result = new InfoCustomizationsFactory(this, infoCustomizations).wrapTypeInfo(result);
		final SpecificitiesIdentifier specificitiesIdentifier = typeSource.getSpecificitiesIdentifier();
		if (specificitiesIdentifier != null) {
			TypeCustomization typeCustomization = InfoCustomizations.getTypeCustomization(infoCustomizations,
					specificitiesIdentifier.getContainingTypeName());
			FieldCustomization fieldCustomization = InfoCustomizations.getFieldCustomization(typeCustomization,
					specificitiesIdentifier.getFieldName());
			result = new InfoCustomizationsFactory(this, fieldCustomization.getSpecificTypeCustomizations()) {

				@Override
				public String getIdentifier() {
					return specificitiesIdentifier.toString();
				}

			}.wrapTypeInfo(result);
		}
		result = getTypeInfoAfterCustomizations(result);
		return result;
	}

	@Override
	public IApplicationInfo getApplicationInfo() {
		IApplicationInfo result = super.getApplicationInfo();
		result = getInfoCustomizationsSetupFactory().wrapApplicationInfo(result);
		result = getApplicationInfoBeforeCustomizations(result);
		result = new InfoCustomizationsFactory(this, infoCustomizations).wrapApplicationInfo(result);
		result = getApplicationInfoAfterCustomizations(result);
		return result;
	}

	/**
	 * @return the UI model proxy factory that will be used to prepare every UI
	 *         model for customizations. This factory will be used before calling
	 *         {@link #getApplicationInfoBeforeCustomizations(IApplicationInfo)}.
	 */
	public InfoProxyFactory getInfoCustomizationsSetupFactory() {
		return new InfoProxyFactory() {

			@Override
			protected ITypeInfo getType(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
				ITypeInfo result = super.getType(param, method, containingType);
				ITypeInfoSource source = result.getSource();
				if (source.getSpecificitiesIdentifier() != null) {
					throw new ReflectionUIError(
							"Invalid parameter type info: specificities identifier of type info source not null, null value expected."
									+ "\n" + "parameter=" + param.getName() + ", method=" + method.getName()
									+ ", containingType=" + containingType.getName() + ", typeInfoSource=" + source);
				}
				return result;
			}

			@Override
			protected ITypeInfo getType(IFieldInfo field, ITypeInfo containingType) {
				ITypeInfo result = super.getType(field, containingType);
				ITypeInfoSource source = result.getSource();
				if (source.getSpecificitiesIdentifier() == null) {
					throw new ReflectionUIError(
							"Invalid field type info: specificities identifier of type info source is null, non-null value expected."
									+ "\n" + "field=" + field.getName() + ", containingType=" + containingType.getName()
									+ ", typeInfoSource=" + source);
				}
				return result;
			}

			@Override
			protected ITypeInfo getReturnValueType(IMethodInfo method, ITypeInfo containingType) {
				ITypeInfo result = super.getReturnValueType(method, containingType);
				if (result == null) {
					return null;
				}
				ITypeInfoSource source = result.getSource();
				if (source.getSpecificitiesIdentifier() != null) {
					throw new ReflectionUIError(
							"Invalid method type info: specificities identifier of type info source is not null, null value expected."
									+ "\n" + "method=" + method.getName() + ", containingType="
									+ containingType.getName() + ", typeInfoSource=" + source);
				}
				return result;
			}

		};
	}

	/**
	 * This method allows to alter the given {@link ITypeInfo} object after applying
	 * the declarative customizations.
	 * 
	 * @param type
	 *            The UI-oriented type information.
	 * @return a potentially proxied version of the input argument.
	 */
	protected ITypeInfo getTypeInfoAfterCustomizations(ITypeInfo type) {
		return type;
	}

	/**
	 * This method allows to alter the given {@link ITypeInfo} object before
	 * applying the declarative customizations.
	 * 
	 * @param type
	 *            The UI-oriented type information.
	 * @return a potentially proxied version of the input argument.
	 */
	protected ITypeInfo getTypeInfoBeforeCustomizations(ITypeInfo type) {
		return type;
	}

	/**
	 * This method allows to alter the given {@link IApplicationInfo} object after
	 * applying the declarative customizations.
	 * 
	 * @param appInfo
	 *            The UI-oriented application information.
	 * @return a potentially proxied version of the input argument.
	 */
	protected IApplicationInfo getApplicationInfoAfterCustomizations(IApplicationInfo appInfo) {
		return appInfo;
	}

	/**
	 * This method allows to alter the given {@link IApplicationInfo} object before
	 * applying the declarative customizations.
	 * 
	 * @param appInfo
	 *            The UI-oriented application information.
	 * @return a potentially proxied version of the input argument.
	 */
	protected IApplicationInfo getApplicationInfoBeforeCustomizations(IApplicationInfo appInfo) {
		return appInfo;
	}

	@Override
	public String toString() {
		if (this == defaultInstance) {
			return "CustomizedUI.DEFAULT";
		} else {
			return super.toString();
		}
	}

}
