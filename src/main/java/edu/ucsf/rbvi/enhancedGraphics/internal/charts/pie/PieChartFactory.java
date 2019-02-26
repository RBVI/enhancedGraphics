package edu.ucsf.rbvi.enhancedGraphics.internal.charts.pie;

import java.net.URL;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public class PieChartFactory implements CyCustomGraphicsFactory {
	private static final Class<? extends CyCustomGraphics> TARGET_CLASS = PieChart.class;

	public PieChartFactory() {
	}

	public CyCustomGraphics<PieLayer> getInstance(String input) {
		return new PieChart(input);
	}

	public CyCustomGraphics<PieLayer> getInstance(URL input) { return null; }

	public String getPrefix() { return "piechart"; }

	public Class<? extends CyCustomGraphics> getSupportedClass() { return TARGET_CLASS; }

	public CyCustomGraphics<PieLayer> parseSerializableString(String string) { return null; }

	public boolean supportsMime(String mimeType) { return false; }
}
