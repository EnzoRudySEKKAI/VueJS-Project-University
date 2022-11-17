package pdl.backend.imageProcessing;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import io.scif.SCIFIO;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import io.scif.img.ImgSaver;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import net.imglib2.exception.IncompatibleTypeException;
import java.io.File;
import java.util.Arrays;
import java.util.Random;

public class ImgProccessing {

	public static void thresholdRGB(Img<UnsignedByteType> img, int t) {
		final RandomAccess<UnsignedByteType> r = img.randomAccess();

		final int iw = (int) img.max(0);
		final int ih = (int) img.max(1);
		for (int channel = 0; channel <= 2; channel++) {
			for (int x = 0; x <= iw; ++x) {
				for (int y = 0; y <= ih; ++y) {
					r.setPosition(x, 0);
					r.setPosition(y, 1);
					r.setPosition(channel, 2);
					final UnsignedByteType val = r.get();
					int value = val.get();

					if (value + t > 255) {
						val.set(255);
					} else if (value + t < 0) {
						val.set(0);
					} else {
						val.set(val.get() + t);
					}
				}
			}
		}
	}

	public static void colorToGray(Img<UnsignedByteType> input) {
        final IntervalView<UnsignedByteType> inputR = Views.hyperSlice(input, 2, 0);
		final IntervalView<UnsignedByteType> inputG = Views.hyperSlice(input, 2, 1);
		final IntervalView<UnsignedByteType> inputB = Views.hyperSlice(input, 2, 2);

		final Cursor<UnsignedByteType> cR = inputR.cursor();
		final Cursor<UnsignedByteType> cG = inputG.cursor();
		final Cursor<UnsignedByteType> cB = inputB.cursor();
		
		while (cR.hasNext() && cG.hasNext() && cB.hasNext()) {
			cR.fwd();
			cG.fwd();
			cB.fwd();
            
            double red =  cR.get().get() * 0.3;
			double green = cG.get().get() * 0.59;
			double blue = cB.get().get() * 0.11;
			double gray = red+green+blue;
			int grayInt =(int) gray;
			cR.get().set(grayInt);
			cG.get().set(grayInt);
			cB.get().set(grayInt);
        }
	}

	public static void dynamicContrastLUTColor(Img<UnsignedByteType> img, int newMin,int newMax){
		Img<UnsignedByteType> imgGray = img.copy();
		colorToGray(imgGray);
		int[] LUT = new int[256];
		Cursor<UnsignedByteType> cursor = imgGray.cursor();
		int min = 255;
		int max = 0;

        while (cursor.hasNext()) {
            cursor.fwd();
			final int pix = cursor.get().get();
			if (pix > max){
				max = pix;
			}
			if (pix < min){
				min = pix;
			}
		}
		for (int i = 0 ; i <=255 ; i++){
				LUT[i]= (newMax-newMin)*(i-min)/(max - min) + newMin;
		}
		final IntervalView<UnsignedByteType> inputR = Views.hyperSlice(img, 2, 0);
		final IntervalView<UnsignedByteType> inputG = Views.hyperSlice(img, 2, 1);
		final IntervalView<UnsignedByteType> inputB = Views.hyperSlice(img, 2, 2);

		final Cursor<UnsignedByteType> cR = inputR.cursor();
		final Cursor<UnsignedByteType> cG = inputG.cursor();
		final Cursor<UnsignedByteType> cB = inputB.cursor();
		while (cR.hasNext() && cG.hasNext() && cB.hasNext()) {
			cR.fwd();
			cG.fwd();
			cB.fwd();
			int red =  cR.get().get();
			int green = cG.get().get();
			int blue = cB.get().get();
			cR.get().set(LUT[red]);
			cG.get().set(LUT[green]);
			cB.get().set(LUT[blue]);

		}

	}

	public static void rgbToHsv(int r, int g, int b, float[] hsv) 
	{
		float red = ((float) r) / 255;
		float green = ((float) g) / 255;
		float blue = ((float) b) / 255;
		float max = (float) Math.max(Math.max(red, blue), green);
		float min = (float) Math.min(Math.min(red, blue), green);
		float h = -1;
		float s = -1;

		if (max == min)
			h = 0;
		else if (max == red)
			h = (60 * ((green - blue) / (max - min)) + 360) % 360;
		else if (max == green)
			h = (60 * ((blue - red) / (max - min)) + 120) % 360;
		else if (max == blue)
			h = (60 * ((red - green) / (max - min)) + 240) % 360;
		if (max == 0)
			s = 0;
		else
			s = ((max - min) / max);
			
		hsv[0] = h;
		hsv[1] = s;
		hsv[2] = max;
	}

