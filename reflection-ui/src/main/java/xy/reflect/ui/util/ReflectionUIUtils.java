/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * The license allows developers and companies to use and integrate a software 
 * component released under the LGPL into their own (even proprietary) software 
 * without being required by the terms of a strong copyleft license to release the 
 * source code of their own components. However, any developer who modifies 
 * an LGPL-covered component is required to make their modified version 
 * available under the same LGPL license. For proprietary software, code under 
 * the LGPL is usually used in the form of a shared library, so that there is a clear 
 * separation between the proprietary and LGPL components.
 * 
 * The GNU Lesser General Public License allows you also to freely redistribute the 
 * libraries under the same license, if you provide the terms of the GNU Lesser 
 * General Public License with them and add the following copyright notice at the 
 * appropriate place (with a link to http://javacollection.net/reflectionui/ web site 
 * when possible).
 ******************************************************************************/
package xy.reflect.ui.util;

import java.io.Serializable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.MethodControlDataProxy;
import xy.reflect.ui.control.plugin.IFieldControlPlugin;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ITransactionInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.AbstractMenuItemInfo;
import xy.reflect.ui.info.menu.IMenuElementInfo;
import xy.reflect.ui.info.menu.IMenuElementPosition;
import xy.reflect.ui.info.menu.MenuInfo;
import xy.reflect.ui.info.menu.MenuElementKind;
import xy.reflect.ui.info.menu.MenuItemCategory;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.factory.PolymorphicTypeOptionsFactory;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.undo.FieldControlDataModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.MethodControlDataModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.UndoOrder;

/**
 * Utilities for dealing with {@link ReflectionUI} instances.
 * 
 * @author olitank
 *
 */
public class ReflectionUIUtils {

	public static final ReflectionUI STANDARD_REFLECTION = new ReflectionUI();
	public static final String METHOD_SIGNATURE_REGEX = "(\\s*[^ ]+\\s*)(\\s+[^ ]+\\s*)?\\(([^\\)]*)\\)\\s*";

	public static String buildMethodSignature(String returnTypeName, String methodName,
			List<String> parameterTypeNames) {
		StringBuilder result = new StringBuilder();
		result.append(returnTypeName);
		result.append(" " + methodName + "(");
		for (int i = 0; i < parameterTypeNames.size(); i++) {
			String paramTypeName = parameterTypeNames.get(i);
			if (i > 0) {
				result.append(", ");
			}
			result.append(paramTypeName);
		}
		result.append(")");
		return result.toString();
	}

	public static String buildMethodSignature(IMethodInfo method) {
		ITypeInfo returnType = method.getReturnValueType();
		String returnTypeName = (returnType == null) ? "void" : returnType.getName();
		String methodName = method.getName();
		List<String> parameterTypeNames = new ArrayList<String>();
		List<IParameterInfo> params = method.getParameters();
		for (int i = 0; i < params.size(); i++) {
			IParameterInfo param = params.get(i);
			parameterTypeNames.add(param.getType().getName());
		}
		return buildMethodSignature(returnTypeName, methodName, parameterTypeNames);
	}

	public static Object buildMethodSignature(Method method) {
		Class<?> returnType = method.getReturnType();
		String returnTypeName = returnType.getName();
		String methodName = method.getName();
		List<String> parameterTypeNames = new ArrayList<String>();
		Class<?>[] paramTypes = method.getParameterTypes();
		for (int i = 0; i < paramTypes.length; i++) {
			Class<?> paramType = paramTypes[i];
			parameterTypeNames.add(paramType.getName());
		}
		return buildMethodSignature(returnTypeName, methodName, parameterTypeNames);
	}

	public static String extractMethodReturnTypeNameFromSignature(String methodSignature) {
		Pattern pattern = Pattern.compile(METHOD_SIGNATURE_REGEX);
		Matcher matcher = pattern.matcher(methodSignature);
		if (!matcher.matches()) {
			return null;
		}
		String result = matcher.group(1);
		if (result != null) {
			result = result.trim();
		}
		return result;
	}

