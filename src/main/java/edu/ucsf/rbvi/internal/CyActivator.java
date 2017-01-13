package edu.ucsf.rbvi.enhancedGraphics.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.osgi.framework.BundleContext;

import org.cytoscape.application.CyUserLog;
import org.apache.log4j.Logger;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.work.TaskFactory;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;

import edu.ucsf.rbvi.enhancedGraphics.internal.gradients.linear.LinearGradientCGFactory;
import edu.ucsf.rbvi.enhancedGraphics.internal.gradients.radial.RadialGradientCGFactory;
import edu.ucsf.rbvi.enhancedGraphics.internal.charts.bar.BarChartFactory;
import edu.ucsf.rbvi.enhancedGraphics.internal.charts.circos.CircosChartFactory;
import edu.ucsf.rbvi.enhancedGraphics.internal.charts.heatstrip.HeatStripFactory;
import edu.ucsf.rbvi.enhancedGraphics.internal.charts.label.LabelFactory;
import edu.ucsf.rbvi.enhancedGraphics.internal.charts.line.LineChartFactory;
import edu.ucsf.rbvi.enhancedGraphics.internal.charts.pie.PieChartFactory;
import edu.ucsf.rbvi.enhancedGraphics.internal.charts.stripe.StripeChartFactory;
import edu.ucsf.rbvi.enhancedGraphics.internal.tasks.ListChartsTaskFactory;


public class CyActivator extends AbstractCyActivator {
	final Logger logger = Logger.getLogger(CyUserLog.NAME);
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		// We'll eventually need the CyApplicationManager to get current network, etc.
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc,CyApplicationManager.class);

		List<CyCustomGraphicsFactory> charts = new ArrayList<CyCustomGraphicsFactory>();
		charts.add(new LinearGradientCGFactory());

		charts.add(new RadialGradientCGFactory());

		charts.add(new PieChartFactory());

		charts.add(new BarChartFactory());

		charts.add(new LabelFactory());

		charts.add(new LineChartFactory());

		charts.add(new StripeChartFactory());

		charts.add(new HeatStripFactory());

		charts.add(new CircosChartFactory());

		for (CyCustomGraphicsFactory chart: charts) {
				Properties chartProps = new Properties();
				registerService(bc, chart, CyCustomGraphicsFactory.class, chartProps);
		}

		{
			ListChartsTaskFactory listFactory = new ListChartsTaskFactory(charts);
			Properties listChartProps = new Properties();
			listChartProps.setProperty(COMMAND_NAMESPACE, "enhancedGraphics");
			listChartProps.setProperty(COMMAND, "list");
			registerService(bc, listFactory, TaskFactory.class, listChartProps);
		}

		// CyCustomGraphicsFactory clearFactory = new ClearFactory();

		logger.info("Enhanced Custom Graphics started");
	}
}

