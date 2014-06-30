
package fr.systemsbiology.cyniDreamFunchisq.internal.FunChisqInference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.systemsbiology.cyni.*;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;


/**
 * The BasicInduction provides a very simple Induction, suitable as
 * the default Induction for Cytoscape data readers.
 */
public class DreamFunchisqAlgorithm extends AbstractCyniAlgorithm {
	
	
	private CyTable selectedTable;
	/**
	 * Creates a new BasicInduction object.
	 */
	public DreamFunchisqAlgorithm() {
		super("dreamFunChisq","Dream8 FunChisq Inference", true, CyniCategory.INDUCTION);
	
	}

	public TaskIterator createTaskIterator(CyniAlgorithmContext context,CyTable table, CyNetworkFactory networkFactory, CyNetworkViewFactory networkViewFactory,
			CyNetworkManager networkManager,CyNetworkTableManager netTableMgr, CyRootNetworkManager rootNetMgr, VisualMappingManager vmMgr,
			CyNetworkViewManager networkViewManager, CyLayoutAlgorithmManager layoutManager, CyCyniMetricsManager metricsManager) {
		selectedTable = table;
		return new TaskIterator(new DreamFunchisqAlgorithmTask(getName(),(DreamFunchisqAlgorithmContext) context, networkFactory,networkViewFactory,
					networkManager,netTableMgr,rootNetMgr,vmMgr,networkViewManager,layoutManager,metricsManager, selectedTable));
	}
	
	public CyniAlgorithmContext createCyniContext(CyTable table, CyCyniMetricsManager metricsManager, TunableSetter tunableSetter,Map<String, Object> mparams) {
		CyniAlgorithmContext context;
		selectedTable = table;
		
		context = new DreamFunchisqAlgorithmContext(supportsSelectedOnly(), selectedTable, null);
		if(mparams != null && !mparams.isEmpty())
			tunableSetter.applyTunables(context, mparams);
		return context;
	}
	
}
