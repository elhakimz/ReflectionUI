package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import xy.reflect.ui.control.input.FieldControlDataProxy;
import xy.reflect.ui.control.input.FieldControlInputProxy;
import xy.reflect.ui.control.input.IFieldControlData;
import xy.reflect.ui.control.input.IFieldControlInput;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.TextualTypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class NullControl extends TextControl {

	protected static final long serialVersionUID = 1L;
	protected Runnable action;
	protected MouseListener mouseListener = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			if (action != null) {
				try {
					action.run();
				} catch (Throwable t) {
					swingRenderer.handleExceptionsFromDisplayedUI(NullControl.this, t);
				}
			}
		}
	};

	public NullControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		super(swingRenderer, new FieldControlInputProxy(input) {
			@Override
			public IFieldControlData getControlData() {
				final IFieldControlData baseControlData = super.getControlData();
				return new FieldControlDataProxy(IFieldControlData.NULL_CONTROL_DATA) {

					@Override
					public Object getValue() {
						String result = baseControlData.getNullValueLabel();
						if (result == null) {
							result = "";
						}
						return result;
					}

					@Override
					public ITypeInfo getType() {
						return new TextualTypeInfo(swingRenderer.getReflectionUI(), String.class);
					}

					@Override
					public String getCaption() {
						return baseControlData.getCaption();
					}

				};
			}

		});
		setBorder(BorderFactory.createTitledBorder(data.getCaption()));
		addMouseListener(mouseListener);
	}

	@Override
	protected Component createTextComponent() {
		final JTextArea result = new JTextArea();
		result.setEditable(false);
		((JComponent) result).setBorder(null);
		if ("".equals(data.getValue())) {
			result.setBackground(swingRenderer.getNullColor());
		} else {
			result.setBackground(ReflectionUIUtils.getDisabledTextBackgroundColor());
		}
		result.addMouseListener(mouseListener);
		return result;
	}

	@Override
	protected JScrollPane createScrollPane() {
		JScrollPane result = super.createScrollPane();
		result.setViewportBorder(null);
		return result;
	}

	public void setAction(Runnable action) {
		this.action = action;
	}

	@Override
	public boolean showsCaption() {
		return true;
	}

	@Override
	public boolean refreshUI() {
		return false;
	}

	@Override
	public boolean displayError(String msg) {
		return false;
	}

	@Override
	public String toString() {
		return "NullControl [data=" + data + "]";
	}

}
