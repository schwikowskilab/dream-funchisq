package org.cytoscape.cyniDreamFunchisq.internal.FunChisqMetric;

import org.cytoscape.cyni.*;
import org.cytoscape.model.CyTable;

import java.util.*;


/**
 * Cyni Metric implementation of FunChisq metric
 */
public class FunChisqMetric extends AbstractCyniMetric {
	/**
	 * Creates a new  object.
	 */
	public FunChisqMetric() {
		super("DreamFunchisqMetric","Dream FunChisq Metric");
		addTag(CyniMetricTags.INPUT_STRINGS.toString());
		addTag(CyniMetricTags.DIRECTIONAL_METRIC.toString());
		addTag(CyniMetricTags.LOW_METRIC.toString());
		stats =  new StatDistributions();
		normalized = true;
	}
	
    private boolean normalized ;
    private StatDistributions stats;
     
	
	public Double getMetric(CyniTable table1, CyniTable table2, int indexBase, List<Integer> indexToCompare) { 
		double result = 0.0;
		ChisqCyniTable chisqTable1 = null;
		ChisqCyniTable chisqTable2 = null;
		int[][] table_obs;
       
		if(table1 instanceof ChisqCyniTable)
			chisqTable1 = (ChisqCyniTable) table1;
		if(table2 instanceof ChisqCyniTable)
			chisqTable2 = (ChisqCyniTable) table2;
		
		if(chisqTable1 == null)
			return result;
		
		int df;
		
		table_obs = createContengency(chisqTable1,indexBase,indexToCompare.get(0));
		
		int nrow =  table_obs.length;
		int K = (int) table_obs[0].length;
				
		if(K == 0)
			return result;
		
		double[] p_null = new double[K];//(K, 1.0/K); 
		Arrays.fill(p_null, 1.0/(double)K);
		int[] n_y = new int[K];  // the histogram of child y
		
		
		for(int j = 0; j < K; j++)
		{

			for(int i = 0; i < nrow; i++)
			{

				n_y[j] += table_obs[i][j];

			}//end for

		}//end for

		double chisquare = 0;

		df = K-1;
		
		for(int i=0; i<nrow; i++) {
			double chisq_row = 0;
			chisq_row = ChisquareTest1DNoPValue(table_obs[i], p_null, K);
			
			chisquare += chisq_row;
		}

		double chisq_y;
		//System.out.println("chisq_row: " + chisquare);
		chisq_y = ChisquareTest1DNoPValue(n_y, p_null, K);

		//System.out.println("chisq_y: " + chisq_y);
		chisquare -= chisq_y;

		df = (nrow-1) * (K-1);
		
		if(normalized) {
	        
	        chisquare = (chisquare - df) / Math.sqrt(2*df);
	        //System.out.println("chisq_f: " + chisquare);
	        result = stats.NormalPvalue(chisquare, 0, 1, false);
	        
	    } else {
	    	 //System.out.println("chisq_f: " + chisquare);
	    	result = stats.ChisqPvalue(chisquare, (int) df); // qchisq((int) df, chisquare);
	    }

		
		return  result;
	}

	public void setParameters(Map<String,Object> params){
		
		if(params.containsKey("Normalized"))
			normalized = (Boolean) params.get("Normalized");
			
	}
	
	@Override
	public  CyniTable getCyniTable( CyTable table, String[] attributes, boolean transpose, boolean ignoreMissing, boolean selectedOnly)
	{
		return  new ChisqCyniTable(table,attributes,transpose,ignoreMissing,selectedOnly);
	}
	
	public void initMetric()
	{
		
	}
	
	int[][] createContengency(ChisqCyniTable table, int index1, int index2)
	{
		
		int cols = table.nColumns();
		int id1,id2;
		int i= 0;
		Map<String,Integer> mapIds1, mapIds2;
		Set<String> tempSet1;
		Set<String> tempSet2;
		tempSet1 = table.getSetValues(index1);
		tempSet2 = table.getSetValues(index2);
		
		mapIds1 =  new HashMap<String,Integer>(tempSet1.size());
		mapIds2 =  new HashMap<String,Integer>(tempSet2.size());
		for(String value : tempSet1)
		{
			mapIds1.put(value, i);
			i++;
		}
		i = 0;
		for(String value : tempSet2)
		{
			mapIds2.put(value, i);
			i++;
		}
		int[][] content = new int[tempSet1.size()][tempSet2.size()];
		
		for( i =0;i < cols ; i++)
		{
			id1 = mapIds1.get(table.stringValue(index1, i));
			id2 = mapIds2.get(table.stringValue(index2, i));
			
			
			content[id1][id2]++;
			
		}
		
		
		return content;
	}
	
	double ChisquareTest1DNoPValue(final int[] x_obs, final double[] p_null, int K)
	{
		int N = 0;
		
		double chisq = 0;
		
		for(int k=0; k<K; k++) 
			N += x_obs[k];
		
		if(N <= 0) 
			return chisq;
		
		
		for(int k=0; k<K; k++) 
		{
			double x_exp = N * p_null[k];
			if(x_exp != 0) 
				chisq += (x_obs[k] - x_exp)*(x_obs[k] - x_exp)/ x_exp;
			else if(x_obs[k] != 0) 
				System.out.println("ERROR: expected is zero, but observed is not. Impossible!" );
				//exit(EXIT_FAILURE); 
			
		}
		
		return chisq;
	}

	
}
