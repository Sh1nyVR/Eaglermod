package net.lax1dude.eaglercraft.v1_8.eaglermod.gui;

import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.lax1dude.eaglercraft.v1_8.eaglermod.EaglModPackage;
import net.lax1dude.eaglercraft.v1_8.eaglermod.EaglModRegistry;
import net.lax1dude.eaglercraft.v1_8.eaglermod.builder.EaglBlock;
import net.lax1dude.eaglercraft.v1_8.eaglermod.builder.EaglModProject;
import net.lax1dude.eaglercraft.v1_8.eaglermod.runtime.EaglModRuntime;
import net.lax1dude.eaglercraft.v1_8.internal.FileChooserResult;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

/** Minecraft-native Scratch-style editor for Eaglermod packages. */
public class GuiEaglModBuilder extends GuiScreen {
	private static final int TAB_CODE = 0;
	private static final int TAB_ASSETS = 1;
	private static final int TAB_INFO = 2;
	private static final int PALETTE_CORE = 0;
	private static final int PALETTE_EFFECTS = 1;

	private final GuiScreen back;
	private final EaglModProject project = new EaglModProject();
	private int tab = TAB_CODE;
	private int palettePage = PALETTE_CORE;
	private int selectedBlock = -1;
	private String status = "New project";
	private String pendingAssetFolder = null;

	private GuiTextField nameField;
	private GuiTextField idField;
	private GuiTextField authorField;
	private GuiTextField versionField;
	private GuiTextField descField;
	private GuiTextField valueField;

	public GuiEaglModBuilder(GuiScreen back) {
		this.back = back;
		project.manifest.name = "My Eaglermod";
		project.manifest.id = "my_eaglermod";
		project.manifest.author = "Player";
		project.manifest.description = "Made with Eaglermod Mod Builder";
		EaglBlock start = project.addBlock("event_test", "", 0, 0);
		EaglBlock hello = project.addBlock("chat", "Hello from Eaglermod!", 0, 38);
		start.next = hello.id;
	}

	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		buttonList.clear();
		buttonList.add(new GuiButton(0, width - 82, height - 26, 76, 20, "Done"));
		buttonList.add(new GuiButton(1, 6, 6, 62, 20, "Code"));
		buttonList.add(new GuiButton(2, 70, 6, 62, 20, "Assets"));
		buttonList.add(new GuiButton(3, 134, 6, 62, 20, "Mod Info"));
		buttonList.add(new GuiButton(4, width - 244, height - 26, 76, 20, "Test"));
		buttonList.add(new GuiButton(5, width - 164, height - 26, 76, 20, "Build"));

