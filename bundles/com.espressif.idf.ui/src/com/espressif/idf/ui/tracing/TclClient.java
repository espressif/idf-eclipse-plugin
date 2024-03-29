/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tracing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.espressif.idf.core.logging.Logger;
/**
 * Tcl client class for application level tracing
 * 
 * @author Denys Almazov
 *
 */
public class TclClient {
	
    private Socket clientSocket;
	private BufferedReader in;
    private BufferedWriter out;
    private final static int openOcdEOL = 0x1a; //end-of-line character for the openocd server
    public TclClient() {
    	try {
			clientSocket = new Socket("localhost", 6666); //$NON-NLS-1$
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    		out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));   
        } catch (IOException e) {
            Logger.log(e);
        }
    }

	public void startTracing(String[] params)
	{
		String startCommand = "esp apptrace start "; //$NON-NLS-1$
		startCommand = startCommand + String.join(" ", params); //$NON-NLS-1$
    	if(clientSocket.isConnected()) {
    		try {
				out.write("capture \"" + startCommand + "\""); //$NON-NLS-1$ //$NON-NLS-2$
				out.write(openOcdEOL);
				out.flush();
			} catch (IOException e) {
				Logger.log(e);
			}
    	}
    }
    
	public void stopTracing()
	{
		String stopCommand = "capture \"esp apptrace stop \""; //$NON-NLS-1$
    	if(clientSocket.isConnected()) {
    		try {

				out.write(stopCommand);
				out.write(openOcdEOL);
				out.flush();
				} catch (IOException e) {
				Logger.log(e);
			}
    	}
    }

	public BufferedReader getInBuffer()
	{
		return in;
	}

}