	public static String extractMethodNameFromSignature(String methodSignature) {
		Pattern pattern = Pattern.compile(METHOD_SIGNATURE_REGEX);
		Matcher matcher = pattern.matcher(methodSignature);
		if (!matcher.matches()) {
			return null;
		}
		String result = matcher.group(2);
		if (result == null) {
			result = "";
		} else {
			result = result.trim();
		}
		return result;
	}

	public static String[] extractMethodParameterTypeNamesFromSignature(String methodSignature) {
		Pattern pattern = Pattern.compile(METHOD_SIGNATURE_REGEX);
		Matcher matcher = pattern.matcher(methodSignature);
		if (!matcher.matches()) {
			return null;
		}
		String paramListString = matcher.group(3);
		paramListString = paramListString.trim();
		List<String> result = new ArrayList<String>(Arrays.asList(paramListString.split("\\s*,\\s*")));
		result.removeAll(Collections.singletonList(""));
		return result.toArray(new String[result.size()]);
	}

	public static String identifierToCaption(String id) {
		StringBuilder result = new StringBuilder();
		int i = 0;
		char lastC = 0;
		for (char c : id.toCharArray()) {
			if (i == 0) {
				result.append(Character.toUpperCase(c));
			} else if (Character.isUpperCase(c) && !Character.isUpperCase(lastC)) {
				result.append(" " + c);
			} else if (Character.isDigit(c) && !Character.isDigit(lastC)) {
				result.append(" " + c);
			} else if (!Character.isLetterOrDigit(c) && Character.isLetterOrDigit(lastC)) {
				result.append(" " + c);
			} else {
				result.append(c);
			}
			lastC = c;
			i++;
		}
		return result.toString();
	}

	public static IMethodInfo getNParametersMethod(List<IMethodInfo> methods, int n) {
		for (IMethodInfo c : methods) {
			if (c.getParameters().size() == n) {
				return c;
			}
		}
		return null;
	}

	public static IMethodInfo getZeroParameterMethod(List<IMethodInfo> methods) {
		return getNParametersMethod(methods, 0);
	}

	public static <T extends IMethodInfo> T findMethodBySignature(List<T> methods, String signature) {
		for (T method : methods) {
			String candidateMethodSignature = method.getSignature();
			if (candidateMethodSignature.equals(signature)) {
				return method;
			}
		}
		return null;
	}

	public static <T extends IInfo> T findInfoByName(List<T> infos, String name) {
		for (T info : infos) {
			if (info.getName().equals(name)) {
				return info;
			}
		}
		return null;
	}

	public static <T extends IInfo> T findInfoByCaption(List<T> infos, String caption) {
		for (T info : infos) {
			if (info.getCaption().equals(caption)) {
				return info;
			}
		}
		return null;
	}

	public static String formatRequiredParameterList(List<IParameterInfo> parameters) {
		StringBuilder result = new StringBuilder();
		int iRequiredParam = 0;
		for (IParameterInfo param : parameters) {
			if (param.isHidden()) {
				continue;
			}
			if (iRequiredParam > 0) {
				if (iRequiredParam == parameters.size() - 1) {
					result.append(" AND ");
				} else {
					result.append(", ");
				}
			}
			String paramCaption = param.getCaption();
			if (paramCaption.length() > 0) {
				result.append(paramCaption);
			} else {
				result.append(param.getType().getCaption());
			}
			iRequiredParam++;
		}
		return result.toString();
	}

	public static <M extends Member> M findJavaMemberByName(M[] members, String memberName) {
		for (M member : members) {
			if (member.getName().equals(memberName)) {
				return member;
			}
		}
		return null;
	}

