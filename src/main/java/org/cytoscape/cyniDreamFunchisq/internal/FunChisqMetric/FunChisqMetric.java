package org.cytoscape.cyniDreamFunchisq.internal.FunChisqMetric;

import org.cytoscape.cyni.*;
import org.cytoscape.model.CyTable;

import java.util.*;


/**
 * The BasicInduction provides a very simple Induction, suitable as
 * the default Induction for Cytoscape data readers.
 */
public class FunChisqMetric extends AbstractCyniMetric {
	/**
	 * Creates a new  object.
	 */
	public FunChisqMetric() {
		super("DreamFunchisqMetric","Dream FunChisq Metric");
		addTag(CyniMetricTags.INPUT_STRINGS.toString());
		addTag(CyniMetricTags.DIRECTIONAL_METRIC.toString());
		size = 1;
		kernelWidth = 0;
	}
	
    private  int miSteps = 6;
    private double kernelWidth;
    private int size;
     
	
	public Double getMetric(CyniTable table1, CyniTable table2, int indexBase, List<Integer> indexToCompare) { 
		double result = 0.0;
		
       
		
		return  result;
	}

	public void setParameters(Map<String,Object> params){
		
		
		
		
	}
	
	@Override
	public  CyniTable getCyniTable( CyTable table, String[] attributes, boolean transpose, boolean ignoreMissing, boolean selectedOnly)
	{
		return  null;
	}
	
	public void initMetric()
	{
		
	}
	

	
}
