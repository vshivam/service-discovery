package com.service.discovery;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

public class GrillboxSocketServer extends WebSocketServer {

	Handler handler;
	InetSocketAddress address;
	Activity activity;

	public GrillboxSocketServer(int port, Handler handler, Activity activity)
			throws UnknownHostException {
		super(new InetSocketAddress(port));
		address = new InetSocketAddress(port);
		this.handler = handler;
		this.activity = activity;
	}

	@Override
	public void start() {
		super.start();
		handler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(activity, "Starting server on " + address,
						Toast.LENGTH_SHORT).show();
			}
		});

	}

	@Override
	public void onOpen(final WebSocket conn, ClientHandshake handshake) {
		this.sendToAll("new connection: " + handshake.getResourceDescriptor());

		handler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(
						activity,
						conn.getRemoteSocketAddress().getAddress()
								.getHostAddress()
								+ " entered the room!", Toast.LENGTH_SHORT)
						.show();
			}
		});

	}

	@Override
	public void onClose(final WebSocket conn, int code, String reason,
			boolean remote) {
		this.sendToAll(conn + " has left the room!");
		handler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(activity, conn + " left the room!",
						Toast.LENGTH_SHORT).show();
			}
		});

	}

	@Override
	public void onMessage(WebSocket conn, final String message) {
		this.sendToAll(message);
		handler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(activity, " All Message Recieved :" + message,
						Toast.LENGTH_SHORT).show();
			}
		});
		if (message.trim().equals("music")) {
			handler.post(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(activity,
							" Request for music received :" + message,
							Toast.LENGTH_SHORT).show();
				}
			});
			File file = new File(Environment.getExternalStorageDirectory()
					+ "/music/parabola.mp3");
			int size = (int) file.length();
			FileInputStream fis = null;
			byte[] bytes = new byte[size];
			try {
				fis = new FileInputStream(file);
				BufferedInputStream buf = new BufferedInputStream(fis);
				buf.read(bytes, 0, bytes.length);
				buf.close();
				conn.send(bytes);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		ex.printStackTrace();
		if (conn != null) {
			// some errors like port binding failed may not be assignable to a
			// specific websocket
		}
	}

	/**
	 * Sends <var>text</var> to all currently connected WebSocket clients.
	 * 
	 * @param text
	 *            The String to send across the network.
	 * @throws InterruptedException
	 *             When socket related I/O errors occur.
	 */
	public void sendToAll(String text) {
		Collection<WebSocket> con = connections();
		synchronized (con) {
			for (WebSocket c : con) {
				c.send(text);
			}
		}
	}
}