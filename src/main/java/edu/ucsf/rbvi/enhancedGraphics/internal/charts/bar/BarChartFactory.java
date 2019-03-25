package edu.ucsf.rbvi.enhancedGraphics.internal.charts.bar;

import java.net.URL;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public class BarChartFactory implements CyCustomGraphicsFactory {
	private static final Class<? extends CyCustomGraphics> TARGET_CLASS = BarChart.class;

	public BarChartFactory() {
	}

	public CyCustomGraphics<BarLayer> getInstance(String input) {
		return new BarChart(input);
	}

	public CyCustomGraphics<BarLayer> getInstance(URL input) { return null; }

	public String getPrefix() { return "barchart"; }

	public Class<? extends CyCustomGraphics> getSupportedClass() { return TARGET_CLASS; }

	public CyCustomGraphics<BarLayer> parseSerializableString(String string) { return null; }

	public boolean supportsMime(String mimeType) { return false; }
}
