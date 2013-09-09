package edu.ucsf.rbvi.enhancedcg.internal;

import java.net.URL;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public class ClearFactory implements CyCustomGraphicsFactory {
	private static final Class<? extends CyCustomGraphics> TARGET_CLASS = ClearLayers.class;

	public ClearFactory() {
	}

	public CyCustomGraphics<ClearLayer> getInstance(String input) {
		return new ClearLayers(input);
	}

	public CyCustomGraphics<ClearLayer> getInstance(URL input) { return null; }

	public String getPrefix() { return "clear"; }

	public Class<? extends CyCustomGraphics> getSupportedClass() { return TARGET_CLASS; }

	public CyCustomGraphics<ClearLayer> parseSerializableString(String string) { return null; }

	public boolean supportsMime(String mimeType) { return false; }
}
