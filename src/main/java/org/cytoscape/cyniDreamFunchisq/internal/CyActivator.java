package org.cytoscape.cyniDreamFunchisq.internal;

import org.cytoscape.application.swing.CySwingApplication;



import org.cytoscape.application.swing.CyAction;
import org.cytoscape.cyni.*;

import org.osgi.framework.BundleContext;

import org.cytoscape.cyniDreamFunchisq.internal.CkMeansDiscretization.*;
import org.cytoscape.cyniDreamFunchisq.internal.FunChisqInference.*;
import org.cytoscape.cyniDreamFunchisq.internal.FunChisqMetric.*;

import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.io.FileUtils;


public class CyActivator extends AbstractCyActivator {
	
	
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		//Define new Cyni Algorithm
		DreamFunchisqAlgorithm inferFun = new DreamFunchisqAlgorithm();
		DreamCkMeansAlgorithm discreteFun = new DreamCkMeansAlgorithm();
		FunChisqMetric metricFun = new FunChisqMetric();
		//Register new Cyni Algorithm
		registerService(bc,inferFun,CyCyniAlgorithm.class, new Properties());
		registerService(bc,discreteFun,CyCyniAlgorithm.class, new Properties());
		registerService(bc,metricFun,CyCyniMetric.class, new Properties());

		

	}
	@Override
	public void shutDown() {
	
	}
	
	
}

