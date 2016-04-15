package com.xebialabs.overthere.cifs.winrm;

import org.apache.http.protocol.HttpContext;

/**
 * Created by davidestes on 4/14/16.
 */
public interface GssTokenAware {
	void initContext(HttpContext context, byte[] token, String serviceName);
}
