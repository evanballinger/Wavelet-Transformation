import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

/**
 * Haar Wavelet Conversion Program
 * @author Evan Ballinger
 *
 */
public class Wavelet {
	static String CONVERT = "convert";
	static String REVERT = "revert";
	static String OUTPUT_FILE_NAME = "wavelet_output.bmp";
	static int PYRAMID_HEIGHT = 3;

	BufferedImage image;
	BufferedImage outImage;

	double[][] pixelMtx;

	int imageWidth;
	int imageHeight;

	/**
	 * This is the main runner method which verifies user command lind parameters
	 * and runs the program by instantiating the Wavelet class.
	 */
	static public void main(String args[]) {
		if (args.length != 2) {
			System.out.println("Incorrect usage.\nUsage: >> java Wavelet [FUNCTION] imageName.jpg");
		} else {
			String function = args[0];
			String fileName = args[1];

			Wavelet obj = new Wavelet(function, fileName);
		}
	}

	/**
	 * @param functionString Command line argument denotes program function
	 * @param fileNameString Name of file to be converted or reverted
	 * For both functions, the image is read into a class member matrix of 
	 * double values representing pixel color values. The conversion is done on
	 * the matrix and then the matrix is written to a new output image file.
	 */
	public Wavelet(String functionString, String fileNameString) {
		try {
			if (functionString.equals(CONVERT)) {
				imageSetup(fileNameString);
				compressImage();
				outputImage();
			} else if (functionString.equals(REVERT)) {
				imageSetup(fileNameString);
				revertImage();
				outputImage();
			} else {
				System.out.println("Unrecognized function command!");
			}
		} catch (IOException e) {
			System.out.println(e);
		}

	}

	/**
	 * Contains top level logic to compress an image using Haar Wavelet Transformation.
	 * 
	 */
	private void compressImage() {
		System.out.println("Compress Image");

		for (int n = 0; n < PYRAMID_HEIGHT; n++) {
			int quadrantSize = imageHeight / twoToThePower(n);

			for (int i = 0; i < quadrantSize; i++) {
				transformRow(i, quadrantSize);
			}
			for (int j = 0; j < quadrantSize; j++) {
				transformCol(j, quadrantSize);
			}
		}
		writeMtxToImage();

	}

	/**
	 * @param rowNumber Row Number of pixelMatrix to be converted
	 * @param size Size parameter shrinks by a factor of 2 for each pass through
	 * the conversion process
	 */
	private void transformRow(int rowNumber, int size) {
		double[] array = new double[size];
		for (int i = 0; i < size; i++) {
			array[i] = pixelMtx[rowNumber][i];
		}
		double[] tempArray = oneDimensionTransform(array);

		for (int i = 0; i < size; i++) {
			pixelMtx[rowNumber][i] = tempArray[i];
		}
	}

	/**
	 * @param colNumber Column number of pixelMatrix to be converted
	 * @param size parameter shrinks by a factor of 2 for each pass through
	 * the conversion process
	 */
	private void transformCol(int colNumber, int size) {
		double[] array = new double[size];
		for (int i = 0; i < size; i++) {
			array[i] = pixelMtx[i][colNumber];
		}
		double[] tempArray = oneDimensionTransform(array);

		for (int i = 0; i < size; i++) {
			pixelMtx[i][colNumber] = tempArray[i];
		}
	}

	/**
	 * @param rowNumber Row number to be reverted
	 * @param size When reverting an image, the size grows by a factor of 2
	 * for each iteration of the reversion process
	 */
	private void revertRow(int rowNumber, int size) {
		double[] array = new double[size];
		for (int i = 0; i < size; i++) {
			array[i] = pixelMtx[rowNumber][i];
		}
		double[] tempArray = oneDimensionRevert(array);

		for (int i = 0; i < size; i++) {
			pixelMtx[rowNumber][i] = tempArray[i];
		}
	}

	/**
	 * @param colNumber Column number to be reverted
	 * @param size When reverting an image, the size grows by a factor of 2
	 * for each iteration of the reversion process
	 */
	private void revertCol(int colNumber, int size) {
		double[] array = new double[size];
		for (int i = 0; i < size; i++) {
			array[i] = pixelMtx[i][colNumber];
		}
		double[] tempArray = oneDimensionRevert(array);

		for (int i = 0; i < size; i++) {
			pixelMtx[i][colNumber] = tempArray[i];
		}
	}

