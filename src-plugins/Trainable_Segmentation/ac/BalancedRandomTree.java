package ac;

/**
 *
 * License: GPL
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Authors: Ignacio Arganda-Carreras (iarganda@mit.edu), 
 * 	    Albert Cardona (acardona@ini.phys.ethz.ch)
 */

import ij.IJ;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

import weka.core.Instance;

/**
 * This class implements a random tree based on the split
 * function specified by the template in Splitter
 * 
 */
public class BalancedRandomTree implements Serializable
{
	/** Generated serial version UID */
	private static final long serialVersionUID = 41518309467L;
	/** root node */
	private final BaseNode rootNode;

	/**
	 * Build random tree for a balanced random forest  
	 * 
	 * @param ins The instances to use.
	 * @param splitter split function generator
	 */
	public BalancedRandomTree(final SortedInstances si, final Splitter splitter)
	{
		this.rootNode = createNode( si, splitter );
	}

	/**
	 * Build the random tree based on the data specified 
	 * in the constructor 
	 */
	private final BaseNode createNode(final SortedInstances si, final Splitter splitter)
	{
			
		final long start = System.currentTimeMillis();
		try {
			return createTree(si, 0, splitter);
		} finally {
			final long end = System.currentTimeMillis();
			IJ.log("Creating tree took: " + (end-start) + "ms");
		}
	}

	/**
	 * Evaluate sample
	 * 
	 * @param SortedInstances Pointer to all instances
	 * @param ith The index of the instance to evaluate
	 * @return array of class probabilities
	 */
	public double[] evaluate(final SortedInstances si, final int ith)
	{
		if (null == rootNode)
			return null;
		return rootNode.eval(si, ith);
	}

	public double[] evaluate(final Instance instance) {
		if (null == rootNode)
			return null;
		return rootNode.eval(instance);
	}

	/**
	 * Basic node of the tree
	 *
	 */
	abstract class BaseNode implements Serializable
	{

		/** serial version ID */
		private static final long serialVersionUID = 46734234231L;
		/**
		 * Evaluate an instance
		 * @param SortedInstances Pointer to all instances
		 * @param ith The index of the instance to evaluate
		 * @return class probabilities
		 */
		public abstract double[] eval( SortedInstances si, final int ith );

		/**
		 * Evaluate an instance
		 * @return class probabilities
		 */
		public abstract double[] eval( Instance instance );
		/**
		 * Get the node depth
		 * 
		 * @return tree depth at that node
		 */
		public int getDepth()
		{
			return 0;
		}
	} // end class BaseNode

	/**
	 * Leaf node in the tree 
	 *
	 */
	class LeafNode extends BaseNode implements Serializable
	{
		/** serial version ID */
		private static final long serialVersionUID = 2019873470157L;
		/** class probabilites */
		double[] probability;

		@Override
		public double[] eval(final SortedInstances si, final int ith)   // TODO isn't this an error? instance is ignored -- or it's just the end, so it's done?
		{		
			return probability;
		}
		@Override
		public double[] eval(final Instance ins)
		{		
			return probability;
		}
		/**
		 * Create a leaf node
		 * 
		 * @param probability class probabilities
		 */
		public LeafNode(double[] probability)
		{
			this.probability = probability;
		}

		/**
		 * Create leaf node based on the current split data
		 *  
		 * @param data pointer to original data
		 * @param indices indices at this node
		 */
		public LeafNode(
				final SortedInstances si,
				final int[] instanceIndices,
				final int instanceIndicesSize,
				final int numClasses)
		{
			this.probability = new double[ numClasses ];

			// Class values are accessed directly by index
			final double[] classValues = si.values[si.classIndex];

			for(int i=0; i<instanceIndicesSize; i++)
			{
				//this.probability[ (int) ins[i].classValue() ] ++;
				this.probability[ (int) classValues[instanceIndices[i]] ] ++;
			}
			// Divide by the number of elements
			for(int i=0; i<probability.length; i++)
				this.probability[i] /= instanceIndices.length;
		}

	} //end class LeafNode
	
	/**
	 * Interior node of the tree
	 *
	 */
	class InteriorNode extends BaseNode implements Serializable
	{
		/** serial version ID */
		private static final long serialVersionUID = 9972970234021L;
		/** left son */
		BaseNode left;
		/** right son */
		BaseNode right;
		/** node depth */
		final int depth;
		/** split function that divides the samples into left and right sons */
		final SplitFunction splitFn;

