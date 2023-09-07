/*******************************************************************************
 * Copyright 2022-2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core;

import java.util.OptionalInt;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.espressif.idf.core.util.StringUtil;

public class DefaultBoardProvider
{
	private static final int DEFAULT_BOARD_EMPTY_INDEX = 0;
	private static final String ESP32C3_DEFAULT_BOARD = "ESP32-C3 chip (via builtin USB-JTAG)"; //$NON-NLS-1$
	private static final String ESP32S3_DEFAULT_BOARD = "ESP32-S3 chip (via builtin USB-JTAG)"; //$NON-NLS-1$

	private enum EspTarget
	{
		ESP32C3(ESP32C3_DEFAULT_BOARD), ESP32S3(ESP32S3_DEFAULT_BOARD), DEFAULT_TARGET(StringUtil.EMPTY);

		private final String board;

		EspTarget(String defaultBoard)
		{
			this.board = defaultBoard;
		}

		public static EspTarget enumOf(String value)
		{
			return Stream.of(EspTarget.values()).filter(target -> target.name().equalsIgnoreCase(value)).findAny()
					.orElse(DEFAULT_TARGET);
		}

	}
	
	public int getIndexOfDefaultBoard(String targetName, String[] boardsForTarget)
	{
		String defaultBoard = EspTarget.enumOf(targetName).board;

		OptionalInt index = IntStream.range(0, boardsForTarget.length)
				.filter(i -> defaultBoard.equals(boardsForTarget[i])).findFirst();

		return index.orElse(DEFAULT_BOARD_EMPTY_INDEX);
	}

}