	public static void hsvToRgb(float h, float s, float v, int[] rgb)
    {
        float l, m, n, f;
        int t = ((int) Math.floor(h / 60)) % 6;

		f = h / 60 - t;
		l = v * (1-s);
		m = v * (1 - f*s);
		n = v * (1 - (1-f) *s);
        switch (t)
        {
            case 0:
                rgb[0] = (int) (v * 255);
                rgb[1] = (int) (n * 255);
                rgb[2] = (int) (l * 255);
                break;
            case 1:
                rgb[0] = (int) (m * 255);
                rgb[1] = (int) (v * 255);
                rgb[2] = (int) (l * 255);
                break;
            case 2:
                rgb[0] = (int) (l * 255);
                rgb[1] = (int) (v * 255);
                rgb[2] = (int) (n * 255);
                break;
            case 3:
                rgb[0] = (int) (l * 255);
                rgb[1] = (int) (m * 255);
                rgb[2] = (int) (v * 255);
                break;
            case 4:
                rgb[0] = (int) (n * 255);
                rgb[1] = (int) (l * 255);
                rgb[2] = (int) (v * 255);
                break;
            case 5:
                rgb[0] = (int) (v * 255);
                rgb[1] = (int) (l * 255);
                rgb[2] = (int) (m * 255);
                break;
        }
    }

	public static void HueFilter(Img<UnsignedByteType> input, int hue) {
		final IntervalView<UnsignedByteType> R = Views.hyperSlice(input, 2, 0);
		final IntervalView<UnsignedByteType> G = Views.hyperSlice(input, 2, 1);
		final IntervalView<UnsignedByteType> B = Views.hyperSlice(input, 2, 2);

		final Cursor<UnsignedByteType> cR = R.cursor();
		final Cursor<UnsignedByteType> cG = G.cursor();
		final Cursor<UnsignedByteType> cB = B.cursor();

		int[] rgb = new int[3];
		float[] hsv = new float[3];
		while (cR.hasNext() && cG.hasNext() && cB.hasNext()) {
			cR.fwd();
			cG.fwd();
			cB.fwd();

			rgbToHsv(cR.get().get(), cG.get().get(), cB.get().get(), hsv);
			hsvToRgb(hue, hsv[1], hsv[2], rgb);

			cR.get().set(rgb[0]);
			cG.get().set(rgb[1]);
			cB.get().set(rgb[2]);
		}
	}

