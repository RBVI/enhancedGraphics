package edu.ucsf.rbvi.enhancedcg.internal.gradients.radial;

import java.awt.Color;
import java.awt.RadialGradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.List;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

import edu.ucsf.rbvi.enhancedcg.internal.gradients.GradientLayer;

public class RadialGradientLayer extends GradientLayer {
	Point2D center = null;
	float radius = 1.0f;
	protected Rectangle2D rectangle = null;

	public RadialGradientLayer(List<Color> colors, List<Float> stops, Point2D center, float radius) {
		super(colors, stops);
		this.center = center;
		this.radius = radius;
		if (this.center == null)
			this.center = new Point2D.Float(0.5f, 0.5f);
		if (this.radius == 0.0f)
			this.radius = 1.0f;

		rectangle = new Rectangle(0, 0, 1, 1);
	}

	public Paint getPaint(Rectangle2D bounds) {
		// Assuming radius and center are of a unit circle, scale appropriately
		double xCenter = bounds.getWidth()*center.getX() + bounds.getX();
		double yCenter = bounds.getHeight()*center.getY() + bounds.getY();
		Point2D newCenter = new Point2D.Float((float)xCenter, 
		                                      (float)yCenter);
		double newRadius = radius * Math.min(bounds.getWidth(), bounds.getHeight());
		this.paint = new RadialGradientPaint(newCenter, (float)newRadius, stopArray, colorArray);
		return this.paint;
	}

	public Rectangle2D getBounds2D() { return rectangle; }
	public CustomGraphicLayer transform(AffineTransform xform) { 
		RadialGradientLayer newLayer = new RadialGradientLayer(colorList, stopList, center, radius);
		newLayer.rectangle = xform.createTransformedShape(rectangle).getBounds2D();
		return newLayer;
	}
}
