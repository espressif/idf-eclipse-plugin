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
 *     Liviu Ionescu - initial version 
 *******************************************************************************/

package ilg.gnumcueclipse.debug.gdbjtag.datamodel;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import ilg.gnumcueclipse.debug.gdbjtag.Activator;
import ilg.gnumcueclipse.packs.core.tree.AbstractTreePreOrderIterator;
import ilg.gnumcueclipse.packs.core.tree.ITreeIterator;
import ilg.gnumcueclipse.packs.core.tree.Leaf;
import ilg.gnumcueclipse.packs.core.tree.Node;

public class SvdRegisterDMNode extends SvdDMNode {

	// ------------------------------------------------------------------------

	private String fDisplayName;
	private BigInteger fAddressOffset;
	private Integer fSize;
	private BigInteger fBigSizeBytes;
	private BigInteger fBigRepeatIncrement;

	private String fReadAction;
	private String fResetValue;
	private String fResetMask;

	// ------------------------------------------------------------------------

	public SvdRegisterDMNode(Leaf node) {

		super(node);

		fDisplayName = null;
		fAddressOffset = null;
		fSize = null;
		fBigSizeBytes = null;
		fBigRepeatIncrement = null;
		fReadAction = null;

		fResetValue = null;
		fResetMask = null;
	}

	public SvdRegisterDMNode(Leaf node, String displayName, BigInteger addressOffset) {

		this(node);

		// Start with given name and address offset.
		fDisplayName = displayName;
		fAddressOffset = addressOffset;
	}

	/**
	 * Clear all internal references.
	 */
	@Override
	public void dispose() {

		fDisplayName = null;
		fAddressOffset = null;
		fSize = null;
		fBigSizeBytes = null;
		fBigRepeatIncrement = null;

		fReadAction = null;

		fResetValue = null;
		fResetMask = null;

		super.dispose();
	}

	// ------------------------------------------------------------------------

	@Override
	protected SvdObjectDMNode[] prepareChildren(Leaf node) {

		if (node == null || !node.hasChildren()) {
			return null;
		}

		// System.out.println("prepareChildren(" + node.getName() +
		// ")");

		Leaf group = ((Node) node).findChild("fields");
		if (!group.hasChildren()) {
			return null;
		}

		List<SvdObjectDMNode> list = new LinkedList<SvdObjectDMNode>();
		for (Leaf child : ((Node) group).getChildren()) {

			// Keep only <field> nodes
			if (child.isType("field")) {
				list.add(new SvdFieldDMNode(child));
			}
		}

		SvdObjectDMNode[] array = list.toArray(new SvdObjectDMNode[list.size()]);

		// Preserve apparition order.
		return array;
	}

	// ------------------------------------------------------------------------

	/**
	 * Enumerate all registers and find the derived from node. The name is taken
	 * from the derivedFrom attribute.
	 * 
	 * @return a register node, or null if not found.
	 */
	@Override
	protected Leaf findDerivedFromNode() {

		String derivedFromName = getNode().getPropertyOrNull("derivedFrom");
		final SvdDerivedFromPath path = SvdDerivedFromPath.createRegisterPath(derivedFromName);

		if (path == null) {
			return null;
		}

		Node root = getNode().getParent();
		while (!root.isType("device")) {
			root = root.getParent();
		}

		ITreeIterator peripheralNodes = new AbstractTreePreOrderIterator() {

			@Override
			public boolean isIterable(Leaf node) {

				String clustersElement = "";
				if (node.getPackType() == Node.PACK_TYPE_CMSIS) {
					clustersElement = "cluster";
				} else if (node.getPackType() == Node.PACK_TYPE_XPACK) {
					clustersElement = "clusters";
				}

				if (node.isType("peripherals")) {
					return true;
				} else if (node.isType("peripheral")) {
					if (path.peripheralName == null) {
						return true;
					}
					if (path.peripheralName.equals(node.getName())) {
						return true;
					}
					return false;
				} else if (node.isType("registers")) {
					return true;
				} else if (node.isType(clustersElement)) {
					return true;
				} else if (node.isType("register")) {
					if (path.registerName == null) {
						return true;
					}
					if (path.registerName.equals(node.getName())) {
						return true;
					}
					return false;
				}
				return false;
			}

			@Override
			public boolean isLeaf(Leaf node) {

				if (node.isType("register")) {
					return true;
				}
				return false;
			}
		};

		// Iterate only the current device children nodes
		peripheralNodes.setTreeNode(root);

		Leaf ret = null;
		for (Leaf node : peripheralNodes) {

			// System.out.println(node);
			if (node.isType("register")) {
				// There should be only one, filtered by the iterator.
				if (ret == null) {
					ret = node;
				} else {
					Activator.log("Non unique SVD path " + path);
				}
			}
		}

		return ret;
	}

