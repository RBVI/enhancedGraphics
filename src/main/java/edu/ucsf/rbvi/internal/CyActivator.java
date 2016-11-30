package edu.ucsf.rbvi.enhancedGraphics.internal;

import java.util.Properties;

import org.osgi.framework.BundleContext;

import org.cytoscape.application.CyUserLog;
import org.apache.log4j.Logger;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;

import edu.ucsf.rbvi.enhancedGraphics.internal.gradients.linear.LinearGradientCGFactory;
import edu.ucsf.rbvi.enhancedGraphics.internal.gradients.radial.RadialGradientCGFactory;
import edu.ucsf.rbvi.enhancedGraphics.internal.charts.bar.BarChartFactory;
import edu.ucsf.rbvi.enhancedGraphics.internal.charts.circos.CircosChartFactory;
import edu.ucsf.rbvi.enhancedGraphics.internal.charts.heatstrip.HeatStripFactory;
import edu.ucsf.rbvi.enhancedGraphics.internal.charts.label.LabelFactory;
import edu.ucsf.rbvi.enhancedGraphics.internal.charts.line.LineChartFactory;
import edu.ucsf.rbvi.enhancedGraphics.internal.charts.pie.PieChartFactory;
import edu.ucsf.rbvi.enhancedGraphics.internal.charts.stripe.StripeChartFactory;


public class CyActivator extends AbstractCyActivator {
	final Logger logger = Logger.getLogger(CyUserLog.NAME);
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		// We'll eventually need the CyApplicationManager to get current network, etc.
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc,CyApplicationManager.class);

		CyCustomGraphicsFactory linGradFactory = new LinearGradientCGFactory();
		Properties linGradProps = new Properties();
		registerService(bc, linGradFactory, CyCustomGraphicsFactory.class, linGradProps);

		CyCustomGraphicsFactory radGradFactory = new RadialGradientCGFactory();
		Properties radGradProps = new Properties();
		registerService(bc, radGradFactory, CyCustomGraphicsFactory.class, radGradProps);

		CyCustomGraphicsFactory pieChartFactory = new PieChartFactory();
		Properties pieChartProps = new Properties();
		registerService(bc, pieChartFactory, CyCustomGraphicsFactory.class, pieChartProps);

		CyCustomGraphicsFactory barChartFactory = new BarChartFactory();
		Properties barChartProps = new Properties();
		registerService(bc, barChartFactory, CyCustomGraphicsFactory.class, barChartProps);

		CyCustomGraphicsFactory labelFactory = new LabelFactory();
		Properties labelProps = new Properties();
		registerService(bc, labelFactory, CyCustomGraphicsFactory.class, labelProps);

		CyCustomGraphicsFactory lineChartFactory = new LineChartFactory();
		Properties lineChartProps = new Properties();
		registerService(bc, lineChartFactory, CyCustomGraphicsFactory.class, lineChartProps);

		CyCustomGraphicsFactory stripeChartFactory = new StripeChartFactory();
		Properties stripeChartProps = new Properties();
		registerService(bc, stripeChartFactory, CyCustomGraphicsFactory.class, stripeChartProps);

		CyCustomGraphicsFactory heatStripChartFactory = new HeatStripFactory();
		Properties heatStripChartProps = new Properties();
		registerService(bc, heatStripChartFactory, CyCustomGraphicsFactory.class, heatStripChartProps);

		CyCustomGraphicsFactory circosChartFactory = new CircosChartFactory();
		Properties circosChartProps = new Properties();
		registerService(bc, circosChartFactory, CyCustomGraphicsFactory.class, circosChartProps);

		// CyCustomGraphicsFactory clearFactory = new ClearFactory();
		// Properties clearProps = new Properties();
		// registerService(bc, clearFactory, CyCustomGraphicsFactory.class, clearProps);

		// CyCustomGraphicsFactory stripChartFactory = new StripChartCustomGraphicsFactory();
		logger.info("Enhanced Custom Graphics started");
	}
}

