package com.xebialabs.overthere.util.gss;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * Created by davidestes on 4/14/16.
 */
public class GssIOVBufferDesc extends Structure implements Structure.ByReference {
	public int type;
	public GssBufferDesc buffer;

	public GssIOVBufferDesc() {
		buffer = new GssBufferDesc();
	}

	protected List<String> getFieldOrder() {
		return Arrays.asList(new String[] { "type", "buffer" });
	}

	public GssIOVBufferDesc(Pointer p) {
		super(p);
		read();
	}


}
