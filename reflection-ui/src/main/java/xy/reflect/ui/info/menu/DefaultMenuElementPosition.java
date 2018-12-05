/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * The GNU General Public License allows you also to freely redistribute 
 * the libraries under the same license, if you provide the terms of the 
 * GNU General Public License with them and add the following 
 * copyright notice at the appropriate place (with a link to 
 * http://javacollection.net/reflectionui/ web site when possible).
 ******************************************************************************/
package xy.reflect.ui.info.menu;

public class DefaultMenuElementPosition implements IMenuElementPosition {

	protected DefaultMenuElementPosition parent;
	protected String elementName;
	protected MenuElementKind elementKind;

	public DefaultMenuElementPosition(String elementName, MenuElementKind elementKind, DefaultMenuElementPosition parent) {
		this.elementName = elementName;
		this.elementKind = elementKind;
		this.parent = parent;
	}

	public DefaultMenuElementPosition getRoot() {
		DefaultMenuElementPosition result = this;
		while (result.getParent() != null) {
			result = result.getParent();
		}
		return result;
	}

	@Override
	public DefaultMenuElementPosition getParent() {
		return parent;
	}

	public void setParent(DefaultMenuElementPosition parent) {
		this.parent = parent;
	}

	@Override
	public String getElementName() {
		return elementName;
	}

	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

	@Override
	public MenuElementKind getElementKind() {
		return elementKind;
	}

	public void setElementKind(MenuElementKind elementKind) {
		this.elementKind = elementKind;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((elementKind == null) ? 0 : elementKind.hashCode());
		result = prime * result + ((elementName == null) ? 0 : elementName.hashCode());
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
		DefaultMenuElementPosition other = (DefaultMenuElementPosition) obj;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (elementKind != other.elementKind)
			return false;
		if (elementName == null) {
			if (other.elementName != null)
				return false;
		} else if (!elementName.equals(other.elementName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DefaultMenuElementPosition [elementName=" + elementName + ", elementKind=" + elementKind + "]";
	}

}