		nameField = field(project.manifest.name, 80);
		idField = field(project.manifest.id, 110);
		authorField = field(project.manifest.author, 140);
		versionField = field(project.manifest.version, 170);
		descField = field(project.manifest.description, 200);
		valueField = field("", height - 52);
		refreshTabButtons();
	}

	private GuiTextField field(String text, int y) {
		GuiTextField f = new GuiTextField(y, fontRendererObj, width / 2 - 110, y, 220, 20);
		f.setMaxStringLength(160);
		f.setText(text);
		return f;
	}

	private void refreshTabButtons() {
		for(int i = 0; i < buttonList.size(); ++i) {
			GuiButton b = (GuiButton)buttonList.get(i);
			if(b.id >= 1 && b.id <= 3) b.enabled = (b.id - 1) != tab;
		}
	}

	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	public void updateScreen() {
		nameField.updateCursorCounter();
		idField.updateCursorCounter();
		authorField.updateCursorCounter();
		versionField.updateCursorCounter();
		descField.updateCursorCounter();
		valueField.updateCursorCounter();
		if(pendingAssetFolder != null && EagRuntime.fileChooserHasResult()) {
			FileChooserResult r = EagRuntime.getFileChooserResult();
			if(r != null && r.fileData != null) {
				String safe = r.fileName == null ? "asset.bin" : r.fileName.replace('\\', '_').replace('/', '_');
				project.assets.put(pendingAssetFolder + "/" + safe, r.fileData);
				status = "Added " + safe;
			}
			pendingAssetFolder = null;
		}
	}

	public void actionPerformed(GuiButton btn) {
		try {
			switch(btn.id) {
			case 0:
				mc.displayGuiScreen(back);
				break;
			case 1:
				tab = TAB_CODE;
				refreshTabButtons();
				break;
			case 2:
				tab = TAB_ASSETS;
				refreshTabButtons();
				break;
			case 3:
				tab = TAB_INFO;
				refreshTabButtons();
				break;
			case 4:
				syncInfo();
				EaglModRegistry.install(project.build());
				EaglModRuntime.reload();
				EaglModRuntime.fireMod(project.manifest.id, "test");
				status = "Test mod loaded - fired TEST event";
				break;
			case 5:
				syncInfo();
				EaglModPackage pkg = project.build();
				EaglModRegistry.install(pkg);
				EaglModRuntime.reload();
				EaglModRegistry.export(pkg);
				status = "Built + installed " + pkg.manifest.id + ".eaglmod";
				break;
			default:
				break;
			}
		}catch(Throwable t) {
			status = "Builder error: " + t.getMessage();
		}
	}

	private void syncInfo() {
		project.manifest.name = nameField.getText().trim().length() == 0 ? "Untitled Mod" : nameField.getText().trim();
		project.manifest.id = net.lax1dude.eaglercraft.v1_8.eaglermod.EaglModManifest.sanitizeId(idField.getText());
		project.manifest.author = authorField.getText().trim();
		project.manifest.version = versionField.getText().trim();
		project.manifest.description = descField.getText().trim();
	}

	protected void keyTyped(char c, int key) {
		if(tab == TAB_INFO) {
			if(nameField.textboxKeyTyped(c, key) || idField.textboxKeyTyped(c, key) || authorField.textboxKeyTyped(c, key)
					|| versionField.textboxKeyTyped(c, key) || descField.textboxKeyTyped(c, key)) return;
		}else if(tab == TAB_CODE && selectedBlock >= 0 && valueField.textboxKeyTyped(c, key)) {
			EaglBlock b = project.getBlock(selectedBlock);
			if(b != null) b.value = valueField.getText();
			return;
		}
		super.keyTyped(c, key);
	}

	protected void mouseClicked(int mx, int my, int button) {
		super.mouseClicked(mx, my, button);
		if(tab == TAB_INFO) {
			nameField.mouseClicked(mx, my, button);
			idField.mouseClicked(mx, my, button);
			authorField.mouseClicked(mx, my, button);
			versionField.mouseClicked(mx, my, button);
			descField.mouseClicked(mx, my, button);
			return;
		}
		if(tab == TAB_ASSETS && button == 0) {
			if(in(mx, my, 18, 62, 154, 84)) chooseAsset("images", "png");
			else if(in(mx, my, 18, 90, 154, 112)) chooseAsset("sounds", "ogg");
			else if(in(mx, my, 18, 118, 154, 140)) chooseAsset("models", "json");
			else if(in(mx, my, 18, 146, 154, 168)) chooseAsset("data", "json");
			return;
		}
		if(tab == TAB_CODE && button == 0) {
			if(in(mx, my, 10, 50, 86, 67)) {
				palettePage = PALETTE_CORE;
				return;
			}else if(in(mx, my, 87, 50, 164, 67)) {
				palettePage = PALETTE_EFFECTS;
				return;
			}
			String op = paletteOpcode(mx, my);
			if(op != null) {
				String def = defaultValue(op);
				EaglBlock b = project.addBlock(op, def, 0, 0);
				connectToTail(b);
				selectedBlock = b.id;
				valueField.setText(b.value);
				status = "Added " + readable(op) + " block";
				return;
			}
			int y = 52;
			for(int i = 0; i < project.blocks.size(); ++i, y += 34) {
				if(in(mx, my, 178, y, width - 20, y + 28)) {
					EaglBlock b = project.blocks.get(i);
					selectedBlock = b.id;
					valueField.setText(b.value);
					return;
				}
			}
		}
	}

	private void connectToTail(EaglBlock added) {
		if(project.blocks.size() <= 1 || added.opcode.startsWith("event_")) return;
		for(int i = project.blocks.size() - 2; i >= 0; --i) {
			EaglBlock b = project.blocks.get(i);
			if(b.next == -1) {
				b.next = added.id;
				return;
			}
		}
	}

	private void chooseAsset(String folder, String ext) {
		pendingAssetFolder = folder;
		EagRuntime.displayFileChooser(null, ext);
		status = "Choose a " + ext.toUpperCase() + " asset";
	}

	private String paletteOpcode(int mx, int my) {
		if(mx < 10 || mx > 164) return null;
		int row = (my - 72) / 30;
		if(my < 72 || row < 0 || row > 6) return null;
		if(palettePage == PALETTE_CORE) {
			switch(row) {
			case 0: return "event_test";
			case 1: return "event_reset";
			case 2: return "event_tick";
			case 3: return "chat";
			case 4: return "title";
			case 5: return "command";
			case 6: return "set_fov";
			default: return null;
			}
		}else {
			switch(row) {
			case 0: return "set_gamma";
			case 1: return "set_camera";
			case 2: return "set_bobbing";
			case 3: return "set_time";
			case 4: return "set_rain";
			case 5: return "set_thunder";
			case 6: return "play_sound";
			default: return null;
			}
		}
	}

	private String defaultValue(String op) {
		if("chat".equals(op)) return "Hello!";
		if("title".equals(op)) return "Eaglermod";
		if("command".equals(op)) return "help";
		if("set_fov".equals(op)) return "70";
		if("set_gamma".equals(op)) return "1.0";
		if("set_camera".equals(op)) return "1";
		if("set_bobbing".equals(op)) return "true";
		if("set_time".equals(op)) return "18000";
		if("set_rain".equals(op) || "set_thunder".equals(op)) return "1.0";
		if("play_sound".equals(op)) return "ambient.cave.cave6";
		return "";
	}

	private String readable(String op) {
		return op.replace("event_", "when ").replace('_', ' ');
	}

	private boolean in(int x, int y, int x1, int y1, int x2, int y2) {
		return x >= x1 && y >= y1 && x < x2 && y < y2;
	}

	public void drawScreen(int mx, int my, float partialTicks) {
		drawDefaultBackground();
		drawCenteredString(fontRendererObj, "Eaglermod Mod Builder", width / 2, 12, 0xFFFFFF);
		if(tab == TAB_CODE) drawCode(mx, my);
		else if(tab == TAB_ASSETS) drawAssets();
		else drawInfo();
		drawString(fontRendererObj, status, 7, height - 39, 0xAAAAAA);
		super.drawScreen(mx, my, partialTicks);
	}

	private void drawCode(int mx, int my) {
		drawRect(7, 34, 168, height - 58, 0xAA111111);
		drawString(fontRendererObj, "BLOCKS", 14, 40, 0xAAAAAA);
		drawPaletteTab(10, 50, 86, "CORE", palettePage == PALETTE_CORE);
		drawPaletteTab(87, 50, 164, "EFFECTS", palettePage == PALETTE_EFFECTS);

		String[] labels;
		int[] colors;
		if(palettePage == PALETTE_CORE) {
			labels = new String[] { "when TEST", "when RESET", "when TICK", "say [text]", "show title [text]", "run command [cmd]", "set FOV [70]" };
			colors = new int[] { 0xFFCC9900, 0xFFCC9900, 0xFFCC9900, 0xFF4466CC, 0xFF4466CC, 0xFF8855BB, 0xFF44AA66 };
		}else {
			labels = new String[] { "set gamma [1]", "camera [0-2]", "view bobbing [true]", "world time [18000]", "rain [0-1]", "thunder [0-1]", "play sound [id]" };
			colors = new int[] { 0xFF44AA66, 0xFF44AA66, 0xFF44AA66, 0xFF3388AA, 0xFF3388AA, 0xFF3388AA, 0xFFAA5577 };
		}
		for(int i = 0; i < labels.length; ++i) drawPuzzleBlock(10, 72 + i * 30, 154, labels[i], colors[i], false);

		drawRect(174, 34, width - 7, height - 58, 0x66000000);
		drawString(fontRendererObj, "WORKSPACE", 181, 40, 0x888888);
		int y = 52;
		for(int i = 0; i < project.blocks.size(); ++i, y += 34) {
			EaglBlock b = project.blocks.get(i);
			int c = b.opcode.startsWith("event_") ? 0xFFCC9900 : (b.opcode.equals("chat") || b.opcode.equals("title") ? 0xFF4466CC : 0xFF44AA66);
			drawPuzzleBlock(178, y, Math.max(150, width - 210), readable(b.opcode) + (b.value.length() > 0 ? "  [" + shortText(b.value, 28) + "]" : ""), c, b.id == selectedBlock);
		}
		if(selectedBlock >= 0) {
			drawString(fontRendererObj, "Block value", width / 2 - 110, height - 63, 0xAAAAAA);
			valueField.drawTextBox();
		}
	}

	private void drawPaletteTab(int x1, int y1, int x2, String label, boolean active) {
		drawRect(x1, y1, x2, y1 + 17, active ? 0xFF555555 : 0xFF222222);
		drawCenteredString(fontRendererObj, label, (x1 + x2) / 2, y1 + 5, active ? 0xFFFFFF : 0x999999);
	}

	/** Pixel/blocky Scratch notch while still using vanilla Minecraft rendering. */
	private void drawPuzzleBlock(int x, int y, int w, String text, int color, boolean selected) {
		int edge = selected ? 0xFFFFFFFF : 0xFF222222;
		drawRect(x, y + 3, x + w, y + 25, color);
		drawRect(x + 8, y, x + 22, y + 4, color);
		drawRect(x + 22, y + 2, x + 34, y + 6, color);
		drawRect(x + 8, y + 24, x + 22, y + 28, color);
		drawRect(x + 22, y + 22, x + 34, y + 26, color);
		drawRect(x, y + 3, x + w, y + 4, edge);
		drawRect(x, y + 24, x + 8, y + 25, edge);
		drawString(fontRendererObj, text, x + 8, y + 10, 0xFFFFFF);
	}

	private String shortText(String s, int len) {
		return s.length() <= len ? s : s.substring(0, len - 3) + "...";
	}

	private void drawAssets() {
		drawCenteredString(fontRendererObj, "Assets are packed directly inside the .eaglmod", width / 2, 42, 0xAAAAAA);
		assetButton(18, 62, "Import Image  PNG", 0xFF5577AA);
		assetButton(18, 90, "Import Sound  OGG", 0xFF7755AA);
		assetButton(18, 118, "Import Model  JSON", 0xFF55AA77);
		assetButton(18, 146, "Import Data  JSON", 0xFFAA7755);
		drawString(fontRendererObj, "FILES", 190, 58, 0x888888);
		int y = 72;
		for(String key : project.assets.keySet()) {
			byte[] b = project.assets.get(key);
			drawString(fontRendererObj, key + "  " + b.length + " bytes", 190, y, 0xDDDDDD);
			y += 12;
			if(y > height - 50) break;
		}
		if(project.assets.isEmpty()) drawString(fontRendererObj, "No custom assets yet", 190, 72, 0x777777);
	}

	private void assetButton(int x, int y, String label, int color) {
		drawRect(x, y, x + 136, y + 22, color);
		drawRect(x, y, x + 136, y + 1, 0xFFFFFFFF);
		drawString(fontRendererObj, label, x + 7, y + 7, 0xFFFFFF);
	}

	private void drawInfo() {
		drawCenteredString(fontRendererObj, "Mod metadata shown in the Mods tab", width / 2, 44, 0xAAAAAA);
		drawString(fontRendererObj, "Name", width / 2 - 180, 87, 0xAAAAAA);
		nameField.drawTextBox();
		drawString(fontRendererObj, "Mod ID", width / 2 - 180, 117, 0xAAAAAA);
		idField.drawTextBox();
		drawString(fontRendererObj, "Author", width / 2 - 180, 147, 0xAAAAAA);
		authorField.drawTextBox();
		drawString(fontRendererObj, "Version", width / 2 - 180, 177, 0xAAAAAA);
		versionField.drawTextBox();
		drawString(fontRendererObj, "Description", width / 2 - 180, 207, 0xAAAAAA);
		descField.drawTextBox();
	}
}