	public static void sobel (final Img<UnsignedByteType> input){
		final RandomAccess<UnsignedByteType> inputAccess = input.randomAccess();
		colorToGray(input);
		final IntervalView<UnsignedByteType> inputR = Views.hyperSlice(input, 2, 0);
		final IntervalView<UnsignedByteType> inputG = Views.hyperSlice(input, 2, 1);
		final IntervalView<UnsignedByteType> inputB = Views.hyperSlice(input, 2, 2);
		int [][]Gx ={{-1,0,1},
				{-2,0,2},
				{-1,0,1}};
		int [][]Gy ={{-1,-2,-1},
				{0,0,0},
				{1,2,1}};

		final int iw = (int) input.max(0);
		final int ih = (int) input.max(1);
		int [][]GdefR= new int [iw][ih];
		int [][]GdefG= new int [iw][ih];
		int [][]GdefB= new int [iw][ih];
		int size = 1;

		for (int x = 1; x <= iw-1; ++x) {
			for (int y = 1; y <= ih-1; ++y) {
				int resultRX = 0;
				int resultRY = 0;
				int resultGX = 0;
				int resultGY = 0;
				int resultBX = 0;
				int resultBY = 0;
				RandomAccessibleInterval<UnsignedByteType> convolutionR = Views.interval (inputR,
						new long[] {x-size,y-size},
						new long[] {x+size,y+size});
				RandomAccessibleInterval <UnsignedByteType> convolutionG = Views.interval (inputG,
						new long[] {x-size,y-size},
						new long[] {x+size,y+size});
				RandomAccessibleInterval <UnsignedByteType> convolutionB = Views.interval (inputB,
						new long[] {x-size,y-size},
						new long[] {x+size,y+size});
				final Cursor<UnsignedByteType> cR = Views.iterable(convolutionR).cursor();
				final Cursor<UnsignedByteType> cG = Views.iterable(convolutionG).cursor();
				final Cursor<UnsignedByteType> cB = Views.iterable(convolutionB).cursor();
				//final Cursor <UnsignedByteType> cursor = Views.iterable(convolution).cursor();
				while(cR.hasNext() && cG.hasNext() && cB.hasNext()){
					cR.fwd();
					cG.fwd();
					cB.fwd();

					resultRX += cR.get().get() * Gx [cR.getIntPosition(0)-x+size][cR.getIntPosition(1)-y+size];
					resultRY += cR.get().get() * Gy [cR.getIntPosition(0)-x+size][cR.getIntPosition(1)-y+size];
					resultGX += cG.get().get() * Gx [cG.getIntPosition(0)-x+size][cG.getIntPosition(1)-y+size];
					resultGY += cG.get().get() * Gy [cG.getIntPosition(0)-x+size][cG.getIntPosition(1)-y+size];
					resultBX += cB.get().get() * Gx [cB.getIntPosition(0)-x+size][cB.getIntPosition(1)-y+size];
					resultBY += cB.get().get() * Gy [cB.getIntPosition(0)-x+size][cB.getIntPosition(1)-y+size];
				}
				//System.out.println("X:" + resultX + " Y: " + resultY + " pixel :" + (int) (Math.sqrt(resultX*resultX+resultY*resultY)));
				GdefR[x][y]=(int) (Math.sqrt(resultRX*resultRX+resultRY*resultRY));
				GdefG[x][y]=(int) (Math.sqrt(resultGX*resultGX+resultGY*resultGY));
				GdefB[x][y]=(int) (Math.sqrt(resultBX*resultBX+resultBY*resultBY));

			}
		}
		for (int i = 1; i <= iw-1; ++i) {
			for (int j = 1; j <= ih-1; ++j) {
				inputAccess.setPosition(i,0);
				inputAccess.setPosition(j,1);
				inputAccess.setPosition(0,2);
				inputAccess.get().set(GdefR[i][j]);
				inputAccess.setPosition(1,2);
				inputAccess.get().set(GdefG[i][j]);
				inputAccess.setPosition(2,2);
				inputAccess.get().set(GdefB[i][j]);

			}
		}

	}

