package xy.reflect.ui.info.type;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;

import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.IListTypeInfo.IListStructuralInfo;

public class TypeInfoProxy {

	public ITypeInfo get(final ITypeInfo type) {
		if (type instanceof IListTypeInfo) {
			return new ListTypeInfoProxy((IListTypeInfo) type);
		} else if (type instanceof IEnumerationTypeInfo) {
			return new EnumerationTypeInfoProxy((IEnumerationTypeInfo) type);
		} else if (type instanceof IBooleanTypeInfo) {
			return new BooleanTypeInfoProxy((IBooleanTypeInfo) type);
		} else if (type instanceof ITextualTypeInfo) {
			return new TextualTypeInfoProxy((ITextualTypeInfo) type);
		} else if (type instanceof FileTypeInfo) {
			return new FileTypeInfoProxy((FileTypeInfo) type);
		} else if (type instanceof IMapEntryTypeInfo) {
			return new MapEntryTypeInfoProxy((IMapEntryTypeInfo) type);
		} else {
			return new BasicTypeInfoProxy(type);
		}
	}

	protected Object getDefaultValue(IParameterInfo param, IMethodInfo method,
			ITypeInfo containingType) {
		return param.getDefaultValue();
	}

	protected int getPosition(IParameterInfo param, IMethodInfo method,
			ITypeInfo containingType) {
		return param.getPosition();
	}

	protected ITypeInfo getType(IParameterInfo param, IMethodInfo method,
			ITypeInfo containingType) {
		return param.getType();
	}

	protected boolean isNullable(IParameterInfo param, IMethodInfo method,
			ITypeInfo containingType) {
		return param.isNullable();
	}

	protected String getCaption(IParameterInfo param, IMethodInfo method,
			ITypeInfo containingType) {
		return param.getCaption();
	}

	protected String getName(IParameterInfo param, IMethodInfo method,
			ITypeInfo containingType) {
		return param.getName();
	}

	protected InfoCategory getCategory(IFieldInfo field,
			ITypeInfo containingType) {
		return field.getCategory();
	}

	protected ITypeInfo getType(IFieldInfo field, ITypeInfo containingType) {
		return field.getType();
	}

	protected Object getValue(Object object, IFieldInfo field,
			ITypeInfo containingType) {
		return field.getValue(object);
	}

	protected boolean isNullable(IFieldInfo field, ITypeInfo containingType) {
		return field.isNullable();
	}

	protected boolean isReadOnly(IFieldInfo field, ITypeInfo containingType) {
		return field.isReadOnly();
	}

	protected void setValue(Object object, Object value, IFieldInfo field,
			ITypeInfo containingType) {
		field.setValue(object, value);
	}

	protected String toString(ITypeInfo type, Object object) {
		return type.toString(object);
	}

	protected String getCaption(IFieldInfo field, ITypeInfo containingType) {
		return field.getCaption();
	}

	protected String getName(IFieldInfo field, ITypeInfo containingType) {
		return field.getName();
	}

	protected InfoCategory getCategory(IMethodInfo method,
			ITypeInfo containingType) {
		return method.getCategory();
	}

	protected boolean isReadOnly(IMethodInfo method, ITypeInfo containingType) {
		return method.isReadOnly();
	}

	protected Object invoke(Object object,
			Map<String, Object> valueByParameterName, IMethodInfo method,
			ITypeInfo containingType) {
		return method.invoke(object, valueByParameterName);
	}

	protected List<IParameterInfo> getParameters(IMethodInfo method,
			ITypeInfo containingType) {
		List<IParameterInfo> result = new ArrayList<IParameterInfo>();
		for (IParameterInfo param : method.getParameters()) {
			result.add(new ParameterInfoProxy(param, method, containingType));
		}
		return result;
	}

	protected ITypeInfo getReturnValueType(IMethodInfo method,
			ITypeInfo containingType) {
		return method.getReturnValueType();
	}

	protected String getCaption(IMethodInfo method, ITypeInfo containingType) {
		return method.getCaption();
	}

	protected String getName(IMethodInfo method, ITypeInfo containingType) {
		return method.getName();
	}

	protected List<?> getPossibleValues(IEnumerationTypeInfo type) {
		return type.getPossibleValues();
	}

	protected Object fromStandardList(IListTypeInfo type, List<?> list) {
		return type.fromStandardList(list);
	}

	protected ITypeInfo getItemType(IListTypeInfo type) {
		return type.getItemType();
	}

