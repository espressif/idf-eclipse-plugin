/*******************************************************************************
 * Copyright (c) 2014 Liviu Ionescu.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Liviu Ionescu - initial implementation.
 *******************************************************************************/

package ilg.gnumcueclipse.packs.cmsis;

import ilg.gnumcueclipse.packs.core.ConsoleStream;
import ilg.gnumcueclipse.packs.core.tree.Leaf;
import ilg.gnumcueclipse.packs.core.tree.Node;
import ilg.gnumcueclipse.packs.core.tree.Property;
import ilg.gnumcueclipse.packs.core.tree.Type;
import ilg.gnumcueclipse.packs.data.Activator;
import ilg.gnumcueclipse.packs.data.Utils;

import org.eclipse.ui.console.MessageConsoleStream;

import com.github.zafarkhaja.semver.Version;

public class PdscTreeParser {

	protected MessageConsoleStream fOut;

	protected Version fSemVer;

	public PdscTreeParser() {

		fOut = ConsoleStream.getConsoleOut();
	}

	protected boolean checkValid(Node node) {

		if (node == null || !node.hasChildren()) {
			return false;
		}

		Leaf firstChild = node.getFirstChild();
		if (!firstChild.isType("package")) {
			String msg = "Missing <package>; instead, <" + firstChild.getType() + "> encountered";

			fOut.println("Error+" + msg);
			Utils.reportError(msg);
			return false;
		}

		String schemaVersion = firstChild.getProperty("schemaVersion");
		fSemVer = Version.valueOf(schemaVersion);
		if (!PdscUtils.isSchemaValid(fSemVer)) {
			Activator.log("Unrecognised schema version " + schemaVersion);
			return false;
		}
		return true;
	}

	protected Node addUniqueVendor(Node parent, String vendorName, String vendorId) {

		if (parent.hasChildren()) {
			for (Leaf child : parent.getChildren()) {
				if (vendorId.equals(child.getProperty(Property.VENDOR_ID))) {
					return (Node) child;
				}
			}
		}

		Node vendor = Node.addNewChild(parent, Type.VENDOR);
		vendor.setName(vendorName);
		vendor.putProperty(Property.VENDOR_ID, vendorId);

		return vendor;
	}

	protected String updatePosixSeparators(String spath) {
		return spath.replace('\\', '/');
	}

}
