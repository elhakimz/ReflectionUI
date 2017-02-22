package xy.reflect.ui.control.swing;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.google.common.collect.MapMaker;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.data.IControlData;
import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.control.swing.SwingRenderer.MethodControlPlaceHolder;
import xy.reflect.ui.info.DesktopSpecificProperty;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.BooleanTypeInfo;
import xy.reflect.ui.info.type.custom.FileTypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.column.IColumnInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.util.InfoCustomizations;
import xy.reflect.ui.info.type.util.TypeCastFactory;
import xy.reflect.ui.info.type.util.TypeInfoProxyFactory;
import xy.reflect.ui.info.type.util.InfoCustomizations.AbstractInfoCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.ColumnCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.EnumerationCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.FieldCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.ListCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.MethodCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.TypeCustomization;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.IModificationListener;
import xy.reflect.ui.undo.ModificationProxy;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.FileUtils;
import xy.reflect.ui.util.ResourcePath;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.SystemProperties;

@SuppressWarnings("unused")
public class SwingCustomizer extends SwingRenderer {

	protected CustomizationTools customizationTools;
	protected InfoCustomizations infoCustomizations;
	protected String infoCustomizationsOutputFilePath;
	protected CustomizationOptions customizationOptions;

	public SwingCustomizer(ReflectionUI reflectionUI, InfoCustomizations infoCustomizations,
			String infoCustomizationsOutputFilePath) {
		super(reflectionUI);
		this.customizationTools = createCustomizationTools();
		this.customizationOptions = initializeCustomizationOptions();
		this.infoCustomizations = infoCustomizations;
		this.infoCustomizationsOutputFilePath = infoCustomizationsOutputFilePath;
		if (infoCustomizationsOutputFilePath != null) {
			File file = new File(infoCustomizationsOutputFilePath);
			if (!file.exists()) {
				try {
					infoCustomizations.saveToFile(file);
				} catch (IOException e) {
					throw new ReflectionUIError(e);
				}
			}
		}
	}

	protected CustomizationOptions initializeCustomizationOptions() {
		return new CustomizationOptions();
	}

	protected CustomizationTools createCustomizationTools() {
		return new CustomizationTools();
	}

	@Override
	public void fillForm(JPanel form) {
		Object object = getObjectByForm().get(form);
		if (areCustomizationsEditable(object)) {
			JPanel mainCustomizationsControl = new JPanel();
			mainCustomizationsControl.setLayout(new BorderLayout());
			mainCustomizationsControl.add(customizationTools.createTypeInfoCustomizer(object), BorderLayout.CENTER);
			mainCustomizationsControl.add(customizationTools.createSaveControl(), BorderLayout.EAST);
			form.add(SwingRendererUtils.flowInLayout(mainCustomizationsControl, FlowLayout.CENTER), BorderLayout.NORTH);
		}
		super.fillForm(form);
	}

	@Override
	public FieldControlPlaceHolder createFieldControlPlaceHolder(Object object, IFieldInfo field) {
		return new FieldControlPlaceHolder(object, field) {
			private static final long serialVersionUID = 1L;
			protected Component infoCustomizationsComponent;

			@Override
			public void refreshUI(boolean recreate) {
				if (areCustomizationsEditable(object)) {
					refreshInfoCustomizationsControl();
				}
				super.refreshUI(recreate);
			}

			protected void refreshInfoCustomizationsControl() {
				if (infoCustomizationsComponent == null) {
					infoCustomizationsComponent = customizationTools.createFieldInfoCustomizer(this);
					add(infoCustomizationsComponent, BorderLayout.EAST);
					SwingRendererUtils.handleComponentSizeChange(this);
				} else {
					remove(infoCustomizationsComponent);
					infoCustomizationsComponent = null;
					refreshInfoCustomizationsControl();
				}
			}

		};
	}