	protected IListStructuralInfo getStructuralInfo(IListTypeInfo type) {
		return type.getStructuralInfo();
	}

	protected boolean isOrdered(IListTypeInfo type) {
		return type.isOrdered();
	}

	protected List<?> toStandardList(IListTypeInfo type, Object value) {
		return type.toStandardList(value);
	}

	protected Component createFieldControl(ITypeInfo type, Object object,
			IFieldInfo field) {
		return type.createFieldControl(object, field);
	}

	protected List<IMethodInfo> getConstructors(ITypeInfo type) {
		List<IMethodInfo> result = new ArrayList<IMethodInfo>();
		for (IMethodInfo constructor : type.getConstructors()) {
			result.add(new MethodInfoProxy(constructor, type));
		}
		return result;
	}

	protected List<IFieldInfo> getFields(ITypeInfo type) {
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		for (IFieldInfo field : type.getFields()) {
			result.add(new FieldInfoProxy(field, type));
		}
		return result;
	}

	protected List<IMethodInfo> getMethods(ITypeInfo type) {
		List<IMethodInfo> result = new ArrayList<IMethodInfo>();
		for (IMethodInfo method : type.getMethods()) {
			result.add(new MethodInfoProxy(method, type));
		}
		return result;
	}

	protected List<ITypeInfo> getPolymorphicInstanceSubTypes(ITypeInfo type) {
		return type.getPolymorphicInstanceSubTypes();
	}

	protected boolean hasCustomFieldControl(ITypeInfo type) {
		return type.hasCustomFieldControl();
	}

	protected boolean isConcrete(ITypeInfo type) {
		return type.isConcrete();
	}

	protected boolean isImmutable(ITypeInfo type) {
		return type.isImmutable();
	}

	protected boolean supportsValue(ITypeInfo type, Object value) {
		return type.supportsValue(value);
	}

	protected String getCaption(ITypeInfo type) {
		return type.getCaption();
	}

	protected String getName(ITypeInfo type) {
		return type.getName();
	}

	protected Boolean toBoolean(Object value, IBooleanTypeInfo type) {
		return type.toBoolean(value);
	}

	protected Object fromBoolean(Boolean b, IBooleanTypeInfo type) {
		return type.fromBoolean(b);
	}

	protected String toText(Object value, ITextualTypeInfo type) {
		return type.toText(value);
	}

	protected Object fromText(String text, ITextualTypeInfo type) {
		return type.fromText(text);
	}

	protected File getDefaultFile(FileTypeInfo type) {
		return type.getDefaultFile();
	}

	protected Component createNonNullFieldValueControl(Object object,
			IFieldInfo field, FileTypeInfo type) {
		return type.createNonNullFieldValueControl(object, field);
	}

	protected String getDialogTitle(FileTypeInfo type) {
		return type.getDialogTitle();
	}

	protected IFieldInfo getKeyField(IMapEntryTypeInfo type) {
		return type.getKeyField();
	}

	protected IFieldInfo getValueField(IMapEntryTypeInfo type) {
		return type.getValueField();
	}

	protected void configureFileChooser(JFileChooser fileChooser,
			File currentFile, FileTypeInfo type) {
		type.configureFileChooser(fileChooser, currentFile);
	}

	protected int hashCode(ITypeInfo type) {
		return type.hashCode();
	}

	protected boolean equals(IParameterInfo param, IMethodInfo method,
			ITypeInfo containingType, IParameterInfo param2,
			IMethodInfo method2, ITypeInfo containingType2) {
		return param.equals(param2);
	}

	protected int hashCode(IParameterInfo param, IMethodInfo method,
			ITypeInfo containingType) {
		return param.hashCode();
	}

	protected String toString(IParameterInfo param, IMethodInfo method,
			ITypeInfo containingType) {
		return param.toString();
	}

	protected boolean equals(IMethodInfo method, ITypeInfo containingType,
			IMethodInfo method2, ITypeInfo containingType2) {
		return method.equals(method2);
	}

	protected int hashCode(IMethodInfo method, ITypeInfo containingType) {
		return method.hashCode();
	}

	protected String toString(IMethodInfo method, ITypeInfo containingType) {
		return method.toString();
	}

	protected String toString(IFieldInfo field, ITypeInfo containingType) {
		return field.toString();
	}

	protected int hashCode(IFieldInfo field, ITypeInfo containingType) {
		return field.hashCode();
	}

	protected boolean equals(IFieldInfo field, ITypeInfo containingType,
			IFieldInfo field2, ITypeInfo containingType2) {
		return field.equals(field2);
	}

