/*******************************************************************************
 * Copyright (c) 2017 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.eavp.service.swtchart.demos.parts;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.eavp.service.swtchart.barcharts.BarChart;
import org.eclipse.eavp.service.swtchart.barcharts.BarSeriesData;
import org.eclipse.eavp.service.swtchart.barcharts.IBarSeriesData;
import org.eclipse.eavp.service.swtchart.barcharts.IBarSeriesSettings;
import org.eclipse.eavp.service.swtchart.converter.PassThroughConverter;
import org.eclipse.eavp.service.swtchart.converter.RelativeIntensityConverter;
import org.eclipse.eavp.service.swtchart.core.IChartSettings;
import org.eclipse.eavp.service.swtchart.core.IPrimaryAxisSettings;
import org.eclipse.eavp.service.swtchart.core.ISecondaryAxisSettings;
import org.eclipse.eavp.service.swtchart.core.ISeriesData;
import org.eclipse.eavp.service.swtchart.core.RangeRestriction;
import org.eclipse.eavp.service.swtchart.core.SecondaryAxisSettings;
import org.eclipse.eavp.service.swtchart.demos.Activator;
import org.eclipse.eavp.service.swtchart.demos.preferences.BarSeriesDataPreferencePage;
import org.eclipse.eavp.service.swtchart.demos.preferences.BarSeriesPreferenceConstants;
import org.eclipse.eavp.service.swtchart.demos.preferences.BarSeriesPreferencePage;
import org.eclipse.eavp.service.swtchart.demos.preferences.BarSeriesPrimaryAxesPreferencePage;
import org.eclipse.eavp.service.swtchart.demos.preferences.BarSeriesSecondaryAxesPreferencePage;
import org.eclipse.eavp.service.swtchart.demos.support.SeriesConverter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.swtchart.IAxis.Position;
import org.swtchart.IBarSeries.BarWidthStyle;
import org.swtchart.LineStyle;

public class BarSeries_Preferences_Part extends Composite {

	private BarChart barChart;
	private Map<RGB, Color> colors;

	@Inject
	public BarSeries_Preferences_Part(Composite parent) {
		super(parent, SWT.NONE);
		try {
			initialize();
		} catch(Exception e) {
			System.out.println(e);
		}
	}

	@Override
	public void dispose() {

		for(Color color : colors.values()) {
			color.dispose();
		}
		super.dispose();
	}

	private void initialize() throws Exception {

		this.setLayout(new GridLayout(1, true));
		/*
		 * Buttons
		 */
		Composite compositeButtons = new Composite(this, SWT.NONE);
		GridData gridDataComposite = new GridData(GridData.FILL_HORIZONTAL);
		gridDataComposite.horizontalAlignment = SWT.END;
		compositeButtons.setLayoutData(gridDataComposite);
		compositeButtons.setLayout(new GridLayout(2, false));
		//
		Button buttonOpenSettings = new Button(compositeButtons, SWT.PUSH);
		buttonOpenSettings.setToolTipText("Open the Settings");
		if(Activator.getDefault() != null) {
			buttonOpenSettings.setText("");
			buttonOpenSettings.setImage(Activator.getDefault().getImage(Activator.ICON_OPEN_SETTINGS));
		} else {
			buttonOpenSettings.setText("Settings");
		}
		buttonOpenSettings.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				IPreferencePage preferencePage = new BarSeriesPreferencePage();
				preferencePage.setTitle("Chart Settings");
				IPreferencePage preferencePrimaryAxesPage = new BarSeriesPrimaryAxesPreferencePage();
				preferencePrimaryAxesPage.setTitle("Primary Axes");
				IPreferencePage preferenceSecondaryAxesPage = new BarSeriesSecondaryAxesPreferencePage();
				preferenceSecondaryAxesPage.setTitle("Secondary Axes");
				IPreferencePage preferenceDataPage = new BarSeriesDataPreferencePage();
				preferenceDataPage.setTitle("Series Data");
				//
				PreferenceManager preferenceManager = new PreferenceManager();
				preferenceManager.addToRoot(new PreferenceNode("1", preferencePage));
				preferenceManager.addToRoot(new PreferenceNode("2", preferencePrimaryAxesPage));
				preferenceManager.addToRoot(new PreferenceNode("3", preferenceSecondaryAxesPage));
				preferenceManager.addToRoot(new PreferenceNode("4", preferenceDataPage));
				//
				PreferenceDialog preferenceDialog = new PreferenceDialog(Display.getDefault().getActiveShell(), preferenceManager);
				preferenceDialog.create();
				preferenceDialog.setMessage("Settings");
				if(preferenceDialog.open() == PreferenceDialog.OK) {
					MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Settings", "The settings have been set successfully. Please use the load button to refresh the chart.");
				}
			}
		});
		//
		Button buttonApplySettings = new Button(compositeButtons, SWT.PUSH);
		buttonApplySettings.setToolTipText("Apply the Settings");
		if(Activator.getDefault() != null) {
			buttonApplySettings.setText("");
			buttonApplySettings.setImage(Activator.getDefault().getImage(Activator.ICON_APPLY_SETTINGS));
		} else {
			buttonApplySettings.setText("Apply");
		}
		buttonApplySettings.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				try {
					applyChartSettings();
					applySeriesSettings();
				} catch(Exception e1) {
					System.out.println(e1);
				}
			}
		});
		//
		barChart = new BarChart(this, SWT.NONE);
		barChart.setLayoutData(new GridData(GridData.FILL_BOTH));
		//
		applyChartSettings();
		applySeriesSettings();
	}

	private void applyChartSettings() throws Exception {

		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		//
		Color colorHintRangeSelector = getColor(PreferenceConverter.getColor(preferenceStore, BarSeriesPreferenceConstants.P_COLOR_HINT_RANGE_SELECTOR));
		Color colorTitle = getColor(PreferenceConverter.getColor(preferenceStore, BarSeriesPreferenceConstants.P_TITLE_COLOR));
		Color colorBackground = getColor(PreferenceConverter.getColor(preferenceStore, BarSeriesPreferenceConstants.P_BACKGROUND));
		Color colorBackgroundChart = getColor(PreferenceConverter.getColor(preferenceStore, BarSeriesPreferenceConstants.P_BACKGROUND_CHART));
		Color colorBackgroundPlotArea = getColor(PreferenceConverter.getColor(preferenceStore, BarSeriesPreferenceConstants.P_BACKGROUND_PLOT_AREA));
		Color colorPrimaryXAxis = getColor(PreferenceConverter.getColor(preferenceStore, BarSeriesPreferenceConstants.P_PRIMARY_X_AXIS_COLOR));
		Color colorPrimaryYAxis = getColor(PreferenceConverter.getColor(preferenceStore, BarSeriesPreferenceConstants.P_PRIMARY_Y_AXIS_COLOR));
		Locale localePrimaryXAxis = new Locale(preferenceStore.getString(BarSeriesPreferenceConstants.P_PRIMARY_X_AXIS_DECIMAL_FORMAT_LOCALE));
		Locale localePrimaryYAxis = new Locale(preferenceStore.getString(BarSeriesPreferenceConstants.P_PRIMARY_Y_AXIS_DECIMAL_FORMAT_LOCALE));
		Color colorSecondaryXAxis = getColor(PreferenceConverter.getColor(preferenceStore, BarSeriesPreferenceConstants.P_SECONDARY_X_AXIS_COLOR));
		Color colorSecondaryYAxis = getColor(PreferenceConverter.getColor(preferenceStore, BarSeriesPreferenceConstants.P_SECONDARY_Y_AXIS_COLOR));
		Locale localeSecondaryXAxis = new Locale(preferenceStore.getString(BarSeriesPreferenceConstants.P_SECONDARY_X_AXIS_DECIMAL_FORMAT_LOCALE));
		Locale localeSecondaryYAxis = new Locale(preferenceStore.getString(BarSeriesPreferenceConstants.P_SECONDARY_Y_AXIS_DECIMAL_FORMAT_LOCALE));
		Color colorPositionMarker = getColor(PreferenceConverter.getColor(preferenceStore, BarSeriesPreferenceConstants.P_COLOR_POSITION_MARKER));
		Color colorCenterMarker = getColor(PreferenceConverter.getColor(preferenceStore, BarSeriesPreferenceConstants.P_COLOR_CENTER_MARKER));
		Color colorPositionLegend = getColor(PreferenceConverter.getColor(preferenceStore, BarSeriesPreferenceConstants.P_COLOR_POSITION_LEGEND));
		//
		IChartSettings chartSettings = barChart.getChartSettings();
		chartSettings.setEnableRangeSelector(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_ENABLE_RANGE_SELECTOR));
		chartSettings.setColorHintRangeSelector(colorHintRangeSelector);
		chartSettings.setRangeSelectorDefaultAxisX(preferenceStore.getInt(BarSeriesPreferenceConstants.P_RANGE_SELECTOR_DEFAULT_AXIS_X));
		chartSettings.setRangeSelectorDefaultAxisY(preferenceStore.getInt(BarSeriesPreferenceConstants.P_RANGE_SELECTOR_DEFAULT_AXIS_Y));
		chartSettings.setVerticalSliderVisible(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_VERTICAL_SLIDER_VISIBLE));
		chartSettings.setHorizontalSliderVisible(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_HORIZONTAL_SLIDER_VISIBLE));
		chartSettings.setTitle(preferenceStore.getString(BarSeriesPreferenceConstants.P_TITLE));
		chartSettings.setTitleVisible(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_TITLE_VISIBLE));
		chartSettings.setTitleColor(colorTitle);
		chartSettings.setLegendPosition(preferenceStore.getInt(BarSeriesPreferenceConstants.P_LEGEND_POSITION));
		chartSettings.setLegendVisible(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_LEGEND_VISIBLE));
		chartSettings.setOrientation(preferenceStore.getInt(BarSeriesPreferenceConstants.P_ORIENTATION));
		chartSettings.setBackground(colorBackground);
		chartSettings.setBackgroundChart(colorBackgroundChart);
		chartSettings.setBackgroundPlotArea(colorBackgroundPlotArea);
		chartSettings.setEnableCompress(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_ENABLE_COMPRESS));
		RangeRestriction rangeRestriction = chartSettings.getRangeRestriction();
		rangeRestriction.setZeroX(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_ZERO_X));
		rangeRestriction.setZeroY(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_ZERO_Y));
		rangeRestriction.setRestrictZoom(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_RESTRICT_ZOOM));
		rangeRestriction.setXZoomOnly(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_X_ZOOM_ONLY));
		rangeRestriction.setYZoomOnly(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_Y_ZOOM_ONLY));
		rangeRestriction.setForceZeroMinY(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_FORCE_ZERO_MIN_Y));
		rangeRestriction.setFactorExtendMinX(preferenceStore.getDouble(BarSeriesPreferenceConstants.P_FACTOR_EXTEND_MIN_X));
		rangeRestriction.setFactorExtendMaxX(preferenceStore.getDouble(BarSeriesPreferenceConstants.P_FACTOR_EXTEND_MAX_X));
		rangeRestriction.setFactorExtendMinY(preferenceStore.getDouble(BarSeriesPreferenceConstants.P_FACTOR_EXTEND_MIN_Y));
		rangeRestriction.setFactorExtendMaxY(preferenceStore.getDouble(BarSeriesPreferenceConstants.P_FACTOR_EXTEND_MAX_Y));
		//
		chartSettings.setShowPositionMarker(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_SHOW_POSITION_MARKER));
		chartSettings.setColorPositionMarker(colorPositionMarker);
		chartSettings.setShowCenterMarker(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_SHOW_CENTER_MARKER));
		chartSettings.setColorCenterMarker(colorCenterMarker);
		chartSettings.setShowPositionLegend(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_SHOW_POSITION_LEGEND));
		chartSettings.setColorPositionLegend(colorPositionLegend);
		chartSettings.setCreateMenu(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_CREATE_MENU));
		/*
		 * Primary X-Axis
		 */
		IPrimaryAxisSettings primaryAxisSettingsX = chartSettings.getPrimaryAxisSettingsX();
		primaryAxisSettingsX.setTitle(preferenceStore.getString(BarSeriesPreferenceConstants.P_PRIMARY_X_AXIS_TITLE));
		primaryAxisSettingsX.setDescription(preferenceStore.getString(BarSeriesPreferenceConstants.P_PRIMARY_X_AXIS_DESCRIPTION));
		primaryAxisSettingsX.setDecimalFormat(new DecimalFormat((preferenceStore.getString(BarSeriesPreferenceConstants.P_PRIMARY_X_AXIS_DECIMAL_FORMAT_PATTERN)), new DecimalFormatSymbols(localePrimaryXAxis)));
		primaryAxisSettingsX.setColor(colorPrimaryXAxis);
		primaryAxisSettingsX.setPosition(Position.valueOf(preferenceStore.getString(BarSeriesPreferenceConstants.P_PRIMARY_X_AXIS_POSITION)));
		primaryAxisSettingsX.setVisible(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_PRIMARY_X_AXIS_VISIBLE));
		primaryAxisSettingsX.setGridLineStyle(LineStyle.valueOf(preferenceStore.getString(BarSeriesPreferenceConstants.P_PRIMARY_X_AXIS_GRID_LINE_STYLE)));
		primaryAxisSettingsX.setEnableLogScale(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_PRIMARY_X_AXIS_ENABLE_LOG_SCALE));
		/*
		 * Primary Y-Axis
		 */
		IPrimaryAxisSettings primaryAxisSettingsY = chartSettings.getPrimaryAxisSettingsY();
		primaryAxisSettingsY.setTitle(preferenceStore.getString(BarSeriesPreferenceConstants.P_PRIMARY_Y_AXIS_TITLE));
		primaryAxisSettingsY.setDescription(preferenceStore.getString(BarSeriesPreferenceConstants.P_PRIMARY_Y_AXIS_DESCRIPTION));
		primaryAxisSettingsY.setDecimalFormat(new DecimalFormat((preferenceStore.getString(BarSeriesPreferenceConstants.P_PRIMARY_Y_AXIS_DECIMAL_FORMAT_PATTERN)), new DecimalFormatSymbols(localePrimaryYAxis)));
		primaryAxisSettingsY.setColor(colorPrimaryYAxis);
		primaryAxisSettingsY.setPosition(Position.valueOf(preferenceStore.getString(BarSeriesPreferenceConstants.P_PRIMARY_Y_AXIS_POSITION)));
		primaryAxisSettingsY.setVisible(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_PRIMARY_Y_AXIS_VISIBLE));
		primaryAxisSettingsY.setGridLineStyle(LineStyle.valueOf(preferenceStore.getString(BarSeriesPreferenceConstants.P_PRIMARY_Y_AXIS_GRID_LINE_STYLE)));
		primaryAxisSettingsY.setEnableLogScale(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_PRIMARY_Y_AXIS_ENABLE_LOG_SCALE));
		/*
		 * Secondary X-Axes
		 */
		chartSettings.getSecondaryAxisSettingsListX().clear();
		ISecondaryAxisSettings secondaryAxisSettingsX = new SecondaryAxisSettings(preferenceStore.getString(BarSeriesPreferenceConstants.P_SECONDARY_X_AXIS_TITLE), new PassThroughConverter());
		secondaryAxisSettingsX.setDescription(preferenceStore.getString(BarSeriesPreferenceConstants.P_SECONDARY_X_AXIS_DESCRIPTION));
		secondaryAxisSettingsX.setDecimalFormat(new DecimalFormat((preferenceStore.getString(BarSeriesPreferenceConstants.P_SECONDARY_X_AXIS_DECIMAL_FORMAT_PATTERN)), new DecimalFormatSymbols(localeSecondaryXAxis)));
		secondaryAxisSettingsX.setColor(colorSecondaryXAxis);
		secondaryAxisSettingsX.setPosition(Position.valueOf(preferenceStore.getString(BarSeriesPreferenceConstants.P_SECONDARY_X_AXIS_POSITION)));
		secondaryAxisSettingsX.setVisible(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_SECONDARY_X_AXIS_VISIBLE));
		secondaryAxisSettingsX.setGridLineStyle(LineStyle.valueOf(preferenceStore.getString(BarSeriesPreferenceConstants.P_SECONDARY_X_AXIS_GRID_LINE_STYLE)));
		secondaryAxisSettingsX.setEnableLogScale(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_SECONDARY_X_AXIS_ENABLE_LOG_SCALE));
		chartSettings.getSecondaryAxisSettingsListX().add(secondaryAxisSettingsX);
		/*
		 * Secondary Y-Axes
		 */
		chartSettings.getSecondaryAxisSettingsListY().clear();
		ISecondaryAxisSettings secondaryAxisSettingsY = new SecondaryAxisSettings(preferenceStore.getString(BarSeriesPreferenceConstants.P_SECONDARY_Y_AXIS_TITLE), new RelativeIntensityConverter(SWT.VERTICAL, true));
		secondaryAxisSettingsY.setDescription(preferenceStore.getString(BarSeriesPreferenceConstants.P_SECONDARY_Y_AXIS_DESCRIPTION));
		secondaryAxisSettingsY.setDecimalFormat(new DecimalFormat((preferenceStore.getString(BarSeriesPreferenceConstants.P_SECONDARY_Y_AXIS_DECIMAL_FORMAT_PATTERN)), new DecimalFormatSymbols(localeSecondaryYAxis)));
		secondaryAxisSettingsY.setColor(colorSecondaryYAxis);
		secondaryAxisSettingsY.setPosition(Position.valueOf(preferenceStore.getString(BarSeriesPreferenceConstants.P_SECONDARY_Y_AXIS_POSITION)));
		secondaryAxisSettingsY.setVisible(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_SECONDARY_Y_AXIS_VISIBLE));
		secondaryAxisSettingsY.setGridLineStyle(LineStyle.valueOf(preferenceStore.getString(BarSeriesPreferenceConstants.P_SECONDARY_Y_AXIS_GRID_LINE_STYLE)));
		secondaryAxisSettingsY.setEnableLogScale(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_SECONDARY_Y_AXIS_ENABLE_LOG_SCALE));
		chartSettings.getSecondaryAxisSettingsListY().add(secondaryAxisSettingsY);
		//
		barChart.applySettings(chartSettings);
	}

	private void applySeriesSettings() {

		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		Color barColorSeries1 = getColor(PreferenceConverter.getColor(preferenceStore, BarSeriesPreferenceConstants.P_BAR_COLOR_SERIES_1));
		//
		barChart.deleteSeries();
		List<IBarSeriesData> barSeriesDataList = new ArrayList<IBarSeriesData>();
		ISeriesData seriesData;
		IBarSeriesData barSeriesData;
		IBarSeriesSettings barSerieSettings;
		/*
		 * Series 1
		 */
		seriesData = SeriesConverter.getSeriesXY(SeriesConverter.BAR_SERIES_1);
		barSeriesData = new BarSeriesData(seriesData);
		barSerieSettings = barSeriesData.getBarSeriesSettings();
		barSerieSettings.setDescription(preferenceStore.getString(BarSeriesPreferenceConstants.P_DESCRIPTION_SERIES_1));
		barSerieSettings.setVisible(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_VISIBLE_SERIES_1));
		barSerieSettings.setVisibleInLegend(preferenceStore.getBoolean(BarSeriesPreferenceConstants.P_VISIBLE_IN_LEGEND_SERIES_1));
		barSerieSettings.setBarColor(barColorSeries1);
		barSerieSettings.setBarPadding(preferenceStore.getInt(BarSeriesPreferenceConstants.P_BAR_PADDING_SERIES_1));
		barSerieSettings.setBarWidth(preferenceStore.getInt(BarSeriesPreferenceConstants.P_BAR_WIDTH_SERIES_1));
		barSerieSettings.setBarWidthStyle(BarWidthStyle.valueOf(preferenceStore.getString(BarSeriesPreferenceConstants.P_BAR_WIDTH_STYLE_SERIES_1)));
		barSeriesDataList.add(barSeriesData);
		//
		barChart.addSeriesData(barSeriesDataList);
	}

	private Color getColor(RGB rgb) {

		Color color = colors.get(rgb);
		if(color == null) {
			color = new Color(Display.getDefault(), rgb);
			colors.put(rgb, color);
		}
		return color;
	}
}