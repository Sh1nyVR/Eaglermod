package net.lax1dude.eaglercraft.v1_8.eaglermod.runtime;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import net.lax1dude.eaglercraft.v1_8.eaglermod.EaglModPackage;
import net.lax1dude.eaglercraft.v1_8.eaglermod.EaglModRegistry;
import net.lax1dude.eaglercraft.v1_8.eaglermod.builder.EaglBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

/**
 * Small safe runtime for builder-created mods. The visual editor compiles into a
 * block graph inside the .eaglmod, and this executes supported events/actions.
 */
public class EaglModRuntime {
	private static final List<LoadedGraph> graphs = new ArrayList<>();

	private static class LoadedGraph {
		final EaglModPackage pkg;
		final List<EaglBlock> blocks = new ArrayList<>();
		LoadedGraph(EaglModPackage pkg) { this.pkg = pkg; }
	}

	public static void reload() {
		graphs.clear();
		List<EaglModPackage> mods = EaglModRegistry.getInstalled();
		for(int i = 0; i < mods.size(); ++i) {
			EaglModPackage pkg = mods.get(i);
			if(!pkg.manifest.enabled) continue;
			byte[] data = pkg.getFile("project/blocks.json");
			if(data == null) continue;
			try {
				JSONObject o = new JSONObject(new String(data, StandardCharsets.UTF_8));
				JSONArray a = o.optJSONArray("blocks");
				LoadedGraph g = new LoadedGraph(pkg);
				if(a != null) for(int j = 0; j < a.length(); ++j) g.blocks.add(EaglBlock.fromJSON(a.getJSONObject(j)));
				graphs.add(g);
			}catch(Throwable t) {
				System.err.println("Eaglermod: could not load runtime graph for " + pkg.manifest.id);
			}
		}
	}

	public static void fire(String event) {
		if(graphs.isEmpty()) reload();
		for(int i = 0; i < graphs.size(); ++i) executeEvent(graphs.get(i), event);
	}

	private static void executeEvent(LoadedGraph g, String event) {
		for(int i = 0; i < g.blocks.size(); ++i) {
			EaglBlock b = g.blocks.get(i);
			if(("event_" + event).equals(b.opcode)) runChain(g, b.next, 0);
		}
	}

	private static void runChain(LoadedGraph g, int id, int depth) {
		if(id < 0 || depth > 256) return;
		EaglBlock b = find(g, id);
		if(b == null) return;
		Minecraft mc = Minecraft.getMinecraft();
		if("chat".equals(b.opcode)) {
			if(mc.thePlayer != null) mc.thePlayer.addChatMessage(new ChatComponentText(b.value));
		}else if("command".equals(b.opcode)) {
			if(mc.thePlayer != null && b.value.length() > 0) mc.thePlayer.sendChatMessage(b.value.charAt(0) == '/' ? b.value : "/" + b.value);
		}else if("title".equals(b.opcode)) {
			if(mc.thePlayer != null) mc.thePlayer.addChatMessage(new ChatComponentText("[" + g.pkg.manifest.name + "] " + b.value));
		}else if("set_fov".equals(b.opcode)) {
			try { mc.gameSettings.fovSetting = Float.parseFloat(b.value); } catch(Throwable t) { }
		}else if("set_gamma".equals(b.opcode)) {
			try { mc.gameSettings.gammaSetting = Float.parseFloat(b.value); } catch(Throwable t) { }
		}
		runChain(g, b.next, depth + 1);
	}

	private static EaglBlock find(LoadedGraph g, int id) {
		for(int i = 0; i < g.blocks.size(); ++i) if(g.blocks.get(i).id == id) return g.blocks.get(i);
		return null;
	}
}