	public static void convolutionRGB(final Img<UnsignedByteType> input,
									  int taille) {
		int[][] kernel = new int[taille][taille];
		for (int i = 0; i<taille;i++){
			for (int j =0; j<taille;j++){
				kernel[i][j]=1;
			}
		}
		final int iw = (int) input.max(0);
		final int ih = (int) input.max(1);
		int size = (kernel.length-1)/2;
		int coeffSum =0;
		for (int i = 0; i< kernel.length;i++){
			for (int j =0; j<kernel.length;j++){
				coeffSum+=kernel[i][j];
			}
		}
		final IntervalView<UnsignedByteType> inputR = Views.hyperSlice(input, 2, 0);
		final IntervalView<UnsignedByteType> inputG = Views.hyperSlice(input, 2, 1);
		final IntervalView<UnsignedByteType> inputB = Views.hyperSlice(input, 2, 2);
		final IntervalView <UnsignedByteType> expandedImgR = Views.expandMirrorDouble(inputR, size,size);
		final IntervalView <UnsignedByteType> expandedImgG = Views.expandMirrorDouble(inputG, size,size);
		final IntervalView <UnsignedByteType> expandedImgB = Views.expandMirrorDouble(inputB, size,size);
		for (int x = 0; x <= iw; ++x) {
			for (int y = 0; y <= ih; ++y) {


				int resultR = 0;
				int resultG = 0;
				int resultB = 0;
				RandomAccessibleInterval <UnsignedByteType> convolutionR = Views.interval (expandedImgR,
						new long[] {x-size,y-size},
						new long[] {x+size,y+size});
				RandomAccessibleInterval <UnsignedByteType> convolutionG = Views.interval (expandedImgG,
						new long[] {x-size,y-size},
						new long[] {x+size,y+size});
				RandomAccessibleInterval <UnsignedByteType> convolutionB = Views.interval (expandedImgB,
						new long[] {x-size,y-size},
						new long[] {x+size,y+size});
				final Cursor<UnsignedByteType> cR = Views.iterable(convolutionR).cursor();
				final Cursor<UnsignedByteType> cG = Views.iterable(convolutionG).cursor();
				final Cursor<UnsignedByteType> cB = Views.iterable(convolutionB).cursor();
				while (cR.hasNext() && cG.hasNext() && cB.hasNext()) {
					cR.fwd();
					cG.fwd();
					cB.fwd();
					resultR += cR.get().get() * kernel [cR.getIntPosition(0)-x+size][cR.getIntPosition(1)-y+size];
					resultG += cG.get().get() * kernel [cG.getIntPosition(0)-x+size][cG.getIntPosition(1)-y+size];
					resultB += cB.get().get() * kernel [cB.getIntPosition(0)-x+size][cB.getIntPosition(1)-y+size];
				}
				cR.get().set(resultR/coeffSum);
				cG.get().set(resultG/coeffSum);
				cB.get().set(resultB/coeffSum);

			}
		}
	}
	public static void convolutionRGBGauss(final Img<UnsignedByteType> input,
									  int taille) {
		double sigma = 1;
		int W = taille;
		double [][]kernel = new  double[W][W] ;
		double mean = W/2;
		double sum = 0.0; // For accumulating the kernel values
		for (int x = 0; x < W; ++x)
			for (int y = 0; y < W; ++y) {
				kernel[x][y] = Math.exp( -0.5 * (Math.pow((x-mean)/sigma, 2.0) + Math.pow((y-mean)/sigma,2.0)) )
						/ (2 * Math.PI * sigma * sigma);
				// Accumulate the kernel values
				sum += kernel[x][y];
		}

		final int iw = (int) input.max(0);
		final int ih = (int) input.max(1);
		int size = (kernel.length-1)/2;
		double coeffSum = 0 ;
		for (int i = 0; i< kernel.length;i++){
			for (int j =0; j<kernel.length;j++){
				coeffSum+=kernel[i][j];
			}
		}
		final IntervalView<UnsignedByteType> inputR = Views.hyperSlice(input, 2, 0);
		final IntervalView<UnsignedByteType> inputG = Views.hyperSlice(input, 2, 1);
		final IntervalView<UnsignedByteType> inputB = Views.hyperSlice(input, 2, 2);
		final IntervalView <UnsignedByteType> expandedImgR = Views.expandMirrorDouble(inputR, size,size);
		final IntervalView <UnsignedByteType> expandedImgG = Views.expandMirrorDouble(inputG, size,size);
		final IntervalView <UnsignedByteType> expandedImgB = Views.expandMirrorDouble(inputB, size,size);
		for (int x = 0; x <= iw; ++x) {
			for (int y = 0; y <= ih; ++y) {


				double resultR = 0;
				double resultG = 0;
				double resultB = 0;
				RandomAccessibleInterval <UnsignedByteType> convolutionR = Views.interval (expandedImgR,
						new long[] {x-size,y-size},
						new long[] {x+size,y+size});
				RandomAccessibleInterval <UnsignedByteType> convolutionG = Views.interval (expandedImgG,
						new long[] {x-size,y-size},
						new long[] {x+size,y+size});
				RandomAccessibleInterval <UnsignedByteType> convolutionB = Views.interval (expandedImgB,
						new long[] {x-size,y-size},
						new long[] {x+size,y+size});
				final Cursor<UnsignedByteType> cR = Views.iterable(convolutionR).cursor();
				final Cursor<UnsignedByteType> cG = Views.iterable(convolutionG).cursor();
				final Cursor<UnsignedByteType> cB = Views.iterable(convolutionB).cursor();
				while (cR.hasNext() && cG.hasNext() && cB.hasNext()) {
					cR.fwd();
					cG.fwd();
					cB.fwd();
					resultR += cR.get().get() * kernel [cR.getIntPosition(0)-x+size][cR.getIntPosition(1)-y+size];
					resultG += cG.get().get() * kernel [cG.getIntPosition(0)-x+size][cG.getIntPosition(1)-y+size];
					resultB += cB.get().get() * kernel [cB.getIntPosition(0)-x+size][cB.getIntPosition(1)-y+size];
				}
				cR.get().set((int) (resultR/coeffSum));
				cG.get().set((int) (resultG/coeffSum));
				cB.get().set((int) (resultB/coeffSum));

			}
		}
	}

