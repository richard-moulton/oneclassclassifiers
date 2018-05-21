/*
 *    HSTrees.java
 *    Copyright (C) 2018
 *    @author Richard Hugh Moulton
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program. If not, see <http://www.gnu.org/licenses/>.
 *    
 */

package moa.classifiers.oneclass;

import com.github.javacliparser.FloatOption;
import com.github.javacliparser.IntOption;
import com.yahoo.labs.samoa.instances.Instance;

import moa.classifiers.AbstractClassifier;
import moa.classifiers.Classifier;
import moa.classifiers.OneClassClassifier;
import moa.core.Measurement;

/**
 * Implements the Streaming Half-Space Trees one-class classifier described in
 * S. C. Tan, K. M. Ting, and T. F. Liu, “Fast anomaly detection for streaming data,”
 * in IJCAI Proceedings-International Joint Conference on Artificial Intelligence,
 * 2011, vol. 22, no. 1, pp. 1511–1516.
 * 
 * @author Richard Hugh Moulton
 *
 */
public class HSTrees extends AbstractClassifier implements Classifier, OneClassClassifier
{
	private static final long serialVersionUID = 1L;

	public String getPurposeString()
	{
		return "HSTrees is a forest of Streaming Half-Space Trees.";
	}
	
	public IntOption windowSizeOption = new IntOption("windowSize", 'p', "The size of the landmark windows used.", 250);
	public IntOption numTreesOption = new IntOption("numberOfTrees", 't', "The number of trees in the ensemble.", 25);
	public IntOption maxDepthOption = new IntOption("maxDepth", 'h', "The maximum depth of the trees", 15);
	public FloatOption anomalyThresholdOption = new FloatOption("anomalyThreshold", 'a', 
			"Threshold (as a fraction of the maximum mass value) below which an instance is declared an anomaly.", 
			0.5, Double.MIN_VALUE, 1.0);
	public FloatOption sizeLimitOption = new FloatOption("sizeLimit", 's', "The minimum mass required in a node (as a "
			+ "fraction of the window size) to calculate the anomaly score.", 0.1, Double.MIN_VALUE, 1.0);
	
	// Respectively: the window size, the number of trees, the maximum depth of the trees,
	// the dimensionality of the data stream and the number of instances seen to date.
	/**
	 * The size of the landmark windows used - 'psi' in the original paper.
	 */
	private int windowSize;
	
	/**
	 * The number of trees in the ensemble - 't' in the original paper.
	 */
	private int numTrees;
	
	/**
	 * The maximum depth of the trees - 'maxDepth' in the original paper.
	 */
	private int maxDepth;
	
	/**
	 * The minimum mass required in a node (as a fraction of the window size) to calculate the anomaly score.
	 * 'sizeLimit' in the original paper.
	 */
	private double sizeLimit;
	
	/**
	 * The dimensionality of the data stream.
	 */
	private int dimensions;
	
	/**
	 * The number of instances from the data stream processed to date.
	 */
	private int numInstances;
	
	/**
	 * The threshold for declaring anomalies
	 */
	private double anomalyThreshold;
	
	/**
	 * The forest of HSTrees
	 */
	private HSTreeNode[] forest;
	
	/**
	 * If the classifier is in the first reference window: <b>true</b>, else: <b>false</b>
	 */
	private boolean referenceWindow;
	
	/**
	 * Reset the classifier's parameters and data structures.
	 */
	@Override
	public void resetLearningImpl()
	{
		this.windowSize = this.windowSizeOption.getValue();
		this.numTrees = this.numTreesOption.getValue();
		this.maxDepth = this.maxDepthOption.getValue();
		this.sizeLimit = this.sizeLimitOption.getValue();
		this.numInstances = 0;
		this.forest = new HSTreeNode[numTrees];
		this.referenceWindow = true;
		this.anomalyThreshold = this.anomalyThresholdOption.getValue();
	}

	/**
	 * Update the forest with the argument instance
	 * 
	 * @param inst the instance to pass to the forest
	 */
	@Override
	public void trainOnInstanceImpl(Instance inst)
	{
		this.numInstances++;
		// If this is the first instance, then initialize the forest.
		if(this.numInstances == 1)
		{
			//System.out.println("Build the forest...");
			this.dimensions = inst.numAttributes();
			double[]max = new double[dimensions];
			double[]min = new double[dimensions];
			double sq;
			
			for (int i = 0 ; i < this.numTrees ; i++)
			{
				for(int j = 0 ; j < this.dimensions ; j++)
				{
					sq = this.classifierRandom.nextDouble();
					min[j] = sq - (2.0*Math.max(sq, 1.0-sq));
					max[j] = sq + (2.0*Math.max(sq, 1.0-sq));
				}

				forest[i] = new HSTreeNode(min, max, 1, maxDepth);
			}
		}
		
		// Update the mass profile of every HSTree in the forest
		for(int i = 0 ; i < this.numTrees ; i++)
		{
			forest[i].updateMass(inst, referenceWindow);
		}
		
		if(this.numInstances > 50)
			referenceWindow = false;
		
		// If this is the last instance of the window, update every HSTree's model
		if(this.numInstances % windowSize == 0)
		{
			//if(this.numInstances == windowSize)
			//{
			//	referenceWindow = false;
			//}
			//else
			//{
				for(int i = 0 ; i < this.numTrees ; i++)
				{
					forest[i].updateModel();
				}
			//}
		}	
	}
	
	/**
	 * Combine the anomaly scores from each HSTree in the forest and convert into a vote score.
	 * 
	 * @param inst the instance to get votes for
	 * 
	 * @return the votes for the instance's label [normal, outlier]
	 */
	@Override
	public double[] getVotesForInstance(Instance inst)
	{
		double[] votes = {0.5, 0.5};
		//System.out.print("(HSTrees "+this.numInstances+" "+this.referenceWindow+")");
		//System.out.print("inst:");
		//for(int i = 0 ; i < inst.numAttributes() ; i++)
		//{
		//	System.out.print(" "+inst.value(i));
		//}
		//System.out.println();
		
		//System.out.print(inst.value(inst.numAttributes() - 1));
		
		if(!referenceWindow)
		{
			double accumulatedScore = 0.0;
			int massLimit = (int) (Math.ceil(this.sizeLimit*this.windowSize));
			double maxScore = this.windowSize * Math.pow(2.0, this.maxDepth);

			//System.out.println("accumulatedScore: 0.0");
			for(int i = 0 ; i < this.numTrees ; i++)
			{
				accumulatedScore += (forest[i].score(inst, massLimit) / maxScore);
				//System.out.println(" "+accumulatedScore);
			}
			//System.out.println();

			accumulatedScore = accumulatedScore / (((double) this.numTrees));
			//System.out.println("accumulatedScore / : "+accumulatedScore);
			votes[0] = accumulatedScore + 0.5 - this.anomalyThreshold;
			votes[1] = 1.0 - votes[0];
		}

		//System.out.println(", "+votes[0]);
		
		return votes;
	}

	/**
	 * HSTrees is randomizable.
	 */
	@Override
	public boolean isRandomizable()
	{
		return true;
	}
	
	@Override
	protected Measurement[] getModelMeasurementsImpl()
	{
		return null;
	}

	@Override
	public void getModelDescription(StringBuilder out, int indent)
	{

	}

}