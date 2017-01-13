package edu.ucsf.rbvi.enhancedGraphics.internal.tasks;

import java.util.List;
import java.util.Properties;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

public class ListChartsTask extends AbstractTask implements ObservableTask {

	List<CyCustomGraphicsFactory> charts;
	public ListChartsTask(final List<CyCustomGraphicsFactory> charts) {
			this.charts = charts;
	}

	public void run(TaskMonitor monitor) {}

	public <R> R getResults(Class<? extends R> type) {
		if (type.equals(String.class)) {
			String response = "Available charts: \n";
			for (CyCustomGraphicsFactory chart: charts) {
					response += "    "+chart.getPrefix()+"\n";
			}
			return (R)response;
		}
		return null;
	}
}
