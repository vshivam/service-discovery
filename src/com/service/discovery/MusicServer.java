package com.service.discovery;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.service.discovery.NanoHTTPD.Response.Status;

public class MusicServer extends NanoHTTPD {

	public MusicServer(int port) {
		super(port);
	}

	static HashMap<String, Song> songList;

	public static ArrayList<Song> getListOfMusicFiles(Context context) {

		songList = new HashMap<String, Song>();

		ArrayList<Song> list = new ArrayList<Song>();
		// String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

		String[] projection = { MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.DISPLAY_NAME,
				MediaStore.Audio.Media.DURATION };

		Cursor cursor = context.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null,
				null, null);

		if (cursor != null) {
			while (cursor.moveToNext()) {
				Log.d("MusicServer",
						cursor.getString(3) + " >>> " + cursor.getString(0));
				songList.put(cursor.getString(0), new Song(cursor.getString(3),
						cursor.getString(4), cursor.getString(2)));
			}
		}

		return list;
	}

	@Override
	public Response serve(String uri, Method method,
			Map<String, String> header, Map<String, String> parameters,
			Map<String, String> files) {

		if (uri.contains("/play/")) {
			String[] parts = uri.split("/");
			Log.d("MusicServer", parts[2]);
			Log.d("MusicServer", songList.get(parts[2]).getPath());
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(songList.get(parts[2]).getPath());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return new NanoHTTPD.Response(Status.OK, "audio/mpeg", fis);
		}
		return null;
	}
}
