package edu.ucsf.rbvi.enhancedGraphics.internal.charts.label;

import java.net.URL;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.PaintedShape;

public class LabelFactory implements CyCustomGraphicsFactory {
	private static final Class<? extends CyCustomGraphics> TARGET_CLASS = Label.class;

	public LabelFactory() {
	}

	public CyCustomGraphics<PaintedShape> getInstance(String input) {
		return new Label(input);
	}

	public CyCustomGraphics<PaintedShape> getInstance(URL input) { return null; }

	public String getPrefix() { return "label"; }

	public Class<? extends CyCustomGraphics> getSupportedClass() { return TARGET_CLASS; }

	public CyCustomGraphics<PaintedShape> parseSerializableString(String string) { return null; }

	public boolean supportsMime(String mimeType) { return false; }
}
