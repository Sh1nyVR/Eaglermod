package net.lax1dude.eaglercraft.v1_8.eaglermod;

import org.json.JSONArray;
import org.json.JSONObject;

/** Metadata stored inside every .eaglmod package. */
public class EaglModManifest {
	public static final int CURRENT_FORMAT = 1;

	public String id = "untitled";
	public String name = "Untitled Mod";
	public String version = "1.0.0";
	public String author = "Unknown";
	public String description = "";
	public String website = "";
	public String entrypoint = "main";
	public int format = CURRENT_FORMAT;
	public boolean enabled = true;
	public String[] tags = new String[0];

	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		o.put("format", format);
		o.put("id", id);
		o.put("name", name);
		o.put("version", version);
		o.put("author", author);
		o.put("description", description);
		o.put("website", website);
		o.put("entrypoint", entrypoint);
		o.put("enabled", enabled);
		o.put("tags", new JSONArray(tags));
		return o;
	}

	public static EaglModManifest fromJSON(JSONObject o) {
		EaglModManifest m = new EaglModManifest();
		m.format = o.optInt("format", CURRENT_FORMAT);
		m.id = sanitizeId(o.optString("id", "untitled"));
		m.name = o.optString("name", "Untitled Mod");
		m.version = o.optString("version", "1.0.0");
		m.author = o.optString("author", "Unknown");
		m.description = o.optString("description", "");
		m.website = o.optString("website", "");
		m.entrypoint = o.optString("entrypoint", "main");
		m.enabled = o.optBoolean("enabled", true);
		JSONArray a = o.optJSONArray("tags");
		if(a != null) {
			m.tags = new String[a.length()];
			for(int i = 0; i < a.length(); ++i) m.tags[i] = a.optString(i, "");
		}
		return m;
	}

	public static String sanitizeId(String s) {
		StringBuilder b = new StringBuilder();
		for(int i = 0; i < s.length(); ++i) {
			char c = Character.toLowerCase(s.charAt(i));
			if((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-') b.append(c);
		}
		return b.length() == 0 ? "untitled" : b.toString();
	}
}