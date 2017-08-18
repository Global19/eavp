/*******************************************************************************
 * Copyright (c) 2017 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.eavp.service.swtchart.linecharts;

import org.eclipse.eavp.service.swtchart.core.AbstractPointSeriesSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.swtchart.LineStyle;

public class LineSeriesSettings extends AbstractPointSeriesSettings implements ILineSeriesSettings {

	private int antialias;
	private boolean enableArea;
	private Color lineColor;
	private int lineWidth;
	private int lineWidthSelected;
	private boolean enableStack;
	private boolean enableStep;
	private LineStyle lineStyle;

	public LineSeriesSettings() {
		antialias = SWT.DEFAULT;
		enableArea = true;
		lineColor = Display.getDefault().getSystemColor(SWT.COLOR_RED);
		lineWidth = 1;
		lineWidthSelected = 2;
		enableStack = false;
		enableStep = false;
		lineStyle = LineStyle.SOLID;
	}

	@Override
	public int getAntialias() {

		return antialias;
	}

	@Override
	public void setAntialias(int antialias) {

		this.antialias = antialias;
	}

	@Override
	public boolean isEnableArea() {

		return enableArea;
	}

	@Override
	public void setEnableArea(boolean enableArea) {

		this.enableArea = enableArea;
	}

	@Override
	public Color getLineColor() {

		return lineColor;
	}

	@Override
	public void setLineColor(Color lineColor) {

		this.lineColor = lineColor;
	}

	@Override
	public int getLineWidth() {

		return lineWidth;
	}

	@Override
	public void setLineWidth(int lineWidth) {

		this.lineWidth = lineWidth;
	}

	@Override
	public int getLineWidthSelected() {

		return lineWidthSelected;
	}

	@Override
	public void setLineWidthSelected(int lineWidthSelected) {

		this.lineWidthSelected = lineWidthSelected;
	}

	@Override
	public boolean isEnableStack() {

		return enableStack;
	}

	@Override
	public void setEnableStack(boolean enableStack) {

		this.enableStack = enableStack;
	}

	@Override
	public boolean isEnableStep() {

		return enableStep;
	}

	@Override
	public void setEnableStep(boolean enableStep) {

		this.enableStep = enableStep;
	}

	@Override
	public LineStyle getLineStyle() {

		return lineStyle;
	}

	@Override
	public void setLineStyle(LineStyle lineStyle) {

		this.lineStyle = lineStyle;
	}
}