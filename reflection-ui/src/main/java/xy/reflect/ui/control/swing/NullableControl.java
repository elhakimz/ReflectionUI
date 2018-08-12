package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import xy.reflect.ui.control.CustomContext;
import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.editor.AbstractEditorFormBuilder;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.ControlDataValueModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class NullableControl extends JPanel implements IAdvancedFieldControl {

	protected SwingRenderer swingRenderer;
	protected static final long serialVersionUID = 1L;
	protected IFieldControlData data;
	protected Component nullStatusControl;
	protected Component subControl;
	protected IFieldControlInput input;
	protected ITypeInfo subControlValueType;
	protected AbstractEditorFormBuilder subFormBuilder;
	protected Runnable nullControlAction;

	public NullableControl(SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = input.getControlData();
		initialize();
	}

	protected void initialize() {
		setLayout(new BorderLayout());
		nullStatusControl = createNullStatusControl();
		add(SwingRendererUtils.flowInLayout(nullStatusControl, GridBagConstraints.CENTER), BorderLayout.WEST);
		refreshUI(true);
	}

	@Override
	public boolean refreshUI(boolean refreshStructure) {
		setNullStatusControlState(data.getValue() == null);
		refreshSubControl(refreshStructure);
		if (refreshStructure) {
			nullStatusControl.setEnabled(!data.isGetOnly());
		}
		return true;
	}

	public Component getSubControl() {
		return subControl;
	}

	protected void setNullStatusControlState(boolean b) {
		((JCheckBox) nullStatusControl).setSelected(!b);
	}

	protected boolean getNullStatusControlState() {
		return !((JCheckBox) nullStatusControl).isSelected();
	}

	protected void onNullingControlStateChange() {
		if (getNullStatusControlState()) {
			ReflectionUIUtils.setValueThroughModificationStack(data, null, input.getModificationStack(),
					input.getModificationsTarget());
		} else {
			nullControlAction.run();
		}
		refreshUI(false);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				requestCustomFocus();
			}
		});
	}

	@Override
	public boolean requestCustomFocus() {
		return SwingRendererUtils.requestAnyComponentFocus(subControl, swingRenderer);
	}

	protected Object generateNonNullValue() {
		Object result = null;
		try {
			result = this.swingRenderer.onTypeInstanciationRequest(this, input.getControlData().getType());
		} catch (Throwable t) {
			swingRenderer.handleExceptionsFromDisplayedUI(this, t);
		}
		return result;
	}

	public void refreshSubControl(boolean refreshStructure) {
		Object value = data.getValue();
		if (value == null) {
			if (subControl != null) {
				if (subControlValueType == null) {
					return;
				}
			}
		} else {
			ITypeInfo newValueType = swingRenderer.getReflectionUI()
					.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(value));
			if (newValueType.equals(subControlValueType)) {
				if (SwingRendererUtils.isForm(subControl, swingRenderer)) {
					subFormBuilder.refreshEditorForm((Form) subControl, refreshStructure);
					return;
				}
			}
		}
		if (subControl != null) {
			remove(subControl);
		}
		if (value == null) {
			subControlValueType = null;
			subControl = createNullControl();
		} else {
			subControlValueType = swingRenderer.getReflectionUI()
					.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(value));
			subControl = createSubForm();
		}
		((JComponent) subControl).setBorder(BorderFactory.createTitledBorder(data.getCaption()));
		add(subControl, BorderLayout.CENTER);
		SwingRendererUtils.handleComponentSizeChange(this);
	}

	protected Component createNullStatusControl() {
		JCheckBox result = new JCheckBox();
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					onNullingControlStateChange();
				} catch (Throwable t) {
					swingRenderer.handleExceptionsFromDisplayedUI(NullableControl.this, t);
				}
			}
		});
		return result;
	}

	protected Component createNullControl() {
		NullControl result = new NullControl(swingRenderer, new FieldControlInputProxy(input) {
			@Override
			public IFieldControlData getControlData() {
				return new FieldControlDataProxy(super.getControlData()) {

					@Override
					public String getCaption() {
						return "";
					}

					@Override
					public void setValue(Object value) {
						ReflectionUIUtils.setValueThroughModificationStack(base, value, input.getModificationStack(),
								input.getModificationsTarget());
					}

				};
			}
		});
		if (!data.isGetOnly()) {
			nullControlAction = result.getAction();
			result.setAction(new Runnable() {
				@Override
				public void run() {
					setNullStatusControlState(false);
					onNullingControlStateChange();
				}
			});
		}
		return result;
	}

	protected Component createSubForm() {
		subFormBuilder = new AbstractEditorFormBuilder() {

			@Override
			public IContext getContext() {
				return input.getContext();
			}

			@Override
			public IContext getSubContext() {
				return new CustomContext("NullableInstance");
			}

			@Override
			public boolean isObjectFormExpanded() {
				return false;
			}

			@Override
			public boolean isObjectNullValueDistinct() {
				return false;
			}

			@Override
			public boolean canCommit() {
				return !data.isGetOnly();
			}

			@Override
			public IModification createCommitModification(Object newObjectValue) {
				return new ControlDataValueModification(data, newObjectValue, input.getModificationsTarget());
			}

			@Override
			public SwingRenderer getSwingRenderer() {
				return swingRenderer;
			}

			@Override
			public ValueReturnMode getObjectValueReturnMode() {
				return data.getValueReturnMode();
			}

			@Override
			public String getCumulatedModificationsTitle() {
				return ControlDataValueModification.getTitle(input.getModificationsTarget());
			}

			@Override
			public IInfo getCumulatedModificationsTarget() {
				return input.getModificationsTarget();
			}

			@Override
			public IInfoFilter getObjectFormFilter() {
				IInfoFilter result = data.getFormControlFilter();
				if (result == null) {
					result = IInfoFilter.DEFAULT;
				}
				return result;
			}

			@Override
			public ITypeInfo getObjectDeclaredType() {
				return data.getType();
			}

			@Override
			public ModificationStack getParentObjectModificationStack() {
				return input.getModificationStack();
			}

			@Override
			public Object getInitialObjectValue() {
				return data.getValue();
			}
		};
		Form result = subFormBuilder.createForm(true, false);
		return result;
	}

	@Override
	public boolean showsCaption() {
		return true;
	}

	@Override
	public boolean displayError(String msg) {
		return false;
	}

	@Override
	public boolean handlesModificationStackUpdate() {
		return true;
	}

	@Override
	public void validateSubForm() throws Exception {
		if (SwingRendererUtils.isForm(subControl, swingRenderer)) {
			((Form) subControl).validateForm();
		}
	}

	@Override
	public void addMenuContribution(MenuModel menuModel) {
		if (SwingRendererUtils.isForm(subControl, swingRenderer)) {
			((Form) subControl).addMenuContribution(menuModel);
		}
	}

	public ITypeInfo getSubControlValueType() {
		return subControlValueType;
	}

	@Override
	public String toString() {
		return "NullableControl [data=" + data + "]";
	}
}