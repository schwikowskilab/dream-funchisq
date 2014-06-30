/*
  File: CyniSampleAlgorithm.java

  Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package fr.systemsbiology.cyniDreamFunchisq.internal.CkMeansDiscretization;


import java.util.*;



public class DreamKmeans_1d_dp  {
	
	final double M_PI = 3.14159265359;
	
	ClusterResult kmeans_1d_dp(double[] x, int Kmin, int Kmax)
	{
		// Input:
		//  x -- a vector of numbers, not necessarily sorted
		//  K -- the number of clusters expected
		// NOTE: All vectors in this program is considered starting at position 1,
		//       position 0 is not used.
		
		ClusterResult result = new ClusterResult();
		int N = x.length - 1;  // N: is the size of input vector
		
		double[]  x_sorted = x.clone();
		
		Arrays.sort(x_sorted);
		
		
		int nUnique = numberOfUnique(x_sorted);
		
		Kmax = nUnique < Kmax ? nUnique : Kmax;
		
		if(nUnique > 1) { // The case when not all elements are equal.
		
			double[][] D = new double[Kmax+1][N+1];
			int[][] B = new int[ (Kmax+1)][N+1];
			
			// Fill in dynamic programming matrix
			fill_dp_matrix(x_sorted, D, B);
			
			// Choose an optimal number of levels between Kmin and Kmax
			int Kopt = select_levels(x_sorted, B, Kmin, Kmax);
			
			if (Kopt < Kmax)  // Reform the dynamic programming matrix D and B
			{
			   // B.erase(B.begin()+ Kopt + 1, B.end());
		
				List<int[]> l = new ArrayList<int[]>(Arrays.asList(B));
				for(int i = (Kopt+1) ;i < B.length; i++)
					l.remove(B[i]);
				
				B = l.toArray(new int[][]{});
			}
			
			// Backtrack to find the clusters beginning and ending indices
			backtrack(x_sorted, B, result);
			
			// Perform clustering on the original data
			for(int i=1; i < x.length; i++) 
			{
			    int indexLeft = 1;
			    int indexRight;
			    
			    for (int k=1; k<result.size.length; k++) 
			    {
			        indexRight = indexLeft + result.size[k] - 1;
			        if ( x[i] <= x_sorted[indexRight] ) {
			            result.cluster[i] = k;
			            break;
			        }
			        indexLeft = indexRight + 1;
			    }
			}
		
		} else {  // A single cluster that contains all elements
		
			result.nClusters = 1;
			
			result.cluster = new int[N+1];
			
			Arrays.fill(result.cluster, 1);
			
			result.centers = Arrays.copyOf(result.centers,2);
			result.withinss = Arrays.copyOf(result.withinss,2);
			result.size = Arrays.copyOf(result.size,2);
			
			
			result.centers[1] = x[1];
			result.withinss[1] = 0.0;
			result.size[1] = N;
		
		}
		return result;
	}  //end of kmeans_1d_dp()
	
	/*
	x: One dimension vector to be clustered
	D: Distance matrix
	B: Backtrack matrix
	
	NOTE: All vector indices in this program start at position 1,
	position 0 is not used.
	*/
	void fill_dp_matrix(double[] x, double[][] D, int[][] B)
	{
		final boolean cubic = false;
		/* When cubic==1 (TRUE), the algorithm runs in cubic time of
		array length N; otherwise it runs in quadratic time.  The TRUE option is for
		testing purpose only. */
		
		int K = D.length - 1;
		int N = D[0].length - 1;
		
		for(int i=1;i<=K;i++) {
			D[i][1] = 0;
			B[i][1] = 1;
		}
		
		double mean_x1, mean_xj, d;
		
		for(int k=1; k<=K; k++) 
		{
			mean_x1 = x[1] ;
			
			for(int i=2; i<=N; i++) 
			{
			    if(k == 1) 
			    {
			        if(cubic) 
			        {
			            double sum=0, mean=0;
			            for(int t=1; t<x.length; t++)
			                sum+=x[t];
			            mean = sum/N;
			            
			            for(int t=1; t<x.length; t++)
			                D[1][t] += (x[t] - mean) * (x[t] - mean);
			        } else 
			        {
			            D[1][i] = D[1][i-1] + (i-1) / (double) i * (x[i] - mean_x1) * (x[i] - mean_x1);
			            mean_x1 = ((i-1) * mean_x1 + x[i]) / (double)i;
			        }
			        
			        B[1][i] = 1;
			        
			    } else 
			    {
			        
			        D[k][i] = -1;
			        d = 0;
			        mean_xj = 0;
			        
			        for(int j=i; j>=1; j--) 
			        {
			            
			            if(cubic) 
			            {
			                double sum=0.0, mean=0.0;
			                for(int a=j; a<=i; a++) 
			                    sum+=x[a];
			                
			                mean = sum/(i-j+1);
			                
			                for(int a=j;a<=i;a++) 
			                    d += (x[a] - mean) * (x[a] - mean);
			                
			                
			            } else 
			            {
			                d = d + (i-j) / (double) (i-j+1) * (x[j] - mean_xj)* (x[j] - mean_xj);
			                mean_xj = ( x[j] + (i-j)*mean_xj ) / (double)(i-j+1);
			            }
			            
			            if(D[k][i] == -1) 
			            { //initialization of D[k,i]
			                
			                if(j == 1) 
			                {
			                    D[k][i] = d;
			                    B[k][i] = j;
			                } else 
			                {
			                    D[k][i] = d + D[k-1][j-1];
			                    B[k][i] = j;
			                }
			                
			            } else {
			                if(j == 1) 
			                {
			                    if(d <= D[k][i]) 
			                    {
			                        D[k][i] = d;
			                        B[k][i] = j;
			                    }
			                } else  {
			                    if(d + D[k-1][j-1] < D[k][i])
			                    {
			                        D[k][i] = d + D[k-1][j-1];
			                        B[k][i] = j;
			                    }
			                }
			            }
			        }
			    }
			}
		}
	}
	
	void backtrack(double[] x,int[][] B,ClusterResult result)
	{
		int K = B.length - 1;
		int N = B[0].length - 1;
		int cluster_right = N;
		int cluster_left;
		
		result.nClusters = K;
		
		result.cluster = Arrays.copyOf(result.cluster,N+1);
		result.centers = Arrays.copyOf(result.centers,K+1);
		result.withinss = Arrays.copyOf(result.withinss,K+1);
		result.size = Arrays.copyOf(result.size,K+1);
		
		
		// Backtrack the clusters from the dynamic programming matrix
		for(int k=K; k>=1; k--) 
		{
			cluster_left = B[k][cluster_right];
			
			for(int i=cluster_left;i<=cluster_right;i++)
			    result.cluster[i] = k;
			
			double sum=0;
			
			for(int a=cluster_left;a<=cluster_right;a++)
			    sum+=x[a];
			
			result.centers[k] = sum/(cluster_right-cluster_left+1);
			
			for(int a=cluster_left;a<=cluster_right;a++)
			    result.withinss[k] += ((x[a] - result.centers[k]) * (x[a] - result.centers[k]));
			
			result.size[k] = cluster_right - cluster_left + 1;
			
			if(k > 1) 
			    cluster_right = cluster_left - 1;
			
		}
	}
	
	//Choose an optimal number of levels between Kmin and Kmax
	int select_levels(double[] x, int[][] B,int Kmin, int Kmax)
	{
		if (Kmin == Kmax) 
			return Kmin;
		
		
		final String method = "normal"; // "uniform" or "normal"
		
		int Kopt=Kmin;
		
		final int base = 1;  // The position of first element in x: 1 or 0.
		final int N = x.length - base;
		
		double maxBIC = 0;
		
		for(int K = Kmin; K <= Kmax; ++K) 
		{
		
			int [][] BK = new int[K+1][];
			for(int i = 0; i < (K+1); i++)
				BK[i] = B[i].clone();
		
			//int[][] BK = new int(B.begin(), B.begin()+K+1);
			
			ClusterResult result = new ClusterResult();
			// Backtrack the matrix to determine boundaries between the bins.
			backtrack(x, BK, result);
			
			int indexLeft = base;
			int indexRight;
			
			double loglikelihood = 0;
			double binLeft = 0, binRight = 0;
			
			for (int k=0; k<K; k++) { // Compute the likelihood
			
			    int numPointsInBin = result.size[k+base];
			    
			    indexRight = indexLeft + numPointsInBin - 1;
			    
			    
			    if(x[indexLeft] < x[indexRight]) {
			        binLeft = x[indexLeft];
			        binRight = x[indexRight];
			    } else if(x[indexLeft] == x[indexRight]) {
			        binLeft = ( indexLeft == base ) ?
			            x[base] : (x[indexLeft-1] + x[indexLeft]) / 2;
			        binRight = ( indexRight < N-1+base ) ?
			            (x[indexRight] + x[indexRight+1]) / 2 : x[N-1+base];
			    } else {
			        // cout << "ERROR: binLeft > binRight" << endl;
			    }
			    
			    double binWidth = binRight - binLeft;
			    
			    if(method == "uniform") {
			    
			        loglikelihood += numPointsInBin * Math.log(numPointsInBin / binWidth / N);
			
			    } else if(method == "normal") {
			
			        double mean = 0;
			        double variance = 0;
			
			        for (int i=indexLeft; i<=indexRight; ++i) {
			            mean += x[i];
			            variance += x[i] * x[i];
			        }
			        mean /= numPointsInBin;
			
			        if (numPointsInBin > 1) {
			            variance = (variance - numPointsInBin * mean * mean)
			                / (numPointsInBin - 1);
			        } else {
			            variance = 0;
			        }
			        
			        if (variance > 0) {
			            for (int i=indexLeft; i<=indexRight; ++i) {
			                loglikelihood += - (x[i] - mean) * (x[i] - mean)
			                    / (2.0 * variance);
			            }
			            loglikelihood += numPointsInBin
			                * (Math.log(numPointsInBin / (double) N)
			                   - 0.5 * Math.log ( 2 * M_PI * variance));
			        } else {
			            loglikelihood += numPointsInBin * Math.log(1.0 / binWidth / N);
			        }
			        
			    } else {
			        // cout << "ERROR: Wrong likelihood method" << endl;
			    }
			    
			    indexLeft = indexRight + 1;
			}
			
			double BIC = 0.0;
			
			// Compute the Bayesian information criterion
			if (method == "uniform") {
			    BIC = 2 * loglikelihood - (3 * K - 1) * Math.log((double)N);  // K-1
			} else if(method == "normal") {
			    BIC = 2 * loglikelihood - (3 * K - 1) * Math.log((double)N);  //(K*3-1)
			}
			
			
			if (K == Kmin) {
			    maxBIC = BIC;
			    Kopt = Kmin;
			} else {
			    if (BIC > maxBIC) {
			        maxBIC = BIC;
			        Kopt = K;
			    }
			}
		}
		
		
		return Kopt;
	}
	
	int numberOfUnique(double[] temp)
	{
		
		Set<Double> list = new HashSet<Double>();
		
		for(int i=0; i< temp.length ; i++)
			list.add(temp[i]);
		
		return list.size();
	}
	
	class ClusterResult {
		
	    public int nClusters;
	    public int[] cluster;  	/*record which cluster each point belongs to*/
	    public double[] centers;	/*record the center of each cluster*/
	    public double[] withinss;   /*within sum of distance square of each cluster*/
	    public int[] size;		/*size of each cluster*/
	    
	    public ClusterResult()
	    {
	    	cluster = new int[1];
	    	centers = new double[1];
	    	withinss = new double[1];
	    	this.size = new int[1];
	    }
	    public ClusterResult(int size)
	    {
	    	cluster = new int[size];
	    	centers = new double[size];
	    	withinss = new double[size];
	    	this.size = new int[size];
	    }
	};
	
}