	@Override
	public MethodControlPlaceHolder createMethodControlPlaceHolder(Object object, IMethodInfo method) {
		return new MethodControlPlaceHolder(object, method) {
			private static final long serialVersionUID = 1L;
			protected Component infoCustomizationsComponent;

			@Override
			public void refreshUI(boolean recreate) {
				if (areCustomizationsEditable(object)) {
					refreshInfoCustomizationsControl();
				}
				super.refreshUI(recreate);
			}

			protected void refreshInfoCustomizationsControl() {
				if (infoCustomizationsComponent == null) {
					infoCustomizationsComponent = customizationTools.createMethodInfoCustomizer(this);
					add(infoCustomizationsComponent, BorderLayout.WEST);
					SwingRendererUtils.handleComponentSizeChange(this);
				} else {
					remove(infoCustomizationsComponent);
					infoCustomizationsComponent = null;
					refreshInfoCustomizationsControl();
				}
			}
		};
	}

	protected ImageIcon getCustomizationsIcon() {
		return SwingRendererUtils.CUSTOMIZATION_ICON;
	}

	protected boolean areCustomizationsEditable(Object object) {
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (!SystemProperties.areInfoCustomizationToolsAuthorized()) {
			return false;
		}
		if (!infoCustomizations
				.equals(type.getSpecificProperties().get(InfoCustomizations.ACTIVE_CUSTOMIZATIONS_PROPERTY_KEY))) {
			return false;
		}
		if (infoCustomizationsOutputFilePath == null) {
			return false;
		}
		if (Boolean.TRUE.equals(customizationOptions.areHiddenFor(type.getName()))) {
			return false;
		}
		return true;
	}

	public class CustomizationTools {
		protected SwingRenderer customizationToolsRenderer;
		protected ReflectionUI customizationToolsUI;
		protected InfoCustomizations customizationToolsCustomizations;

		public CustomizationTools() {
			customizationToolsCustomizations = new InfoCustomizations();
			URL url = ReflectionUI.class.getResource("resource/customizations-tools.icu");
			try {
				File customizationsFile = FileUtils.getStreamAsFile(url.openStream());
				String customizationsFilePath = customizationsFile.getPath();
				customizationToolsCustomizations.loadFromFile(new File(customizationsFilePath));
			} catch (IOException e) {
				throw new ReflectionUIError(e);
			}
			customizationToolsUI = createCustomizationToolsUI();
			customizationToolsRenderer = createCustomizationToolsRenderer();

		}

		public SwingRenderer getCustomizationToolsRenderer() {
			return customizationToolsRenderer;
		}

		public ReflectionUI getCustomizationToolsUI() {
			return customizationToolsUI;
		}

		public InfoCustomizations getCustomizationToolsCustomizations() {
			return customizationToolsCustomizations;
		}

		protected JButton createToolAccessButton(ImageIcon imageIcon) {
			final JButton result = new JButton(imageIcon);
			result.setPreferredSize(new Dimension(result.getPreferredSize().height, result.getPreferredSize().height));
			return result;
		}

		protected SwingRenderer createCustomizationToolsRenderer() {
			if (SystemProperties.isInfoCustomizationToolsCustomizationAllowed()) {
				String customizationToolsCustomizationsOutputFilePath = System
						.getProperty(SystemProperties.INFO_CUSTOMIZATION_TOOLS_CUSTOMIZATIONS_FILE_PATH);
				return new SwingCustomizer(customizationToolsUI, customizationToolsCustomizations,
						customizationToolsCustomizationsOutputFilePath) {

					@Override
					protected CustomizationTools createCustomizationTools() {
						return new CustomizationTools() {

							@Override
							protected SwingRenderer createCustomizationToolsRenderer() {
								return new SwingRenderer(this.customizationToolsUI);
							}

						};
					}

					@Override
					protected CustomizationOptions initializeCustomizationOptions() {
						return new CustomizationOptions();
					}

				};
			} else {
				return new SwingRenderer(customizationToolsUI);
			}
		}

