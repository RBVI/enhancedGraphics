package edu.ucsf.rbvi.enhancedGraphics.internal.tasks;

import java.util.List;
import java.util.Properties;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ListChartsTaskFactory extends AbstractTaskFactory {

	List<CyCustomGraphicsFactory> charts;
	public ListChartsTaskFactory(final List<CyCustomGraphicsFactory> charts) {
			this.charts = charts;
	}

	public boolean isReady() {
		return true;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ListChartsTask(charts));
	}
}
