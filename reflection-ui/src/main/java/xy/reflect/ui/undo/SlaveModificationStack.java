


package xy.reflect.ui.undo;

import xy.reflect.ui.info.ITransactionInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.Listener;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * A sub-class of {@link ModificationStack} that forwards its
 * {@link IModification} instances to the given master modification stack. These
 * modifications can then be undone or redone through this master modification
 * stack.
 * 
 * @author olitank
 *
 */
public class SlaveModificationStack extends ModificationStack {

	protected Accessor<Boolean> valueModifAcceptedGetter;
	protected Accessor<ValueReturnMode> valueReturnModeGetter;
	protected Accessor<Boolean> valueReplacedGetter;
	protected Accessor<ITransactionInfo> valueTransactionGetter;
	protected Accessor<String> masterModificationTitleGetter;
	protected Accessor<ModificationStack> masterModificationStackGetter;
	protected Accessor<Boolean> masterModificationFakeGetter;
	protected Accessor<IModification> committingModificationGetter;
	protected boolean exclusiveLinkWithParent;
	protected Listener<String> debugLogListener;
	protected Listener<String> errorLogListener;
	protected Listener<Throwable> masterModificationExceptionListener;

	public SlaveModificationStack(String name, Accessor<Boolean> valueModifAcceptedGetter,
			Accessor<ValueReturnMode> valueReturnModeGetter, Accessor<Boolean> valueReplacedGetter,
			Accessor<ITransactionInfo> valueTransactionGetter, Accessor<IModification> committingModificationGetter,
			Accessor<String> masterModificationTitleGetter, Accessor<ModificationStack> masterModificationStackGetter,
			Accessor<Boolean> masterModificationFakeGetter, boolean exclusiveLinkWithParent,
			Listener<String> debugLogListener, Listener<String> errorLogListener,
			Listener<Throwable> masterModificationExceptionListener) {
		super(name);
		this.valueModifAcceptedGetter = valueModifAcceptedGetter;
		this.valueReturnModeGetter = valueReturnModeGetter;
		this.valueReplacedGetter = valueReplacedGetter;
		this.valueTransactionGetter = valueTransactionGetter;
		this.committingModificationGetter = committingModificationGetter;
		this.masterModificationTitleGetter = masterModificationTitleGetter;
		this.masterModificationStackGetter = masterModificationStackGetter;
		this.masterModificationFakeGetter = masterModificationFakeGetter;
		this.exclusiveLinkWithParent = exclusiveLinkWithParent;
		this.debugLogListener = debugLogListener;
		this.errorLogListener = errorLogListener;
		this.masterModificationExceptionListener = masterModificationExceptionListener;
	}

	@Override
	public boolean push(final IModification undoModif) {
		if (undoModif.isNull()) {
			return false;
		}
		boolean result = super.push(undoModif);
		if (isInComposite()) {
			return result;
		}
		ModificationStack valueModifStack = new ModificationStack(null);
		valueModifStack.push(new ModificationStackShitf(this, -1, undoModif.getTitle()) {

			@Override
			protected void shiftBackward() {
				SlaveModificationStack.this.super_undo();
			}

			@Override
			protected void shiftForeward() {
				SlaveModificationStack.this.super_redo();
			}

			@Override
			public boolean isFake() {
				return undoModif.isFake();
			}

		});
		Boolean valueModifAccepted = valueModifAcceptedGetter.get();
		ValueReturnMode valueReturnMode = valueReturnModeGetter.get();
		boolean valueReplaced = valueReplacedGetter.get();
		ITransactionInfo valueTransaction = valueTransactionGetter.get();
		IModification committingModif = committingModificationGetter.get();
		String modifTitle = AbstractModification.getUndoTitle(undoModif.getTitle());
		String modifTitlePrefix = masterModificationTitleGetter.get();
		if ((modifTitlePrefix != null) && (modifTitle != null)) {
			modifTitle = ReflectionUIUtils.composeMessage(modifTitlePrefix, modifTitle);
		} else if (modifTitlePrefix != null) {
			modifTitle = modifTitlePrefix;
		}
		ModificationStack parentObjectModifStack = masterModificationStackGetter.get();
		boolean masterModificationFake = masterModificationFakeGetter.get();
		try {
			ReflectionUIUtils.finalizeSubModifications(parentObjectModifStack, valueModifStack, valueModifAccepted,
					valueReturnMode, valueReplaced, valueTransaction, committingModif, modifTitle,
					masterModificationFake, debugLogListener, errorLogListener);
		} catch (Throwable t) {
			masterModificationExceptionListener.handle(t);
		}
		return true;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (isInComposite()) {
			return;
		}
		forwardInvalidation();
	}

	protected void forwardInvalidation() {
		ModificationStack valueModifStack = new ModificationStack(null);
		valueModifStack.invalidate();
		Boolean valueModifAccepted = valueModifAcceptedGetter.get();
		ValueReturnMode valueReturnMode = valueReturnModeGetter.get();
		boolean valueReplaced = valueReplacedGetter.get();
		ITransactionInfo valueTransaction = valueTransactionGetter.get();
		IModification committingModif = committingModificationGetter.get();
		String parentObjectModifTitle = null;
		ModificationStack parentObjectModifStack = masterModificationStackGetter.get();
		boolean parentObjectModificationFake = masterModificationFakeGetter.get();
		try {
			ReflectionUIUtils.finalizeSubModifications(parentObjectModifStack, valueModifStack, valueModifAccepted,
					valueReturnMode, valueReplaced, valueTransaction, committingModif, parentObjectModifTitle,
					parentObjectModificationFake, debugLogListener, errorLogListener);
		} catch (Throwable t) {
			masterModificationExceptionListener.handle(t);
		}
	}

	@Override
	public void forget() {
		super.forget();
		if (exclusiveLinkWithParent) {
			ModificationStack parentObjectModifStack = masterModificationStackGetter.get();
			parentObjectModifStack.forget();
		} else {
			forwardInvalidation();
		}
	}

	@Override
	public void undo() {
		super_undo();
		ModificationStack parentObjectModifStack = masterModificationStackGetter.get();
		parentObjectModifStack.invalidate();
	}

	@Override
	public void redo() {
		super_redo();
		ModificationStack parentObjectModifStack = masterModificationStackGetter.get();
		parentObjectModifStack.invalidate();
	}

	protected void super_undo() {
		super.undo();
	}

	protected void super_redo() {
		super.redo();
	}

	@Override
	public boolean isEventFiringEnabled() {
		ModificationStack parentObjectModifStack = masterModificationStackGetter.get();
		return parentObjectModifStack.isEventFiringEnabled();
	}

	@Override
	public void setEventFiringEnabled(boolean eventFiringEnabled) {
		ModificationStack parentObjectModifStack = masterModificationStackGetter.get();
		parentObjectModifStack.setEventFiringEnabled(eventFiringEnabled);
	}

	@Override
	public void addListener(IModificationListener listener) {
		ModificationStack parentObjectModifStack = masterModificationStackGetter.get();
		parentObjectModifStack.addListener(listener);
	}

	@Override
	public void removeListener(IModificationListener listener) {
		ModificationStack parentObjectModifStack = masterModificationStackGetter.get();
		parentObjectModifStack.removeListener(listener);
	}

	@Override
	public IModificationListener[] getListeners() {
		ModificationStack parentObjectModifStack = masterModificationStackGetter.get();
		return parentObjectModifStack.getListeners();
	}

	@Override
	public String toString() {
		return "Slave" + super.toString();
	}

}
