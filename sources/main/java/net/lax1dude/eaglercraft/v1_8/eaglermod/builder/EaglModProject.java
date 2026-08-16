package net.lax1dude.eaglercraft.v1_8.eaglermod.builder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import net.lax1dude.eaglercraft.v1_8.eaglermod.EaglModManifest;
import net.lax1dude.eaglercraft.v1_8.eaglermod.EaglModPackage;

/** Editable project used by the in-game block builder. */
public class EaglModProject {
	public final EaglModManifest manifest = new EaglModManifest();
	public final List<EaglBlock> blocks = new ArrayList<>();
	public final Map<String, byte[]> assets = new LinkedHashMap<>();
	private int nextBlockId = 1;

	public EaglBlock addBlock(String opcode, String value, int x, int y) {
		EaglBlock b = new EaglBlock(nextBlockId++, opcode, value, x, y);
		blocks.add(b);
		return b;
	}

	public EaglBlock getBlock(int id) {
		for(int i = 0; i < blocks.size(); ++i) if(blocks.get(i).id == id) return blocks.get(i);
		return null;
	}

	public void removeBlock(int id) {
		for(int i = blocks.size() - 1; i >= 0; --i) if(blocks.get(i).id == id) blocks.remove(i);
		for(int i = 0; i < blocks.size(); ++i) if(blocks.get(i).next == id) blocks.get(i).next = -1;
	}

	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		o.put("manifest", manifest.toJSON());
		JSONArray a = new JSONArray();
		for(int i = 0; i < blocks.size(); ++i) a.put(blocks.get(i).toJSON());
		o.put("blocks", a);
		return o;
	}

	public EaglModPackage build() {
		EaglModPackage pkg = new EaglModPackage();
		pkg.manifest = manifest;
		pkg.putFile("project/blocks.json", toJSON().toString().getBytes(StandardCharsets.UTF_8));
		for(Map.Entry<String, byte[]> e : assets.entrySet()) pkg.putFile("assets/" + e.getKey(), e.getValue());
		return pkg;
	}

	public static EaglModProject fromPackage(EaglModPackage pkg) {
		EaglModProject p = new EaglModProject();
		p.manifest.id = pkg.manifest.id;
		p.manifest.name = pkg.manifest.name;
		p.manifest.version = pkg.manifest.version;
		p.manifest.author = pkg.manifest.author;
		p.manifest.description = pkg.manifest.description;
		p.manifest.website = pkg.manifest.website;
		p.manifest.entrypoint = pkg.manifest.entrypoint;
		p.manifest.enabled = pkg.manifest.enabled;
		byte[] graph = pkg.getFile("project/blocks.json");
		if(graph != null) {
			try {
				JSONObject o = new JSONObject(new String(graph, StandardCharsets.UTF_8));
				JSONArray a = o.optJSONArray("blocks");
				if(a != null) for(int i = 0; i < a.length(); ++i) {
					EaglBlock b = EaglBlock.fromJSON(a.getJSONObject(i));
					p.blocks.add(b);
					if(b.id >= p.nextBlockId) p.nextBlockId = b.id + 1;
				}
			}catch(Throwable t) {
				System.err.println("Eaglermod: bad block graph: " + t.toString());
			}
		}
		for(Map.Entry<String, byte[]> e : pkg.files.entrySet()) {
			if(e.getKey().startsWith("assets/")) p.assets.put(e.getKey().substring(7), e.getValue());
		}
		return p;
	}
}