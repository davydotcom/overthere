package com.xebialabs.overthere.util.gss;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * Created by davidestes on 4/14/16.
 */
public class GssBufferDesc extends Structure implements Structure.ByReference {
	public int length=0;
	public Pointer value;

	public GssBufferDesc() {
	}

	public GssBufferDesc(Pointer p) {
		super(p);
		read();
	}

	protected List<String> getFieldOrder() {
		return Arrays.asList(new String[] { "length", "value" });
	}

	public void setValue(String value) {
		if(value != null && value.length() != 0) {
			Pointer strNative = new Memory(value.length()+1);
			strNative.setString(0,value);
			this.value = strNative;
			this.length = value.length();
		} else {
			this.length = 0;
			this.value = null;
		}
	}

	public String getValue() {
		if(value != null && length != 0) {
			return this.value.getString(0);
		}
		return null;
	}
}
//class GssIOVBufferDesc < FFI::Struct
//		layout :type, :OM_uint32,
//		:buffer, UnManagedGssBufferDesc
//		end