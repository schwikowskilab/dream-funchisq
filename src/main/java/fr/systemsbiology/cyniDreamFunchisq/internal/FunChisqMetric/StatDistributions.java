package fr.systemsbiology.cyniDreamFunchisq.internal.FunChisqMetric;


import org.apache.commons.math3.special.Gamma;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;



public class StatDistributions  {
	double FPvalue(double Fstat, int df1, int df2)
	{
		FDistribution F = new FDistribution(df1, df2);
	    double pval = F.cumulativeProbability(Fstat);
	    return pval;
	}

	double ChisqPvalue(double x, int df)
	{
	    // Compute the quantity int_x^+infty [chisq(df, x)], which 
	    // can be used as the p-value in a chisquare test 
	    // assert(df >= 0 && x >= 0);
	    // modified by Yang Zhang 11.2.2010 x may be a slightly smaller than zero due to numerical issues
	    	    
	    assert(df >= 0);
	    
	    if(x <= 0)
	        return 1;
	    else
	        return Gamma.regularizedGammaQ(df/2.0, x/2.0);
	}

	double GammaPvalue(double x, double shape, double scale)
	{
		GammaDistribution g = new  GammaDistribution(shape, scale);
	    double pval = 1 - g.cumulativeProbability(x);
	    return pval;
	}

	double NormalPvalue(double x, double mu, double stdev, boolean two_sided)
	{
		NormalDistribution Normal = new NormalDistribution(mu, stdev);
		//Normaldist Normal = new Normaldist(mu, stdev);
	    double pval =0;
	    if(two_sided) {
	        if (x >= mu) {
	            pval = 2.0 * (1.0 - Normal.cumulativeProbability(x));
	        } else {
	            pval = 2.0 * Normal.cumulativeProbability(x);
	        }
	    } else {
	    	//System.out.println("cdf( " + x + "): " +  Normal.cumulativeProbability(x));
	        pval = 1.0 - Normal.cumulativeProbability(x);
	    	//pval = 1.0 - Normal.cdf(x);
	    }
	    return pval;
	}
	
	
}
