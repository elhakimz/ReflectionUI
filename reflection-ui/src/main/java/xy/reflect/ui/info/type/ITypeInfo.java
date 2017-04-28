package xy.reflect.ui.info.type;

import java.util.List;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.menu.Menu;

public interface ITypeInfo extends IInfo {

	boolean isImmutable();

	boolean isPrimitive();

	boolean isConcrete();

	List<IMethodInfo> getConstructors();

	List<IFieldInfo> getFields();

	List<IMethodInfo> getMethods();

	boolean supportsInstance(Object object);

	List<ITypeInfo> getPolymorphicInstanceSubTypes();

	String toString(Object object);

	void validate(Object object) throws Exception;

	boolean canCopy(Object object);

	Object copy(Object object);

	boolean isModificationStackAccessible();

	String getIconImagePath();

	ITypeInfo.FieldsLayout getFieldsLayout();

	List<Menu> getMenus();

	enum FieldsLayout {
		VERTICAL_FLOW, HORIZONTAL_FLOW

	}

}
