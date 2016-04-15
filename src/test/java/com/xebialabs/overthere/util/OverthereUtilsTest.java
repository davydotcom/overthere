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
package com.xebialabs.overthere.util;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.itest.OverthereConnectionItestBase;
import com.xebialabs.overthere.local.LocalConnection;
import com.xebialabs.overthere.local.LocalFile;
import com.xebialabs.overthere.util.gss.GssCli;
import com.xebialabs.overthere.util.gss.GssIOVBufferDesc;
import com.xebialabs.overthere.util.gss.LibGss;
import org.testng.annotations.Test;

import static com.xebialabs.overthere.ConnectionOptions.TEMPORARY_DIRECTORY_PATH;
import static com.xebialabs.overthere.local.LocalConnection.LOCAL_PROTOCOL;
import static org.testng.Assert.*;

public class OverthereUtilsTest extends OverthereConnectionItestBase {
    @Override
    protected String getProtocol() {
        return LOCAL_PROTOCOL;
    }

    @Override
    protected ConnectionOptions getOptions() {
        ConnectionOptions options = new ConnectionOptions();
        options.set(TEMPORARY_DIRECTORY_PATH, temp.getRoot().getPath());
        return options;
    }

    @Override
    protected String getExpectedConnectionClassName() {
        return LocalConnection.class.getName();
    }

    @Test
    public void shouldGenerateUniqueTempFile() {
        OverthereFile base = LocalFile.valueOf(temp.getRoot());
        OverthereFile folderOne = OverthereUtils.getUniqueFolder(base, "same");
        OverthereFile folderTwo = OverthereUtils.getUniqueFolder(base, "same");
        assertTrue(folderOne.exists());
        assertTrue(folderTwo.exists());
        assertNotEquals(folderOne.getPath(), folderTwo.getPath());
    }


	@Test
	public void shouldInitializzeGssJNI() throws Exception {
		assertTrue(LibGss.INSTANCE != null);

		GssCli gsscli = new GssCli("DAVIDESTES-PC","WSMAN",null);

		System.out.println("Context Token: " + gsscli.initContext());

		GssIOVBufferDesc[] iov = new GssIOVBufferDesc[3];

		iov[0] = new GssIOVBufferDesc();
		iov[0].type = LibGss.GSS_IOV_BUFFER_TYPE_HEADER | LibGss.GSS_IOV_BUFFER_FLAG_ALLOCATE;
		iov[1] = new GssIOVBufferDesc();
		iov[1].type = LibGss.GSS_IOV_BUFFER_TYPE_DATA;
		iov[1].buffer.setValue("blah");

		iov[2] = new GssIOVBufferDesc();
		iov[2].type = LibGss.GSS_IOV_BUFFER_TYPE_PADDING | LibGss.GSS_IOV_BUFFER_FLAG_ALLOCATE;


	}
}
