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
package xy.reflect.ui.control;

import java.util.concurrent.Future;

public abstract class ScheduledUpdateFieldControlData extends FieldControlDataProxy {

	protected Object scheduledFieldValue;
	protected boolean scheduling = false;

	protected abstract Future<?> scheduleUpdate(Runnable updateJob);

	public ScheduledUpdateFieldControlData(IFieldControlData base) {
		super(base);
	}

	@Override
	public Object getValue() {
		if (scheduling) {
			return scheduledFieldValue;
		} else {
			return base.getValue();
		}
	}

	@Override
	public void setValue(final Object newValue) {
		scheduledFieldValue = newValue;
		scheduling = true;
		scheduleUpdate(new Runnable() {
			@Override
			public void run() {
				try {
					base.setValue(scheduledFieldValue);
				} finally {
					scheduling = false;
				}
			}
		});
	}

}