		protected ReflectionUI createCustomizationToolsUI() {
			return new ReflectionUI() {

				ReflectionUI thisReflectionUI = this;

				@Override
				public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
					ITypeInfo result = super.getTypeInfo(typeSource);
					result = new TypeInfoProxyFactory() {
						@Override
						public String toString() {
							return CustomizationTools.class.getName() + TypeInfoProxyFactory.class.getSimpleName();
						}

						@Override
						protected List<IFieldInfo> getFields(ITypeInfo type) {
							if (type.getName().equals(TypeCustomization.class.getName())) {
								List<IFieldInfo> result = new ArrayList<IFieldInfo>(super.getFields(type));
								result.add(getIconImageFileField());
								return result;
							} else if (type.getName().equals(FieldCustomization.class.getName())) {
								List<IFieldInfo> result = new ArrayList<IFieldInfo>(super.getFields(type));
								result.add(getEmbeddedFormCreationField());
								return result;
							} else if (type.getName().equals(MethodCustomization.class.getName())) {
								List<IFieldInfo> result = new ArrayList<IFieldInfo>(super.getFields(type));
								result.add(getIconImageFileField());
								return result;
							} else {
								return super.getFields(type);
							}
						}

					}.get(result);
					result = customizationToolsCustomizations.get(thisReflectionUI, result);
					return result;
				}

			};
		}

		protected IFieldInfo getEmbeddedFormCreationField() {
			return new IFieldInfo() {

				@Override
				public String getName() {
					return "expandSubForm";
				}

				@Override
				public String getCaption() {
					return "Expand Sub-form";
				}

				@Override
				public String getOnlineHelp() {
					return null;
				}

				@Override
				public String getNullValueLabel() {
					return null;
				}


				@Override
				public Map<String, Object> getSpecificProperties() {
					return Collections.emptyMap();
				}

				@Override
				public ITypeInfo getType() {
					return new BooleanTypeInfo(customizationToolsRenderer.getReflectionUI(), boolean.class);
				}

				@Override
				public Object getValue(Object object) {
					FieldCustomization f = (FieldCustomization) object;
					return DesktopSpecificProperty
							.isSubFormExpanded(DesktopSpecificProperty.accessCustomizationsProperties(f));
				}

				@Override
				public void setValue(Object object, Object value) {
					FieldCustomization f = (FieldCustomization) object;
					DesktopSpecificProperty.setSubFormExpanded(
							DesktopSpecificProperty.accessCustomizationsProperties(f), (Boolean) value);
				}

				@Override
				public Runnable getCustomUndoUpdateJob(Object object, Object value) {
					return null;
				}

				@Override
				public Object[] getValueOptions(Object object) {
					return null;
				}

				@Override
				public boolean isNullable() {
					return false;
				}

				@Override
				public boolean isGetOnly() {
					return false;
				}

				@Override
				public ValueReturnMode getValueReturnMode() {
					return ValueReturnMode.COPY;
				}

				@Override
				public InfoCategory getCategory() {
					return null;
				}

				@Override
				public String toString() {
					return getCaption();
				}

			};
		}

		protected IFieldInfo getIconImageFileField() {
			return new IFieldInfo() {

				@Override
				public String getName() {
					return "iconImageFile";
				}

				@Override
				public String getCaption() {
					return "Icon Image File";
				}

				@Override
				public String getNullValueLabel() {
					return null;
				}


				@Override
				public String getOnlineHelp() {
					return null;
				}

				@Override
				public Map<String, Object> getSpecificProperties() {
					return Collections.emptyMap();
				}

				@Override
				public ITypeInfo getType() {
					return customizationToolsRenderer.getReflectionUI()
							.getTypeInfo(new JavaTypeInfoSource(ResourcePath.class));
				}

				@Override
				public Object[] getValueOptions(Object object) {
					return null;
				}

				@Override
				public Object getValue(Object object) {
					AbstractInfoCustomization c = (AbstractInfoCustomization) object;
					String path = DesktopSpecificProperty
							.getIconImageFilePath(DesktopSpecificProperty.accessCustomizationsProperties(c));
					if (path == null) {
						path = "";
					}
					return new ResourcePath(path);
				}

				@Override
				public void setValue(Object object, Object value) {
					value = ((ResourcePath) value).getSpecification();
					AbstractInfoCustomization c = (AbstractInfoCustomization) object;
					DesktopSpecificProperty.setIconImageFilePath(
							DesktopSpecificProperty.accessCustomizationsProperties(c), ((String) value));
				}

				@Override
				public Runnable getCustomUndoUpdateJob(Object object, Object value) {
					return null;
				}

				@Override
				public boolean isNullable() {
					return false;
				}

				@Override
				public boolean isGetOnly() {
					return false;
				}

				@Override
				public ValueReturnMode getValueReturnMode() {
					return ValueReturnMode.COPY;
				}

				@Override
				public InfoCategory getCategory() {
					return null;
				}

				@Override
				public String toString() {
					return getCaption();
				}
			};
		}

