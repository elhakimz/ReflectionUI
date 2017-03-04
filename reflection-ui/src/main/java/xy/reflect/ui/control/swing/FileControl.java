package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;

import xy.reflect.ui.control.data.ControlDataProxy;
import xy.reflect.ui.control.data.IControlData;
import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.FileTypeInfo;
import xy.reflect.ui.info.type.custom.TextualTypeInfo;

public class FileControl extends DialogAccessControl implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;

	protected boolean textChangedByUser = true;

	protected static File lastDirectory = new File(".").getAbsoluteFile();

	public FileControl(SwingRenderer swingRenderer, FieldControlPlaceHolder placeHolder) {
		super(swingRenderer, placeHolder);
	}

	@Override
	protected TextControl createStatusControl(FieldControlPlaceHolder placeHolder) {
		return new TextControl(swingRenderer, placeHolder){

			private static final long serialVersionUID = 1L;

			@Override
			protected IControlData retrieveData(FieldControlPlaceHolder placeHolder) {
				return new ControlDataProxy(IControlData.NULL_CONTROL_DATA) {

					@Override
					public void setValue(Object value) {
						FileControl.this.data.setValue(new File((String) value));
					}

					@Override
					public boolean isGetOnly() {
						return FileControl.this.data.isGetOnly();
					}

					@Override
					public Object getValue() {
						File currentFile = (File) FileControl.this.data.getValue();
						return currentFile.getPath();
					}

					@Override
					public ITypeInfo getType() {
						return new TextualTypeInfo(swingRenderer.getReflectionUI(), String.class);
					}
				};
			}
			
		};
	}

	@Override
	protected Component createButton() {
		Component result = super.createButton();
		if (data.isGetOnly()) {
			result.setEnabled(false);
		}
		return result;
	}

	protected void configureFileChooser(JFileChooser fileChooser, File currentFile) {
		if ((currentFile != null) && !currentFile.equals(FileTypeInfo.getDefaultFile())) {
			fileChooser.setSelectedFile(currentFile.getAbsoluteFile());
		}
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	}

	protected String getDialogTitle() {
		return "Select";
	}

	@Override
	protected void openDialog() {
		final JFileChooser fileChooser = new JFileChooser();
		File currentFile = (File) data.getValue();
		fileChooser.setCurrentDirectory(lastDirectory);
		configureFileChooser(fileChooser, currentFile);
		int returnVal = fileChooser.showDialog(this,
				swingRenderer.prepareStringToDisplay(getDialogTitle()));
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		lastDirectory = fileChooser.getCurrentDirectory();
		data.setValue(fileChooser.getSelectedFile());
		updateControls();
	}

	@Override
	public boolean displayError(String msg) {
		return false;
	}

	@Override
	public boolean showCaption() {
		return false;
	}

	@Override
	public boolean refreshUI() {
		updateControls();
		return true;
	}

	@Override
	public boolean handlesModificationStackUpdate() {
		return false;
	}

	@Override
	public void requestFocus() {
		statusControl.requestFocus();
	}

}