	protected String toString(ITypeInfo type) {
		return type.toString();
	}

	protected boolean equals(ITypeInfo type1, ITypeInfo type2) {
		return type1.equals(type2);
	}

	protected String getDocumentation(IFieldInfo field, ITypeInfo containingType) {
		return field.getDocumentation();
	}

	protected String getDocumentation(IParameterInfo param, IMethodInfo method,
			ITypeInfo containingType) {
		return param.getDocumentation();
	}

	protected String getDocumentation(ITypeInfo type) {
		return type.getDocumentation();
	}

	protected String getDocumentation(IMethodInfo method,
			ITypeInfo containingType) {
		return method.getDocumentation();
	}

	protected String formatValue(Object value, IEnumerationTypeInfo type) {
		return type.formatValue(value);
	}

	private class BasicTypeInfoProxy implements ITypeInfo {

		protected ITypeInfo type;

		public BasicTypeInfoProxy(ITypeInfo type) {
			this.type = type;
		}

		@Override
		public String getName() {
			return TypeInfoProxy.this.getName(type);
		}

		@Override
		public String getCaption() {
			return TypeInfoProxy.this.getCaption(type);
		}

		@Override
		public boolean supportsValue(Object value) {
			return TypeInfoProxy.this.supportsValue(type, value);
		}

		@Override
		public boolean isImmutable() {
			return TypeInfoProxy.this.isImmutable(type);
		}

		@Override
		public boolean isConcrete() {
			return TypeInfoProxy.this.isConcrete(type);
		}

		@Override
		public boolean hasCustomFieldControl() {
			return TypeInfoProxy.this.hasCustomFieldControl(type);
		}

		@Override
		public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
			return TypeInfoProxy.this.getPolymorphicInstanceSubTypes(type);
		}

		@Override
		public List<IMethodInfo> getMethods() {
			return TypeInfoProxy.this.getMethods(type);
		}

		@Override
		public List<IFieldInfo> getFields() {
			return TypeInfoProxy.this.getFields(type);
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			return TypeInfoProxy.this.getConstructors(type);
		}

		@Override
		public Component createFieldControl(Object object, IFieldInfo field) {
			return TypeInfoProxy.this.createFieldControl(type, object, field);
		}

		@Override
		public String toString(Object object) {
			return TypeInfoProxy.this.toString(type, object);
		}

		@Override
		public int hashCode() {
			return TypeInfoProxy.this.hashCode(type);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (!getClass().equals(obj.getClass())) {
				return false;
			}
			if (!TypeInfoProxy.this.equals(type,
					((BasicTypeInfoProxy) obj).type)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return TypeInfoProxy.this.toString(type);
		}

		@Override
		public String getDocumentation() {
			return TypeInfoProxy.this.getDocumentation(type);
		}

	}

	private class ListTypeInfoProxy extends BasicTypeInfoProxy implements
			IListTypeInfo {

		public ListTypeInfoProxy(IListTypeInfo type) {
			super(type);
		}

		@Override
		public List<?> toStandardList(Object value) {
			return TypeInfoProxy.this.toStandardList((IListTypeInfo) type,
					value);
		}

		@Override
		public boolean isOrdered() {
			return TypeInfoProxy.this.isOrdered((IListTypeInfo) type);
		}

		@Override
		public IListStructuralInfo getStructuralInfo() {
			return TypeInfoProxy.this.getStructuralInfo((IListTypeInfo) type);
		}

		@Override
		public ITypeInfo getItemType() {
			return TypeInfoProxy.this.getItemType((IListTypeInfo) type);
		}

		@Override
		public Object fromStandardList(List<?> list) {
			return TypeInfoProxy.this.fromStandardList((IListTypeInfo) type,
					list);
		}

	}

	private class EnumerationTypeInfoProxy extends BasicTypeInfoProxy implements
			IEnumerationTypeInfo {

		public EnumerationTypeInfoProxy(IEnumerationTypeInfo type) {
			super(type);
		}

		@Override
		public List<?> getPossibleValues() {
			return TypeInfoProxy.this
					.getPossibleValues((IEnumerationTypeInfo) type);
		}

		@Override
		public String formatValue(Object value) {
			return TypeInfoProxy.this.formatValue(value,
					(IEnumerationTypeInfo) type);
		}

	}