	/**
	 * @param array Input array to be reverted
	 * @return A new array converted based on the input array
	 */
	private double[] oneDimensionRevert(double[] array) {
		double[] newArray = new double[array.length];
		for (int i = 0; i < array.length / 2; i++) {
			double avgValue = array[i];
			double diffValue = array[i + (array.length / 2)] - (256 / 2);

			newArray[2 * i] = avgValue + diffValue;

		}

		for (int i = 0; i < array.length / 2; i++) {
			double avgValue = array[i];
			double diffValue = array[i + (array.length / 2)] - (256 / 2);
			newArray[(2 * i) + 1] = avgValue - diffValue;
		}
		return newArray;
	}

	/**
	 * @param array Array of double values to be converted using the 1 dimension Haar Wavelet Transformation
	 * @return Returns a new array
	 */
	private double[] oneDimensionTransform(double[] array) {
		double[] newArray = new double[array.length];
		for (int i = 0; i < array.length / 2; i++) {
			newArray[i] = (array[2 * i] + array[(2 * i) + 1]) / 2;
		}

		for (int i = (array.length / 2); i < array.length; i++) {

			int index = i - (array.length / 2);
			double value = (array[2 * index] - array[(2 * index) + 1]) / 2;
			newArray[i] = value + (256 / 2);
		}
		return newArray;
	}

	/**
	 * Writes double values from the class member pixelMatrix to the Buffered image 
	 */
	private void writeMtxToImage() {
		for (int i = 0; i < imageHeight; i++) {
			for (int j = 0; j < imageWidth; j++) {
				int intValue = (int) Math.round(pixelMtx[i][j]);

				if (intValue < 0) {
					intValue = 0;
				} else if (intValue > 255) {
					intValue = 255;
				}

				Color c = new Color(intValue, intValue, intValue);
				image.setRGB(j, i, c.getRGB());
			}
		}
	}

	/**
	 * Produces a matrix from color values from a Buffered image class member
	 */
	private void buildPixelMatrixFromImage() {
		pixelMtx = new double[imageHeight][imageWidth];

		for (int i = 0; i < imageHeight; i++) {
			for (int j = 0; j < imageWidth; j++) {
				pixelMtx[i][j] = greyIntValueAt(j, i);
			}
		}
	}

	/**
	 * @param x X coordinate of image
	 * @param y Y coordinate of image
	 * @return Integer gray value of Image at specified coordinates
	 */
	private int greyIntValueAt(int x, int y) {
		Color c = new Color(image.getRGB(x, y));
		return c.getRed();
	}

	/**
	 * This method controls logic to revert a previously converted image to
	 * an approximation of its original state
	 */
	private void revertImage() {
		System.out.println("Revert Image");

		for (int n = PYRAMID_HEIGHT - 1; n >= 0; n--) {
			int quadrantSize = imageHeight / twoToThePower(n);

			for (int i = 0; i < quadrantSize; i++) {
				revertRow(i, quadrantSize);
			}
			for (int j = 0; j < quadrantSize; j++) {
				revertCol(j, quadrantSize);
			}
		}
		writeMtxToImage();

	}

	/**
	 * @param fName File name of image to be processed
	 * @throws IOException
	 */
	private void imageSetup(String fName) throws IOException {
		File input = new File(fName);
		image = ImageIO.read(input);
		imageDimensionsSetup();
		buildPixelMatrixFromImage();
	}

	/**
	 * Fetches dimension values from image
	 */
	private void imageDimensionsSetup() {
		imageWidth = image.getWidth();
		imageHeight = image.getHeight();
	}

	/**
	 * Writes buffered image object to a file with the specified output name
	 * @throws IOException
	 */
	private void outputImage() throws IOException {
		File ouptut = new File(OUTPUT_FILE_NAME);
		ImageIO.write(image, "bmp", ouptut);
	}

	/**
	 * @param n a power value
	 * @return the value of 2 raised to the power n
	 */
	private int twoToThePower(int n) {
		return (int) Math.pow(2, n);
	}
}