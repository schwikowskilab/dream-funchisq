
package org.cytoscape.cyniDreamFunchisq.internal.FunChisqInference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cytoscape.work.util.*;
import org.cytoscape.model.CyTable;

import org.cytoscape.cyni.*;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;

public class DreamFunchisqAlgorithmContext extends CyniAlgorithmContext implements TunableValidator {
	@Tunable(description="P-Value threshold to add new edge", gravity=1.0)
	public double thresholdAddEdge = 0.05;
	
	@Tunable(description="Type of computation", gravity=2.0)
	public ListSingleSelection<String> type = new ListSingleSelection<String>(CHI_SQUARE,NORMALIZED);
	
	@Tunable(description="Use selected nodes only", groups="Parameters if a network associated to table data", gravity=3.0)
	public boolean selectedOnly = false;
	
	@Tunable(description="Data Attributes", groups="Sources for Network Inference", gravity=5.0)
	public ListMultipleSelection<String> attributeList;
	
	private List<String> attributes;
	private CyTable selectedTable;
	public static String CHI_SQUARE = "chi-square p-value";
	public static String NORMALIZED = "normalized chi-square";
	
	public DreamFunchisqAlgorithmContext(boolean supportsSelectedOnly, CyTable table,  List<CyCyniMetric> metrics) {
		super(supportsSelectedOnly);
		selectedTable = table;
		
		attributes = getAllAttributesStrings(table);
			
		if(attributes.size() > 0)
		{
			attributeList = new  ListMultipleSelection<String>(attributes);
			List<String> temp = new ArrayList<String>( attributes);
			temp.remove(table.getPrimaryKey().getName());
			if(!temp.isEmpty())
				attributeList.setSelectedValues(temp);
		}
		else
		{
			attributeList = new  ListMultipleSelection<String>("No sources available");
		}
	}
	
	@Override
	public ValidationState getValidationState(final Appendable errMsg) {
		setSelectedOnly(selectedOnly);
		if (thresholdAddEdge < 0.0)
		{
			try {
				errMsg.append("Threshold needs to be greater than 0.0!!!!");
			} catch (IOException e) {
				e.printStackTrace();
				return ValidationState.INVALID;
			}
			return ValidationState.INVALID;
		}
		
		if(attributeList.getPossibleValues().get(0).matches("No sources available") || attributeList.getSelectedValues().size() == 0) {
			try {
				errMsg.append("No sources selected to apply the algorithm or there are no available. Please, select sources from the list if available.");
			} catch (IOException e) {
				e.printStackTrace();
				return ValidationState.INVALID;
			}
			return ValidationState.INVALID;
			
		}
		return ValidationState.OK;
	}
}
