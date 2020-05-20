/*******************************************************************************
 * Copyright (c) 2017, 2018 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package com.espressif.idf.terminal.connector.serial.connector;

import org.eclipse.cdt.serial.BaudRate;
import org.eclipse.cdt.serial.ByteSize;
import org.eclipse.cdt.serial.Parity;
import org.eclipse.cdt.serial.StopBits;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;

public class SerialSettings {

	public static final String PORT_NAME_ATTR = "cdtserial.portName"; //$NON-NLS-1$
	public static final String BAUD_RATE_ATTR = "cdtserial.baudRate"; //$NON-NLS-1$
	public static final String BYTE_SIZE_ATTR = "cdtserial.byteSize"; //$NON-NLS-1$
	public static final String PARITY_ATTR = "cdtserial.parity"; //$NON-NLS-1$
	public static final String STOP_BITS_ATTR = "cdtserial.stopBits"; //$NON-NLS-1$

	private String portName;
	private BaudRate baudRate;
	private ByteSize byteSize;
	private Parity parity;
	private StopBits stopBits;

	/**
	 * Load information into the RemoteSettings object.
	 */
	public void load(ISettingsStore store) {
		portName = store.get(PORT_NAME_ATTR, ""); //$NON-NLS-1$

		String baudRateStr = store.get(BAUD_RATE_ATTR, ""); //$NON-NLS-1$
		if (baudRateStr.isEmpty()) {
			baudRate = BaudRate.getDefault();
		} else {
			String[] rates = BaudRate.getStrings();
			for (int i = 0; i < rates.length; ++i) {
				if (baudRateStr.equals(rates[i])) {
					baudRate = BaudRate.fromStringIndex(i);
					break;
				}
			}
		}

		String byteSizeStr = store.get(BYTE_SIZE_ATTR, ""); //$NON-NLS-1$
		if (byteSizeStr.isEmpty()) {
			byteSize = ByteSize.getDefault();
		} else {
			String[] sizes = ByteSize.getStrings();
			for (int i = 0; i < sizes.length; ++i) {
				if (byteSizeStr.equals(sizes[i])) {
					byteSize = ByteSize.fromStringIndex(i);
					break;
				}
			}
		}

		String parityStr = store.get(PARITY_ATTR, ""); //$NON-NLS-1$
		if (parityStr.isEmpty()) {
			parity = Parity.getDefault();
		} else {
			String[] parities = Parity.getStrings();
			for (int i = 0; i < parities.length; ++i) {
				if (parityStr.equals(parities[i])) {
					parity = Parity.fromStringIndex(i);
					break;
				}
			}
		}

		String stopBitsStr = store.get(STOP_BITS_ATTR, ""); //$NON-NLS-1$
		if (stopBitsStr.isEmpty()) {
			stopBits = StopBits.getDefault();
		} else {
			String[] bits = StopBits.getStrings();
			for (int i = 0; i < bits.length; ++i) {
				if (stopBitsStr.equals(bits[i])) {
					stopBits = StopBits.fromStringIndex(i);
					break;
				}
			}
		}
	}

	/**
	 * Extract information from the RemoteSettings object.
	 */
	public void save(ISettingsStore store) {
		store.put(PORT_NAME_ATTR, portName);
		store.put(BAUD_RATE_ATTR, BaudRate.getStrings()[BaudRate.getStringIndex(baudRate)]);
		store.put(BYTE_SIZE_ATTR, ByteSize.getStrings()[ByteSize.getStringIndex(byteSize)]);
		store.put(PARITY_ATTR, Parity.getStrings()[Parity.getStringIndex(parity)]);
		store.put(STOP_BITS_ATTR, StopBits.getStrings()[StopBits.getStringIndex(stopBits)]);
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public BaudRate getBaudRate() {
		return baudRate;
	}

	public void setBaudRate(BaudRate baudRate) {
		this.baudRate = baudRate;
	}

	public ByteSize getByteSize() {
		return byteSize;
	}

	public void setByteSize(ByteSize byteSize) {
		this.byteSize = byteSize;
	}

	public Parity getParity() {
		return parity;
	}

	public void setParity(Parity parity) {
		this.parity = parity;
	}

	public StopBits getStopBits() {
		return stopBits;
	}

	public void setStopBits(StopBits stopBits) {
		this.stopBits = stopBits;
	}

	public String getSummary() {
		return portName;
	}

}
