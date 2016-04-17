/**
 * Copyright (c) 2008-2016, XebiaLabs B.V., All rights reserved.
 *
 *
 * Overthere is licensed under the terms of the GPLv2
 * <http://www.gnu.org/licenses/old-licenses/gpl-2.0.html>, like most XebiaLabs Libraries.
 * There are special exceptions to the terms and conditions of the GPLv2 as it is applied to
 * this software, see the FLOSS License Exception
 * <http://github.com/xebialabs/overthere/blob/master/LICENSE>.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; version 2
 * of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth
 * Floor, Boston, MA 02110-1301  USA
 */
package com.xebialabs.overthere.cifs.winrm;

import com.xebialabs.overthere.util.gss.GssCli;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.KerberosCredentials;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.impl.auth.SPNegoScheme;
import org.apache.http.protocol.HttpContext;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WsmanSPNegoScheme extends SPNegoScheme {

    private final String spnServiceClass;

    private final String spnAddress;
	private byte[] token;
	private byte[] exportedContext;
	private String spn;
    private final int spnPort;

    public WsmanSPNegoScheme(final boolean stripPort, final String spnServiceClass, final String spnAddress, final int spnPort) {
        super(stripPort);
        this.spnServiceClass = spnServiceClass;
        this.spnAddress = spnAddress;
        this.spnPort = spnPort;
    }

    @Override
    protected byte[] generateGSSToken(final byte[] input, final Oid oid, final String authServer) throws GSSException {
        logger.trace("WsmanSPNegoScheme.generateGSSToken invoked for authServer = {} without credentials", authServer);
        return doGenerateGSSToken(input, oid, authServer, null);
    }

    @Override
    protected byte[] generateGSSToken(final byte[] input, final Oid oid, final String authServer, final Credentials credentials) throws GSSException {
        logger.trace("WsmanSPNegoScheme.generateGSSToken invoked for authServer = {} with credentials", authServer);
        return doGenerateGSSToken(input, oid, authServer, credentials);
    }

   private byte[] doGenerateGSSToken(final byte[] input, final Oid oid, final String authServer, final Credentials credentials) throws GSSException {
        byte[] token = input;
        if (token == null) {
            token = new byte[0];
        }

        final String gssAuthServer;
        if (authServer.equals("localhost")) {
            if (authServer.indexOf(':') > 0) {
                gssAuthServer = spnAddress + ":" + spnPort;
            } else {
                gssAuthServer = spnAddress;
            }
        } else {
            gssAuthServer = authServer;
        }
        final String spn = spnServiceClass + "@" + gssAuthServer;

        final GSSCredential gssCredential;
        if (credentials instanceof KerberosCredentials) {
            gssCredential = ((KerberosCredentials) credentials).getGSSCredential();
        } else {
            gssCredential = null;
        }

        logger.debug("Canonicalizing SPN {}", spn);
        GSSManager manager = getManager();
        GSSName serverName = manager.createName(spn, GSSName.NT_HOSTBASED_SERVICE);
        GSSName canonicalizedName = serverName.canonicalize(oid);

        logger.debug("Requesting SPNego ticket for canonicalized SPN {}", canonicalizedName);
        GSSContext gssContext = manager.createContext(canonicalizedName, oid, gssCredential, JavaVendor.getSpnegoLifetime());
        gssContext.requestMutualAuth(true);
        gssContext.requestCredDeleg(true);
		this.token = gssContext.initSecContext(token, 0, token.length);
		this.spn = spn;
	   	this.exportedContext = gssContext.export();
        return this.token;
    }

	@Override
	public Header authenticate(Credentials credentials, HttpRequest request, HttpContext context) throws AuthenticationException {
		Header hdr = super.authenticate(credentials,request,context);
		if(token != null) {
			try {
				GssCli gssCli = new GssCli(spn,null);
				gssCli.importContext(this.exportedContext);
				context.setAttribute("gssCli",gssCli);
			} catch(Exception ex) {
				logger.error("Error initializing native gssCli library - " + ex.getMessage());
			}
		}
		return hdr;
	}

    private static final Logger logger = LoggerFactory.getLogger(WsmanSPNegoScheme.class);

}