	public static void RandomPixelSort (final Img<UnsignedByteType> input, boolean direction){
		final RandomAccess<UnsignedByteType> inputAccess = input.randomAccess();
		final IntervalView<UnsignedByteType> inputR = Views.hyperSlice(input, 2, 0);
		final IntervalView<UnsignedByteType> inputG = Views.hyperSlice(input, 2, 1);
		final IntervalView<UnsignedByteType> inputB = Views.hyperSlice(input, 2, 2);
		final Cursor<UnsignedByteType> cR = inputR.cursor();
		final Cursor<UnsignedByteType> cG = inputG.cursor();
		final Cursor<UnsignedByteType> cB = inputB.cursor();
		final int iw = (int) input.max(0);
		final int ih = (int) input.max(1);
		int[][][] pixels = new int [iw+1][ih+1][3];
		Random rand = new Random();
		while(cR.hasNext() && cG.hasNext() && cB.hasNext()){
			cR.fwd();
			cG.fwd();
			cB.fwd();
			int xR = cR.getIntPosition(0);
			int yR = cR.getIntPosition(1);
			int valueR = cR.get().get();
			int xG = cG.getIntPosition(0);
			int yG = cG.getIntPosition(1);
			int valueG = cG.get().get();
			int xB = cB.getIntPosition(0);
			int yB = cB.getIntPosition(1);
			int valueB = cB.get().get();
			pixels [xR][yR][0]= valueR;
			pixels [xG][yG][1]= valueG;
			pixels [xB][yB][2]= valueB;
		}
		if (direction){
			for (int x =0 ; x< iw ; x++){
				for (int i = 0 ; i< ih; i++){
					int randomIndexToSwap = rand.nextInt(ih);
					int[] temp = pixels[x][randomIndexToSwap];
					pixels[x][randomIndexToSwap]= pixels [x][i];
					pixels[x][i]= temp;
				}
			}
		}else{
			for (int i =0 ; i< iw ; i++){
				for (int y = 0 ; y< ih; y++){
					int randomIndexToSwap = rand.nextInt(iw);
					int[] temp = pixels[randomIndexToSwap][y];
					pixels[randomIndexToSwap][y]= pixels [i][y];
					pixels[i][y]= temp;
				}
			}
		}
		for (int i = 0; i <= iw; ++i) {
			for (int j = 0; j <= ih; ++j) {
				inputAccess.setPosition(i,0);
				inputAccess.setPosition(j,1);
				inputAccess.setPosition(0,2);
				inputAccess.get().set(pixels[i][j][0]);
				inputAccess.setPosition(1,2);
				inputAccess.get().set(pixels[i][j][1]);
				inputAccess.setPosition(2,2);
				inputAccess.get().set(pixels[i][j][2]);

			}
		}

	}

	public static void negative (final Img<UnsignedByteType> input){

		final IntervalView<UnsignedByteType> inputR = Views.hyperSlice(input, 2, 0);
		final IntervalView<UnsignedByteType> inputG = Views.hyperSlice(input, 2, 1);
		final IntervalView<UnsignedByteType> inputB = Views.hyperSlice(input, 2, 2);
		final Cursor<UnsignedByteType> cR = inputR.cursor();
		final Cursor<UnsignedByteType> cG = inputG.cursor();
		final Cursor<UnsignedByteType> cB = inputB.cursor();
		while(cR.hasNext() && cG.hasNext() && cB.hasNext()){
			cR.fwd();
			cG.fwd();
			cB.fwd();

			int currentR = cR.get().get();
			int currentG = cG.get().get();
			int currentB = cB.get().get();

			cR.get().set(255-currentR);
			cG.get().set(255-currentG);
			cB.get().set(255-currentB);
		}

	}
}
