/*******************************************************************************
 * Copyright (c) 2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.util;

import gnu.gettext.GettextResource;

import java.util.ResourceBundle;

public class Translate {
    private final static ResourceBundle RESOURCEBUNDLECONSOLE = ResourceBundle
	    .getBundle("com.blogspot.thedsweb.local.yawls_template");

    private Translate() {
    }

    public static String _(String s) {
	return GettextResource.gettext(RESOURCEBUNDLECONSOLE, s);
    }

}
