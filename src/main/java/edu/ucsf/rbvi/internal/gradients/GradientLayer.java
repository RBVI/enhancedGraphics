package edu.ucsf.rbvi.enhancedGraphics.internal.gradients;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public abstract class GradientLayer implements CustomGraphicLayer {
	protected List<Color> colorList;
	protected List<Float> stopList;

	protected Color[] colorArray;
	protected float[] stopArray;

	protected Paint paint;

	public GradientLayer(List<Color>colors, List<Float>stops) {
		colorList = colors;
		stopList = stops;

		colorArray = new Color[colorList.size()];
		stopArray = new float[colorList.size()];
		for (int index = 0; index < colorArray.length; index++) {
			colorArray[index] = colorList.get(index);
			stopArray[index] = stopList.get(index).floatValue();
		}
	}

	protected Point2D scale(Point2D point, Rectangle2D bound) { 
		double xvalue = point.getX() * bound.getWidth() + bound.getX();
		double yvalue = point.getY() * bound.getHeight() + bound.getY();
		return new Point2D.Float((float)xvalue, (float)yvalue);
	}

}

