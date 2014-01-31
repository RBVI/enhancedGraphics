package edu.ucsf.rbvi.enhancedGraphics.internal.charts.stripe;

import java.net.URL;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public class StripeChartFactory implements CyCustomGraphicsFactory {
	private static final Class<? extends CyCustomGraphics> TARGET_CLASS = StripeChart.class;

	public StripeChartFactory() {
	}

	public CyCustomGraphics<StripeLayer> getInstance(String input) {
		return new StripeChart(input);
	}

	public CyCustomGraphics<StripeLayer> getInstance(URL input) { return null; }

	public String getPrefix() { return "stripechart"; }

	public Class<? extends CyCustomGraphics> getSupportedClass() { return TARGET_CLASS; }

	public CyCustomGraphics<StripeLayer> parseSerializableString(String string) { return null; }

	public boolean supportsMime(String mimeType) { return false; }
}
