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

package ilg.gnumcueclipse.debug.gdbjtag.viewmodel.peripheral;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IRegister;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IValue;

import ilg.gnumcueclipse.debug.gdbjtag.Activator;
import ilg.gnumcueclipse.debug.gdbjtag.datamodel.SvdClusterDMNode;
import ilg.gnumcueclipse.debug.gdbjtag.datamodel.SvdDMNode;
import ilg.gnumcueclipse.debug.gdbjtag.datamodel.SvdFieldDMNode;
import ilg.gnumcueclipse.debug.gdbjtag.datamodel.SvdObjectDMNode;
import ilg.gnumcueclipse.debug.gdbjtag.datamodel.SvdPeripheralDMNode;
import ilg.gnumcueclipse.debug.gdbjtag.datamodel.SvdRegisterDMNode;
import ilg.gnumcueclipse.packs.core.tree.Node;

public abstract class PeripheralTreeVMNode implements IRegister, Comparable<PeripheralTreeVMNode> {

	// ------------------------------------------------------------------------

	protected PeripheralGroupVMNode fPeripheral;

	protected PeripheralTreeVMNode fParent;
	protected ArrayList<PeripheralTreeVMNode> fChildren;
	protected SvdDMNode fDMNode;
	protected PeripheralPath fPath;
	protected String fName;
	private boolean fHasChanged;
	protected int fFadingLevel;

	// protected BigInteger fBigArrayAddressOffset;

	// ------------------------------------------------------------------------

	public PeripheralTreeVMNode(PeripheralTreeVMNode parent, SvdDMNode dmNode) {

		fParent = parent;
		if (fParent != null) {
			// Automatically register to parent.
			fParent.addChild(this);
		}

		fChildren = null;

		fDMNode = dmNode;
		assert fDMNode != null;

		fPath = null;
		fHasChanged = false;

		fFadingLevel = 0;

		// The implementation display name is used for views.
		fName = fDMNode.getDisplayName();
		assert fName != null;

		if (fDMNode.getNode().getPackType() == Node.PACK_TYPE_XPACK) {
			if (fName.indexOf("%s") < 0) {
				// If XPack, and the %s was not used, use defaults.
				if (fDMNode.isArray()) {
					fName += "[%s]";
				} else if (fDMNode.isRepetition()) {
					fName += "%s";
				}
			}
		}

		// Only the root node is fully functional as a group node, the
		// intermediate cluster nodes are only for presentation (this might
		// change).
		PeripheralTreeVMNode node = this;
		while (node.getParent() != null) {
			node = (PeripheralTreeVMNode) node.getParent();
		}

		// Return the root node of the hierarchy
		fPeripheral = (PeripheralGroupVMNode) node;
		assert fPeripheral != null;

		// fBigArrayAddressOffset = BigInteger.ZERO;
	}

	public void dispose() {

		// System.out.println("dispose() " + this);

		fParent = null;
		fChildren = null;
		if (fDMNode != null) {
			fDMNode.dispose();
			fDMNode = null;
		}
		fPath = null;
	}

	// ------------------------------------------------------------------------

	/**
	 * Register the given node as child of the current node.
	 * 
	 * @param child
	 *            the node to be registered.
	 */
	protected void addChild(PeripheralTreeVMNode child) {

		if (fChildren == null) {
			fChildren = new ArrayList<PeripheralTreeVMNode>();
		}
		fChildren.add(child);
	}

	// ------------------------------------------------------------------------
	// Functions used in the view classes.

	/**
	 * Get the children nodes. If the node has not children, return an empty array,
	 * not null.
	 * 
	 * @return an array of children nodes.
	 */
	public Object[] getChildren() {

		if (fChildren == null) {
			prepareChildren();
		}
		PeripheralTreeVMNode[] children = fChildren.toArray(new PeripheralTreeVMNode[fChildren.size()]);

		Arrays.sort(children);
		// System.out.println(getPath() + " has " + children.length +
		// " children");
		return children;
	}

	/**
	 * Get the node parent. All nodes, except the root node, have a parent node.
	 * 
	 * @return a node or null, if root.
	 */
	public Object getParent() {
		return fParent;
	}

