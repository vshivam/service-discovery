package com.service.discovery;

public class Song {
	String path;
	String display_name;
	String title;

	public Song(String path, String display_name, String title) {
		this.path = path;
		this.display_name = display_name;
		this.title = title;
	}

	public String getPath() {
		return path;
	}

	public String getDisplay_name() {
		return display_name;
	}

	public String getTitle() {
		return title;
	}
}
