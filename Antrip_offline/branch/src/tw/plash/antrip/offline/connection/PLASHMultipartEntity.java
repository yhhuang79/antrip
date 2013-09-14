package tw.plash.antrip.offline.connection;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;

import tw.plash.antrip.offline.connection.PLASHUrlEncodedFormEntity.CountingOutputStream;
import tw.plash.antrip.offline.connection.PLASHUrlEncodedFormEntity.ProgressListener;

public class PLASHMultipartEntity extends MultipartEntity {
	
	private final ProgressListener listener;
	
	public PLASHMultipartEntity(final ProgressListener listener) {
		this.listener = listener;
	}
	
	public PLASHMultipartEntity(final HttpMultipartMode mode, final ProgressListener listener) {
		super(mode);
		this.listener = listener;
	}
	
	public PLASHMultipartEntity(final HttpMultipartMode mode, final String boundary, final Charset charset, final ProgressListener listener){
		super(mode, boundary, charset);
		this.listener = listener;
	}
	
	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		super.writeTo(new CountingOutputStream(outstream, this.listener, getContentLength()));
	}
}
