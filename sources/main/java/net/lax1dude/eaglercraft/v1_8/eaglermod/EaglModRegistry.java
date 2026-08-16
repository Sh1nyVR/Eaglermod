package net.lax1dude.eaglercraft.v1_8.eaglermod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.internal.FileChooserResult;
import net.lax1dude.eaglercraft.v1_8.internal.vfs2.VFile2;

/** Persistent installed-mod registry backed by Eaglercraft's VFS/IndexedDB. */
public class EaglModRegistry {
	private static final String ROOT = "eaglermod/mods";
	private static final List<EaglModPackage> installed = new ArrayList<>();
	private static boolean loaded = false;

	public static void load() {
		installed.clear();
		VFile2 root = new VFile2(ROOT);
		List<VFile2> files = root.listFiles(false);
		for(int i = 0; i < files.size(); ++i) {
			VFile2 f = files.get(i);
			if(!f.getName().endsWith(".eaglmod")) continue;
			try {
				byte[] b = f.getAllBytes();
				if(b != null) installed.add(EaglModPackage.decode(b));
			}catch(Throwable t) {
				System.err.println("Eaglermod: failed to load " + f.getName() + ": " + t.toString());
			}
		}
		Collections.sort(installed, new Comparator<EaglModPackage>() {
			public int compare(EaglModPackage a, EaglModPackage b) {
				return a.manifest.name.compareToIgnoreCase(b.manifest.name);
			}
		});
		loaded = true;
	}

	public static List<EaglModPackage> getInstalled() {
		if(!loaded) load();
		return Collections.unmodifiableList(installed);
	}

	public static EaglModPackage get(String id) {
		if(!loaded) load();
		for(int i = 0; i < installed.size(); ++i) {
			EaglModPackage p = installed.get(i);
			if(p.manifest.id.equals(id)) return p;
		}
		return null;
	}

	public static void install(EaglModPackage pkg) throws IOException {
		if(pkg == null) throw new IllegalArgumentException("pkg");
		pkg.manifest.id = EaglModManifest.sanitizeId(pkg.manifest.id);
		new VFile2(ROOT, pkg.manifest.id + ".eaglmod").setAllBytes(pkg.encode());
		load();
	}

	public static void uninstall(String id) {
		new VFile2(ROOT, EaglModManifest.sanitizeId(id) + ".eaglmod").delete();
		load();
	}

	public static void setEnabled(String id, boolean enabled) throws IOException {
		EaglModPackage pkg = get(id);
		if(pkg == null) return;
		pkg.manifest.enabled = enabled;
		install(pkg);
	}

	public static void beginImport() {
		EagRuntime.displayFileChooser(null, "eaglmod");
	}

	/** Call from updateScreen while an import chooser is open. */
	public static EaglModPackage pollImport() throws IOException {
		if(!EagRuntime.fileChooserHasResult()) return null;
		FileChooserResult result = EagRuntime.getFileChooserResult();
		if(result == null || result.fileData == null) return null;
		EaglModPackage pkg = EaglModPackage.decode(result.fileData);
		install(pkg);
		return pkg;
	}

	public static void export(EaglModPackage pkg) throws IOException {
		EagRuntime.downloadFileWithName(pkg.manifest.id + "-" + pkg.manifest.version + ".eaglmod", pkg.encode());
	}
}