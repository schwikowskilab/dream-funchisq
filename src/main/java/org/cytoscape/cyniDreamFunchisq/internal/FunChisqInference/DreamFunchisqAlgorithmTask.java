
package org.cytoscape.cyniDreamFunchisq.internal.FunChisqInference;



import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.cyni.*;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;


/**
 * The BasicInduction provides a very simple Induction, suitable as
 * the default Induction for Cytoscape data readers.
 */
public class DreamFunchisqAlgorithmTask extends AbstractCyniTask {
	private double thresholdAddEdge;
	private boolean removeNodes = false;
	private boolean normalized;
	private final List<String> attributeArray;
	private final CyTable table;
	
	private CyLayoutAlgorithmManager layoutManager;
	private CyCyniMetricsManager metricsManager;
	private CyCyniMetric selectedMetric;
	private CyniNetworkUtils netUtils;
	private static int iteration = 0;

	/**
	 * Creates a new BasicInduction object.
	 */
	public DreamFunchisqAlgorithmTask(final String name, final DreamFunchisqAlgorithmContext context, CyNetworkFactory networkFactory, CyNetworkViewFactory networkViewFactory,
			CyNetworkManager networkManager,CyNetworkTableManager netTableMgr, CyRootNetworkManager rootNetMgr, VisualMappingManager vmMgr,
			CyNetworkViewManager networkViewManager,CyLayoutAlgorithmManager layoutManager, 
			CyCyniMetricsManager metricsManager, CyTable selectedTable)
	{
		super(name, context,networkFactory,networkViewFactory,networkManager, networkViewManager,netTableMgr,rootNetMgr, vmMgr);

		this.thresholdAddEdge = context.thresholdAddEdge;
		this.layoutManager = layoutManager;
		this.metricsManager = metricsManager;
		//this.removeNodes = context.removeNodes;
		if(context.type.getSelectedValue().matches(DreamFunchisqAlgorithmContext.CHI_SQUARE))
			normalized = false;
		else
			normalized = true;
		this.attributeArray = context.attributeList.getSelectedValues();
		this.table = selectedTable;
		this.netUtils = new CyniNetworkUtils(networkViewFactory,networkManager,networkViewManager,netTableMgr,rootNetMgr,vmMgr);
		iteration++;
		
	}

