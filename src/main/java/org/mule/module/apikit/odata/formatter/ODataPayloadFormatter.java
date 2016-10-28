/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.formatter;

public abstract class ODataPayloadFormatter {
	public enum Format {
		Json, Atom, Plain, Default
	}

	boolean supportsAtom = false;

	abstract public String format(Format format) throws Exception;

	public boolean supportsAtom() {
		return supportsAtom;
	}

	public void setSupportsAtom(boolean supportsAtom) {
		this.supportsAtom = supportsAtom;
	}
}
