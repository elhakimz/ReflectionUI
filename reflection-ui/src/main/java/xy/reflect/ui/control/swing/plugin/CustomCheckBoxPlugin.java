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
package xy.reflect.ui.control.swing.plugin;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.CheckBoxControl;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.util.ReflectionUIUtils;

public class CustomCheckBoxPlugin extends AbstractSimpleCustomizableFieldControlPlugin {

	@Override
	public String getControlTitle() {
		return "Custom Check Box";
	}

	@Override
	protected boolean handles(Class<?> javaType) {
		return Boolean.class.equals(javaType) || boolean.class.equals(javaType);
	}

	@Override
	public boolean canDisplayDistinctNullValue() {
		return false;
	}

	@Override
	public AbstractConfiguration getDefaultControlCustomization() {
		return new CustomCheckBoxConfiguration();
	}

	@Override
	public CustomCheckBoxControl createControl(Object renderer, IFieldControlInput input) {
		return new CustomCheckBoxControl((SwingRenderer) renderer, input);
	}

	public static class CustomCheckBoxConfiguration extends AbstractConfiguration {
		private static final long serialVersionUID = 1L;

		protected static ImageIcon createDefaultIcon(Color color) {
			BufferedImage result = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = result.createGraphics();
			g.setColor(color);
			g.fillOval(0, 0, result.getWidth(), result.getHeight());
			g.setColor(Color.BLACK);
			g.setStroke(new BasicStroke(1f));
			g.drawOval(1, 1, result.getWidth() - 2, result.getHeight() - 2);
			g.dispose();
			return new ImageIcon(result);
		}

		public static final ImageIcon DEFAULT_TRUE_VALUE_ICON = createDefaultIcon(Color.GREEN);
		public static final ImageIcon DEFAULT_FALSE_VALUE_ICON = createDefaultIcon(Color.RED);

		public ResourcePath trueValueIconPath;
		public ResourcePath falseValueIconPath;

		public ResourcePath pressedIconPath;

		public ResourcePath trueValueRollOverIconPath;
		public ResourcePath falseValueRollOverIconPath;

		public ResourcePath trueValueDisabledIconPath;
		public ResourcePath falseValueDisabledIconPath;

	}

	public class CustomCheckBoxControl extends CheckBoxControl {
		private static final long serialVersionUID = 1L;

		protected Class<?> numberClass;

		public CustomCheckBoxControl(SwingRenderer swingRenderer, IFieldControlInput input) {
			super(swingRenderer, input);
		}

		@Override
		public boolean refreshUI(boolean refreshStructure) {
			if (refreshStructure) {
				CustomCheckBoxConfiguration controlCustomization = (CustomCheckBoxConfiguration) loadControlCustomization(
						input);
				setSelectedIcon(getFirstNonNullImageIcon(CustomCheckBoxConfiguration.DEFAULT_TRUE_VALUE_ICON,
						controlCustomization.trueValueIconPath));
				setIcon(getFirstNonNullImageIcon(CustomCheckBoxConfiguration.DEFAULT_FALSE_VALUE_ICON,
						controlCustomization.falseValueIconPath));

				setRolloverSelectedIcon(getFirstNonNullImageIcon(CustomCheckBoxConfiguration.DEFAULT_TRUE_VALUE_ICON,
						controlCustomization.trueValueRollOverIconPath, controlCustomization.trueValueIconPath));
				setRolloverIcon(getFirstNonNullImageIcon(CustomCheckBoxConfiguration.DEFAULT_FALSE_VALUE_ICON,
						controlCustomization.falseValueRollOverIconPath, controlCustomization.falseValueIconPath));

				setDisabledSelectedIcon(getFirstNonNullImageIcon(CustomCheckBoxConfiguration.DEFAULT_TRUE_VALUE_ICON,
						controlCustomization.trueValueDisabledIconPath, controlCustomization.trueValueIconPath));
				setDisabledIcon(getFirstNonNullImageIcon(CustomCheckBoxConfiguration.DEFAULT_FALSE_VALUE_ICON,
						controlCustomization.falseValueDisabledIconPath, controlCustomization.falseValueIconPath));

				setPressedIcon(getFirstNonNullImageIcon(getDefaultPressedIcon(), controlCustomization.pressedIconPath));

				SwingRendererUtils.handleComponentSizeChange(this);
			}
			return super.refreshUI(refreshStructure);
		}

		protected Icon getDefaultPressedIcon() {
			List<Icon> iconsToBlend = Arrays.asList(getSelectedIcon(), getIcon());
			for (Icon icon : iconsToBlend) {
				if (icon == null) {
					return null;
				}
			}
			Dimension resultSize = new Dimension(0, 0);
			for (Icon icon : iconsToBlend) {
				resultSize.width = Math.max(resultSize.width, icon.getIconWidth());
				resultSize.height = Math.max(resultSize.height, icon.getIconHeight());
			}
			List<BufferedImage> iconImages = new ArrayList<BufferedImage>();
			for (Icon icon : iconsToBlend) {
				BufferedImage iconImage = new BufferedImage(resultSize.width, resultSize.height,
						BufferedImage.TYPE_4BYTE_ABGR);
				Graphics2D g = iconImage.createGraphics();
				icon.paintIcon(null, g, 0, 0);
				g.dispose();
				iconImages.add(iconImage);
			}
			BufferedImage resultImage = new BufferedImage(resultSize.width, resultSize.height,
					BufferedImage.TYPE_4BYTE_ABGR);
			for (int x = 0; x < resultSize.width; x++) {
				for (int y = 0; y < resultSize.height; y++) {
					int redSum = 0;
					int greenSum = 0;
					int blueSum = 0;
					int alphaSum = 0;
					for (BufferedImage iconImage : iconImages) {
						Color color = new Color(iconImage.getRGB(x, y), true);
						redSum += color.getRed();
						greenSum += color.getGreen();
						blueSum += color.getBlue();
						alphaSum += color.getAlpha();
					}
					int red = Math.round((float) redSum / (float) iconImages.size());
					int green = Math.round((float) greenSum / (float) iconImages.size());
					int blue = Math.round((float) blueSum / (float) iconImages.size());
					int alpha = Math.round((float) alphaSum / (float) iconImages.size());
					resultImage.setRGB(x, y, new Color(red, green, blue, alpha).getRGB());
				}
			}
			return new ImageIcon(resultImage);
		}

		protected Icon getFirstNonNullImageIcon(Icon defaultIcon, ResourcePath... iconPaths) {
			for (ResourcePath iconPath : iconPaths) {
				if (iconPath != null) {
					Image iconImage = SwingRendererUtils.loadImageThroughCache(iconPath,
							ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()));
					if (iconImage != null) {
						return new ImageIcon(iconImage);
					}
				}
			}
			return defaultIcon;
		}

		@Override
		public String toString() {
			return "CustomCheckBoxPlugin [data=" + data + "]";
		}
	}
}
