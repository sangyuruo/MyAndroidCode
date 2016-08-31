package net.suntec.sdl.config;

import java.util.List;

public class SampleConfig {
	String id;
	String jsonFilePath;
	List<String> depends = null;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getJsonFilePath() {
		return jsonFilePath;
	}

	public void setJsonFilePath(String jsonFilePath) {
		this.jsonFilePath = jsonFilePath;
	}

	public List<String> getDepends() {
		return depends;
	}

	public void setDepends(List<String> dependences) {
		this.depends = dependences;
	}
}
