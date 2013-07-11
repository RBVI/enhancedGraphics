package edu.ucsf.rbvi.enhancedcg.internal.gradients.radial;

import java.net.URL;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public class RadialGradientCGFactory implements CyCustomGraphicsFactory<RadialGradientLayer> {
	private static final Class<? extends CyCustomGraphics> TARGET_CLASS = RadialGradientCustomGraphics.class;

	public RadialGradientCGFactory() {
	}

	public CyCustomGraphics<RadialGradientLayer> getInstance(String input) {
		return new RadialGradientCustomGraphics(input);
	}

	public CyCustomGraphics<RadialGradientLayer> getInstance(URL input) { return null; }

	public String getPrefix() { return "radgrad"; }

	public Class<? extends CyCustomGraphics> getSupportedClass() { return TARGET_CLASS; }

	public CyCustomGraphics<RadialGradientLayer> parseSerializableString(String string) { return null; }

	public boolean supportsMime(String mimeType) { return false; }
}
