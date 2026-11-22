package dev.minime;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;

/** Выбор цветов ползунками (R, G, B) с живым предпросмотром пузыря. */
public class MiniColorScreen extends Screen {
    private final Screen parent;
    private final String[] labels = {"Фон пузыря", "Обводка пузыря", "Текст", "Ник", "Подложка меню"};
    private final int[] defaults = {0xFFFFFF, 0x000000, 0x000000, 0x9146FF, 0xF4F4F4};
    private static int target = 0;

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

    private int color(int i) { return Config.rgb(get(i), defaults[i]); }

    /** Один ползунок канала 0..255. */
    private static class ChannelSlider extends AbstractSliderButton {
        private final String name;
        private final IntConsumer onChange;

        ChannelSlider(int x, int y, int w, int h, String name, int initial, IntConsumer onChange) {
            super(x, y, w, h, Component.empty(), initial / 255.0);
            this.name = name;
            this.onChange = onChange;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(name + ": " + (int) Math.round(this.value * 255)));
        }

        @Override
        protected void applyValue() {
            onChange.accept((int) Math.round(this.value * 255));
        }
    }

    private int y0() { return this.height / 2 - 95; }

    @Override
    protected void init() {
        int x0 = this.width / 2 - 150, y0 = y0();
        int cur = color(target);
        int[] rgb = {(cur >> 16) & 255, (cur >> 8) & 255, cur & 255};
        final int[] state = {rgb[0], rgb[1], rgb[2]};

        addRenderableWidget(Button.builder(Component.literal("Что красим: " + labels[target] + "  ›"), b -> {
            target = (target + 1) % labels.length;
            this.rebuildWidgets();
        }).bounds(x0, y0, 300, 20).build());

        String[] names = {"Красный", "Зелёный", "Синий"};
        for (int i = 0; i < 3; i++) {
            final int ch = i;
            addRenderableWidget(new ChannelSlider(x0, y0 + 26 * (i + 1), 300, 20, names[i], state[i], v -> {
                state[ch] = v;
                set(target, String.format("%02X%02X%02X", state[0], state[1], state[2]));
            }));
        }

        addRenderableWidget(Button.builder(Component.literal("Белая тема"), b -> { cfg().applyTheme("light"); this.rebuildWidgets(); })
                .bounds(x0, y0 + 26 * 4 + 52, 148, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Чёрная тема"), b -> { cfg().applyTheme("dark"); this.rebuildWidgets(); })
                .bounds(x0 + 152, y0 + 26 * 4 + 52, 148, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Готово"), b -> onClose())
                .bounds(x0, y0 + 26 * 4 + 78, 300, 20).build());
    }

    @Override
    public void onClose() {
        cfg().save();
        Minecraft.getInstance().setScreenAndShow(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
        int panel = color(4);
        int txt = Config.contrast(panel);
        int y0 = y0();
        g.fill(this.width / 2 - 165, y0 - 36, this.width / 2 + 165, y0 + 26 * 4 + 108, 0xDD000000 | panel);
        super.extractRenderState(g, mouseX, mouseY, partial);
        g.centeredText(this.font, Component.literal("Цвета: двигайте ползунки"), this.width / 2, y0 - 24, txt);

        // предпросмотр пузыря с текущими цветами
        int bFill = color(0), bBorder = color(1), bText = color(2), bName = color(3);
        int bx = this.width / 2 - 70, by = y0 + 26 * 4 + 2, bw = 140, bh = 40;
        g.fill(bx - 1, by - 1, bx + bw + 1, by + bh + 1, 0xFF000000 | bBorder);
        g.fill(bx, by, bx + bw, by + bh, 0xFF000000 | bFill);
        g.text(this.font, "Streamer", bx + 5, by + 5, 0xFF000000 | bName, false);
        g.text(this.font, "Привет! Как дела?", bx + 5, by + 20, 0xFF000000 | bText, false);
    }
}
