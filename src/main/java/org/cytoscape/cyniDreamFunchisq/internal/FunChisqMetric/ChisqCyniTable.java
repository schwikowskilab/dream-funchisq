package org.cytoscape.cyniDreamFunchisq.internal.FunChisqMetric;

import org.cytoscape.cyni.*;
import org.cytoscape.model.CyTable;

import java.util.*;



public class ChisqCyniTable extends CyniTable {
	
	List<Set<String>> rowListStringDiff;

	public ChisqCyniTable(  CyTable table, String[] attributes, boolean transpose, boolean ignoreMissing, boolean selectedOnly) {
		super(table,attributes,transpose,ignoreMissing,  selectedOnly);
		
		rowListStringDiff = new ArrayList<Set<String>>(nRows);
		
		for(int i=0;i< nRows;i++)
		{
			Set<String> newSet = new HashSet<String>();
			for(int j=0;j<nColumns;j++)
			{
				newSet.add(stringValue(i,j));
			}
			rowListStringDiff.add(i, newSet);
		}
		//createIdMapValues();
	}
	
	public ChisqCyniTable(CyniTable table)
	{
		super(table);
		
	}
	
	public Set<String> getSetValues(int row)
	{
		
		return rowListStringDiff.get(row);
	}
	
	

}


