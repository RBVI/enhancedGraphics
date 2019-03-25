package edu.ucsf.rbvi.enhancedGraphics.internal.charts.circos;

import java.net.URL;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public class CircosChartFactory implements CyCustomGraphicsFactory {
	private static final Class<? extends CyCustomGraphics> TARGET_CLASS = CircosChart.class;

	public CircosChartFactory() {
	}

	public CyCustomGraphics<CircosLayer> getInstance(String input) {
		return new CircosChart(input);
	}

	public CyCustomGraphics<CircosLayer> getInstance(URL input) { return null; }

	public String getPrefix() { return "circoschart"; }

	public Class<? extends CyCustomGraphics> getSupportedClass() { return TARGET_CLASS; }

	public CyCustomGraphics<CircosLayer> parseSerializableString(String string) { return null; }

	public boolean supportsMime(String mimeType) { return false; }
}
