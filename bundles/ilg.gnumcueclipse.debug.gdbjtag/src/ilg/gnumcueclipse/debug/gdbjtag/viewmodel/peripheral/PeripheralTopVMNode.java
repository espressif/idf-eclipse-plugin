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

import ilg.gnumcueclipse.debug.gdbjtag.Activator;
import ilg.gnumcueclipse.debug.gdbjtag.datamodel.SvdDMNode;
import ilg.gnumcueclipse.debug.gdbjtag.datamodel.SvdPeripheralDMNode;
import ilg.gnumcueclipse.debug.gdbjtag.memory.PeripheralMemoryBlockExtension;

public class PeripheralTopVMNode extends PeripheralGroupVMNode {

	// ------------------------------------------------------------------------

	private PeripheralMemoryBlockExtension fMemoryBlock;

	// ------------------------------------------------------------------------

	public PeripheralTopVMNode(PeripheralTreeVMNode parent, SvdDMNode dmNode,
			PeripheralMemoryBlockExtension memoryBlock) {

		super(parent, dmNode);

		if (Activator.getInstance().isDebugging()) {
			System.out.println("PeripheralTopVMNode() " + dmNode.getName());
		}
		fMemoryBlock = memoryBlock;
	}

	@Override
	public void dispose() {

		fMemoryBlock = null;
		if (Activator.getInstance().isDebugging()) {
			System.out.println("PeripheralTopVMNode.dispose()");
		}
		super.dispose();
	}

	// ------------------------------------------------------------------------

	public PeripheralMemoryBlockExtension getMemoryBlock() {
		return fMemoryBlock;
	}

	/**
	 * Register groups are peripherals or clusters, return the address of the
	 * peripheral.
	 * 
	 * @return a big integer with the start address.
	 */
	@Override
	public BigInteger getBigAbsoluteAddress() {
		return fDMNode.getBigAbsoluteAddress();
	}

	@Override
	public String getDisplayNodeType() {
		return "Peripheral";
	}

	@Override
	public String getImageName() {
		return "peripheral";
	}

	// ------------------------------------------------------------------------

	public String getDisplayGroupName() {
		return ((SvdPeripheralDMNode) fDMNode).getGroupName();
	}

	public String getDisplayVersion() {
		return ((SvdPeripheralDMNode) fDMNode).getVersion();
	}

	// ------------------------------------------------------------------------
}