	private class BooleanTypeInfoProxy extends BasicTypeInfoProxy implements
			IBooleanTypeInfo {

		public BooleanTypeInfoProxy(IBooleanTypeInfo type) {
			super(type);
		}

		@Override
		public Boolean toBoolean(Object value) {
			return TypeInfoProxy.this.toBoolean(value, (IBooleanTypeInfo) type);
		}

		@Override
		public Object fromBoolean(Boolean b) {
			return TypeInfoProxy.this.fromBoolean(b, (IBooleanTypeInfo) type);
		}

	}

	private class TextualTypeInfoProxy extends BasicTypeInfoProxy implements
			ITextualTypeInfo {

		public TextualTypeInfoProxy(ITextualTypeInfo type) {
			super(type);
		}

		@Override
		public String toText(Object value) {
			return TypeInfoProxy.this.toText(value, (ITextualTypeInfo) type);
		}

		@Override
		public Object fromText(String text) {
			return TypeInfoProxy.this.fromText(text, (ITextualTypeInfo) type);
		}
	}

	private class MapEntryTypeInfoProxy extends BasicTypeInfoProxy implements
			IMapEntryTypeInfo {

		public MapEntryTypeInfoProxy(IMapEntryTypeInfo type) {
			super(type);
		}

		@Override
		public IFieldInfo getKeyField() {
			return TypeInfoProxy.this.getKeyField((IMapEntryTypeInfo) type);
		}

		@Override
		public IFieldInfo getValueField() {
			return TypeInfoProxy.this.getValueField((IMapEntryTypeInfo) type);
		}

	}

	private class FileTypeInfoProxy extends FileTypeInfo {

		private FileTypeInfo type;

		public FileTypeInfoProxy(FileTypeInfo type) {
			super(null);
			this.type = type;
		}

		@Override
		public File getDefaultFile() {
			return TypeInfoProxy.this.getDefaultFile(type);
		}

		@Override
		public void configureFileChooser(JFileChooser fileChooser,
				File currentFile) {
			TypeInfoProxy.this.configureFileChooser(fileChooser, currentFile,
					type);
		}

		@Override
		public String getDialogTitle() {
			return TypeInfoProxy.this.getDialogTitle(type);
		}

		@Override
		public Component createNonNullFieldValueControl(Object object,
				IFieldInfo field) {
			return TypeInfoProxy.this.createNonNullFieldValueControl(object,
					field, type);
		}

		@Override
		public String getName() {
			return TypeInfoProxy.this.getName(type);
		}

		@Override
		public String getCaption() {
			return TypeInfoProxy.this.getCaption(type);
		}

		@Override
		public boolean supportsValue(Object value) {
			return TypeInfoProxy.this.supportsValue(type, value);
		}

		@Override
		public boolean isImmutable() {
			return TypeInfoProxy.this.isImmutable(type);
		}

		@Override
		public boolean isConcrete() {
			return TypeInfoProxy.this.isConcrete(type);
		}

		@Override
		public boolean hasCustomFieldControl() {
			return TypeInfoProxy.this.hasCustomFieldControl(type);
		}

		@Override
		public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
			return TypeInfoProxy.this.getPolymorphicInstanceSubTypes(type);
		}

		@Override
		public List<IMethodInfo> getMethods() {
			return TypeInfoProxy.this.getMethods(type);
		}

		@Override
		public List<IFieldInfo> getFields() {
			return TypeInfoProxy.this.getFields(type);
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			return TypeInfoProxy.this.getConstructors(type);
		}

		@Override
		public Component createFieldControl(Object object, IFieldInfo field) {
			return TypeInfoProxy.this.createFieldControl(type, object, field);
		}

		@Override
		public String toString(Object object) {
			return TypeInfoProxy.this.toString(type, object);
		}

		@Override
		public int hashCode() {
			return TypeInfoProxy.this.hashCode(type);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (!getClass().equals(obj.getClass())) {
				return false;
			}
			if (!TypeInfoProxy.this
					.equals(type, ((FileTypeInfoProxy) obj).type)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return TypeInfoProxy.this.toString(type);
		}

	}

	private class FieldInfoProxy implements IFieldInfo {

		private IFieldInfo field;
		private ITypeInfo containingType;

		public FieldInfoProxy(IFieldInfo field, ITypeInfo containingType) {
			this.field = field;
			this.containingType = containingType;
		}

		@Override
		public String getName() {
			return TypeInfoProxy.this.getName(field, containingType);
		}

		@Override
		public String getCaption() {
			return TypeInfoProxy.this.getCaption(field, containingType);
		}

		@Override
		public void setValue(Object object, Object value) {
			TypeInfoProxy.this.setValue(object, value, field, containingType);
		}

