import java.util.ArrayList;

import io.scif.img.ImgIOException;
import io.scif.img.ImgSaver;
import net.imagej.ImageJ;
import net.imglib2.FinalDimensions;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;

public class TestOpenLargeStack {

	final public static ImageJ ij = new ImageJ();

	public static void main(String[] args) throws ImgIOException, IncompatibleTypeException {
		// launch it
		ij.launch(args);

		int[] frames = new int[] { 500, 1000, 2000, 4000, 8000 };
		// int[] frames=new int[] {500,1000};
		ArrayList<Double> times = new ArrayList<Double>();

		for (int n : frames) {
			times.add(ImgSaverTest(n));
		}

		for (int n = 0; n < frames.length; n++) {
			System.out.println("frames/times " + frames[n] + " " + times.get(n));
		}

	}

	public static double ImgSaverTest(int numFrames) {

		long startTime = System.currentTimeMillis();

		Img<FloatType> test = ij.op().create().img(new FinalDimensions(20, 20, numFrames), new FloatType());

		// set a pixel value just to eliminate max/min cacluation issues for an 0 image
		test.firstElement().set(20.0f);

		System.out.println("Save using ImgSaver");
		new ImgSaver().saveImg("./test.tif", test);

		return (System.currentTimeMillis() - startTime) / 1000.;

	}
}
