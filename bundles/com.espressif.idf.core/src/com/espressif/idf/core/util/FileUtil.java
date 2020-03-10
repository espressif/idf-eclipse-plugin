/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.logging.Logger;

/**
 * General File IO operations
 * 
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class FileUtil
{

	public static final String UTF_8 = "UTF-8"; //$NON-NLS-1$
	private static final String EMPTY_STR = ""; //$NON-NLS-1$
	private static final int BUFFER_SIZE = 8192;

	
	/**
	 * Recursively copy one directory to a new destination directory while showing progress.
	 * 
	 * @param source
	 * @param destination
	 * @param monitor
	 * @param count
	 * @param updateSize
	 * @param cancelable
	 * @return
	 * @throws IOException
	 */
	public static IStatus copyDirectory(File source, File destination, IProgressMonitor monitor, int[] count,
			int updateSize, boolean cancelable) throws IOException
	{
		if (monitor != null)
		{
			if (cancelable && monitor.isCanceled())
			{
				return new Status(IStatus.CANCEL, IDFCorePlugin.PLUGIN_ID, IStatus.CANCEL, EMPTY_STR, null);
			}

			count[0]++;
			if (updateSize < 2 || count[0] % updateSize == 0)
			{
				monitor.setTaskName(MessageFormat.format(Messages.FileUtil_CopyingMsg, destination.toString()));
				monitor.worked(1);
			}
		}

		if (source.isDirectory())
		{
			String error = null;

			// make sure we can read the source directory and that we have a writable destination directory
			if (source.canRead() == false)
			{
				error = Messages.FileUtil_SourceDirNotavailable;
			} else
				if (destination.exists() == false)
				{
					if (destination.mkdir() == false)
					{
						error = Messages.FileUtil_DesDirNotavailable;
					}
				} else
					if (destination.isDirectory() == false)
					{
						error = Messages.FileUtil_DestinationNotaDir;
					} else
						if (destination.canWrite() == false)
						{
							error = Messages.FileUtil_WritableProblemMsg;
						}

			if (error == null)
			{
				// copy all files in the source directory
				for (String filename : source.list())
				{
					IStatus status = copyDirectory(new File(source, filename), new File(destination, filename), monitor,
							count, updateSize, cancelable);
					if (status != null && !status.isOK())
					{
						return status;
					}
				}
			} else
			{
				String message = MessageFormat.format(Messages.FileUtil_UnableToCopy, source, destination,
						error);

				Logger.logError(message);
			}
		} else
		{
			try
			{
				// It's a file, can we copy with normal
				IFileSystem system = EFS.getLocalFileSystem();
				IFileStore src = system.fromLocalFile(source);
				src.copy(system.fromLocalFile(destination), EFS.OVERWRITE, new NullProgressMonitor());
			} catch (CoreException e)
			{
				return new Status(IStatus.ERROR, IDFCorePlugin.PLUGIN_ID, IStatus.ERROR, e.getMessage(), e);
			}
		}

		return Status.OK_STATUS;
	}

	/**
	 * Recursively copy one directory to a new destination directory.
	 * 
	 * @param source
	 * @param destination
	 * @return
	 * @throws IOException
	 */
	public static IStatus copyDirectory(File source, File destination) throws IOException
	{
		return copyDirectory(source, destination, null, new int[] { 0 }, 0, false);
	}

	/**
	 * Copy the contents of one file to another. Attempts to use channels for files < 20Mb, uses streams for larger
	 * files. Closes the streams after transfer.
	 * 
	 * @param source
	 * @param destination
	 * @throws IOException
	 */
	public static void copyFile(File source, File destination) throws IOException
	{
		long fileSize = source.length();
		FileInputStream in = new FileInputStream(source);
		FileOutputStream out = new FileOutputStream(destination);
		// for larger files (20Mb) use streams
		if (fileSize > 20971520l)
		{
			try
			{
				pipe(in, out);
			} finally
			{
				try
				{
					if (in != null)
					{
						in.close();
					}
				} catch (Exception e)
				{
					// ignore
				}

				try
				{
					if (out != null)
					{
						out.close();
					}
				} catch (Exception e)
				{
					// ignore
				}
			}
		}
		// smaller files, use channels
		else
		{
			copy(in, out);
		}
	}

	/**
	 * Special optimized version of copying a {@link FileInputStream} to a {@link FileOutputStream}. Uses
	 * {@link FileChannel#transferTo(long, long, java.nio.channels.WritableByteChannel)}. Closes the streams after
	 * copying.
	 * 
	 * @param iStream
	 * @param oStream
	 * @throws IOException
	 */
	private static void copy(FileInputStream iStream, FileOutputStream oStream) throws IOException
	{
		try
		{
			FileChannel inChannel = iStream.getChannel();
			FileChannel outChannel = oStream.getChannel();
			long fileSize = inChannel.size();
			long offs = 0, doneCnt = 0, copyCnt = Math.min(65536, fileSize);
			do
			{
				doneCnt = inChannel.transferTo(offs, copyCnt, outChannel);
				offs += doneCnt;
				fileSize -= doneCnt;
			} while (fileSize > 0);
		} finally
		{
			try
			{
				if (iStream != null)
				{
					iStream.close();
				}
			} catch (Exception e)
			{
				// ignore
			}

			try
			{
				if (oStream != null)
				{
					oStream.close();
				}
			} catch (Exception e)
			{
				// ignore
			}
		}
	}


	public static void write(OutputStream stream, String rawSource, String charset)
	{
		if (stream == null)
		{
			return;
		}

		if (rawSource == null)
		{
			rawSource = EMPTY_STR;
		}
		if (charset == null)
		{
			charset = UTF_8;
		}

		Writer writer = null;
		try
		{
			writer = new OutputStreamWriter(stream, charset);
			writer.write(rawSource);
		} catch (IOException e)
		{
			Logger.log(e);
		} finally
		{
			try
			{
				if (writer != null)
				{
					writer.close();
				}
			} catch (IOException e)
			{
				// ignore
			}
		}
	}

	/**
	 * Pipes from input stream to output stream. Uses a byte buffer of size 8192. Does no flushing or closing of
	 * streams!
	 * 
	 * @param inputStream
	 * @param outputStream
	 * @throws IOException
	 */
	public static void pipe(InputStream input, OutputStream output) throws IOException
	{
		byte[] buffer = new byte[BUFFER_SIZE];
		for (int bytes = input.read(buffer); bytes >= 0; bytes = input.read(buffer))
		{
			output.write(buffer, 0, bytes);
		}
	}

}
