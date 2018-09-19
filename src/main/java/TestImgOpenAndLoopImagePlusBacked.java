
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import io.scif.services.DatasetIOService;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import net.imagej.ops.slice.SlicesII;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.cache.img.SingleCellArrayImg;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;

@Plugin(type = Command.class, headless = true, menuPath = "Plugins>Test Open and Loop ImagePlus Backed")
public class TestImgOpenAndLoopImagePlusBacked<T extends RealType<T> & NativeType<T>> implements Command {

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

		// assuming we know it is a 3D, 16-bit stack...
		final long[] dimensions = new long[] { image.getStack().getWidth(), image.getStack().getHeight(),
				image.getStack().getSize() };

		// set up cell size such that one cell is one plane
		final int[] cellDimensions = new int[] { image.getStack().getWidth(), image.getStack().getHeight(), 1 };

		// make a CellLoader that copies one plane of data from the virtual stack
		final CellLoader<UnsignedShortType> loader = new CellLoader<UnsignedShortType>() {
			@Override
			public void load(final SingleCellArrayImg<UnsignedShortType, ?> cell) throws Exception {
				final int z = (int) cell.min(2);
				final short[] impdata = (short[]) image.getStack().getProcessor(1 + z).getPixels();
				final short[] celldata = (short[]) cell.getStorageArray();
				System.arraycopy(impdata, 0, celldata, 0, celldata.length);
			}
		};

		// create a CellImg with that CellLoader
		final Img<UnsignedShortType> img = new ReadOnlyCachedCellImgFactory().create(dimensions,
				new UnsignedShortType(), loader, ReadOnlyCachedCellImgOptions.options().cellDimensions(cellDimensions));

		final SlicesII<T> inSlicer = new SlicesII<T>((RandomAccessibleInterval<T>) img, new int[] { 0, 1 });

		final Cursor<RandomAccessibleInterval<T>> inCursor = inSlicer.cursor();

		long startTime = System.currentTimeMillis();
		
		while (inCursor.hasNext()) {
			inCursor.fwd();
			System.out.println(ops.stats().mean(Views.iterable(inCursor.get())));
		}

		long endTime = System.currentTimeMillis();
		
		System.out.println("time was "+(float)(endTime-startTime));
	}
}
