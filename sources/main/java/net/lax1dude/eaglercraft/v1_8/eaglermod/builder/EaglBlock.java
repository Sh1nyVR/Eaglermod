package net.lax1dude.eaglercraft.v1_8.eaglermod.builder;

import org.json.JSONObject;

/** One Scratch-style block in an Eaglermod project. */
public class EaglBlock {
	public int id;
	public String opcode;
	public String value;
	public int x;
	public int y;
	public int next = -1;

	public EaglBlock(int id, String opcode, String value, int x, int y) {
		this.id = id;
		this.opcode = opcode;
		this.value = value == null ? "" : value;
		this.x = x;
		this.y = y;
	}

	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		o.put("id", id);
		o.put("opcode", opcode);
		o.put("value", value);
		o.put("x", x);
		o.put("y", y);
		o.put("next", next);
		return o;
	}

	public static EaglBlock fromJSON(JSONObject o) {
		EaglBlock b = new EaglBlock(o.optInt("id", 0), o.optString("opcode", "noop"), o.optString("value", ""),
				o.optInt("x", 0), o.optInt("y", 0));
		b.next = o.optInt("next", -1);
		return b;
	}
}