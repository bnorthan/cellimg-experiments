
import java.io.File;
import java.io.IOException;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import io.scif.config.SCIFIOConfig;
import io.scif.config.SCIFIOConfig.ImgMode;
import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import net.imagej.ops.slice.SlicesII;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

@Plugin(type = Command.class, headless = true, menuPath = "Plugins>Test Open and Loop")
public class TestImgOpenAndLoop<T extends RealType<T> & NativeType<T>> implements Command {

	public static void main(String[] args) {
		final ImageJ ij = new ImageJ();
		// launch it
		ij.launch(args);

	}

	@Parameter
	DatasetIOService io;

	@Parameter
	ImagePlus image;

	@Parameter
	boolean cell = true;

	@Parameter
	OpService ops;

	public void run() {

		// create the SCIFIOConfig. This gives us configuration control over
		// how the ImgOpener will open its datasets.
		final SCIFIOConfig config = new SCIFIOConfig();

		// Use CellImg mode to load the image. CellImgs dynamically load
		// image regions
		// and are useful when an image won't fit in memory
		config.imgOpenerSetImgModes(ImgMode.CELL);

		File imageFile = new File(image.getOriginalFileInfo().directory + image.getOriginalFileInfo().fileName);
		Dataset data = null;
		try {
			if (cell) {
				// open with DatasetIOService
				data = io.open(imageFile.getAbsolutePath(), config);
			} else {
				data = io.open(imageFile.getAbsolutePath());
			}

			final SlicesII<T> inSlicer = new SlicesII<T>((RandomAccessibleInterval<T>) data.getImgPlus().getImg(),
					new int[] { 0, 1 });

			final Cursor<RandomAccessibleInterval<T>> inCursor = inSlicer.cursor();
		
			long startTime = System.currentTimeMillis();
			
			while (inCursor.hasNext()) {
				inCursor.fwd();
				System.out.println(ops.stats().mean(Views.iterable(inCursor.get())));
			}
			
			long endTime = System.currentTimeMillis();
			
			System.out.println("time was "+(float)(endTime-startTime));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
