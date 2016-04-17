package com.xebialabs.overthere.util.gss;

import com.sun.jna.*;
import com.xebialabs.overthere.cifs.winrm.KerberosStringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Basic Wrapper for common GSS Functions
 * For more advanced interactions with the GSS API Use the {@link LibGss} JNA interface
 * @author David Estes
 */

public class GssCli {

	public final static int INT32_SIZE = 4;
	private String hostname;
	private String serviceName;
	private String keyTab;
	private Pointer minStat;
	private int majStat;
	private Pointer context;
	private Pointer intSvcName; //internal


	public GssCli(String hostname, String serviceName, String keyTab) throws Exception {
		this.hostname = hostname;
		this.keyTab = keyTab;

		this.serviceName = serviceName == null ? "host@" + this.hostname : (serviceName.indexOf('@') >= 0 ?serviceName : serviceName + "@" + hostname);
		this.intSvcName = importName(this.serviceName);
	}

	public GssCli(String spn, String keyTab) throws Exception{
		this.serviceName = spn;
		this.keyTab = keyTab;
		this.serviceName = spn;
		this.intSvcName = importName(this.serviceName);
	}


	public Pointer getContext() {
		return context;
	}


	public Pointer importName(String str) throws Exception {
		GssBufferDesc buffer = new GssBufferDesc();
		buffer.setValue(str);
		Pattern p = Pattern.compile("[A-Za-z0-9]+/[^@]+@.+$");
		Matcher m = p.matcher(str);
		Pointer mech;
		logger.debug("Gss Import Name " + str);
		if(m.matches()) {
			mech = Pointer.NULL;
		} else {
			logger.debug("This is an NT_HOSTBASED_SERVICE");
			NativeLibrary libraryInstance = NativeLibrary.getInstance(Platform.isMac() ? "libgssapi_krb5.dylib" : (Platform.isLinux() ? "libgssapi_krb5.so.2" : "C:\\Program Files (x86)\\MIT\\Kerberos\\bin\\gssapi32.dll"));
			Pointer GSS_C_NT_HOSTBASED_SERVICE  = libraryInstance.getGlobalVariableAddress("GSS_C_NT_HOSTBASED_SERVICE");
			Pointer GSS_C_NT_EXPORT_NAME  = libraryInstance.getGlobalVariableAddress("GSS_C_NT_EXPORT_NAME");
			mech = GSS_C_NT_HOSTBASED_SERVICE.getPointer(0);

		}
		minStat = new Memory(INT32_SIZE); //32bit int
		Pointer name = new Memory(Native.POINTER_SIZE);
		logger.debug("Calling native import name");
		majStat = LibGss.INSTANCE.gss_import_name(minStat, buffer, mech, name);

		if(majStat != 0) {
			throw new Exception("gss_import_name did not return GSS_S_COMPLETE - " + LibGss.GSS_C_ROUTINE_ERRORS.get(majStat));
		}
		return name.getPointer(0);
	}

	public void importContext(byte[] inputToken) throws Exception {
		minStat = new Memory(INT32_SIZE); //32bit int
		minStat.setInt(0,0);
		if(context != null) {
			deleteContext();
		}
		context = new Memory(Pointer.SIZE);
		GssBufferDesc inTok = new GssBufferDesc();
		if(inputToken != null) {
			Pointer inTokByte = new Memory(inputToken.length+1);
			inTokByte.write(0,inputToken,0,inputToken.length);
			inTok.value = inTokByte;
			inTok.length = inputToken.length;
		}

		majStat = LibGss.INSTANCE.gss_import_sec_context(minStat, inTok, context);
		if(majStat != 0) {
			throw new Exception("gss_import_context did not return GSS_S_COMPLETE - " + LibGss.GSS_C_ROUTINE_ERRORS.get(majStat));

		}
	}

	public void finalize() {
		if(context != null) {
			deleteContext();
		}
	}


	public byte[] deleteContext() {
		GssBufferDesc outTok = new GssBufferDesc();
		majStat = LibGss.INSTANCE.gss_delete_sec_context(minStat,context, outTok);
		context = null;
		return outTok.value.getByteArray(0,outTok.length);
	}

	public byte[] initContext(byte[] inputToken, Integer flags, boolean delegate) {

		if(context != null) {
			deleteContext();
		}
		context = new Memory(Pointer.SIZE);

		if(context == null)
			context.setPointer(0, LibGss.GSS_C_NO_CONTEXT);
		else {
			context.setPointer(0,context);
		}



		GssOID mech = GssOID.noOid();

		if(flags == null) {
			flags = (LibGss.GSS_C_MUTUAL_FLAG | LibGss.GSS_C_SEQUENCE_FLAG | LibGss.GSS_C_CONF_FLAG | LibGss.GSS_C_INTEG_FLAG);
			if(delegate) {
				flags |= LibGss.GSS_C_DELEG_FLAG | LibGss.GSS_C_DELEG_POLICY_FLAG;
			}
		}

		GssBufferDesc inTok = new GssBufferDesc();
		if(inputToken != null) {
			Pointer inTokByte = new Memory(inputToken.length+1);
			inTokByte.write(0,inputToken,0,inputToken.length);
			inTok.value = inTokByte;
			inTok.length = inputToken.length;
		}

		GssBufferDesc outTok = new GssBufferDesc();

		Pointer returnFlags = new Memory(INT32_SIZE);
		majStat = LibGss.INSTANCE.gss_init_sec_context(minStat, null, context, intSvcName, mech, flags, 0 ,null, inTok, null, outTok, returnFlags, null);
		if(majStat == 1) {
			logger.debug("Native gss InitContext majState = 1 - " + (outTok.length > 0 ? outTok.value.getByteArray(0,outTok.length) : null));
			return outTok.length > 0 ? outTok.value.getByteArray(0,outTok.length) : null;
		}
		logger.debug("Not resultant token - " + LibGss.GSS_C_ROUTINE_ERRORS.get(majStat) + " - Minor: "  + Integer.toHexString(minStat.getInt(0)));
		return null;
	}

	public byte[] initContext(byte[] inputToken) {
		return initContext(inputToken,null,false);
	}

	public byte[] initContext(Integer flags, boolean delegate) {
		return initContext(null,flags,delegate);
	}

	public byte[] initContext() {
		return initContext(null,null,false);
	}

	private static Logger logger = LoggerFactory.getLogger(KerberosStringEntity.class);


}