	public static void sortFields(List<IFieldInfo> list) {
		Collections.sort(list, new Comparator<IFieldInfo>() {
			@Override
			public int compare(IFieldInfo f1, IFieldInfo f2) {
				int result;

				result = MiscUtils.compareNullables(f1.getCategory(), f2.getCategory());
				if (result != 0) {
					return result;
				}

				result = MiscUtils.compareNullables(f1.getType().getName().toUpperCase(),
						f2.getType().getName().toUpperCase());
				if (result != 0) {
					return result;
				}

				result = MiscUtils.compareNullables(f1.getName(), f2.getName());
				if (result != 0) {
					return result;
				}

				return 0;
			}
		});
	}

	public static void sortMethods(List<IMethodInfo> list) {
		Collections.sort(list, new Comparator<IMethodInfo>() {
			@Override
			public int compare(IMethodInfo m1, IMethodInfo m2) {
				int result;

				result = MiscUtils.compareNullables(m1.getCategory(), m2.getCategory());
				if (result != 0) {
					return result;
				}

				List<String> parameterTypeNames1 = new ArrayList<String>();
				for (IParameterInfo param : m1.getParameters()) {
					parameterTypeNames1.add(param.getType().getName());
				}
				Collections.sort(parameterTypeNames1);
				List<String> parameterTypeNames2 = new ArrayList<String>();
				for (IParameterInfo param : m2.getParameters()) {
					parameterTypeNames2.add(param.getType().getName());
				}
				Collections.sort(parameterTypeNames2);
				result = MiscUtils.stringJoin(parameterTypeNames1, "\n")
						.compareTo(MiscUtils.stringJoin(parameterTypeNames2, "\n"));
				if (result != 0) {
					return result;
				}

				String returnTypeName1;
				{
					if (m1.getReturnValueType() == null) {
						returnTypeName1 = "";
					} else {
						returnTypeName1 = m1.getReturnValueType().getName();
					}
				}
				String returnTypeName2;
				{
					if (m2.getReturnValueType() == null) {
						returnTypeName2 = "";
					} else {
						returnTypeName2 = m2.getReturnValueType().getName();
					}
				}
				result = MiscUtils.compareNullables(returnTypeName1, returnTypeName2);
				if (result != 0) {
					return result;
				}

				result = MiscUtils.compareNullables(m1.getName(), m2.getName());
				if (result != 0) {
					return result;
				}

				return 0;
			}
		});
	}

	public static String composeMessage(String contextMessage, String localMessage) {
		if ((contextMessage == null) || (contextMessage.length() == 0)) {
			return localMessage;
		}
		return contextMessage + " - " + localMessage;
	}

	public static Object createDefaultInstance(ITypeInfo type) {
		return createDefaultInstance(type, true);
	}

	public static Object createDefaultInstance(ITypeInfo type, boolean subTypeInstanceAllowed) {
		try {
			if (!type.isConcrete()) {
				if (subTypeInstanceAllowed) {
					if (ReflectionUIUtils.hasPolymorphicInstanceSubTypes(type)) {
						List<ITypeInfo> subTypes = type.getPolymorphicInstanceSubTypes();
						for (ITypeInfo subType : subTypes) {
							try {
								return createDefaultInstance(subType, true);
							} catch (Throwable ignore) {
							}
						}
					}
				}
				throw new ReflectionUIError(
						"Cannot instanciate abstract (or " + Object.class.getSimpleName() + ") type");

			}

			IMethodInfo zeroParamConstructor = getZeroParameterMethod(type.getConstructors());
			if (zeroParamConstructor != null) {
				return zeroParamConstructor.invoke(null, new InvocationData(null, zeroParamConstructor));
			}
			for (IMethodInfo constructor : type.getConstructors()) {
				InvocationData invocationData = new InvocationData(null, constructor);
				if (!ReflectionUIUtils.requiresParameterValue(constructor)) {
					return constructor.invoke(null, invocationData);
				}
			}
			throw new ReflectionUIError("Default constructor not found");
		} catch (Throwable t) {
			throw new ReflectionUIError(
					"Failed to create a default instance of type '" + type.getName() + "': " + t.toString(), t);

		}
	}

