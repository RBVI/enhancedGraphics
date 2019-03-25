package edu.ucsf.rbvi.enhancedGraphics.internal.gradients.linear;

import java.net.URL;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public class LinearGradientCGFactory implements CyCustomGraphicsFactory <LinearGradientLayer> {
	private static final Class<? extends CyCustomGraphics> TARGET_CLASS = LinearGradientCustomGraphics.class;

	public LinearGradientCGFactory() {
	}

	public CyCustomGraphics<LinearGradientLayer> getInstance(String input) {
		return new LinearGradientCustomGraphics(input);
	}

	public CyCustomGraphics<LinearGradientLayer> getInstance(URL input) { return null; }

	public String getPrefix() { return "lingrad"; }

	public Class<? extends CyCustomGraphics> getSupportedClass() { return TARGET_CLASS; }

	public CyCustomGraphics<LinearGradientLayer> parseSerializableString(String string) { return null; }

	public boolean supportsMime(String mimeType) { return false; }
}
