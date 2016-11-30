package edu.ucsf.rbvi.enhancedGraphics.internal.charts.label;

import java.net.URL;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public class LabelFactory implements CyCustomGraphicsFactory {
	private static final Class<? extends CyCustomGraphics> TARGET_CLASS = Label.class;

	public LabelFactory() {
	}

	public CyCustomGraphics<LabelLayer> getInstance(String input) {
		return new Label(input);
	}

	public CyCustomGraphics<LabelLayer> getInstance(URL input) { return null; }

	public String getPrefix() { return "label"; }

	public Class<? extends CyCustomGraphics> getSupportedClass() { return TARGET_CLASS; }

	public CyCustomGraphics<LabelLayer> parseSerializableString(String string) { return null; }

	public boolean supportsMime(String mimeType) { return false; }
}