	/**
	 * Lazy load implementation, prepares the list of children nodes only when
	 * needed.
	 * 
	 * @return true if the node actually has children.
	 */
	public boolean hasChildren() {

		if (fChildren == null) {
			prepareChildren();
		}
		// System.out.println(getPath() + " has children is " +
		// !fChildren.isEmpty());
		return (!fChildren.isEmpty());
	}

	// ------------------------------------------------------------------------
	// Functions contributed by IRegister.

	@Override
	public String getModelIdentifier() {
		return null;
	}

	@Override
	public IDebugTarget getDebugTarget() {
		return null;
	}

	@Override
	public ILaunch getLaunch() {
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public IValue getValue() throws DebugException {
		return null; // RegisterVMNode will return it
	}

	@Override
	public String getReferenceTypeName() throws DebugException {
		return null;
	}

	@Override
	public boolean hasValueChanged() throws DebugException {
		return fHasChanged;
	}

	@Override
	public void setValue(String expression) throws DebugException {
		;
	}

	@Override
	public void setValue(IValue value) throws DebugException {
		;
	}

	@Override
	public boolean supportsValueModification() {
		return !isReadOnly();
	}

	@Override
	public boolean verifyValue(String expression) throws DebugException {
		return false;
	}

	@Override
	public boolean verifyValue(IValue value) throws DebugException {
		return false;
	}

	@Override
	public IRegisterGroup getRegisterGroup() throws DebugException {
		return fPeripheral;
	}

	@Override
	public String getName() throws DebugException {
		return fName;
	}

	// ------------------------------------------------------------------------

	public boolean isField() {
		return (fDMNode instanceof SvdFieldDMNode);
	}

	public boolean isRegister() {
		return (fDMNode instanceof SvdRegisterDMNode);
	}

	public boolean isCluster() {
		return (fDMNode instanceof SvdClusterDMNode);
	}

	public boolean isPeripheral() {
		return (fDMNode instanceof SvdPeripheralDMNode);
	}

	// ------------------------------------------------------------------------

	public PeripheralPath getPath() {

		if (fPath == null) {

			PeripheralTreeVMNode parent = (PeripheralTreeVMNode) getParent();

			if (parent != null) {
				return new PeripheralPath(parent.getPath(), new PeripheralPath(fName));
			} else {
				return new PeripheralPath(fName);
			}
		}

		return fPath;
	}

	public String getQualifiedName() {
		return getPath().toString();
	}

	public String getDescription() {
		return fDMNode.getDescription();
	}

	// public long getLongAddressOffset() {
	// return fDMNode.getBigAddressOffset().longValue()
	// + fBigArrayAddressOffset.longValue();
	// }

	public BigInteger getThisBigAddressOffset() {
		return fDMNode.getBigAddressOffset();
	}

	/**
	 * Compute the cumulated offset, up to the peripheral node.
	 * 
	 * @return a big integer.
	 */
	public BigInteger getPeripheralBigAddressOffset() {

		BigInteger offset = getThisBigAddressOffset();
		if (offset == null) {
			System.out.println("");
		}
		PeripheralTreeVMNode parent = (PeripheralTreeVMNode) getParent();
		if (parent != null) {
			BigInteger b = parent.getPeripheralBigAddressOffset();
			if (b == null) {
				System.out.println();
			}
			offset = offset.add(b);
		}

		return offset;
	}

	/**
	 * Compute the absolute address of the node, by adding the total offset to the
	 * peripheral base.
	 * 
	 * @return a big integer with the absolute address.
	 */
	public BigInteger getBigAbsoluteAddress() {

		BigInteger peripheralBaseAddress;
		try {
			peripheralBaseAddress = ((PeripheralTopVMNode) getRegisterGroup()).getBigAbsoluteAddress();
		} catch (DebugException e) {
			peripheralBaseAddress = BigInteger.ZERO;
		}
		BigInteger offset;
		offset = getPeripheralBigAddressOffset();

		return peripheralBaseAddress.add(offset);
	}

	public BigInteger getBigSize() {
		return fDMNode.getBigSizeBytes();
	}

	/**
	 * Get a string defining the current element. To be used only to prefix
	 * tool-tips, for decision making, use the isXXX() test functions.
	 * 
	 * @return a string, possibly empty.
	 */
	public abstract String getDisplayNodeType();

	/**
	 * Get the name of the icon file used for the node icon.
	 * 
	 * @return a string.
	 */
	public abstract String getImageName();

	/**
	 * Get the access string.
	 * 
	 * @return a string, possibly empty.
	 */
	public String getAccess() {
		return fDMNode.getAccess();
	}

	/**
	 * Get the read action string. A non-empty string means the peripheral/field
	 * should not be read, since this will trigger some clean/set action.
	 * 
	 * @return a string, possibly empty.
	 */
	public String getReadAction() {
		return fDMNode.getReadAction();
	}

	/**
	 * Test if the element is write only.
	 * 
	 * @return true if write only.
	 */
	public boolean isWriteOnly() {
		return fDMNode.isWriteOnly();
	}

	public boolean isReadOnly() {
		return fDMNode.isReadOnly();
	}

	/**
	 * Test if the element can be read.
	 * 
	 * @return true if readable.
	 */
	public boolean isReadAllowed() {
		return fDMNode.isReadAllowed();
	}

	public boolean hasReadAction() {
		return fDMNode.hasReadAction();
	}

	/**
	 * Get the string with the node address as to be displayed.
	 * 
	 * @return a string or null;
	 */
	public String getDisplayAddress() {

		BigInteger bigAddress = getBigAbsoluteAddress();
		if (bigAddress != null) {
			return String.format("0x%08X", bigAddress.longValue());
		}

		return null;
	}

	public String getDisplayOffset() {

		long value = getPeripheralBigAddressOffset().longValue();
		if (value < 0x10000) {
			return String.format("0x%04X", value);
		} else {
			return String.format("0x%08X", value);
		}
	}

	public abstract String getDisplaySize();

	public String getDisplayValue() {
		return null;
	}

	protected PeripheralTopVMNode getPeripheral() {

		try {
			return (PeripheralTopVMNode) getRegisterGroup();
		} catch (DebugException e) {
		}
		return null;
	}

	public boolean isArray() {
		return fDMNode.isArray();
	}

	public boolean isRepetition() {
		return fDMNode.isRepetition();
	}

	public int getArraySize() {
		return fDMNode.getArraySize();
	}

	// ------------------------------------------------------------------------

	/**
	 * Prepare the list of children. After this call fChildren can no longer be
	 * null, it contains a list of nodes, even if this list is empty.
	 */
	private void prepareChildren() {

		if (fChildren != null) {
			// If children are already there, nothing to do, return.
			return;
		}

		// If not, start by creating the list.
		fChildren = new ArrayList<PeripheralTreeVMNode>();

		// Get the array of actual children from each node implementation.
		SvdObjectDMNode[] svdChildren = fDMNode.getChildren();
		assert svdChildren != null;

		for (int i = 0; i < svdChildren.length; ++i) {
			SvdDMNode child = (SvdDMNode) svdChildren[i];

			/**
			 * Based on context and node type, create the proper nodes which will
			 * automatically register as children of the current node.
			 */
			if (child.isArray()) {
				processArray((SvdDMNode) child);
			} else if (child.isRepetition()) {
				processRepetitions((SvdDMNode) child);
			} else {
				// Simple case, a single element.
				if (child instanceof SvdClusterDMNode) {
					new PeripheralClusterVMNode(this, child);
				} else if (child instanceof SvdRegisterDMNode) {
					new PeripheralRegisterVMNode(this, child);
				} else if (child instanceof SvdFieldDMNode) {
					new PeripheralRegisterFieldVMNode(this, child);
				} else {
					Activator.log(child.getClass().getSimpleName() + " not processed");
				}
			}

		}
	}

	private void processRepetitions(SvdDMNode child) {

		BigInteger increment = child.getBigRepeatIncrement();
		String[] indices = child.getRepetitionSubstitutions();
		if (indices.length == 0) {
			Activator.log(child.getClass().getSimpleName() + " has no repetitions");
			return;
		}

		if (child instanceof SvdClusterDMNode) {

			BigInteger offset = BigInteger.ZERO;
			for (int i = 0; i < indices.length; ++i) {
				new PeripheralClusterRepetitionVMNode(this, child, indices[i], offset);

				if (increment != null) {
					offset = offset.add(increment);
				}
			}
		} else if (child instanceof SvdRegisterDMNode) {

			BigInteger offset = BigInteger.ZERO;
			for (int i = 0; i < indices.length; ++i) {
				new PeripheralRegisterRepetitionVMNode(this, child, indices[i], offset);

				assert (increment != null);
				assert (increment.compareTo(BigInteger.ZERO) > 0);
				offset = offset.add(increment);
			}
		} else if (child instanceof SvdFieldDMNode) {

			int offset = 0;
			int intIncrement = increment.intValue();
			for (int i = 0; i < indices.length; ++i) {
				new PeripheralRegisterFieldRepetitionVMNode(this, child, indices[i], offset);

				assert (intIncrement > 0);
				offset += intIncrement;
			}
		} else {
			Activator.log(child.getClass().getSimpleName() + " not processed");
		}
	}

	private void processArray(SvdDMNode child) {
		BigInteger increment = child.getBigRepeatIncrement();

		// For arrays, create an intermediate group and below it add the
		// array elements.
		if (child instanceof SvdClusterDMNode) {

			PeripheralTreeVMNode arrayNode = new PeripheralClusterArrayVMNode(this, child);
			arrayNode.substituteRepetition("");
			BigInteger offset = BigInteger.ZERO;
			for (int i = 0; i < child.getArraySize(); ++i) {
				new PeripheralClusterArrayElementVMNode(arrayNode, child, i, offset);

				if (increment != null) {
					offset = offset.add(increment);
				}
			}
		} else if (child instanceof SvdRegisterDMNode) {

			PeripheralTreeVMNode arrayNode = new PeripheralRegisterArrayVMNode(this, child);
			arrayNode.substituteRepetition("");
			BigInteger offset = BigInteger.ZERO;
			for (int i = 0; i < child.getArraySize(); ++i) {
				new PeripheralRegisterArrayElementVMNode(arrayNode, child, i, offset);

				assert (increment != null);
				assert (increment.compareTo(BigInteger.ZERO) > 0);
				offset = offset.add(increment);
			}
		} else {
			Activator.log(child.getClass().getSimpleName() + " not processed");
		}
	}

	/**
	 * For array elements, substitute the %s with the actual index; for repetitions,
	 * substitute with the generated value.
	 * 
	 * @param str
	 *            a string, must be a valid C variable name.
	 */
	protected void substituteRepetition(String str) {
		if (fName.indexOf("%s") >= 0) {
			if (str.isEmpty() && fName.indexOf("[%s]") < 0) {
				str = "[]";
			}
			fName = String.format(fName, str);
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Inform node when content changed. If really changed, set the fading level to
	 * 3.
	 * 
	 * @param hasChanged
	 */
	public void setChanged(boolean hasChanged) {

		fHasChanged = hasChanged;

		if (fHasChanged) {
			setFadingLevel(3);
		}
	}

	public int getFadingLevel() {
		return fFadingLevel;
	}

	public void setFadingLevel(int level) {

		fFadingLevel = level;

		if (fFadingLevel > 0) {
			if (Activator.getInstance().isDebugging()) {
				System.out.println("PeripheralTreeVMNode.setFadingLevel() " + fName + " " + fFadingLevel);
			}
		}
	}

	public void decrementFadingLevel() {

		if (fFadingLevel > 0) {
			setFadingLevel(fFadingLevel - 1);
		}

		if (fChildren != null) {
			for (PeripheralTreeVMNode node : fChildren) {
				node.decrementFadingLevel();
			}
		}
	}

	// ------------------------------------------------------------------------

	@Override
	public String toString() {
		return "[" + getClass().getSimpleName() + ": " + getDisplayNodeType() + ":" + getQualifiedName() + "]";
	}

	@Override
	public int compareTo(PeripheralTreeVMNode comp) {
		return fDMNode.compareTo(comp.fDMNode);
	}

	// ------------------------------------------------------------------------
}
