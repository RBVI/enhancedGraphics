package edu.ucsf.rbvi.enhancedcg.internal.charts.line;

import java.net.URL;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public class LineChartFactory implements CyCustomGraphicsFactory {
	private static final Class<? extends CyCustomGraphics> TARGET_CLASS = LineChart.class;

	public LineChartFactory() {
	}

	public CyCustomGraphics<LineLayer> getInstance(String input) {
		return new LineChart(input);
	}

	public CyCustomGraphics<LineLayer> getInstance(URL input) { return null; }

	public String getPrefix() { return "linechart"; }

	public Class<? extends CyCustomGraphics> getSupportedClass() { return TARGET_CLASS; }

	public CyCustomGraphics<LineLayer> parseSerializableString(String string) { return null; }

	public boolean supportsMime(String mimeType) { return false; }
}
