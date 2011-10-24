package fiji.plugin.cwnt.segmentation;

import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.util.TMUtils;
import ij.IJ;
import ij.ImagePlus;
import mpicbg.imglib.algorithm.MultiThreadedBenchmarkAlgorithm;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.display.imagej.ImageJFunctions;
import mpicbg.imglib.labeling.Labeling;

public class CWNTFrameSegmenter extends MultiThreadedBenchmarkAlgorithm {

	private CWNTPanel source;
	private ImagePlus imp;

	/*
	 * CONSTRUCTOR
	 */
	
	public CWNTFrameSegmenter(CWNTPanel panel) {
		super();
		this.source = panel;
		this.imp = panel.getTargetImagePlus();
	}
	
	
	/*
	 * METHODS
	 */
	
	@Override
	public boolean checkInput() {
		return true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public boolean process() {
		
		final long start = System.currentTimeMillis();
		final int frame = imp.getFrame();
		final Settings settings = new Settings(imp);
		final Image img = TMUtils.getSingleFrameAsImage(imp, frame-1, settings);
		final CrownWearingSegmenter cws = new CrownWearingSegmenter();
		cws.setTarget(img, settings.getCalibration(), source.getSegmenterSettings());
		
		if (!cws.process()) {
			errorMessage = cws.getErrorMessage();
			return false;
		}
		
		final long end = System.currentTimeMillis();
		processingTime = end - start;
		
		Labeling labels = cws.getLabeling();
		ImagePlus result = ImageJFunctions.copyToImagePlus(labels);
		result.setCalibration(imp.getCalibration());
		IJ.run(result, "glasbey", null);
		result.setSlice(result.getNSlices());
		result.setDisplayRange(result.getDisplayRangeMin(), result.getDisplayRangeMax());
		IJ.run(result, "RGB Color", null);
		result.show();
		
		int tmin = (int) Math.ceil(processingTime / 1e3 / 60); //min 
		source.labelDurationEstimate.setText("Total duration rough estimate: "+tmin+" min.");
		
		return true;
	}

}
