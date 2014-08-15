package com.service.discovery;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import android.os.Environment;

import com.service.discovery.NanoHTTPD.Response.Status;

public class MusicServer extends NanoHTTPD {

	public MusicServer(int port) {
		super(port);
	}

	@Override
	public Response serve(String uri, Method method,
			Map<String, String> header, Map<String, String> parameters,
			Map<String, String> files) {

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(Environment.getExternalStorageDirectory()
					+ "/Samsung/Music/Over_the_horizon.mp3");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new NanoHTTPD.Response(Status.OK, "audio/mpeg", fis);
	}

}
