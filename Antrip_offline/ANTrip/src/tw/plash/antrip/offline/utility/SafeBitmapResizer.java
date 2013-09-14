package tw.plash.antrip.offline.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Log;

public class SafeBitmapResizer {
	
	private final static int shortSide = 1080;
	private final static int longSide = 1920;
	
//	private final static int shortSide = 720;
//	private final static int longSide = 1280;
	
	static public void resize(String pathOfInputImage) {
		
		if (pathOfInputImage != null) {
			try {
				int inWidth = 0;
				int inHeight = 0;
				
				InputStream in = new FileInputStream(pathOfInputImage);
				
				// decode image size (decode metadata only, not the whole image)
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(in, null, options);
				in.close();
				in = null;
				
				// save width and height
				inWidth = options.outWidth;
				inHeight = options.outHeight;
				
				// check if already smaller than 1080p, if so, no need to resize
				if(inWidth > inHeight){
					if(inHeight < shortSide){
						return;
					}
				} else{
					if(inWidth < shortSide){
						return;
					}
				}
				
				/**
				 * need to determine whether the input image is in landscape or portrait orientation
				 * 
				 */
				
				int dstWidth = 0;
				int dstHeight = 0;
				
				if(inWidth > inHeight){
					//landscape
					dstHeight = shortSide;
					dstWidth = longSide;
				} else{
					//portrait
					dstHeight = longSide;
					dstWidth = shortSide;
				}
				
				
				// decode full image pre-resized
				in = new FileInputStream(pathOfInputImage);
				options = new BitmapFactory.Options();
				// calc rought re-size (this is no exact resize)
				options.inSampleSize = Math.max(inWidth / dstWidth, inHeight / dstHeight);
				// decode full image
				Bitmap roughBitmap = BitmapFactory.decodeStream(in, null, options);
				
				in.close();
				in = null;
				
				// calc exact destination size
				Matrix m = new Matrix();
				RectF inRect = new RectF(0, 0, roughBitmap.getWidth(), roughBitmap.getHeight());
				RectF outRect = new RectF(0, 0, dstWidth, dstHeight);
				m.setRectToRect(inRect, outRect, Matrix.ScaleToFit.CENTER);
				float[] values = new float[9];
				m.getValues(values);
				
				// resize bitmap
				Bitmap resizedBitmap = Bitmap.createScaledBitmap(roughBitmap,
						(int) (roughBitmap.getWidth() * values[0]), (int) (roughBitmap.getHeight() * values[4]), true);
				
				roughBitmap = null;
				
				// save image
				try {
					String pathOfOutputImage = pathOfInputImage + ".out";
					FileOutputStream out = new FileOutputStream(pathOfOutputImage);
					resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
					
					out.close();
					out = null;
					resizedBitmap = null;
					
					File from = new File(pathOfInputImage);
					if(from.delete()){
						Log.e("bitmap resizer", "delete success");
					} else{
						Log.e("bitmap resizer", "delete fail");
					}
					from = null;
					
					File to = new File(pathOfOutputImage);
					to.renameTo(new File(pathOfInputImage));
					to = null;
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
