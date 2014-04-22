/*
  File: EqualDiscretizationTask.java

  Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.cyniDreamFunchisq.internal.CkMeansDiscretization;



import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.cyni.*;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.model.CyTable;
import org.cytoscape.cyniDreamFunchisq.internal.CkMeansDiscretization.DreamKmeans_1d_dp.ClusterResult;





/**
 * The CyniSampleAlgorithmTask provides a simple example on how to create a cyni task
 */
public class DreamCkMeansAlgorithmTask extends AbstractCyniTask {
	
	private  final int bins;
	private final CyTable mytable;
	private final List<String> attributeArray;
	private final Boolean all, byCol;
	private List<String> columnsNames;
	private Component parent;
	private int kmin,kmax;
	

	/**
	 * Creates a new object.
	 */
	public DreamCkMeansAlgorithmTask(final String name, final DreamCkMeansAlgorithmContext context,CyTable selectedTable)
	{
		super(name, context);
		
		this.mytable = selectedTable;
		bins = context.bins;
		this.all = context.all;
		if(context.intervalChoice.getSelectedValue().matches(DreamCkMeansAlgorithmContext.RANGE_INTERVALS))
		{
			this.kmax = context.maxK;
			this.kmin = context.minK;
		}
		else
		{
			this.kmax = context.bins;
			this.kmin = context.bins;
		}
			
		this.attributeArray = context.attributeList.getSelectedValues();
		
		if(context.type.getSelectedValue().matches(DreamCkMeansAlgorithmContext.BY_COLUMNS))
			byCol = true;
		else
			byCol = false;
		
		parent = context.getParentSwingComponent();
		
		
	}

	/**
	 *  Perform actualtask.
	 */
	@Override
	final protected void doCyniTask(final TaskMonitor taskMonitor) {
		
		Double progress = 0.0d;
		Double step;
		String label = "";
		int pos = 0;
		CyColumn column;
		String newColName = "";
		ClusterResult clResult = null;
		columnsNames = new ArrayList<String>();
		double matrix[][];
		CyRow arrayRows[], row = null;
		Map<String,String> mapNamesCols = new HashMap<String,String>();
		String[] colNames;
		double[] data ;
		matrix = new double[mytable.getAllRows().size()][attributeArray.size()];
		DreamKmeans_1d_dp kmeans = new DreamKmeans_1d_dp();
		
		int rows = mytable.getAllRows().size();
		arrayRows = new CyRow[rows];
		int index = 0;
		colNames = attributeArray.toArray(new String[attributeArray.size()]);
   
        step = 1.0 /  attributeArray.size();
        
        taskMonitor.setTitle("Dream8 CkMeans Discretization Algorithm ");
        taskMonitor.setStatusMessage("Discretizating data...");
		taskMonitor.setProgress(progress);
		
		for(CyRow rowTemp :  mytable.getAllRows())
		{
			arrayRows[index] = rowTemp;
			for(int j=0;j<attributeArray.size();j++)
			{
				Class<?> type = mytable.getColumn(colNames[j]).getType();
				matrix[index][j] = (Double) rowTemp.get(colNames[j],  type);
			}
			
			index++;
		}
		
		if(all)
		{
			data = new double[rows*attributeArray.size()+1];
			pos = 1;
			
			for (int i= 0; i < attributeArray.size(); i++)
				for(int j = 0; j< rows;j++)
					data[pos++]= matrix[j][i];
			
			clResult = kmeans.kmeans_1d_dp(data, kmin, kmax);
			
			System.out.println("num clusters: " + Arrays.toString(clResult.size));
			
			System.out.println("means clusters: " + Arrays.toString(clResult.centers));
		
			System.out.println("withiness clusters: " + Arrays.toString(clResult.withinss));
		}
		
		int size1 = 0, size2 = 0;
		
		if(all || byCol)
		{
			size1 = colNames.length;
			size2 = arrayRows.length;
		}
		else
		{
			if(!byCol)
			{
				size2 = colNames.length;
				size1 = arrayRows.length;
			}
		}
	
		for (final String  columnName : attributeArray)
		 {
			
			column = mytable.getColumn(columnName);
			newColName = "nominal."+columnName;
			newColName = getUniqueColumnName(mytable,newColName);
			
			columnsNames.add(newColName);
			
			mytable.createColumn(newColName, String.class, false);
			
			mapNamesCols.put(columnName, newColName);
			
		 }
		
		for(int i=0;i < size1;i++)
		{
			
			if(!all)
			{
				if(byCol)
				{
					data = new double[rows+1];
					pos = 1;
					for (int t= 0; t < rows; t++)
						data[pos++] = matrix[t][i];
					
					newColName = mapNamesCols.get(colNames[i]);
				}
				else
				{
					row = arrayRows[i];
					data = new double[attributeArray.size()+1];
					pos = 1;
					for (int t= 0; t < attributeArray.size(); t++)
						data[pos++] = matrix[i][t];
				}
			
				clResult = kmeans.kmeans_1d_dp(data, kmin, kmax);
			}
			else
				newColName = mapNamesCols.get(colNames[i]);
			
			for ( int j = 0 ;j < size2 ; j++ ) 
			{
				index = j;
				
				if(all || byCol)
				{
					row = arrayRows[j];
					if(all)
						index  += i*rows;
				}
				
				if(!all && !byCol)
					newColName = mapNamesCols.get(colNames[j]);
				
				
				if( clResult != null)
					label = "Cluster " + clResult.cluster[index +1];
				
				row.set(newColName, label);
			}
			
			if(!all)
			{
				System.out.println("num clusters: " + Arrays.toString(clResult.size));
			
				System.out.println("means clusters: " + Arrays.toString(clResult.centers));
			
				System.out.println("withiness clusters: " + Arrays.toString(clResult.withinss));
			}
			 
			 progress = progress + step;
			 taskMonitor.setProgress(progress);
		 }
		
		if(!columnsNames.isEmpty())
		{
			outputMessage = "Discretization successful: " + columnsNames.size() + " new columns created in the chosen table. Their name has the prefix nominal. ";
			taskMonitor.setStatusMessage(outputMessage);
			if(parent != null)
			{
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						JOptionPane.showMessageDialog(parent, outputMessage, "Results", JOptionPane.INFORMATION_MESSAGE);
					}
				});
			}
		}

		
		taskMonitor.setProgress(1.0d);
		
	}
	
	private String getUniqueColumnName(CyTable table, final String preferredName) {
		if (table.getColumn(preferredName) == null)
			return preferredName;

		String newUniqueName;
		int i = 0;
		do {
			++i;
			newUniqueName = preferredName + "-" + i;
		} while (table.getColumn(newUniqueName) != null);

		return newUniqueName;
	}
	
	
	@Override
	public Object getResults(Class requestedType) {
		if(columnsNames == null)
			return "FAILED";
	
		if(requestedType.equals(List.class))
			return columnsNames;
		else if(requestedType.equals(String.class))
			return outputMessage;
		
		
		return "OK";
	}
	
	
}