		protected JButton createSaveControl() {
			final JButton result = createToolAccessButton(SwingRendererUtils.SAVE_ALL_ICON);
			result.setToolTipText(customizationToolsRenderer.prepareStringToDisplay("Save all the customizations"));
			result.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final File file = new File(infoCustomizationsOutputFilePath);
					try {
						infoCustomizations.saveToFile(file);
					} catch (IOException e1) {
						customizationToolsRenderer.handleExceptionsFromDisplayedUI(result, e1);
					}
				}
			});
			return result;
		}

		protected Component createTypeInfoCustomizer(final Object object) {
			final ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
			final JButton result = createToolAccessButton(getCustomizationsIcon());
			result.setToolTipText(customizationToolsRenderer
					.prepareStringToDisplay("Customize the type <" + type.getName() + "> display"));
			final TypeCustomization t = infoCustomizations.getTypeCustomization(type.getName(), true);
			result.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final JPopupMenu popupMenu = new JPopupMenu();
					popupMenu.add(new AbstractAction(prepareStringToDisplay("Type Options...")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							openTypeCustomizationDialog(result, t);
						}
					});
					popupMenu.add(new AbstractAction(prepareStringToDisplay("Refresh")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							updateUI(type.getName());
						}
					});
					popupMenu.add(new AbstractAction(prepareStringToDisplay("Lock")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							hideCustomizationTools(result, type.getName());
						}
					});

					showMenu(popupMenu, result);
				}
			});
			return result;
		}

		protected void hideCustomizationTools(Component activatorComponent, String typeName) {
			customizationOptions.hideFor(typeName);
		}

		protected void openTypeCustomizationDialog(Component activatorComponent, final TypeCustomization t) {
			openCustomizationEditor(activatorComponent, t, t.getTypeName());

		}

		protected void openCustomizationEditor(Component activatorComponent, Object customization,
				final String impactedTypeName) {
			final ObjectDialogBuilder dialogBuilder = new ObjectDialogBuilder(customizationToolsRenderer,
					activatorComponent, customization);
			dialogBuilder.setIconImage(getCustomizationsIcon().getImage());
			dialogBuilder.setCancellable(true);
			dialogBuilder.build();
			dialogBuilder.getModificationStack()
					.addListener(getCustomizedWindowsReloadingAdviser(dialogBuilder.getBuiltDialog()));
			customizationToolsRenderer.showDialog(dialogBuilder.getBuiltDialog(), true);

			ValueReturnMode childValueReturnMode = ValueReturnMode.SELF;
			boolean childModifAccepted = dialogBuilder.isOkPressed();
			ModificationStack childModifStack = dialogBuilder.getModificationStack();
			ModificationStack parentModifStack = new ModificationStack(null);
			boolean childValueNew = dialogBuilder.isValueNew();
			IModification commitModif = null;
			IInfo childModifTarget = null;
			String subModifTitle = "";
			if (ReflectionUIUtils.integrateSubModifications(customizationToolsRenderer.getReflectionUI(),
					parentModifStack, childModifStack, childModifAccepted, childValueReturnMode, childValueNew,
					commitModif, childModifTarget, subModifTitle)) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						updateUI(impactedTypeName);
					}
				});
			}
		}

		protected IModificationListener getCustomizedWindowsReloadingAdviser(final Component ownerComponent) {
			return new IModificationListener() {

				@Override
				public void handleUdno(IModification undoModification) {
				}

				@Override
				public void handleRedo(IModification modification) {
				}

				@Override
				public void handleInvalidate() {
				}

				@Override
				public void handleInvalidationCleared() {
				}

				@Override
				public void handleDo(IModification modification) {
					if (modification != null) {
						boolean showReloadWarning = false;
						IInfo modifTarget = modification.getTarget();
						if (modifTarget instanceof IFieldInfo) {
							IFieldInfo field = (IFieldInfo) modifTarget;
							if (field.getName().equals("undoManagementHidden")) {
								showReloadWarning = true;
							}
							if (field.getName().equals(getIconImageFileField().getName())) {
								showReloadWarning = true;
							}
							if (field.getName().equals("validating")) {
								showReloadWarning = true;
							}
						}
						if (showReloadWarning) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									customizationToolsRenderer.openInformationDialog(ownerComponent,
											"You must reload the customized windows\nto view all this change effects.",
											"Information", getCustomizationsIcon().getImage());
								}
							});
						}
					}
				}
			};
		}

		protected Component createFieldInfoCustomizer(final FieldControlPlaceHolder fieldControlPlaceHolder) {
			final JButton result = createToolAccessButton(getCustomizationsIcon());
			SwingRendererUtils.setMultilineToolTipText(result,
					customizationToolsRenderer.prepareStringToDisplay("Customize this field display"));
			result.addActionListener(new ActionListener() {

				private ITypeInfo getCurrentFormObjectCustomizedType() {
					return reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(fieldControlPlaceHolder.object));
				}

				private String getFieldName() {
					return fieldControlPlaceHolder.controlAwareField.getName();
				}

				private ITypeInfo getFieldControlObjectCustomizedType() {
					if (fieldControlPlaceHolder.fieldControl instanceof IAdvancedFieldControl) {
						ITypeInfo dynamicType = ((IAdvancedFieldControl) fieldControlPlaceHolder.fieldControl)
								.getDynamicObjectType();
						if (dynamicType != null) {
							return dynamicType;
						}
					}
					final IControlData fieldControlData = getFieldControlData(fieldControlPlaceHolder.object,
							fieldControlPlaceHolder.controlAwareField);
					return fieldControlData.getType();
				}

				@Override
				public void actionPerformed(ActionEvent e) {
					final JPopupMenu popupMenu = new JPopupMenu();
					popupMenu.add(new AbstractAction(prepareStringToDisplay("Hide")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							hideField(result, getCurrentFormObjectCustomizedType(), getFieldName());
						}
					});
					popupMenu.add(new AbstractAction(prepareStringToDisplay("Move Up")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							moveField(result, getCurrentFormObjectCustomizedType(), getFieldName(), -1);
						}
					});
					popupMenu.add(new AbstractAction(prepareStringToDisplay("Move Down")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							moveField(result, getCurrentFormObjectCustomizedType(), getFieldName(), 1);
						}
					});
					popupMenu.add(new AbstractAction(prepareStringToDisplay("Move To Top")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							moveField(result, getCurrentFormObjectCustomizedType(), getFieldName(), Short.MIN_VALUE);
						}
					});
					popupMenu.add(new AbstractAction(prepareStringToDisplay("Move To Bottom")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							moveField(result, getCurrentFormObjectCustomizedType(), getFieldName(), Short.MAX_VALUE);
						}
					});
					popupMenu.add(new AbstractAction(prepareStringToDisplay("Field Type...")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							TypeCustomization t = infoCustomizations
									.getTypeCustomization(fieldControlPlaceHolder.getField().getType().getName(), true);
							openTypeCustomizationDialog(result, t);
						}
					});
					if (getFieldControlObjectCustomizedType() instanceof IListTypeInfo) {
						JMenu listSubMenu = new JMenu(prepareStringToDisplay("List"));
						{
							popupMenu.add(listSubMenu);
							listSubMenu.add(new AbstractAction(prepareStringToDisplay("Move Columns...")) {
								private static final long serialVersionUID = 1L;

								@Override
								public void actionPerformed(ActionEvent e) {
									openListColumnsOrderDialog(result,
											(IListTypeInfo) getFieldControlObjectCustomizedType());
								}
							});
							listSubMenu.add(new AbstractAction(prepareStringToDisplay("More Options...")) {
								private static final long serialVersionUID = 1L;

								@Override
								public void actionPerformed(ActionEvent e) {
									openListCutomizationDialog(result,
											(IListTypeInfo) getFieldControlObjectCustomizedType());
								}
							});
						}
					}
					if (getFieldControlObjectCustomizedType() instanceof IEnumerationTypeInfo) {
						JMenu enumSubMenu = new JMenu(prepareStringToDisplay("Enumeration"));
						{
							popupMenu.add(enumSubMenu);
							enumSubMenu.add(new AbstractAction(prepareStringToDisplay("More Options...")) {
								private static final long serialVersionUID = 1L;

								@Override
								public void actionPerformed(ActionEvent e) {
									openEnumerationCutomizationDialog(result,
											(IEnumerationTypeInfo) getFieldControlObjectCustomizedType());
								}
							});
						}
					}
					popupMenu.add(new AbstractAction(prepareStringToDisplay("More Options...")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							openFieldCutomizationDialog(result, getCurrentFormObjectCustomizedType(), getFieldName());
						}
					});
					showMenu(popupMenu, result);
				}
			});
			return result;
		}

		protected void showMenu(JPopupMenu popupMenu, JButton source) {
			popupMenu.show(source, source.getWidth(), source.getHeight() / 2);
		}

		protected void hideMethod(Component activatorComponent, ITypeInfo customizedType, String methodSignature) {
			MethodCustomization mc = infoCustomizations.getMethodCustomization(customizedType.getName(),
					methodSignature, true);
			mc.setHidden(true);
			updateUI(customizedType.getName());
		}

		protected void hideField(Component activatorComponent, ITypeInfo customizedType, String fieldName) {
			FieldCustomization fc = infoCustomizations.getFieldCustomization(customizedType.getName(), fieldName, true);
			fc.setHidden(true);
			updateUI(customizedType.getName());
		}

		protected void moveField(Component activatorComponent, ITypeInfo customizedType, String fieldName, int offset) {
			TypeCustomization tc = infoCustomizations.getTypeCustomization(customizedType.getName(), true);
			try {
				tc.moveField(customizedType.getFields(), fieldName, offset);
			} catch (Throwable t) {
				handleExceptionsFromDisplayedUI(activatorComponent, t);
			}
			updateUI(customizedType.getName());
		}

		protected void moveMethod(Component activatorComponent, ITypeInfo customizedType, String methodSignature,
				int offset) {
			TypeCustomization tc = infoCustomizations.getTypeCustomization(customizedType.getName(), true);
			try {
				tc.moveMethod(customizedType.getMethods(), methodSignature, offset);
			} catch (Throwable t) {
				handleExceptionsFromDisplayedUI(activatorComponent, t);
			}
			updateUI(customizedType.getName());
		}

		@SuppressWarnings("unchecked")
		protected void openListColumnsOrderDialog(Component activatorComponent,
				final IListTypeInfo customizedListType) {
			ITypeInfo customizedItemType = customizedListType.getItemType();
			String itemTypeName = (customizedItemType == null) ? null : customizedItemType.getName();
			ListCustomization lc = infoCustomizations.getListCustomization(customizedListType.getName(), itemTypeName,
					true);
			IListStructuralInfo customizedListStructure = customizedListType.getStructuralInfo();
			class ColumnOrderItem {
				IColumnInfo columnInfo;

				public ColumnOrderItem(IColumnInfo columnInfo, ColumnCustomization columnCustomization) {
					super();
					this.columnInfo = columnInfo;
				}

				public IColumnInfo getColumnInfo() {
					return columnInfo;
				}

				@Override
				public String toString() {
					return columnInfo.getCaption();
				}

			}
			List<ColumnOrderItem> columnOrder = new ArrayList<ColumnOrderItem>();
			for (final IColumnInfo c : customizedListStructure.getColumns()) {
				ColumnOrderItem orderItem = new ColumnOrderItem(c, lc.getColumnCustomization(c.getName()));
				columnOrder.add(orderItem);
			}
			ObjectDialogBuilder dialogStatus = customizationToolsRenderer.openObjectDialog(activatorComponent,
					columnOrder, "Columns Order", getCustomizationsIcon().getImage(), true, true);
			if (dialogStatus.isOkPressed()) {
				columnOrder = (List<ColumnOrderItem>) dialogStatus.getValue();
				List<String> newOrder = new ArrayList<String>();
				for (ColumnOrderItem item : columnOrder) {
					newOrder.add(item.getColumnInfo().getName());
				}
				lc.setColumnsCustomOrder(newOrder);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						updateUI(customizedListType.getName());
					}
				});
			}
		}

		protected void openEnumerationCutomizationDialog(Component activatorComponent,
				final IEnumerationTypeInfo customizedEnumType) {
			EnumerationCustomization ec = infoCustomizations.getEnumerationCustomization(customizedEnumType.getName(),
					true);
			updateEnumerationItemCustomizationList(ec, customizedEnumType);
			openCustomizationEditor(activatorComponent, ec, customizedEnumType.getName());
		}

		protected void updateEnumerationItemCustomizationList(EnumerationCustomization ec,
				IEnumerationTypeInfo customizedEnumType) {
			for (Object item : customizedEnumType.getPossibleValues()) {
				IEnumerationItemInfo itemInfo = customizedEnumType.getValueInfo(item);
				infoCustomizations.getEnumerationItemCustomization(ec.getEnumerationTypeName(), itemInfo.getName(),
						true);
			}
		}

		protected void openListCutomizationDialog(Component activatorComponent,
				final IListTypeInfo customizedListType) {
			ITypeInfo customizedItemType = customizedListType.getItemType();
			String itemTypeName = (customizedItemType == null) ? null : customizedItemType.getName();
			ListCustomization lc = infoCustomizations.getListCustomization(customizedListType.getName(), itemTypeName,
					true);
			updateColumnCustomizationList(lc, customizedListType);
			openCustomizationEditor(activatorComponent, lc, customizedListType.getName());
		}

		protected void updateColumnCustomizationList(ListCustomization lc, IListTypeInfo customizedListType) {
			for (IColumnInfo column : customizedListType.getStructuralInfo().getColumns()) {
				String itemTypeName = (customizedListType.getItemType() == null) ? null
						: customizedListType.getItemType().getName();
				infoCustomizations.getColumnCustomization(customizedListType.getName(), itemTypeName, column.getName(),
						true);
			}
		}

		protected void openFieldCutomizationDialog(Component activatorComponent, final ITypeInfo customizedType,
				String fieldName) {
			FieldCustomization fc = infoCustomizations.getFieldCustomization(customizedType.getName(), fieldName, true);
			openCustomizationEditor(activatorComponent, fc, customizedType.getName());
		}

		protected void openMethodCutomizationDialog(Component activatorComponent, final ITypeInfo customizedType,
				String methodSignature) {
			MethodCustomization mc = infoCustomizations.getMethodCustomization(customizedType.getName(),
					methodSignature, true);
			updateParameterCustomizationList(mc, customizedType);
			openCustomizationEditor(activatorComponent, mc, customizedType.getName());
		}

		protected void updateParameterCustomizationList(MethodCustomization mc, ITypeInfo customizedType) {
			IMethodInfo customizedMethod = ReflectionUIUtils.findMethodBySignature(customizedType.getMethods(),
					mc.getMethodSignature());
			for (IParameterInfo param : customizedMethod.getParameters()) {
				infoCustomizations.getParameterCustomization(customizedType.getName(), mc.getMethodSignature(),
						param.getName(), true);
			}
		}

		protected Component createMethodInfoCustomizer(final MethodControlPlaceHolder methodControlPlaceHolder) {
			final JButton result = createToolAccessButton(getCustomizationsIcon());
			SwingRendererUtils.setMultilineToolTipText(result,
					customizationToolsRenderer.prepareStringToDisplay("Customize this method display"));
			result.addActionListener(new ActionListener() {

				private ITypeInfo getCurrentFormObjectCustomizedType() {
					return reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(methodControlPlaceHolder.object));
				}

				private String getMethodInfoSignature() {
					return ReflectionUIUtils.getMethodSignature(methodControlPlaceHolder.controlAwareMethod);
				}

				@Override
				public void actionPerformed(ActionEvent e) {
					final JPopupMenu popupMenu = new JPopupMenu();
					popupMenu.add(new AbstractAction(prepareStringToDisplay("Hide")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							hideMethod(result, getCurrentFormObjectCustomizedType(), getMethodInfoSignature());
						}
					});
					popupMenu.add(new AbstractAction(prepareStringToDisplay("Move Left")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							moveMethod(result, getCurrentFormObjectCustomizedType(), getMethodInfoSignature(), -1);
						}
					});
					popupMenu.add(new AbstractAction(prepareStringToDisplay("Move Right")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							moveMethod(result, getCurrentFormObjectCustomizedType(), getMethodInfoSignature(), 1);
						}
					});
					final IMethodInfo customizedMethod = ReflectionUIUtils.findMethodBySignature(
							getCurrentFormObjectCustomizedType().getMethods(), getMethodInfoSignature());
					final ITypeInfo returnValueType = customizedMethod.getReturnValueType();
					if (returnValueType != null) {
						popupMenu.add(new AbstractAction(prepareStringToDisplay("Method Return Type...")) {
							private static final long serialVersionUID = 1L;

							@Override
							public void actionPerformed(ActionEvent e) {
								TypeCustomization t = infoCustomizations.getTypeCustomization(returnValueType.getName(),
										true);
								openTypeCustomizationDialog(result, t);
							}
						});
					}
					popupMenu.add(new AbstractAction(prepareStringToDisplay("More Options...")) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							openMethodCutomizationDialog(result, getCurrentFormObjectCustomizedType(),
									getMethodInfoSignature());
						}
					});
					showMenu(popupMenu, result);
				}
			});
			return result;
		}

		protected void updateUI(String typeName) {
			for (Map.Entry<JPanel, Object> entry : getObjectByForm().entrySet()) {
				Object object = entry.getValue();
				ITypeInfo objectType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
				if (typeName.equals(objectType.getName())) {
					for (JPanel form : getForms(object)) {
						recreateFormContent(form);
						updateStatusBarInBackground(form);
					}
				}
				JPanel form = entry.getKey();
				for (FieldControlPlaceHolder placeHolder : getFieldControlPlaceHolders(form)) {
					IFieldInfo field = placeHolder.getField();
					if (typeName.equals(field.getType().getName())) {
						refreshFieldControlsByName(form, field.getName(), true);
					}
				}
			}
			TypeCustomization t = infoCustomizations.getTypeCustomization(typeName, true);
			for (JPanel form : customizationToolsRenderer.getForms(t)) {
				customizationToolsRenderer.refreshAllFieldControls(form, false);
			}
			for (JPanel form : customizationToolsRenderer.getForms(infoCustomizations)) {
				customizationToolsRenderer.refreshAllFieldControls(form, false);
			}
		}

	}

	public class CustomizationOptions {
		protected final TreeSet<String> hiddenCustomizationToolsTypeNames = new TreeSet<String>();
		protected AWTEventListener openWindowListener;

		protected void openWindow(Component activatorComponent) {
			customizationTools.getCustomizationToolsRenderer().openObjectDialog(activatorComponent,
					CustomizationOptions.this,
					customizationTools.getCustomizationToolsRenderer().getObjectTitle(CustomizationOptions.this),
					getCustomizationsIcon().getImage(), false, true);
		}

		public Set<String> getHiddenCustomizationToolsTypeNames() {
			return new HashSet<String>(hiddenCustomizationToolsTypeNames);
		}

		public void setHiddenCustomizationToolsTypeNames(Set<String> hiddenCustomizationToolsTypeNames) {
			Set<String> impacted = new HashSet<String>();
			impacted.addAll(this.hiddenCustomizationToolsTypeNames);
			impacted.addAll(hiddenCustomizationToolsTypeNames);
			impacted.removeAll(ReflectionUIUtils.getIntersection(this.hiddenCustomizationToolsTypeNames,
					hiddenCustomizationToolsTypeNames));

			this.hiddenCustomizationToolsTypeNames.clear();
			this.hiddenCustomizationToolsTypeNames.addAll(hiddenCustomizationToolsTypeNames);

			for (String typeName : impacted) {
				customizationTools.updateUI(typeName);
			}
		}

		public void hideFor(String typeName) {
			Set<String> newHiddenCustomizationToolsTypeNames = new HashSet<String>(
					getHiddenCustomizationToolsTypeNames());
			newHiddenCustomizationToolsTypeNames.add(typeName);
			setHiddenCustomizationToolsTypeNames(newHiddenCustomizationToolsTypeNames);
		}

		public boolean areHiddenFor(String typeName) {
			return hiddenCustomizationToolsTypeNames.contains(typeName);
		}

	}
}
