package org.cytoscape.cyniDreamFunchisq.internal.FunChisqMetric;

import org.cytoscape.cyni.*;
import org.cytoscape.model.CyTable;

import java.util.*;



public class ChisqCyniTable extends CyniTable {
	
	Map<String,Integer> mapIds;

	public ChisqCyniTable(  CyTable table, String[] attributes, boolean transpose, boolean ignoreMissing, boolean selectedOnly) {
		super(table,attributes,transpose,ignoreMissing,  selectedOnly);
		
		mapIds =  new HashMap<String,Integer>(this.getAttributeStringValues().size());
		createIdMapValues();
	}
	
	public ChisqCyniTable(CyniTable table)
	{
		super(table);
		
	}
	
	private void createIdMapValues()
	{
		int i= 0;
		for(String value : getAttributeStringValues())
		{
			mapIds.put(value, i);
			i++;
		}
	}
	
	public int getIdForStringValue(String value)
	{
		
		if(mapIds.get(value) == null)
			return -1;
		else
			return mapIds.get(value);
	}
    

}


