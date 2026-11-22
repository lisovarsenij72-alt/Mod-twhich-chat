package dev.minime;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** Свои цвета: фон, обводка, текст, ник, подложка меню (HEX RRGGBB). */
public class MiniColorScreen extends Screen {
    private final Screen parent;
    private final String[] labels = {"Фон пузыря", "Обводка пузыря", "Текст", "Ник", "Подложка меню"};
    private final String[] defaults = {"FFFFFF", "000000", "000000", "9146FF", "F4F4F4"};
    private final EditBox[] boxes = new EditBox[5];

    public MiniColorScreen(Screen parent) {
        super(Component.literal("Цвета"));
        this.parent = parent;
    }

    private Config cfg() { return MiniMeClient.config; }

    private String get(int i) {
        return switch (i) { case 0 -> cfg().cBubbleFill; case 1 -> cfg().cBubbleBorder; case 2 -> cfg().cBubbleText; case 3 -> cfg().cBubbleName; default -> cfg().cPanel; };
    }

    private void set(int i, String v) {
        switch (i) { case 0 -> cfg().cBubbleFill = v; case 1 -> cfg().cBubbleBorder = v; case 2 -> cfg().cBubbleText = v; case 3 -> cfg().cBubbleName = v; default -> cfg().cPanel = v; }
    }

    @Override
    protected void init() {
        int x0 = this.width / 2 - 150, y0 = this.height / 2 - 80;
        for (int i = 0; i < 5; i++) {
            final int idx = i;
            EditBox box = new EditBox(this.font, this.width / 2 - 10, y0 + i * 26, 80, 20, Component.literal(labels[i]));
            box.setMaxLength(7);
            box.setValue(get(i));
            box.setResponder(v -> {
                String h = v.trim().replace("#", "");
                if (h.matches("[0-9a-fA-F]{6}")) set(idx, h.toUpperCase());   // применяем только корректный HEX
            });
            boxes[i] = box;
            addRenderableWidget(box);
        }
        addRenderableWidget(Button.builder(Component.literal("Белая тема"), b -> { cfg().applyTheme("light"); reload(); })
                .bounds(x0, y0 + 5 * 26 + 6, 148, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Чёрная тема"), b -> { cfg().applyTheme("dark"); reload(); })
                .bounds(x0 + 152, y0 + 5 * 26 + 6, 148, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Готово"), b -> onClose())
                .bounds(x0, y0 + 5 * 26 + 32, 300, 20).build());
    }

    private void reload() {
        for (int i = 0; i < 5; i++) boxes[i].setValue(get(i));
    }

    @Override
    public void onClose() {
        cfg().save();
        Minecraft.getInstance().setScreenAndShow(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
        int panel = Config.rgb(cfg().cPanel, 0xF4F4F4);
        int txt = Config.contrast(panel);
        int x0 = this.width / 2 - 150, y0 = this.height / 2 - 80;
        g.fill(this.width / 2 - 165, y0 - 40, this.width / 2 + 165, y0 + 5 * 26 + 62, 0xDD000000 | panel);
        super.extractRenderState(g, mouseX, mouseY, partial);
        g.centeredText(this.font, Component.literal("Свои цвета (HEX, например FF8800)"), this.width / 2, y0 - 28, txt);
        for (int i = 0; i < 5; i++) {
            g.text(this.font, labels[i], x0, y0 + i * 26 + 6, txt, false);
            int col = Config.rgb(get(i), 0x808080);
            int sx = this.width / 2 + 78;
            g.fill(sx - 1, y0 + i * 26 - 1, sx + 21, y0 + i * 26 + 21, 0xFF808080);   // рамка образца
            g.fill(sx, y0 + i * 26, sx + 20, y0 + i * 26 + 20, 0xFF000000 | col);     // образец цвета
        }
    }
}
