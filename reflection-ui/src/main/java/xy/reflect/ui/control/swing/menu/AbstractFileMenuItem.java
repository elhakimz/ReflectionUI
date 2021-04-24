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
package xy.reflect.ui.control.swing.menu;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import com.google.common.collect.MapMaker;

import xy.reflect.ui.control.DefaultFieldControlData;
import xy.reflect.ui.control.DefaultFieldControlInput;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin.FileBrowser;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin.FileBrowserConfiguration;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.menu.StandradActionMenuItemInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * Base class for persistence (open, save, save as) menu items.
 * 
 * @author olitank
 *
 */
public abstract class AbstractFileMenuItem extends AbstractStandardActionMenuItem {

	private static final long serialVersionUID = 1L;

	protected static Map<Form, File> lastFileByForm = new MapMaker().weakKeys().makeMap();
	protected static Map<Form, Long> lastPersistedVersionByForm = new MapMaker().weakKeys().makeMap();

	protected FileBrowserConfiguration fileBrowserConfiguration;

	protected abstract void persist(SwingRenderer swingRenderer, Form form, File file);

	public AbstractFileMenuItem(SwingRenderer swingRenderer, Form form, StandradActionMenuItemInfo menuItemInfo) {
		super(swingRenderer, form, menuItemInfo);
		fileBrowserConfiguration = menuItemInfo.getFileBrowserConfiguration();
		if (fileBrowserConfiguration == null) {
			throw new ReflectionUIError();
		}
	}

	public static Map<Form, File> getLastFileByForm() {
		return lastFileByForm;
	}

	public static Map<Form, Long> getLastPersistedVersionByForm() {
		return lastPersistedVersionByForm;
	}

	public FileBrowserConfiguration getFileBrowserConfiguration() {
		return fileBrowserConfiguration;
	}

	public void setFileBrowserConfiguration(FileBrowserConfiguration fileBrowserConfiguration) {
		this.fileBrowserConfiguration = fileBrowserConfiguration;
	}

	@Override
	protected boolean isActive() {
		Object object = form.getObject();
		ITypeInfo type = swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(object));
		if (!type.canPersist()) {
			throw new ReflectionUIError("Type '" + type.getName() + "' cannot persist its instances state");
		}
		return true;
	}

	protected File retrieveFile() {
		final File[] fileHolder = new File[1];
		final FileBrowserPlugin fileBrowserPlugin = new FileBrowserPlugin();
		IFieldControlInput fileBrowserInput = new DefaultFieldControlInput(swingRenderer.getReflectionUI()) {

			@Override
			public IFieldControlData getControlData() {
				return new DefaultFieldControlData(swingRenderer.getReflectionUI()) {

					@Override
					public Object getValue() {
						return fileHolder[0];
					}

					@Override
					public void setValue(Object value) {
						fileHolder[0] = (File) value;
					}

					@Override
					public ITypeInfo getType() {
						return new InfoProxyFactory() {

							@Override
							protected Map<String, Object> getSpecificProperties(ITypeInfo type) {
								Map<String, Object> result = super.getSpecificProperties(type);
								result = new HashMap<String, Object>();
								fileBrowserPlugin.storeControlCustomization(fileBrowserConfiguration, result);
								return result;
							}

						}.wrapTypeInfo(
								swingRenderer.getReflectionUI().getTypeInfo(new JavaTypeInfoSource(File.class, null)));
					}

				};
			}

		};
		FileBrowser fileBrowser = fileBrowserPlugin.createControl(swingRenderer, fileBrowserInput);
		fileBrowser.openDialog(form);
		File result = fileHolder[0];
		return result;
	}

	@Override
	protected void execute() {
		File file = retrieveFile();
		if (file == null) {
			return;
		}
		try {
			persist(swingRenderer, (Form) form, file);
			ModificationStack modifStack = ((Form) form).getModificationStack();
			lastPersistedVersionByForm.put((Form) form, modifStack.getStateVersion());
			lastFileByForm.put((Form) form, file);
		} catch (Throwable t) {
			lastPersistedVersionByForm.put((Form) form, -1l);
			throw new ReflectionUIError(t);
		} finally {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					SwingRendererUtils.updateWindowMenu(form, swingRenderer);
				}
			});
		}
	}

}
