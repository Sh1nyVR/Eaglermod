package net.lax1dude.eaglercraft.v1_8.eaglermod;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;

/**
 * Compact binary Eaglermod container.
 *
 * Layout:
 * EAGLMOD1 + manifest length + manifest json + file count + repeated path/data.
 * This deliberately avoids ZIP so the browser client can parse packages with very
 * little overhead and without depending on java.util.zip support.
 */
public class EaglModPackage {
	private static final byte[] MAGIC = new byte[] { 'E', 'A', 'G', 'L', 'M', 'O', 'D', '1' };
	private static final int MAX_FILES = 4096;
	private static final int MAX_PATH = 512;
	private static final int MAX_MANIFEST = 1024 * 1024;
	private static final int MAX_FILE = 64 * 1024 * 1024;

	public EaglModManifest manifest = new EaglModManifest();
	public final Map<String, byte[]> files = new LinkedHashMap<>();

	public void putFile(String path, byte[] data) {
		path = normalizePath(path);
		if(data == null) throw new IllegalArgumentException("data cannot be null");
		files.put(path, data);
	}

	public byte[] getFile(String path) {
		return files.get(normalizePath(path));
	}

	public boolean hasFile(String path) {
		return files.containsKey(normalizePath(path));
	}

	public byte[] encode() throws IOException {
		ByteArrayOutputStream raw = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(raw);
		out.write(MAGIC);
		byte[] manifestBytes = manifest.toJSON().toString().getBytes(StandardCharsets.UTF_8);
		out.writeInt(manifestBytes.length);
		out.write(manifestBytes);
		out.writeInt(files.size());
		for(Map.Entry<String, byte[]> e : files.entrySet()) {
			byte[] path = normalizePath(e.getKey()).getBytes(StandardCharsets.UTF_8);
			byte[] data = e.getValue();
			out.writeShort(path.length);
			out.write(path);
			out.writeInt(data.length);
			out.write(data);
		}
		out.flush();
		return raw.toByteArray();
	}

	public static EaglModPackage decode(byte[] bytes) throws IOException {
		if(bytes == null || bytes.length < 16) throw new IOException("Invalid .eaglmod file");
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
		for(int i = 0; i < MAGIC.length; ++i) {
			if(in.readByte() != MAGIC[i]) throw new IOException("Not an Eaglermod package");
		}
		int manifestLength = in.readInt();
		if(manifestLength <= 0 || manifestLength > MAX_MANIFEST) throw new IOException("Invalid manifest length");
		byte[] manifestBytes = new byte[manifestLength];
		in.readFully(manifestBytes);
		EaglModPackage pkg = new EaglModPackage();
		try {
			pkg.manifest = EaglModManifest.fromJSON(new JSONObject(new String(manifestBytes, StandardCharsets.UTF_8)));
		}catch(Throwable t) {
			throw new IOException("Invalid Eaglermod manifest", t);
		}
		int fileCount = in.readInt();
		if(fileCount < 0 || fileCount > MAX_FILES) throw new IOException("Invalid file count");
		for(int i = 0; i < fileCount; ++i) {
			int pathLength = in.readUnsignedShort();
			if(pathLength <= 0 || pathLength > MAX_PATH) throw new IOException("Invalid asset path");
			byte[] pathBytes = new byte[pathLength];
			in.readFully(pathBytes);
			String path = normalizePath(new String(pathBytes, StandardCharsets.UTF_8));
			int dataLength = in.readInt();
			if(dataLength < 0 || dataLength > MAX_FILE) throw new IOException("Asset is too large: " + path);
			byte[] data = new byte[dataLength];
			in.readFully(data);
			pkg.files.put(path, data);
		}
		return pkg;
	}

	public static String normalizePath(String path) {
		if(path == null) throw new IllegalArgumentException("path cannot be null");
		path = path.replace('\\', '/');
		while(path.startsWith("/")) path = path.substring(1);
		if(path.length() == 0 || path.contains("../") || path.equals("..")) {
			throw new IllegalArgumentException("Invalid Eaglermod path: " + path);
		}
		return path;
	}
}