		@Override
		public boolean isReadOnly() {
			return TypeInfoProxy.this.isReadOnly(field, containingType);
		}

		@Override
		public boolean isNullable() {
			return TypeInfoProxy.this.isNullable(field, containingType);
		}

		@Override
		public Object getValue(Object object) {
			return TypeInfoProxy.this.getValue(object, field, containingType);
		}

		@Override
		public ITypeInfo getType() {
			return TypeInfoProxy.this.getType(field, containingType);
		}

		@Override
		public InfoCategory getCategory() {
			return TypeInfoProxy.this.getCategory(field, containingType);
		}

		@Override
		public String getDocumentation() {
			return TypeInfoProxy.this.getDocumentation(field, containingType);
		}

		@Override
		public String toString() {
			return TypeInfoProxy.this.toString(field, containingType);
		}

		@Override
		public int hashCode() {
			return TypeInfoProxy.this.hashCode(field, containingType);
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
			return TypeInfoProxy.this.equals(field, containingType,
					((FieldInfoProxy) obj).field,
					((FieldInfoProxy) obj).containingType);
		}

	}

	private class MethodInfoProxy implements IMethodInfo {

		private IMethodInfo method;
		private ITypeInfo containingType;

		public MethodInfoProxy(IMethodInfo method, ITypeInfo containingType) {
			this.method = method;
			this.containingType = containingType;
		}

		@Override
		public String getName() {
			return TypeInfoProxy.this.getName(method, containingType);
		}

		@Override
		public String getCaption() {
			return TypeInfoProxy.this.getCaption(method, containingType);
		}

		@Override
		public ITypeInfo getReturnValueType() {
			return TypeInfoProxy.this
					.getReturnValueType(method, containingType);
		}

		@Override
		public List<IParameterInfo> getParameters() {
			return TypeInfoProxy.this.getParameters(method, containingType);
		}

		@Override
		public Object invoke(Object object,
				Map<String, Object> valueByParameterName) {
			return TypeInfoProxy.this.invoke(object, valueByParameterName,
					method, containingType);
		}

		@Override
		public boolean isReadOnly() {
			return TypeInfoProxy.this.isReadOnly(method, containingType);
		}

		@Override
		public InfoCategory getCategory() {
			return TypeInfoProxy.this.getCategory(method, containingType);
		}

		@Override
		public String getDocumentation() {
			return TypeInfoProxy.this.getDocumentation(method, containingType);
		}

		@Override
		public String toString() {
			return TypeInfoProxy.this.toString(method, containingType);
		}

		@Override
		public int hashCode() {
			return TypeInfoProxy.this.hashCode(method, containingType);
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
			return TypeInfoProxy.this.equals(method, containingType,
					((MethodInfoProxy) obj).method,
					((MethodInfoProxy) obj).containingType);
		}

	}

	private class ParameterInfoProxy implements IParameterInfo {

		private IParameterInfo param;
		private IMethodInfo method;
		private ITypeInfo containingType;

		public ParameterInfoProxy(IParameterInfo param, IMethodInfo method,
				ITypeInfo containingType) {
			this.param = param;
			this.method = method;
			this.containingType = containingType;
		}

		@Override
		public String getName() {
			return TypeInfoProxy.this.getName(param, method, containingType);
		}

		@Override
		public String getCaption() {
			return TypeInfoProxy.this.getCaption(param, method, containingType);
		}

		@Override
		public boolean isNullable() {
			return TypeInfoProxy.this.isNullable(param, method, containingType);
		}

		@Override
		public ITypeInfo getType() {
			return TypeInfoProxy.this.getType(param, method, containingType);
		}

		@Override
		public int getPosition() {
			return TypeInfoProxy.this
					.getPosition(param, method, containingType);
		}

		@Override
		public Object getDefaultValue() {
			return TypeInfoProxy.this.getDefaultValue(param, method,
					containingType);
		}

		@Override
		public String toString() {
			return TypeInfoProxy.this.toString(param, method, containingType);
		}

		@Override
		public String getDocumentation() {
			return TypeInfoProxy.this.getDocumentation(param, method,
					containingType);
		}

		@Override
		public int hashCode() {
			return TypeInfoProxy.this.hashCode(param, method, containingType);
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
			return TypeInfoProxy.this.equals(param, method, containingType,
					((ParameterInfoProxy) obj).param,
					((ParameterInfoProxy) obj).method,
					((ParameterInfoProxy) obj).containingType);
		}

	}
}
