package org.cytoscape.cyniDreamFunchisq.internal.CkMeansDiscretization;

import java.io.IOException;
import java.util.*;

import org.cytoscape.cyni.CyniAlgorithmContext;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.TunableValidator.ValidationState;
import org.cytoscape.work.util.*;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;

public class DreamCkMeansAlgorithmContext extends CyniAlgorithmContext implements TunableValidator {
	
	@Tunable(description="Interval selection", groups="Interval Definition", xorChildren=true, gravity=1.0)
	public ListSingleSelection<String> intervalChoice = new ListSingleSelection<String>(ONE_INTERVAL,RANGE_INTERVALS);
	
	@Tunable(description="Intervals",groups={"Interval Definition","One Interval Definition"},xorKey="One interval", gravity=2.0)
	public int bins = 5;
	
	@Tunable(description="Minimum intervals",groups={"Interval Definition","Range Interval Definition"},xorKey="Range of intervals", gravity=3.0)
	public int minK = 1;
	@Tunable(description="Maximum intervals",groups={"Interval Definition","Range Interval Definition"},xorKey="Range of intervals", gravity=4.0)
	public int maxK = 5;
	@Tunable(description="Apply same discretization thresholds for all selected attributes", gravity=5.0)
	public Boolean all = true;

	@Tunable(description="Numerical Attributes", groups="Attributes to discretize", gravity=6.0)
	public ListMultipleSelection<String> attributeList;
	
	private List<String> attributes;
	public static String ONE_INTERVAL = "One interval";
	public static String RANGE_INTERVALS = "Range of intervals";

	public DreamCkMeansAlgorithmContext( CyTable table) {
		super(true);
	    attributes = getAllAttributesNumbers(table);
		if(attributes.size() > 0)
		{
			attributeList = new  ListMultipleSelection<String>(attributes);
		}
		else
		{
			attributeList = new  ListMultipleSelection<String>("No sources available");
		}
	}
	
	@Override
	public ValidationState getValidationState(final Appendable errMsg) {
		if (bins > 0 && !attributeList.getPossibleValues().get(0).matches("No sources available")  && attributeList.getSelectedValues().size() >0)
			return ValidationState.OK;
		else {
			try {
				if (bins <= 0)
					errMsg.append("Intervals parameter needs to be greater than 0.\n");
				if(attributeList.getSelectedValues().size() == 0)
					errMsg.append("There are no numerical attributes selected to apply the Discretization Algorithm. Please, select them.\n");
				else if(attributeList.getSelectedValues().get(0).matches("No sources available"))
					errMsg.append("There are no numerical attributes available to apply the Discretization Algorithm.\n");
			} catch (IOException e) {
				e.printStackTrace();
				return ValidationState.INVALID;
			}
			return ValidationState.INVALID;
		}
	}
}
