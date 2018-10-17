package xy.reflect.ui.control.swing.renderer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import java.util.SortedMap;

import xy.reflect.ui.control.DefaultMethodControlData;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.IMethodControlInput;
import xy.reflect.ui.control.MethodContext;
import xy.reflect.ui.control.MethodControlDataProxy;
import xy.reflect.ui.control.swing.MethodControl;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.AbstractModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.component.ControlPanel;

public class MethodControlPlaceHolder extends ControlPanel implements IMethodControlInput {

	protected static final long serialVersionUID = 1L;

	protected final SwingRenderer swingRenderer;
	protected Form form;
	protected Component methodControl;
	protected IMethodInfo method;
	protected IMethodControlData controlData;

	public MethodControlPlaceHolder(SwingRenderer swingRenderer, Form form, IMethodInfo method) {
		super();
		this.swingRenderer = swingRenderer;
		this.form = form;
		this.method = method;
		setLayout(new BorderLayout());
		refreshUI();
	}

	public Form getForm() {
		return form;
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension result = super.getPreferredSize();
		if (result == null) {
			return null;
		}
		int maxMethodControlWidth = 0;
		SortedMap<InfoCategory, List<MethodControlPlaceHolder>> methodControlPlaceHoldersByCategory = form
				.getMethodControlPlaceHoldersByCategory();
		for (InfoCategory category : methodControlPlaceHoldersByCategory.keySet()) {
			for (MethodControlPlaceHolder methodControlPlaceHolder : methodControlPlaceHoldersByCategory
					.get(category)) {
				Component methodControl = methodControlPlaceHolder.getMethodControl();
				maxMethodControlWidth = Math.max(maxMethodControlWidth, methodControl.getPreferredSize().width);
			}
		}
		maxMethodControlWidth = maxMethodControlWidth - (maxMethodControlWidth % getIndentWidth()) + getIndentWidth();
		result.width = maxMethodControlWidth;
		return result;
	}

	public int getIndentWidth() {
		return SwingRendererUtils.getStandardCharacterWidth(form) * 10;
	}

	public IMethodControlData delayInvocations(final IMethodControlData data) {
		return new MethodControlDataProxy(data) {

			@Override
			public Object invoke(InvocationData invocationData) {
				if (swingRenderer.getDataUpdateDelayMilliseconds() > 0) {
					try {
						Thread.sleep(swingRenderer.getDataUpdateDelayMilliseconds());
					} catch (InterruptedException e) {
						throw new ReflectionUIError(e);
					}
				}
				return data.invoke(invocationData);
			}
		};
	}

	public IMethodControlData makeMethodModificationsUndoable(final IMethodControlData data) {
		return new MethodControlDataProxy(data) {

			@Override
			public Object invoke(InvocationData invocationData) {
				return ReflectionUIUtils.invokeMethodThroughModificationStack(data, invocationData,
						getModificationStack());
			}

		};
	}

	public IMethodControlData indicateWhenBusy(final IMethodControlData data) {
		return new MethodControlDataProxy(data) {

			@Override
			public Object invoke(final InvocationData invocationData) {
				return SwingRendererUtils.showBusyDialogWhileInvokingMethod(MethodControlPlaceHolder.this,
						swingRenderer, data, invocationData);
			}

			@Override
			public Runnable getNextUpdateCustomUndoJob(InvocationData invocationData) {
				final Runnable result = data.getNextUpdateCustomUndoJob(invocationData);
				if (result == null) {
					return null;
				}
				return new Runnable() {
					@Override
					public void run() {
						MethodControlPlaceHolder.this.swingRenderer.showBusyDialogWhile(MethodControlPlaceHolder.this,
								new Runnable() {
									public void run() {
										result.run();
									}
								}, AbstractModification.getUndoTitle(
										ReflectionUIUtils.composeMessage(data.getCaption(), "Executing...")));
					}
				};
			}

		};
	}

	public Component getMethodControl() {
		return methodControl;
	}

	public Object getObject() {
		return form.getObject();
	}

	@Override
	public IMethodControlData getControlData() {
		return controlData;
	}

	@Override
	public ModificationStack getModificationStack() {
		return form.getModificationStack();
	}

	@Override
	public IContext getContext() {
		ITypeInfo objectType = this.swingRenderer.reflectionUI
				.getTypeInfo(this.swingRenderer.reflectionUI.getTypeInfoSource(getObject()));
		return new MethodContext(objectType, method);
	}

	public IMethodInfo getMethod() {
		return method;
	}

	public Component createMethodControl() {
		Component result = createCustomMethodControl();
		if (result != null) {
			return result;
		}
		return new MethodControl(this.swingRenderer, this);
	}

	public Component createCustomMethodControl() {
		return null;
	}

	public void refreshUI() {
		if (methodControl != null) {
			remove(methodControl);
			methodControl = null;
		}
		controlData = getInitialControlData();
		methodControl = createMethodControl();
		add(methodControl, BorderLayout.CENTER);
		SwingRendererUtils.handleComponentSizeChange(this);
	}

	public IMethodControlData getInitialControlData() {
		IMethodControlData result = new InitialMethodControlData(method);

		result = indicateWhenBusy(result);
		result = makeMethodModificationsUndoable(result);
		result = delayInvocations(result);

		return result;
	}

	@Override
	public String toString() {
		return "MethodControlPlaceHolder [method=" + method + ", form=" + form + "]";
	}

	protected class InitialMethodControlData extends DefaultMethodControlData {

		public InitialMethodControlData(IMethodInfo finalMethod) {
			super(swingRenderer.getReflectionUI(), form.getObject(), finalMethod);
		}

		@Override
		public Object getObject() {
			return form.getObject();
		}

		private Object getOuterType() {
			return MethodControlPlaceHolder.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + super.hashCode();
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
			InitialMethodControlData other = (InitialMethodControlData) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (!super.equals(other))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "InitialControlData [of=" + MethodControlPlaceHolder.this + ", finalMethod=" + getMethod() + "]";
		}

	};

}