		/**
		 * Constructs an interior node of the random tree
		 * 
		 * @param depth tree depth at this node
		 * @param splitFn split function
		 */
		private InteriorNode(int depth, SplitFunction splitFn) 
		{
			this.depth = depth;
			this.splitFn = splitFn;
		}

		/**
		 * Evaluate sample at this node
		 */
		public double[] eval(final SortedInstances si, final int ith) 
		{
			if( null != right)
			{
				if (this.splitFn.evaluate( si, ith ) )
				{
					return left.eval( si, ith );
				}
				else
					return right.eval( si, ith );
			}
			else // leaves are always left nodes 
				return left.eval( si, ith );
		}

		/**
		 * Evaluate sample at this node
		 */
		public final double[] eval(final Instance instance)
		{
			if( null != right)
			{
				if(this.splitFn.evaluate( instance ) )
				{
					return left.eval(instance);
				}
				else
					return right.eval(instance);
			}
			else // leaves are always left nodes 
				return left.eval(instance);				
		}

		/**
		 * Get node depth
		 */
		public int getDepth()
		{
			return this.depth;
		}
	}

	/**
	 * Create random tree (non-recursively)
	 * 
	 * @param data original data
	 * @param indices indices of the samples to use
	 * @param depth starting depth
	 * @param splitFnProducer split function producer
	 * @return root node 
	 */
	private InteriorNode createTree(
			final SortedInstances si,
			final int depth,
			final Splitter splitFnProducer)
	{
		int maxDepth = depth;
		// Create root node
		int[] range = si.createIndexRange();
		InteriorNode root = new InteriorNode(depth, splitFnProducer.getSplitFunction(si, range, range.length));

		// Create list of nodes to process and add the root to it
		final LinkedList<InteriorNode> remainingNodes = new LinkedList<InteriorNode>();
		remainingNodes.add(root);
		
		// Create list of indices to process (it must match all the time with the node list)
		final LinkedList<int[]> remainingInstanceIndices = new LinkedList<int[]>();
		remainingInstanceIndices.add(range);
		final LinkedList<Integer> remainingSizes = new LinkedList<Integer>();
		remainingSizes.add(range.length);

		// Forget the array:
		range = null;

		// While there still are nodes to process
		while (!remainingNodes.isEmpty())
		{
			final InteriorNode currentNode = remainingNodes.removeFirst(); // remove first, to forget the large arrays quickly
			final int[] currentInstanceIndices = remainingInstanceIndices.removeFirst();
			final int currentSize = remainingSizes.removeFirst();

			// new arrays for the left and right branches
			final int[] leftArray = new int[currentSize];
			final int[] rightArray = new int[currentSize];
			int nextLeft = 0,
			    nextRight = 0;

			// split data
			for(int i=0; i < currentSize; i++)
			{
				if( currentNode.splitFn.evaluate( si, currentInstanceIndices[i] ) )
				{
					leftArray[nextLeft++] = currentInstanceIndices[i];
				}
				else
				{
					rightArray[nextRight++] = currentInstanceIndices[i];
				}
			}
			//System.out.println("total left = " + nextLeft + ", total right = " + nextRight + ", depth = " + currentNode.depth);					

			// Update maximum depth (for the record)
			if(currentNode.depth > maxDepth)
				maxDepth = currentNode.depth;

			if (0 == nextLeft)
			{
				currentNode.left = new LeafNode(si, rightArray, nextRight, si.numClasses);
				//System.out.println("Created leaf with feature " + currentNode.splitFn.index);
			}
			//else if ( rightArray.isEmpty() )
			else if (0 == nextRight)
			{
				currentNode.left = new LeafNode(si, leftArray, nextLeft, si.numClasses);
				//System.out.println("Created leaf with feature " + currentNode.splitFn.index);
			}
			else
			{
				currentNode.left = new InteriorNode(currentNode.depth+1, splitFnProducer.getSplitFunction(si, leftArray, nextLeft));
				remainingNodes.add((InteriorNode)currentNode.left);
				remainingInstanceIndices.add(leftArray);
				remainingSizes.add(nextLeft);

				currentNode.right = new InteriorNode(currentNode.depth+1, splitFnProducer.getSplitFunction(si, rightArray, nextRight));
				remainingNodes.add((InteriorNode)currentNode.right);
				remainingInstanceIndices.add(rightArray);
				remainingSizes.add(nextRight);
			}
		}

		//System.out.println("Max depth = " + maxDepth);
		return root;
	}
}
