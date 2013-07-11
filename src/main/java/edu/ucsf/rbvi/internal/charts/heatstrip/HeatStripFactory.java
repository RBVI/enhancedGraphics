package edu.ucsf.rbvi.enhancedcg.internal.charts.heatstrip;

import java.net.URL;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public class HeatStripFactory implements CyCustomGraphicsFactory {
	private static final Class<? extends CyCustomGraphics> TARGET_CLASS = HeatStripChart.class;

	public HeatStripFactory() {
	}

	public CyCustomGraphics<HeatStripLayer> getInstance(String input) {
		return new HeatStripChart(input);
	}

	public CyCustomGraphics<HeatStripLayer> getInstance(URL input) { return null; }

	public String getPrefix() { return "heatstripchart"; }

	public Class<? extends CyCustomGraphics> getSupportedClass() { return TARGET_CLASS; }

	public CyCustomGraphics<HeatStripLayer> parseSerializableString(String string) { return null; }

	public boolean supportsMime(String mimeType) { return false; }
}
