package com.xebialabs.overthere.util.gss;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Basic Wrapper for common GSS Functions
 * For more advanced interactions with the GSS API Use the {@link LibGss} JNA interface
 * Created by davidestes on 4/14/16.
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




//	# Convert a String to a GSSAPI usable buffer (gss_buffer_desc)
////	# @param [String] str the string to convert
//	def import_name(str)
//	buff_str = LibGSSAPI::UnManagedGssBufferDesc.new
//	buff_str.value = str
//	# Choose the appropriate mechanism based on the string passed.
//	if (str =~ /[A-Za-z0-9]+\/[^@]+@.+$/)
//	mech = LibGSSAPI::GssOID.gss_c_no_oid
//	else
//	mech = LibGSSAPI::GSS_C_NT_HOSTBASED_SERVICE
//			end
//	name = FFI::MemoryPointer.new :pointer # gss_name_t
//			min_stat = FFI::MemoryPointer.new :OM_uint32
//
//			maj_stat = LibGSSAPI.gss_import_name(min_stat, buff_str.pointer, mech, name)
//	raise GssApiError.new(maj_stat, min_stat), "gss_import_name did not return GSS_S_COMPLETE" if maj_stat != 0
//
//	LibGSSAPI::GssNameT.new(name.get_pointer(0))
//	end
//

	public Pointer importName(String str) throws Exception {
		GssBufferDesc buffer = new GssBufferDesc();
		buffer.setValue(str);
		Pattern p = Pattern.compile("[A-Za-z0-9]+/[^@]+@.+$");
		Matcher m = p.matcher(str);
		GssOID mech;
		if(m.matches()) {
			mech = GssOID.noOid();
		} else {
			NativeLibrary libraryInstance = NativeLibrary.getInstance("libgssapi_krb5.dylib");
			Pointer GSS_C_NT_HOSTBASED_SERVICE  = libraryInstance.getGlobalVariableAddress("GSS_C_NT_HOSTBASED_SERVICE");
			Pointer GSS_C_NT_EXPORT_NAME  = libraryInstance.getGlobalVariableAddress("GSS_C_NT_EXPORT_NAME");
			mech = new GssOID(GSS_C_NT_HOSTBASED_SERVICE.getPointer(0));
//			System.out.println("Reading Mech Length, " + mech.length);
		}
		minStat = new Memory(INT32_SIZE); //32bit int
		Pointer name = new Memory(Native.POINTER_SIZE);
		majStat = LibGss.INSTANCE.gss_import_name(minStat, buffer.getPointer(), mech, name);

		if(majStat != 0) {
			throw new Exception("gss_import_name did not return GSS_S_COMPLETE");
		}
		return name.getPointer(0);
	}

	public String initContext(String inputToken, Integer flags, boolean delegate) {
		minStat = new Memory(INT32_SIZE); //32bit int
		Pointer pctx = new Memory(Pointer.SIZE);


		if(context == null)
			pctx.setPointer(0, LibGss.GSS_C_NO_CONTEXT);
		else {
			pctx.setPointer(0,context);
		}



		GssOID mech = GssOID.noOid();

		if(flags == null) {
			flags = (LibGss.GSS_C_MUTUAL_FLAG | LibGss.GSS_C_SEQUENCE_FLAG | LibGss.GSS_C_CONF_FLAG | LibGss.GSS_C_INTEG_FLAG);
			if(delegate) {
				flags |= LibGss.GSS_C_DELEG_FLAG | LibGss.GSS_C_DELEG_POLICY_FLAG;
			}
		}


		GssBufferDesc inTok = new GssBufferDesc();
		inTok.setValue(inputToken);
		GssBufferDesc outTok = new GssBufferDesc();

		Pointer returnFlags = new Memory(INT32_SIZE);
		majStat = LibGss.INSTANCE.gss_init_sec_context(minStat, null, pctx, intSvcName, mech, flags, 0 ,null, inTok.getPointer(), null, outTok.getPointer(), returnFlags, null);
		if(majStat == 1) {
			return outTok.getValue();
		}
		return null;
	}

	public String initContext(String inputToken) {
		return initContext(inputToken,null,false);
	}

	public String initContext(Integer flags, boolean delegate) {
		return initContext(null,flags,delegate);
	}

	public String initContext() {
		return initContext(null,null,false);
	}

//	public static LibGss loadLibrary() {
//
//	}
//	public static NativeLibrary loadNativeLibrary() {
//
//	}
}
