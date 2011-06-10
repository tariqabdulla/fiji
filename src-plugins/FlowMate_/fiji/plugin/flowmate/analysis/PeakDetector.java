package fiji.plugin.flowmate.analysis;

import java.util.ArrayList;

import mpicbg.imglib.algorithm.Algorithm;
import mpicbg.imglib.algorithm.Benchmark;

/**
 * A standard peak detector in time series.
 * <p>
 * The goal of this class is to identify peaks in a 1D time series (float[]).
 * It simply implements G.K. Palshikar's <i>Simple Algorithms for Peak Detection 
 * in Time-Series</i> ( Proc. 1st Int. Conf. Advanced Data Analysis, Business 
 * Analytics and Intelligence (ICADABAI2009), Ahmedabad, 6-7 June 2009),
 * We retained the first "spikiness" function he proposed, based on computing
 * the max signed distance to left and right neighbors.
 * <p>
 * <pre> 
 * 		http://sites.google.com/site/girishpalshikar/Home/mypublications/
 * 		SimpleAlgorithmsforPeakDetectioninTimeSeriesACADABAI_2009.pdf
 * </pre>
 * @author Jean-Yves Tinevez <jeanyves.tinevez@gmail.com> May 10, 2011
 */
public class PeakDetector implements Benchmark, Algorithm {

	private float[] T;
	private int[] peakArray;
	private int windowSize;
	private float stringency;
	private long processingTime;

	/**
	 * Create a peak detector for the given time series.
	 */
	public PeakDetector(final float[] timeSeries, int windowSize, float stringency) {
		this.T = timeSeries; 
		this.windowSize = windowSize;
		this.stringency = stringency;
	}
	
	/**
	 * Return the peak locations as array index for the time series set at creation.
	 * @param windowSize  the window size to look for peaks. a neighborhood of +/- windowSize
	 * will be inspected to search for peaks. Typical values start at 3.
	 * @param stringency  threshold for peak values. Peak with values lower than <code>
	 * mean + stringency * std</code> will be rejected. <code>Mean</code> and <code>std</code> are calculated on the 
	 * spikiness function. Typical values range from 1 to 3.
	 * @return an int array, with one element by retained peak, containing the index of 
	 * the peak in the time series array.
	 */
	@Override
	public boolean process() {
		
		long start = System.currentTimeMillis();
		
		// Compute peak function values
		float[] S = new float[T.length];
		float maxLeft, maxRight;
		for (int i = windowSize; i < S.length - windowSize; i++) {
			
			maxLeft = T[i] - T[i-1];
			maxRight = T[i] - T[i+1];
			for (int j = 2; j <= windowSize; j++) {
				if (T[i]-T[i-j] > maxLeft)
					maxLeft = T[i]-T[i-j];
				if (T[i]-T[i+j] > maxRight)
					maxRight = T[i]-T[i+j];
			}
			S[i] = 0.5f * (maxRight + maxLeft);
			
		}
		
		// Compute mean and std of peak function
		float mean = 0;
		int n = 0;
	    float M2 = 0;
	    float delta;
	    for (int i = 0; i < S.length; i++) {
	        n = n + 1;
	        delta = S[i] - mean;
	        mean = mean + delta/n;
	        M2 = M2 + delta*(S[i] - mean) ;
	    }

	    float variance = M2/(n - 1);
	    float std = (float) Math.sqrt(variance);
	    
	    // Collect only large peaks
	    ArrayList<Integer> peakLocations = new ArrayList<Integer>();
	    for (int i = 0; i < S.length; i++) {
	    	if (S[i] > 0 && (S[i]-mean) > stringency * std) {
	    		peakLocations.add(i);
	    	}
		}
	    
	    // Remove peaks too close
	    ArrayList<Integer> toPrune = new ArrayList<Integer>();
	    int peak1, peak2, weakerPeak;
	    for (int i = 0; i < peakLocations.size()-1; i++) {
			peak1 = peakLocations.get(i);
			peak2 = peakLocations.get(i+1);
			
			if (peak2 - peak1 < windowSize) {
				// Too close, prune the smallest one
				if (T[peak2] > T[peak1])
					weakerPeak = peak1;
				else 
					weakerPeak = peak2;
				toPrune.add(weakerPeak);
			}
		}
	    peakLocations.removeAll(toPrune);
	    
	    // Convert to int[]
	    peakArray = new int[peakLocations.size()];
	    for (int i = 0; i < peakArray.length; i++) {
			peakArray[i] = peakLocations.get(i);
		}
	    
	    processingTime = System.currentTimeMillis() - start;
	    return true;
	}
	
	public int[] getResults() {
		return peakArray;
	}

	@Override
	public boolean checkInput() {
		return true;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public long getProcessingTime() {
		return processingTime;
	}
	
	
}