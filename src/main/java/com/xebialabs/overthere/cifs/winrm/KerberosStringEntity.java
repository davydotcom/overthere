package com.xebialabs.overthere.cifs.winrm;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.xebialabs.overthere.util.gss.GssCli;
import com.xebialabs.overthere.util.gss.GssIOVBufferDesc;
import com.xebialabs.overthere.util.gss.LibGss;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 *
 * @author David Estes
 */
@NotThreadSafe
public class KerberosStringEntity extends StringEntity implements GssTokenAware, Cloneable{

//	protected String content;
	protected byte[] body;
	private Charset charset;
	private GssCli gssCli;
	private String sourceText;
	public KerberosStringEntity(final String string, final ContentType contentType) throws UnsupportedCharsetException {
		super(string,contentType);
		Args.notNull(string, "Source string");
		charset = contentType != null ? contentType.getCharset() : null;
		if (charset == null) {
			charset = HTTP.DEF_CONTENT_CHARSET;
		}
		this.sourceText = string;

		if (contentType != null) {
			setContentType(contentType.toString());
		}
	}



	private byte[] getBody() {
		if(body != null){
			return body;
		}

		if(sourceText == null || sourceText.length() == 0) {
			return new byte[0];
		}

		try {
			if(gssCli != null) {
				int originalLength = sourceText.getBytes(charset.name()).length;
				EncryptedMessage emsg = encryptMessage(sourceText);
				int totalLength = originalLength + emsg.padLength;

				StringBuilder strBuilder = new StringBuilder(sourceText.length());
				strBuilder.append("--Encrypted Boundary\r\n");
				strBuilder.append("Content-Type: application/HTTP-Kerberos-session-encrypted\r\n");
				strBuilder.append("OriginalContent: type=application/soap+xml;charset=UTF-8;Length=" + totalLength + "\r\n");
				strBuilder.append("--Encrypted Boundary\r\n");
				strBuilder.append("Content-Type: application/octet-stream\r\n");
				ByteArrayOutputStream buffer = new ByteArrayOutputStream(totalLength);
				try {
					buffer.write(strBuilder.toString().getBytes(charset.name()));
					buffer.write(emsg.message);
					buffer.write("--Encrypted Boundary--\r\n".getBytes(charset.name()));
				} catch(IOException ex) {
					//also not going to happen.
				}

				body = buffer.toByteArray();
			} else {
				body = sourceText.getBytes(charset.name());
			}
		}catch (final UnsupportedEncodingException ex) {
			// should never happen
		}
		logger.debug("Sending Request SOAP Body: " + new String(body));
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
			return new BasicHeader("Content-Type","multipart/encrypted;protocol=\"application/HTTP-Kerberos-session-encrypted\";boundary=\"Encrypted Boundary\"");
		}
		return super.getContentType();
	}


	@Override
	public InputStream getContent() throws IOException, UnsupportedOperationException {
		return new ByteArrayInputStream(getBody());
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		Args.notNull(outstream, "Output stream");
		outstream.write(this.getBody());
		outstream.flush();
	}

	@Override
	public boolean isStreaming() {
		return false;
	}


	@Override
	public void initContext(HttpContext context, String token, String serviceName) {
		this.body = null; //reset body since it now might be encrypted

		try {
			if(gssCli == null) {
				gssCli = new GssCli(serviceName, null);
			}
			logger.debug("Received Token: " + token);
			String outToken = gssCli.initContext(token,null,true);
			logger.debug("Lib Gss Output Token: " + outToken);
			context.setAttribute("gssCli", gssCli);
		} catch(Exception ex) {
			logger.error("Error Initializing Native LibGss Cli Context (Encryption not supported): " + ex.getMessage(),ex);
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

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	private static Logger logger = LoggerFactory.getLogger(KerberosStringEntity.class);

}
