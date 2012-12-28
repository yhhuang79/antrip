package tw.plash.antrip.offline;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;

public class PLASHUrlEncodedFormEntity extends UrlEncodedFormEntity {
	
	private final ProgressListener listener;
	
	public PLASHUrlEncodedFormEntity(List<? extends NameValuePair> parameters, final ProgressListener listener) throws UnsupportedEncodingException {
		super(parameters);
		this.listener = listener;
	}
	
	public PLASHUrlEncodedFormEntity(List<? extends NameValuePair> parameters, String encoding, final ProgressListener listener) throws UnsupportedEncodingException {
		super(parameters, encoding);
		this.listener = listener;
	}
	
	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		super.writeTo(new CountingOutputStream(outstream, this.listener, getContentLength()));
	}
	
	public static interface ProgressListener
	{
		void transferred(long num, long total);
	}
	
	public static class CountingOutputStream extends FilterOutputStream{
		
		private final ProgressListener listener;
		private long transferred;
		private final long totalsize;
		
		public CountingOutputStream(final OutputStream out, final ProgressListener listener, final long total) {
			super(out);
			this.listener = listener;
			this.transferred = 0;
			this.totalsize = total;
		}
		
		@Override
		public void write(byte[] buffer) throws IOException {
			super.write(buffer);
			this.transferred++;
			this.listener.transferred(this.transferred, totalsize);
		}
		
		@Override
		public void write(byte[] buffer, int offset, int length) throws IOException {
			super.write(buffer, offset, length);
			this.transferred+=length;
			this.listener.transferred(this.transferred, totalsize);
		}
	}
}
