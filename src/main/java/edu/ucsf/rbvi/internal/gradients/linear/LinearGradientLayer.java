package edu.ucsf.rbvi.enhancedcg.internal.gradients.linear;

import java.awt.Color;
import java.awt.LinearGradientPaint;
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

public class LinearGradientLayer extends GradientLayer {
	Point2D start = null;
	Point2D end = null;
	protected Rectangle2D rectangle = null;

	public LinearGradientLayer(List<Color> colors, List<Float> stops, Point2D start, Point2D end) {
		super(colors, stops);
		this.start = start;
		this.end = end;
		if (this.start == null)
			this.start = new Point2D.Float(0f, 0f);
		if (this.end == null)
			this.end = new Point2D.Float(1f, 0f);

		rectangle = new Rectangle(0, 0, 100, 100);
	}

	public Paint getPaint(Rectangle2D bounds) {
		this.paint = new LinearGradientPaint(scale(start,bounds), scale(end,bounds), stopArray, colorArray);
		return this.paint;
	}

	public Rectangle2D getBounds2D() { return rectangle; }
	public CustomGraphicLayer transform(AffineTransform xform) { 
		LinearGradientLayer newLayer = new LinearGradientLayer(colorList, stopList, start, end);
		newLayer.rectangle = xform.createTransformedShape(rectangle).getBounds2D();
		return newLayer;
	}
}
