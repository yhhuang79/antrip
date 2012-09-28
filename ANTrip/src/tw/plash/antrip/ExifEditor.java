package tw.plash.antrip;

import java.io.IOException;

import android.media.ExifInterface;
import android.util.Log;

public class ExifEditor {
	
	static public void GeoTagPicture(String path, double latitude, double longitude, String timestamp) {
		ExifInterface exif;
		
		try {
			exif = new ExifInterface(path);
			
			//replace whatever is in this column with our standard GPS timestamp
			exif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, timestamp);
			
			//get the width and length to calculate whether photo is landscape or portrait
			int width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1);
			int length = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, -1);
			//check if width/length are larger than the default null value
			if(width > -1 && length > -1){
				if(width >= length){
					//it's a square(rare!) or landscape photo
					exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_NORMAL));
				} else{
					//XXX need to check the correct rotation value(90 or 270)
					//it's a portrait photo
					exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
					//not sure it's 90 or 270 degrees when shooting portrait photos
//					exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_270));
				}
			} else{
				//set the orientation to a default value, since we can't get the width/length value from the input photo
				exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_NORMAL));
			}
			
			//reformat latitude from DD.DDDDD to DD/MM/SS
			int num1Lat = (int) Math.floor(latitude);
			int num2Lat = (int) Math.floor((latitude - num1Lat) * 60);
			double num3Lat = (latitude - ((double) num1Lat + ((double) num2Lat / 60))) * 3600000;
			//reformat longitude from DD.DDDDD to DD/MM/SS
			int num1Lon = (int) Math.floor(longitude);
			int num2Lon = (int) Math.floor((longitude - num1Lon) * 60);
			double num3Lon = (longitude - ((double) num1Lon + ((double) num2Lon / 60))) * 3600000;
			//set reformatted latitude/longitude values
			exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, num1Lat + "/1," + num2Lat + "/1," + num3Lat + "/1000");
			exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, num1Lon + "/1," + num2Lon + "/1," + num3Lon + "/1000");
			//set N/S reference according to latitude value
			if (latitude > 0) {
				exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
			} else {
				exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
			}
			//set E/W reference according to longitude value
			if (longitude > 0) {
				exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
			} else {
				exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
			}
			//save all exif attributes
			exif.saveAttributes();
		} catch (IOException e) {
			Log.e("ExifEditor Error", "input path: " + path);
			e.printStackTrace();
		}
	}
}
