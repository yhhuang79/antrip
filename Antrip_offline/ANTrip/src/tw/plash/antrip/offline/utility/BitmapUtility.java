package tw.plash.antrip.offline.utility;

import java.io.File;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class BitmapUtility {
	
	static public Bitmap getPreview(String uri, int THUMBNAIL_SIZE) {
		//invalid input size
		if(THUMBNAIL_SIZE < 1 || uri == null || uri.length() < 1){
			return null;
		} else{
			File image = new File(uri);
			
			BitmapFactory.Options bounds = new BitmapFactory.Options();
			bounds.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(image.getPath(), bounds);
			if ((bounds.outWidth == -1) || (bounds.outHeight == -1))
				return null;
			
			int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight : bounds.outWidth;
			
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize = originalSize / THUMBNAIL_SIZE;
			return BitmapFactory.decodeFile(image.getPath(), opts);
		}
	}
	
	static public Bitmap getPreview(InputStream inputStream, int THUMBNAIL_SIZE){
		Log.i("bitmap util", "getypreview");
		if(THUMBNAIL_SIZE < 1 || inputStream == null){
			return null;
		} else{
			BitmapFactory.Options bounds = new BitmapFactory.Options();
			bounds.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(inputStream, null, bounds);
			if ((bounds.outWidth == -1) || (bounds.outHeight == -1))
				return null;
			
			int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight : bounds.outWidth;
			
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize = originalSize / THUMBNAIL_SIZE;
			return BitmapFactory.decodeStream(inputStream, null, opts);
		}
	}
}
