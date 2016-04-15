package com.xebialabs.overthere.cifs.winrm;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.xebialabs.overthere.util.gss.GssCli;
import com.xebialabs.overthere.util.gss.GssIOVBufferDesc;
import com.xebialabs.overthere.util.gss.LibGss;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.io.*;
import java.nio.ByteBuffer;

/**
 *
 * @author David Estes
 */
public class KerberosStringEntity implements HttpEntity, GssTokenAware{

	protected String contentType;
	protected String content;
	protected byte[] body;
	private GssCli gssCli;

	public KerberosStringEntity(String content, String contentType) {
		this.content = content;
		this.contentType = contentType;
	}


	private byte[] getBody() {
		if(body != null){
			return body;
		}

		if(content == null || content.length() == 0) {
			return new byte[0];
		}

		if(gssCli != null) {
			int originalLength = content.getBytes(HTTP.DEF_CONTENT_CHARSET).length;
			EncryptedMessage emsg = encryptMessage(content);
			int totalLength= originalLength + emsg.padLength;

			StringBuilder strBuilder = new StringBuilder(content.length());
			strBuilder.append("--Encrypted Boundary\r\n");
			strBuilder.append("Content-Type: application/HTTP-Kerberos-session-encrypted\r\n");
			strBuilder.append("OriginalContent: type=application/soap+xml;charset=UTF-8;Length=" + totalLength + "\r\n");
			strBuilder.append("--Encrypted Boundary\r\n");
			strBuilder.append("Content-Type: application/octet-stream\r\n");
			ByteArrayOutputStream buffer = new ByteArrayOutputStream(totalLength);
			try {
				buffer.write(strBuilder.toString().getBytes(HTTP.DEF_CONTENT_CHARSET));
				buffer.write(emsg.message);
				buffer.write("--Encrypted Boundary--\r\n".getBytes(HTTP.DEF_CONTENT_CHARSET));
			} catch(IOException ex) {
				//also not going to happen.
			}

			body = buffer.toByteArray();
		} else {
			body = content.getBytes();
		}
		return body;
	}

	@Override
	public boolean isRepeatable() {
		return true;
	}

	@Override
	public boolean isChunked() {
		return false;
	}


	@Override
	public long getContentLength() {
		return getBody().length;
	}

	@Override
	public Header getContentType() {
		if(gssCli != null) {
			new BasicHeader("Content-Type","multipart/encrypted;protocol=\"application/HTTP-Kerberos-session-encrypted\";boundary=\"Encrypted Boundary\"");
		}
		return new BasicHeader("Content-Type", contentType);
	}

	@Override
	public Header getContentEncoding() {
		return new BasicHeader("Content-Type","charset=" + HTTP.DEF_CONTENT_CHARSET);
	}

	@Override
	public InputStream getContent() throws IOException, UnsupportedOperationException {
		return new ByteArrayInputStream(getBody());
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		byte[] buff = new byte[1024];
		int c = 0;
		InputStream is = getContent();
		while((c = is.read(buff)) != -1) {
			outstream.write(buff,0,c);
		}
		outstream.flush();
	}

	@Override
	public boolean isStreaming() {
		return false;
	}

	@Override
	public void consumeContent() throws IOException {
	}

	@Override
	public void initContext(HttpContext context, String token, String serviceName) {
		this.body = null; //reset body since it now might be encrypted

		try {
			if(gssCli == null) {
				gssCli = new GssCli(serviceName, null);
			}
			gssCli.initContext(token,null,true);
			context.setAttribute("gssCli", gssCli);
		} catch(Exception ex) {
			//do something here if it fails probably
			ex.printStackTrace(); // temporary for testing
		}
	}

	public EncryptedMessage encryptMessage(String content) {
		int iovCount = 3;
		GssIOVBufferDesc[] iov = new GssIOVBufferDesc[3];

		iov[0] = new GssIOVBufferDesc();
		iov[0].type = LibGss.GSS_IOV_BUFFER_TYPE_HEADER | LibGss.GSS_IOV_BUFFER_FLAG_ALLOCATE;
		iov[1] = new GssIOVBufferDesc();
		iov[1].type = LibGss.GSS_IOV_BUFFER_TYPE_DATA;
		iov[1].buffer.setValue(content);

		iov[2] = new GssIOVBufferDesc();
		iov[2].type = LibGss.GSS_IOV_BUFFER_TYPE_PADDING | LibGss.GSS_IOV_BUFFER_FLAG_ALLOCATE;

		Pointer confState = new Memory(GssCli.INT32_SIZE);
		Pointer minStat = new Memory(GssCli.INT32_SIZE);

		LibGss.INSTANCE.gss_wrap_iov(minStat, gssCli.getContext(),1,LibGss.GSS_C_QOP_DEFAULT,confState,iov,iovCount);

		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		ByteBuffer intLen = ByteBuffer.allocate(4);
		intLen.putInt(iov[0].buffer.length);
		try {
			byteBuffer.write(intLen.array());
			byteBuffer.write(iov[0].buffer.value.getByteArray(0,iov[0].buffer.length));
			byteBuffer.write(iov[1].buffer.value.getByteArray(0,iov[1].buffer.length));
			int padLength = iov[2].buffer.length;
			if(padLength > 0) {
				byteBuffer.write(iov[2].buffer.value.getByteArray(0, iov[2].buffer.length));
			}
			return new EncryptedMessage(padLength, byteBuffer.toByteArray());
		} catch(IOException ex) {
			//not gonna happen
		}

		return null;
	}

	public class EncryptedMessage {
		public byte[] message;
		public int padLength;
		EncryptedMessage(int padLength, byte[] message) {
			this.padLength = padLength;
			this.message = message;
		}
	}
}
