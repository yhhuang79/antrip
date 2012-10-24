package tw.plash.antrip;

import android.location.Location;

public class AntripLocation extends Location {
	
	private Integer emotion;
	private String text;
	private String photoPath;
	private String photoName;
	
	public AntripLocation() {
		super("skyhook"); // defaults provider to skyhook
		emotion = null;
		text = null;
		photoPath = null;
		photoName = null;
	}
	
	public Integer getEmotion() {
		return emotion;
	}
	
	public void setEmotion(Integer emotion) {
		this.emotion = emotion;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getPhotoPath() {
		return photoPath;
	}
	
	public void setPhotoPath(String photoPath) {
		this.photoPath = photoPath;
		try {
			this.photoName = photoPath.substring(photoPath.lastIndexOf("/"));
		} catch (Exception e) {
			e.printStackTrace();
			this.photoName = null;
		}
	}
	
	public String getPhotoName() {
		return photoName;
	}
	
}
