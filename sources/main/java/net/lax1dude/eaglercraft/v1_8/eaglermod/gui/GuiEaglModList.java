package net.lax1dude.eaglercraft.v1_8.eaglermod.gui;

import java.io.IOException;
import java.util.List;

import net.lax1dude.eaglercraft.v1_8.eaglermod.EaglModPackage;
import net.lax1dude.eaglercraft.v1_8.eaglermod.EaglModRegistry;
import net.lax1dude.eaglercraft.v1_8.eaglermod.runtime.EaglModRuntime;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/** Forge-like installed mods browser. */
public class GuiEaglModList extends GuiScreen {
	private final GuiScreen back;
	private int selected = -1;
	private String status = "";

	public GuiEaglModList(GuiScreen back) {
		this.back = back;
	}

	public void initGui() {
		buttonList.clear();
		EaglModRegistry.load();
		buttonList.add(new GuiButton(0, width / 2 + 104, height - 28, 76, 20, "Done"));
		buttonList.add(new GuiButton(1, width / 2 - 180, height - 28, 76, 20, "Import"));
		buttonList.add(new GuiButton(2, width / 2 - 102, height - 28, 76, 20, "Mod Builder"));
		buttonList.add(new GuiButton(3, width / 2 - 24, height - 28, 62, 20, "Toggle"));
		buttonList.add(new GuiButton(4, width / 2 + 40, height - 28, 62, 20, "Export"));
		buttonList.add(new GuiButton(5, width / 2 + 104, height - 54, 76, 20, "Test"));
		updateButtons();
	}

	private void updateButtons() {
		boolean has = selected >= 0 && selected < EaglModRegistry.getInstalled().size();
		for(int i = 0; i < buttonList.size(); ++i) {
			GuiButton b = (GuiButton)buttonList.get(i);
			if(b.id == 3 || b.id == 4 || b.id == 5) b.enabled = has;
		}
	}

	public void updateScreen() {
		try {
			EaglModPackage p = EaglModRegistry.pollImport();
			if(p != null) {
				status = "Installed " + p.manifest.name;
				selected = indexOf(p.manifest.id);
				EaglModRuntime.reload();
				updateButtons();
			}
		}catch(IOException ex) {
			status = "Import failed: " + ex.getMessage();
		}
	}

	private int indexOf(String id) {
		List<EaglModPackage> mods = EaglModRegistry.getInstalled();
		for(int i = 0; i < mods.size(); ++i) if(mods.get(i).manifest.id.equals(id)) return i;
		return -1;
	}

	public void actionPerformed(GuiButton btn) {
		try {
			switch(btn.id) {
			case 0:
				mc.displayGuiScreen(back);
				break;
			case 1:
				EaglModRegistry.beginImport();
				status = "Choose a .eaglmod file";
				break;
			case 2:
				mc.displayGuiScreen(new GuiEaglModBuilder(this));
				break;
			case 3:
				EaglModPackage p = EaglModRegistry.getInstalled().get(selected);
				EaglModRegistry.setEnabled(p.manifest.id, !p.manifest.enabled);
				EaglModRuntime.reload();
				selected = indexOf(p.manifest.id);
				break;
			case 4:
				EaglModRegistry.export(EaglModRegistry.getInstalled().get(selected));
				status = "Exported mod";
				break;
			case 5:
				EaglModPackage test = EaglModRegistry.getInstalled().get(selected);
				if(!test.manifest.enabled) {
					status = "Enable " + test.manifest.name + " before testing";
				}else {
					EaglModRuntime.reload();
					EaglModRuntime.fireMod(test.manifest.id, "test");
					status = "Fired TEST for " + test.manifest.name;
				}
				break;
			default:
				break;
			}
		}catch(Throwable t) {
			status = "Error: " + t.getMessage();
		}
		updateButtons();
	}

	protected void mouseClicked(int mx, int my, int button) {
		super.mouseClicked(mx, my, button);
		if(button != 0) return;
		List<EaglModPackage> mods = EaglModRegistry.getInstalled();
		int listX = width / 2 - 180;
		int listY = 42;
		int rowH = 38;
		if(mx >= listX && mx < width / 2 + 180 && my >= listY && my < height - 72) {
			int idx = (my - listY) / rowH;
			if(idx >= 0 && idx < mods.size()) {
				selected = idx;
				updateButtons();
			}
		}
	}

	public void drawScreen(int mx, int my, float partialTicks) {
		drawDefaultBackground();
		drawCenteredString(fontRendererObj, "Eaglermod Mods", width / 2, 13, 0xFFFFFF);
		drawCenteredString(fontRendererObj, EaglModRegistry.getInstalled().size() + " mods installed", width / 2, 25, 0x888888);

		List<EaglModPackage> mods = EaglModRegistry.getInstalled();
		int x1 = width / 2 - 180;
		int x2 = width / 2 + 180;
		int y = 42;
		for(int i = 0; i < mods.size() && y + 34 < height - 72; ++i, y += 38) {
			EaglModPackage p = mods.get(i);
			int bg = i == selected ? 0xAA555555 : 0x88000000;
			drawRect(x1, y, x2, y + 34, bg);
			drawRect(x1, y, x1 + 3, y + 34, p.manifest.enabled ? 0xFF55FF55 : 0xFF777777);
			drawString(fontRendererObj, p.manifest.name + "  " + p.manifest.version, x1 + 9, y + 5, 0xFFFFFF);
			drawString(fontRendererObj, "by " + p.manifest.author, x1 + 9, y + 16, 0xAAAAAA);
			String desc = p.manifest.description;
			if(desc.length() > 55) desc = desc.substring(0, 52) + "...";
			drawString(fontRendererObj, desc, x1 + 145, y + 16, 0x999999);
			drawString(fontRendererObj, p.manifest.enabled ? "Enabled" : "Disabled", x2 - 55, y + 5,
					p.manifest.enabled ? 0x55FF55 : 0xAAAAAA);
		}
		if(mods.isEmpty()) drawCenteredString(fontRendererObj, "No mods yet  import one or open Mod Builder", width / 2, height / 2, 0xAAAAAA);
		if(status.length() > 0) drawCenteredString(fontRendererObj, status, width / 2, height - 66, 0xCCCCCC);
		super.drawScreen(mx, my, partialTicks);
	}
}