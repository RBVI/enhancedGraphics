package edu.ucsf.rbvi.enhancedGraphics.internal.gradients.radial;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

import edu.ucsf.rbvi.enhancedGraphics.internal.AbstractEnhancedCustomGraphics;

public class RadialGradientCustomGraphics extends AbstractEnhancedCustomGraphics <RadialGradientLayer> {

	// Parse the input string, which is always of the form:
	// 	radgrad: center="x,y" radius="r" stoplist="r,g,b,a,stop|r,g,b,a,stop|r,g,b,a,stop"
	public RadialGradientCustomGraphics(String input) {
		Map<String, String> inputMap = parseInput(input);
		Point2D center = new Point2D.Float(0.5f, 0.5f);
		float radius = 1.0f;

		// Create our defaults
		List<Float> stopList = new ArrayList<Float>();
		List<Color> colorList = new ArrayList<Color>();
		int nStops = 0;

		if (inputMap.containsKey("center")) {
			center = parsePoint(inputMap.get("center"));
		}
		if (inputMap.containsKey("radius")) {
			radius = Float.parseFloat(inputMap.get("radius"));
		}
		if (inputMap.containsKey("stoplist")) {
			nStops = parseStopList(inputMap.get("stoplist"), colorList, stopList);
		}
		if (nStops == 0) {
			colorList.add(new Color(255,255,255,255));
			stopList.add(0.0f);
			colorList.add(new Color(100,100,100,100));
			stopList.add(1.0f);
		}
		RadialGradientLayer cg = new RadialGradientLayer(colorList, stopList, center, radius);
		layers.add(cg);
	}

	public String toSerializableString() { return this.getIdentifier().toString()+","+displayName; }

	public Image getRenderedImage() {
		CustomGraphicLayer cg = layers.get(0);
		// Create a rectangle and fill it with our current paint
		Rectangle bounds = cg.getBounds2D().getBounds();
		Shape shape = new Ellipse2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
		BufferedImage image = new BufferedImage(bounds.width, bounds.height,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = image.createGraphics();
		g2d.setPaint(cg.getPaint(bounds));
		g2d.fill(shape);
		return image;
	}
}
