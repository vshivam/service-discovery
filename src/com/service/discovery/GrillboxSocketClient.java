package com.service.discovery;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import android.app.Activity;
import android.os.Handler;
import android.widget.Toast;

public class GrillboxSocketClient extends WebSocketClient {
	Activity activity;
	Handler handler;

	public GrillboxSocketClient(URI serverURI, Handler handler,
			Activity activity) {
		super(serverURI);
		this.handler = handler;
		this.activity = activity;
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {

		handler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(activity, "opened Connection", Toast.LENGTH_LONG)
						.show();

			}
		});
		// if you plan to refuse connection based on ip or httpfields overload:
		// onWebsocketHandshakeReceivedAsClient
	}

	@Override
	public void onMessage(final String message) {

		handler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(activity, "Message Recieved :" + message,
						Toast.LENGTH_SHORT).show();

			}
		});

	}

	/*
	 * @Override public void onMessage(ByteBuffer bytes) {
	 * super.onMessage(bytes); byte[] b = new byte[bytes.remaining()];
	 * playMp3(b); }
	 * 
	 * MediaPlayer mediaPlayer;
	 * 
	 * private void playMp3(byte[] mp3SoundByteArray) { try { File tempMp3 =
	 * File.createTempFile("kurchina", ".mp3",
	 * Environment.getExternalStorageDirectory()); tempMp3.deleteOnExit();
	 * FileOutputStream fos = new FileOutputStream(tempMp3);
	 * fos.write(mp3SoundByteArray); fos.close();
	 * 
	 * // Tried reusing instance of media player // but that resulted in system
	 * crashes... mediaPlayer = new MediaPlayer();
	 * 
	 * // Tried passing path directly, but kept getting //
	 * "Prepare failed.: status=0x1" // so using file descriptor instead
	 * FileInputStream fis = new FileInputStream(tempMp3);
	 * mediaPlayer.setDataSource(fis.getFD(), 0, tempMp3.length());
	 * 
	 * mediaPlayer.prepare(); mediaPlayer.setOnPreparedListener(new
	 * OnPreparedListener() {
	 * 
	 * @Override public void onPrepared(MediaPlayer mp) { mediaPlayer.start(); }
	 * }); } catch (IOException ex) { String s = ex.toString();
	 * ex.printStackTrace(); } }
	 */
	@Override
	public void onClose(int code, String reason, boolean remote) {
		// The codecodes are documented in class
		// org.java_websocket.framing.CloseFrame
		System.out.println("Connection closed by "
				+ (remote ? "remote peer" : "us") + "Reason " + reason);
	}

	@Override
	public void onError(Exception ex) {
		ex.printStackTrace();
		// if the error is fatal then onClose will be called additionally
	}
}
