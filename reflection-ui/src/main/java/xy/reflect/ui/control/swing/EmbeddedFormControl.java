package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import xy.reflect.ui.control.input.IFieldControlData;
import xy.reflect.ui.control.input.IFieldControlInput;
import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.info.DesktopSpecificProperty;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.ControlDataValueModification;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

@SuppressWarnings("unused")
public class EmbeddedFormControl extends JPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IFieldControlData data;

	protected Component textControl;
	protected Component iconControl;
	protected JButton button;
	protected Object subFormObject;
	protected JPanel subForm;
	protected IFieldControlInput input;

	public EmbeddedFormControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = retrieveData();
		setLayout(new BorderLayout());
		refreshUI();
	}

	protected IFieldControlData retrieveData() {
		return input.getControlData();
	}

	public JPanel getSubForm() {
		return subForm;
	}

	@Override
	public Object getFocusDetails() {
		Object subFormFocusDetails = swingRenderer.getFormFocusDetails(subForm);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("subFormFocusDetails", subFormFocusDetails);
		return result;
	}

	@Override
	public void requestDetailedFocus(Object value) {
		@SuppressWarnings("unchecked")
		Map<String, Object> focusDetails = (Map<String, Object>) value;
		Object subFormFocusDetails = focusDetails.get("subFormFocusDetails");
		if (subFormFocusDetails != null) {
			swingRenderer.requestFormDetailedFocus(subForm, subFormFocusDetails);
		} else {
			subForm.requestFocusInWindow();
		}
	}

	@Override
	public boolean requestFocusInWindow() {
		List<FieldControlPlaceHolder> fieldControlPlaceHolders = swingRenderer.getFieldControlPlaceHolders(subForm);
		if (fieldControlPlaceHolders.size() > 0) {
			return fieldControlPlaceHolders.get(0).getFieldControl().requestFocusInWindow();
		}
		return false;
	}

	protected void forwardSubFormModifications() {
		if (!ReflectionUIUtils.canPotentiallyIntegrateSubModifications(swingRenderer.getReflectionUI(), data.getValue(),
				data.getValueReturnMode(), !data.isGetOnly())) {
			ModificationStack childModifStack = swingRenderer.getModificationStackByForm().get(subForm);
			childModifStack.addListener(new AbstractSimpleModificationListener() {
				@Override
				protected void handleAnyEvent(IModification modification) {
					refreshUI();
				}
			});
		} else {
			Accessor<Boolean> childModifAcceptedGetter = Accessor.returning(Boolean.TRUE);
			Accessor<ValueReturnMode> childValueReturnModeGetter = Accessor.returning(data.getValueReturnMode());
			Accessor<Boolean> childValueNewGetter = Accessor.returning(Boolean.FALSE);
			Accessor<IModification> commitModifGetter = new Accessor<IModification>() {
				@Override
				public IModification get() {
					if (data.isGetOnly()) {
						return null;
					}
					return new ControlDataValueModification(data, subFormObject, input.getModificationsTarget());
				}
			};
			Accessor<IInfo> childModifTargetGetter = new Accessor<IInfo>() {
				@Override
				public IInfo get() {
					return input.getModificationsTarget();
				}
			};
			Accessor<String> childModifTitleGetter = new Accessor<String>() {
				@Override
				public String get() {
					return ControlDataValueModification.getTitle(input.getModificationsTarget());
				}
			};
			Accessor<ModificationStack> parentModifStackGetter = new Accessor<ModificationStack>() {
				@Override
				public ModificationStack get() {
					return input.getModificationStack();
				}
			};
			SwingRendererUtils.forwardSubModifications(swingRenderer, subForm, childModifAcceptedGetter,
					childValueReturnModeGetter, childValueNewGetter, commitModifGetter, childModifTargetGetter,
					childModifTitleGetter, parentModifStackGetter);
		}
	}

	@Override
	public boolean showCaption() {
		setBorder(BorderFactory.createTitledBorder(data.getCaption()));
		return true;
	}

	@Override
	public boolean displayError(String msg) {
		return false;
	}

	@Override
	public boolean refreshUI() {
		if (subForm == null) {
			subFormObject = data.getValue();
			if (subFormObject == null) {
				throw new ReflectionUIError();
			}
			IInfoFilter filter = DesktopSpecificProperty
					.getFilter(DesktopSpecificProperty.accessControlDataProperties(data));
			{
				if (filter == null) {
					filter = IInfoFilter.NO_FILTER;
				}
			}
			subForm = swingRenderer.createForm(subFormObject, filter);
			add(subForm, BorderLayout.CENTER);
			forwardSubFormModifications();
			SwingRendererUtils.handleComponentSizeChange(this);
		} else {
			Object newSubFormObject = data.getValue();
			if (newSubFormObject == null) {
				throw new ReflectionUIError();
			}
			Object subFormObjectType = swingRenderer.getReflectionUI()
					.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(subFormObject));
			Object newSubFormObjectType = swingRenderer.getReflectionUI()
					.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(newSubFormObject));
			if (subFormObjectType.equals(newSubFormObjectType)) {
				swingRenderer.getObjectByForm().put(subForm, newSubFormObject);
				swingRenderer.refreshAllFieldControls(subForm, false);
			} else {
				remove(subForm);
				subForm = null;
				refreshUI();
			}
		}
		return true;
	}

	@Override
	public boolean handlesModificationStackUpdate() {
		return true;
	}

	@Override
	public void validateSubForm() throws Exception {
		swingRenderer.validateForm(subForm);
	}

}