	public static boolean canCreateDefaultInstance(ITypeInfo type, boolean subTypeInstanceAllowed) {
		if (!type.isConcrete()) {
			if (subTypeInstanceAllowed) {
				if (ReflectionUIUtils.hasPolymorphicInstanceSubTypes(type)) {
					List<ITypeInfo> subTypes = type.getPolymorphicInstanceSubTypes();
					for (ITypeInfo subType : subTypes) {
						return canCreateDefaultInstance(subType, true);
					}
				}
			}
			return false;
		}

		IMethodInfo zeroParamConstructor = getZeroParameterMethod(type.getConstructors());
		if (zeroParamConstructor != null) {
			return true;
		}
		for (IMethodInfo constructor : type.getConstructors()) {
			if (!ReflectionUIUtils.requiresParameterValue(constructor)) {
				return true;
			}
		}
		return false;
	}

	public static <T extends IInfo> Comparator<T> getInfosComparator(final List<String> expectedOrderSpecification,
			List<T> initialOrder) {
		final List<T> initialOrderCopy = new ArrayList<T>(initialOrder);
		return new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				if (expectedOrderSpecification.contains(o1.getName())
						&& expectedOrderSpecification.contains(o2.getName())) {
					Integer index1 = new Integer(expectedOrderSpecification.indexOf(o1.getName()));
					Integer index2 = new Integer(expectedOrderSpecification.indexOf(o2.getName()));
					return index1.compareTo(index2);
				}
				if (expectedOrderSpecification.contains(o1.getName())) {
					return 1;
				}
				if (expectedOrderSpecification.contains(o2.getName())) {
					return -1;
				}
				Integer index1 = new Integer(initialOrderCopy.indexOf(o1));
				Integer index2 = new Integer(initialOrderCopy.indexOf(o2));
				return index1.compareTo(index2);
			}
		};
	}

	public static boolean hasPolymorphicInstanceSubTypes(ITypeInfo type) {
		if (PolymorphicTypeOptionsFactory.isRecursivityDetected(type)) {
			return false;
		}
		List<ITypeInfo> polyTypes = type.getPolymorphicInstanceSubTypes();
		return (polyTypes != null) && (polyTypes.size() > 0);
	}

	public static String toString(ReflectionUI reflectionUI, Object object) {
		if (object == null) {
			return "";
		}
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type instanceof IListTypeInfo) {
			IListTypeInfo listType = (IListTypeInfo) type;
			List<String> result = new ArrayList<String>();
			for (Object item : listType.toArray(object)) {
				result.add(toString(reflectionUI, item));
			}
			return MiscUtils.stringJoin(result, ", ");
		} else {
			return type.toString(object);
		}
	}

	public static ResourcePath getIconImagePath(ReflectionUI reflectionUI, Object object) {
		if (object == null) {
			return null;
		}
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		return type.getIconImagePath();
	}

	public static boolean canCopy(ReflectionUI reflectionUI, Object object) {
		if (object == null) {
			return false;
		}
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		return type.canCopy(object);
	}

	public static Object copy(ReflectionUI reflectionUI, Object object) {
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		return type.copy(object);
	}

	public static void copyFieldValues(ReflectionUI reflectionUI, Object src, Object dst, boolean deeply) {
		ITypeInfo srcType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(src));
		ITypeInfo dstType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(dst));
		for (IFieldInfo dstField : dstType.getFields()) {
			if (dstField.isGetOnly()) {
				continue;
			}
			IFieldInfo srcField = ReflectionUIUtils.findInfoByName(srcType.getFields(), dstField.getName());
			if (srcField == null) {
				continue;
			}
			Object srcFieldValue = srcField.getValue(src);
			if (srcFieldValue == null) {
				dstField.setValue(dst, null);
			} else {
				ITypeInfo fieldValueType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(srcFieldValue));
				if (deeply && !fieldValueType.isImmutable()) {
					Object dstFieldValue;
					if (fieldValueType instanceof IListTypeInfo) {
						Object[] srcArray = ((IListTypeInfo) fieldValueType).toArray(srcFieldValue);
						if (((IListTypeInfo) fieldValueType).canInstanciateFromArray()) {
							Object[] dstArray = new Object[srcArray.length];
							int i = 0;
							for (Object srcItem : srcArray) {
								ITypeInfo itemType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(srcItem));
								if (itemType.isImmutable()) {
									dstArray[i] = srcItem;
								} else {
									Object dstItem = ReflectionUIUtils.createDefaultInstance(itemType, false);
									copyFieldValues(reflectionUI, srcItem, dstItem, true);
									dstArray[i] = dstItem;
								}
								i++;
							}
							dstFieldValue = ((IListTypeInfo) fieldValueType).fromArray(dstArray);
						} else if (((IListTypeInfo) fieldValueType).canReplaceContent()) {
							dstFieldValue = ReflectionUIUtils.createDefaultInstance(fieldValueType, false);
							((IListTypeInfo) fieldValueType).replaceContent(dstFieldValue, srcArray);
						} else {
							throw new ReflectionUIError("Cannot copy list value: '" + srcFieldValue + "'");
						}
					} else {
						dstFieldValue = ReflectionUIUtils.createDefaultInstance(fieldValueType, false);
						copyFieldValues(reflectionUI, srcFieldValue, dstFieldValue, true);
					}
					dstField.setValue(dst, dstFieldValue);
				} else {
					dstField.setValue(dst, srcFieldValue);
				}
			}
		}
	}

	public static void checkInstance(ITypeInfo type, Object object) {
		if (!type.supportsInstance(object)) {
			throw new ReflectionUIError();
		}
	}

	public static boolean finalizeModifications(final ModificationStack parentModificationStack,
			final ModificationStack currentModificationsStack, boolean currentModificationsAccepted,
			final ValueReturnMode valueReturnMode, final boolean valueReplaced, ITransactionInfo valueTransaction,
			final IModification committingModification, String parentModificationTitle, boolean fakeParentModification,
			final Listener<String> debugLogListener, Listener<String> errorLogListener) {

		if (currentModificationsStack == null) {
			throw new ReflectionUIError();
		}

		if (!mayModificationsHaveImpact(false, valueReturnMode, (committingModification != null))) {
			return false;
		}

		if (currentModificationsStack.isInitial()) {
			if (!currentModificationsStack.isExhaustive()) {
				if (parentModificationStack != null) {
					parentModificationStack.push(IModification.FAKE_MODIFICATION);
				}
				return true;
			}
			return false;
		}

		boolean parentObjectImpacted = false;
		if (currentModificationsAccepted) {
			if (parentModificationStack != null) {
				parentObjectImpacted = parentModificationStack.insideComposite(parentModificationTitle, UndoOrder.FIFO,
						new Accessor<Boolean>() {
							@Override
							public Boolean get() {
								if (valueReturnMode != ValueReturnMode.CALCULATED) {
									if (currentModificationsStack.wasInvalidated()) {
										parentModificationStack.invalidate();
									} else {
										parentModificationStack
												.push(currentModificationsStack.toCompositeUndoModification(null));
									}
								}
								if (valueTransaction != null) {
									valueTransaction.commit();
								}
								if ((valueReturnMode != ValueReturnMode.DIRECT_OR_PROXY) || valueReplaced) {
									if (committingModification != null) {
										if (debugLogListener != null) {
											debugLogListener.handle("Executing " + committingModification);
										}
										parentModificationStack.apply(committingModification);
									}
								}
								return true;
							}
						}, fakeParentModification);
			}
		} else {
			if (valueReturnMode != ValueReturnMode.CALCULATED) {
				if (valueTransaction != null) {
					valueTransaction.rollback();
				} else {
					if (!currentModificationsStack.wasInvalidated()) {
						if (debugLogListener != null) {
							debugLogListener.handle("Undoing " + currentModificationsStack);
						}
						currentModificationsStack.undoAll();
					} else {
						if (errorLogListener != null) {
							errorLogListener.handle(
									"Cannot undo invalidated sub-modification stack: " + currentModificationsStack);
						}
						if (parentModificationStack != null) {
							if (debugLogListener != null) {
								debugLogListener.handle(
										"=> Invalidating parent modification stack: " + parentModificationStack);
							}
							parentModificationStack.invalidate();
						}
						parentObjectImpacted = true;
					}
				}
			}
		}
		return parentObjectImpacted;
	}

	public static boolean mayModificationsHaveImpact(boolean valueKnownAsImmutable, ValueReturnMode valueReturnMode,
			boolean canCommit) {
		if ((valueReturnMode != ValueReturnMode.CALCULATED) && !valueKnownAsImmutable) {
			return true;
		}
		if (canCommit) {
			return true;
		}
		return false;
	}

	public static boolean isValueImmutable(ReflectionUI reflectionUI, Object value) {
		if (value == null) {
			return true;
		}
		ITypeInfo valueType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(value));
		return valueType.isImmutable();
	}

	public static void setFieldValueThroughModificationStack(IFieldControlData data, Object newValue,
			ModificationStack modifStack) {
		if (data.isTransient()) {
			try {
				data.setValue(newValue);
			} finally {
				modifStack.apply(IModification.FAKE_MODIFICATION);
			}
		} else {
			FieldControlDataModification modif = new FieldControlDataModification(data, newValue);
			try {
				modifStack.apply(modif);
			} catch (Throwable t) {
				modifStack.invalidate();
				throw new ReflectionUIError(t);
			}
		}
	}

	public static Object invokeMethodThroughModificationStack(IMethodControlData data, InvocationData invocationData,
			ModificationStack modifStack) {
		if (data.isReadOnly()) {
			return data.invoke(invocationData);
		} else {
			final Runnable nextInvocationUndoJob = data.getNextInvocationUndoJob(invocationData);
			if (nextInvocationUndoJob != null) {
				final Object[] resultHolder = new Object[1];
				data = new MethodControlDataProxy(data) {
					@Override
					public Object invoke(InvocationData invocationData) {
						return resultHolder[0] = super.invoke(invocationData);
					}
				};
				MethodControlDataModification modif = new MethodControlDataModification(data, invocationData) {

					@Override
					protected Runnable createUndoJob() {
						return nextInvocationUndoJob;
					}

				};
				try {
					modifStack.apply(modif);
				} catch (Throwable t) {
					modifStack.invalidate();
					throw new ReflectionUIError(t);
				}
				return resultHolder[0];
			} else {
				try {
					Object result = data.invoke(invocationData);
					return result;
				} finally {
					modifStack.invalidate();
				}
			}
		}
	}

	public static String getDefaultListTypeCaption(IListTypeInfo listType) {
		ITypeInfo itemType = listType.getItemType();
		if (itemType == null) {
			return "List";
		} else {
			return "List of " + itemType.getCaption() + " elements";
		}
	}

	public static Listener<String> getDebugLogListener(final ReflectionUI reflectionUI) {
		return new Listener<String>() {
			@Override
			public void handle(String event) {
				reflectionUI.logDebug(event);
			}
		};
	}

	public static Listener<String> getErrorLogListener(final ReflectionUI reflectionUI) {
		return new Listener<String>() {
			@Override
			public void handle(String event) {
				reflectionUI.logError(event);
			}
		};
	}

	public static String getContructorDescription(IMethodInfo ctor) {
		StringBuilder result = new StringBuilder(ctor.getCaption());
		if (!ReflectionUIUtils.requiresParameterValue(ctor)) {
			result.append(" - by default");
		} else {
			result.append(" - specify ");
			result.append(formatRequiredParameterList(ctor.getParameters()));
		}
		return result.toString();
	}

	public static MenuElementKind getMenuElementKind(IMenuElementInfo element) {
		if (element instanceof MenuInfo) {
			return MenuElementKind.MENU;
		} else if (element instanceof MenuItemCategory) {
			return MenuElementKind.ITEM_CATEGORY;
		} else if (element instanceof AbstractMenuItemInfo) {
			return MenuElementKind.ITEM;
		} else {
			throw new ReflectionUIError();
		}
	}

	public static boolean equalsAccordingInfos(Object o1, Object o2, ReflectionUI reflectionUI,
			IInfoFilter infoFilter) {
		if (o1 == o2) {
			return true;
		}
		if ((o1 == null) || (o2 == null)) {
			return false;
		}
		if (ReflectionUtils.isPrimitiveClassOrWrapperOrString(o1.getClass())) {
			return o1.equals(o2);
		}
		ITypeInfo type1 = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(o1));
		ITypeInfo type2 = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(o2));
		if (!type1.equals(type2)) {
			return false;
		}
		if (type1.isPrimitive()) {
			return o1.equals(o2);
		}
		for (IFieldInfo field : type1.getFields()) {
			if (infoFilter.excludeField(field)) {
				continue;
			}
			Object value1 = field.getValue(o1);
			Object value2 = field.getValue(o2);
			if (!equalsAccordingInfos(value1, value2, reflectionUI, infoFilter)) {
				return false;
			}
		}
		if (type1 instanceof IListTypeInfo) {
			IListTypeInfo listType = (IListTypeInfo) type1;
			Object[] rawList1 = listType.toArray(o1);
			Object[] rawList2 = listType.toArray(o2);
			if (rawList1.length != rawList2.length) {
				return false;
			}
			for (int i = 0; i < rawList1.length; i++) {
				Object item1 = rawList1[i];
				Object item2 = rawList2[i];
				if (!equalsAccordingInfos(item1, item2, reflectionUI, infoFilter)) {
					return false;
				}
			}
		}
		if (type1 instanceof IEnumerationTypeInfo) {
			IEnumerationTypeInfo enumType = (IEnumerationTypeInfo) type1;
			IEnumerationItemInfo valueInfo1 = enumType.getValueInfo(o1);
			IEnumerationItemInfo valueInfo2 = enumType.getValueInfo(o2);
			if (!valueInfo1.getName().equals(valueInfo2.getName())) {
				return false;
			}
		}
		return true;
	}

	public static String formatMethodControlCaption(String methodCaption, List<IParameterInfo> methodParameters) {
		if (methodCaption.length() > 0) {
			if (requiresParameterValue(methodParameters)) {
				methodCaption += "...";
			}
		}
		return methodCaption;
	}

	public static String formatMethodControlTooltipText(String methodCaption, String methodOnlineHelp,
			List<IParameterInfo> methodParameters) {
		if (methodOnlineHelp != null) {
			return methodOnlineHelp;
		} else {
			if (ReflectionUIUtils.requiresParameterValue(methodParameters)) {
				String toolTipText = formatMethodControlCaption(methodCaption, methodParameters);
				if (toolTipText.length() > 0) {
					toolTipText += "\n";
				}
				toolTipText += "Parameter(s): " + ReflectionUIUtils.formatRequiredParameterList(methodParameters);
				return toolTipText;
			} else {
				return null;
			}
		}
	}

	public static List<IMenuElementPosition> getAncestors(IMenuElementPosition elementPosition) {
		List<IMenuElementPosition> result = new ArrayList<IMenuElementPosition>();
		while (elementPosition.getParent() != null) {
			result.add(elementPosition.getParent());
			elementPosition = elementPosition.getParent();
		}
		return result;
	}

	public static Runnable getNextUpdateUndoJob(final IFieldControlData data, Object newValue) {
		Runnable result = data.getNextUpdateCustomUndoJob(newValue);
		if (result == null) {
			result = createNextUpdateDefaultUndoJob(data);
		}
		return result;
	}

	public static Runnable getNextUpdateUndoJob(Object object, IFieldInfo field, Object newValue) {
		Runnable result = field.getNextUpdateCustomUndoJob(object, newValue);
		if (result == null) {
			result = createNextUpdateDefaultUndoJob(object, field);
		}
		return result;
	}

	public static Runnable createNextUpdateDefaultUndoJob(final IFieldControlData data) {
		final Object oldValue = data.getValue();
		return new Runnable() {
			@Override
			public void run() {
				data.setValue(oldValue);
			}
		};
	}

	public static Runnable createNextUpdateDefaultUndoJob(final Object object, final IFieldInfo field) {
		final Object oldValue = field.getValue(object);
		return new Runnable() {
			@Override
			public void run() {
				field.setValue(object, oldValue);
			}
		};
	}

	public static boolean requiresParameterValue(IMethodInfo method) {
		return requiresParameterValue(method.getParameters());
	}

	public static boolean requiresParameterValue(List<IParameterInfo> parameters) {
		for (IParameterInfo param : parameters) {
			if (!param.isHidden()) {
				return true;
			}
		}
		return false;
	}

	public static void setFieldControlPluginIdentifier(Map<String, Object> specificProperties, String identifier) {
		specificProperties.put(IFieldControlPlugin.CHOSEN_PROPERTY_KEY, identifier);
	}

	public static String getFieldControlPluginIdentifier(Map<String, Object> specificProperties) {
		return (String) specificProperties.get(IFieldControlPlugin.CHOSEN_PROPERTY_KEY);
	}

	public static void setFieldControlPluginConfiguration(Map<String, Object> specificProperties, String identifier,
			Serializable controlConfiguration) {
		if (controlConfiguration == null) {
			specificProperties.remove(identifier);
		} else {
			specificProperties.put(identifier, IOUtils.serializeToHexaText(controlConfiguration));
		}
	}

	public static Serializable getFieldControlPluginConfiguration(Map<String, Object> specificProperties,
			String identifier) {
		String text = (String) specificProperties.get(identifier);
		if (text == null) {
			return null;
		}
		return (Serializable) IOUtils.deserializeFromHexaText(text);
	}

	public static InfoCategory getCategory(IInfo info) {
		if (info instanceof IFieldInfo) {
			return ((IFieldInfo) info).getCategory();
		} else if (info instanceof IMethodInfo) {
			return ((IMethodInfo) info).getCategory();
		} else {
			return null;
		}
	}

	public static boolean isTypeEmpty(ITypeInfo type, IInfoFilter infoFilter) {
		List<IFieldInfo> fields = type.getFields();
		List<IMethodInfo> methods = type.getMethods();

		fields = new ArrayList<IFieldInfo>(fields);
		for (Iterator<IFieldInfo> it = fields.iterator(); it.hasNext();) {
			IFieldInfo field = it.next();
			if (field.isHidden() || infoFilter.excludeField(field)) {
				it.remove();
			}
		}

		methods = new ArrayList<IMethodInfo>(methods);
		for (Iterator<IMethodInfo> it = methods.iterator(); it.hasNext();) {
			IMethodInfo method = it.next();
			if (method.isHidden() || infoFilter.excludeMethod(method)) {
				it.remove();
			}
		}

		return (fields.size() + methods.size()) == 0;
	}

	public static boolean isConstructor(IMethodInfo method) {
		return method.getName().length() == 0;
	}

	public static String buildNameFromMethodSignature(String baseMethodSignature) {
		String baseMethodReturnTypeName = ReflectionUIUtils
				.extractMethodReturnTypeNameFromSignature(baseMethodSignature);
		String baseMethodName = ReflectionUIUtils.extractMethodNameFromSignature(baseMethodSignature);
		String[] baseMethodParameterTypeNames = ReflectionUIUtils
				.extractMethodParameterTypeNamesFromSignature(baseMethodSignature);
		return baseMethodReturnTypeName + "-" + ((baseMethodName.length() == 0) ? "<constructor>" : baseMethodName)
				+ ((baseMethodParameterTypeNames.length == 0) ? ""
						: ("-" + MiscUtils.stringJoin(Arrays.asList(baseMethodParameterTypeNames), "-")));
	}

}