	/**
	 *  Perform actual Induction task.
	 *  This creates the Cyni Induction Task
	 */
	@Override
	final protected void doCyniTask(final TaskMonitor taskMonitor) {
		Integer numNodes = 1;
		CyNode node1,node2;
		CyEdge edge;
		CyLayoutAlgorithm layout;
		Double progress = 0.0d;
		Double step;
		int nRows,threadNumber;
		ArrayList<Integer> index = new ArrayList<Integer>();
		Map<Object,CyNode> mapRowNodes;
		CyNetwork networkSelected = null;
		CyNetworkView newNetworkView ;
		double threadResults[] = new double[nThreads];
		double result;
		boolean directional = false;
		int threadIndex[] = new int[nThreads];
		int start;
		threadNumber=0;
		Arrays.fill(threadResults, 0.0);
		selectedMetric = metricsManager.getCyniMetric("DreamFunchisqMetric");
		newNetwork = netFactory.createNetwork();
		networkSelected = netUtils.getNetworkAssociatedToTable(table);
		
		taskMonitor.setTitle("Dream8 FunChisq Inference Algorithm");
		taskMonitor.setStatusMessage("Applying FunChisq network inference to all possible node pairs...");
		taskMonitor.setProgress(progress);
		mapRowNodes = new HashMap<Object,CyNode>();
		index.add(0);
		// Create the CyniTable
		CyniTable data = selectedMetric.getCyniTable(table,attributeArray.toArray(new String[0]), false, false, selectedOnly);
		
		if(selectedMetric.getTagsList().contains(CyniMetricTags.DIRECTIONAL_METRIC.toString()))
			directional = true;
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("Normalized", normalized);
		selectedMetric.setParameters(params);
		
		nRows = data.nRows();
		step = 1.0 / nRows;
		
		threadResults = new double[nRows];
		threadIndex = new int[nRows];
		Arrays.fill(threadResults, 0.0);
		
		netUtils.setNetworkName(newNetwork, "Dream8 FunChisq Inference " + iteration);
		
		
		//netUtils.addColumns(networkSelected,newNetwork,table,CyNode.class, CyNetwork.LOCAL_ATTRS);
		netUtils.copyNodeColumns(newNetwork, table);
	
		netUtils.createNetworkColumn(newNetwork,"Metric", String.class, false);	
		netUtils.createEdgeColumn(newNetwork,"Score", Double.class, false);	
		newNetwork.getDefaultNetworkTable().getRow(newNetwork.getSUID()).set("Metric",selectedMetric.toString());
		
		selectedMetric.initMetric();
		
		// Create the thread pools
		ExecutorService executor = Executors.newFixedThreadPool(nThreads);

		for (int i = 0; i < nRows; i++) 
		{
			threadNumber = 0;
			if (cancelled)
				break;
			if(directional)
				start = 0;
			else
				start = i+1;
			for (int j = start; j < nRows; j++) 
			{
				if (cancelled)
					break;
				
				if(i == j)
					continue;
				index.set(0, j);
				executor.execute(new ThreadedGetMetric(data,i,index,threadNumber,threadResults));
				threadIndex[threadNumber] = j;
				threadNumber++;
			}
			executor.shutdown();
			// Wait until all threads are finish
			try {
	         	executor.awaitTermination(7, TimeUnit.DAYS);
	        } catch (Exception e) {}
			
			for(int pool = 0; pool< threadNumber;pool++)
			{
				result = threadResults[pool];
				//System.out.println("result: " + result);
				
				if(result <= thresholdAddEdge)
				{
					if(!mapRowNodes.containsKey(data.getRowLabel(i)))
					{
						node1 = newNetwork.addNode();
						netUtils.cloneNodeRow(newNetwork,table.getRow(data.getRowLabel(i)), node1);
						if(newNetwork.getRow(node1).get(CyNetwork.NAME,String.class ) == null || newNetwork.getRow(node1).get(CyNetwork.NAME,String.class ).isEmpty() == true)
						{
							if(table.getPrimaryKey().getType().equals(String.class) && networkSelected == null)
								newNetwork.getRow(node1).set(CyNetwork.NAME,table.getRow(data.getRowLabel(i)).get(table.getPrimaryKey().getName(),String.class));
							else
								newNetwork.getRow(node1).set(CyNetwork.NAME, "Node " + numNodes);
						}
						if(newNetwork.getRow(node1).get(CyNetwork.SELECTED,Boolean.class ) == true)
							newNetwork.getRow(node1).set(CyNetwork.SELECTED, false);
						mapRowNodes.put(data.getRowLabel(i),node1);
						numNodes++;
					}
					if(!mapRowNodes.containsKey(data.getRowLabel(threadIndex[pool])))
					{
						node2 = newNetwork.addNode();
						netUtils.cloneNodeRow(newNetwork,table.getRow(data.getRowLabel(threadIndex[pool])), node2);
						if(newNetwork.getRow(node2).get(CyNetwork.NAME,String.class ) == null || newNetwork.getRow(node2).get(CyNetwork.NAME,String.class ).isEmpty() == true)
						{
							if(table.getPrimaryKey().getType().equals(String.class) && networkSelected == null)
								newNetwork.getRow(node2).set(CyNetwork.NAME,table.getRow(data.getRowLabel(threadIndex[pool])).get(table.getPrimaryKey().getName(),String.class));
							else
								newNetwork.getRow(node2).set(CyNetwork.NAME, "Node " + numNodes);
						}
						if(newNetwork.getRow(node2).get(CyNetwork.SELECTED,Boolean.class ) == true)
							newNetwork.getRow(node2).set(CyNetwork.SELECTED, false);
						mapRowNodes.put(data.getRowLabel(threadIndex[pool]),node2);
						numNodes++;
					}
							
					if(!newNetwork.containsEdge(mapRowNodes.get(data.getRowLabel(i)), mapRowNodes.get(data.getRowLabel(threadIndex[pool]))) || directional)
					{
						edge = newNetwork.addEdge(mapRowNodes.get(data.getRowLabel(i)), mapRowNodes.get(data.getRowLabel(threadIndex[pool])), directional);
						newNetwork.getRow(edge).set("Score", threadResults[pool]);
						newNetwork.getRow(edge).set("name", newNetwork.getRow(mapRowNodes.get(data.getRowLabel(i))).get("name", String.class)
								+ " (FunChisq) " + newNetwork.getRow( mapRowNodes.get(data.getRowLabel(threadIndex[pool]))).get("name", String.class));
					}
				}
			}
			threadNumber = 0;
			executor = Executors.newFixedThreadPool(nThreads);

			progress = progress + step;
			taskMonitor.setProgress(progress);
		}
		
		if (!cancelled)
		{
			taskMonitor.setStatusMessage("New network " +  newNetwork.getRow(newNetwork).get(CyNetwork.NAME, String.class)+ " created");
			if(removeNodes)
				netUtils.removeNodesWithoutEdges(newNetwork);
			newNetworkView = netUtils.displayNewNetwork(newNetwork,networkSelected, false);
			taskMonitor.setProgress(1.0d);
			layout = layoutManager.getDefaultLayout();
			Object context = layout.getDefaultLayoutContext();
			insertTasksAfterCurrentTask(layout.createTaskIterator(newNetworkView, context, CyLayoutAlgorithm.ALL_NODE_VIEWS,""));
		}
	
	}
	
	private class ThreadedGetMetric implements Runnable {
		private ArrayList<Integer> index2;
		private int index1;
		private CyniTable tableData;
		private double results[];
		private int pos;
		
		ThreadedGetMetric(CyniTable data,int index1, ArrayList<Integer> parentsToIndex,int pos, double results[])
		{
			this.index2 = new ArrayList<Integer>( parentsToIndex);
			this.index1 = index1;
			this.tableData = data;
			this.pos = pos;
			this.results = results;
		}
		
		public void run() {
			results[pos] = selectedMetric.getMetric(tableData, tableData, index1, index2);

		}
		

	}
	

}