	// ------------------------------------------------------------------------

	/**
	 * Get the address offset inside the peripheral block or the cluster.
	 * <p>
	 * Remark: <tt>addressOffset</tt> is mandatory and cannot be derived.
	 * 
	 * @return a big integer with the address offset.
	 */
	@Override
	public BigInteger getBigAddressOffset() {

		if (fAddressOffset == null) {

			try {
				String offset = getNode().getProperty("addressOffset");
				if (!offset.isEmpty()) {

					fAddressOffset = SvdUtils.parseScaledNonNegativeBigInteger(offset);
				} else {
					Activator.log("Missing addressOffset, node " + getNode());
					fAddressOffset = BigInteger.ZERO;
				}
			} catch (NumberFormatException e) {
				Activator.log("Bad offset, node " + getNode());
				fAddressOffset = BigInteger.ZERO;
			}
		}

		return fAddressOffset;
	}

	/**
	 * Get the display name; if missing, use the name.
	 * <p>
	 * It may contain special characters and white spaces. The place holder %s can
	 * be used and is replaced by the dimIndex substring.
	 * 
	 * @return a short string.
	 */
	@Override
	public String getDisplayName() {

		if (fDisplayName == null) {
			fDisplayName = getPropertyWithDerived("displayName", getName());
		}
		return fDisplayName;
	}

	/**
	 * Get the register size, in bits.
	 * 
	 * @return an integer (usually 32).
	 */
	@Override
	public int getWidthBits() {

		if (fSize == null) {

			String size = "32";
			assert (getNode() != null) : "getWidthBits() of " + this;
			if (getNode().getPackType() == Leaf.PACK_TYPE_CMSIS) {
				size = getPropertyWithDerivedWithParent("size", "32");
			} else if (getNode().getPackType() == Leaf.PACK_TYPE_XPACK) {
				size = getPropertyWithDerivedWithParent("regWidth", "32");
			}

			try {
				fSize = new Integer(SvdUtils.parseScaledNonNegativeBigInteger(size).intValue());
			} catch (NumberFormatException e) {
				Activator.log("Bad size, node " + getNode());
				fSize = new Integer(32);
			}

		}

		return fSize.intValue();
	}

	@Override
	public BigInteger getBigSizeBytes() {

		if (fBigSizeBytes == null) {
			// Round to next byte.
			String sizeBytes = String.valueOf((getWidthBits() + 7) / 8);
			fBigSizeBytes = new BigInteger(sizeBytes);
		}
		return fBigSizeBytes;
	}

	@Override
	public BigInteger getBigRepeatIncrement() {

		if (fBigRepeatIncrement == null) {
			fBigRepeatIncrement = getBigSizeBytes();
			BigInteger arrayAddressIncrement = getBigArrayAddressIncrement();
			if (arrayAddressIncrement != BigInteger.ZERO) {
				if (arrayAddressIncrement.compareTo(fBigRepeatIncrement) > 0) {
					fBigRepeatIncrement = arrayAddressIncrement;
				}
			}
		}
		return fBigRepeatIncrement;
	}

	// ------------------------------------------------------------------------

	public String getReadAction() {

		if (fReadAction == null) {
			fReadAction = getPropertyWithDerivedWithParent("readAction");
		}

		return fReadAction;
	}

	public String getResetValue() {

		if (fResetValue == null) {
			fResetValue = getPropertyWithDerivedWithParent("resetValue");
		}

		return fResetValue;
	}

	public String getResetMask() {

		if (fResetMask == null) {
			fResetMask = getPropertyWithDerivedWithParent("resetMask");
		}

		return fResetMask;
	}

	// ------------------------------------------------------------------------

	@Override
	public String toString() {

		String hexAddr = String.format("0x%08X", getBigAddressOffset().longValue());
		return "[" + getClass().getSimpleName() + ": " + getDisplayName() + ", " + hexAddr + ", " + getAccess() + ", \""
				+ getDescription() + "\"]";
	}
	// ------------------------------------------------------------------------
}
