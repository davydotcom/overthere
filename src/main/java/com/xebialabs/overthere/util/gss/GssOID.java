package com.xebialabs.overthere.util.gss;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * Created by davidestes on 4/14/16.
 */
public class GssOID extends Structure implements Structure.ByReference {
	public int length;
	public Pointer elements;

	public GssOID() {
	}

	protected List<String> getFieldOrder() {
		return Arrays.asList(new String[] { "length", "elements" });
	}

	public GssOID(Pointer p) {
		super(p);
		read();
	}

	public static GssOID noOid() {
		return LibGss.GSS_C_NO_OID;
	}
}


