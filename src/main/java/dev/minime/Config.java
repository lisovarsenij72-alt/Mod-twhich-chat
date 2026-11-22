package dev.minime;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
import java.util.*;

public class Config {
    public String channel = "your_channel";   // канал, чей чат читаем (без #)
    public String twitchNick = "";            // ваш логин Twitch (аккаунт, которому выдан токен)
    public String oauthToken = "";            // OAuth-токен со скоупом chat:read. НИКОМУ не показывайте!
    public String model = "chibi";             // chibi | fox | player (настоящие сущности) | tank (плоский спрайт) | имя из customModels | имя из customModels
    public String skin = "own";               // own | steve | alex | girl | имя файла из config/minime/skins (без .png)
    public List<String> customModels = new ArrayList<>();
    public float scale = 0.3f;               // размер мини-сущности (1.0 = как игрок)
    public String position = "front";         // front (спереди) | side (сбоку) | back (сзади)
    public boolean autoAway = true;           // улетает, когда в чате тихо, и прилетает при новом сообщении
    public int idleSeconds = 20;              // через сколько секунд тишины улетает
    public int respawnSeconds = 4;            // через сколько секунд мини возвращается после «лопания»
    public float distance = 2.2f;             // расстояние мини от игрока (блоков)
    public String cBubbleFill = "FFFFFF";     // цвета (HEX RRGGBB): фон пузыря
    public String cBubbleBorder = "000000";   // обводка пузыря
    public String cBubbleText = "000000";     // текст пузыря
    public String cBubbleName = "9146FF";     // ник в пузыре
    public String cPanel = "F4F4F4";          // подложка меню
    public String theme = "light";            // light | dark (пузыри и меню)
    public String popEffect = "poof";         // poof | snow | cherry | sand | souls | hearts
    public boolean ttsEnabled = true;         // озвучка сообщений чата
    public boolean ttsReadNames = true;       // читать ник автора
    public float ttsVolume = 1.0f;
    public int bubbleSeconds = 8;
    public int maxBubbles = 4;
    public boolean enabled = true;

    static final Path DIR = FabricLoader.getInstance().getConfigDir().resolve("minime");
    static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("minime.json");
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Config load() {
        try {
            Files.createDirectories(DIR.resolve("skins"));
            if (Files.exists(FILE)) {
                Config c = GSON.fromJson(Files.readString(FILE), Config.class);
                if (c != null) return c;
            }
        } catch (Exception e) { e.printStackTrace(); }
        Config c = new Config(); c.save(); return c;
    }
    public void save() {
        try { Files.writeString(FILE, GSON.toJson(this)); } catch (Exception e) { e.printStackTrace(); }
    }
    /** self и girl — настоящие сущности в мире, остальное — плоские спрайты. */
    public boolean isEntityModel() { return List.of("chibi", "fox", "player").contains(model); }

    public List<String> allModels() {
        List<String> l = new ArrayList<>(List.of("chibi", "fox", "player", "tank")); l.addAll(customModels); return l;
    }
    /** own + все PNG из config/minime/skins */
    public List<String> allSkins() {
        List<String> l = new ArrayList<>(List.of("own", "steve", "alex", "girl"));
        try (var s = Files.list(DIR.resolve("skins"))) {
            s.map(p -> p.getFileName().toString()).filter(n -> n.toLowerCase().endsWith(".png"))
             .map(n -> n.substring(0, n.length() - 4)).sorted().forEach(l::add);
        } catch (Exception ignored) {}
        return l;
    }

    /** HEX RRGGBB -> int, при ошибке — значение по умолчанию. */
    public static int rgb(String hex, int def) {
        try {
            String h = hex.trim().replace("#", "");
            return h.length() == 6 ? Integer.parseInt(h, 16) : def;
        } catch (Exception e) { return def; }
    }

    /** Готовые темы: перезаписывают цвета. */
    public void applyTheme(String t) {
        theme = t;
        if (t.equals("dark")) { cBubbleFill = "161616"; cBubbleBorder = "FFFFFF"; cBubbleText = "FFFFFF"; cBubbleName = "B388FF"; cPanel = "0B0B0B"; }
        else { cBubbleFill = "FFFFFF"; cBubbleBorder = "000000"; cBubbleText = "000000"; cBubbleName = "9146FF"; cPanel = "F4F4F4"; }
    }

    /** Чёрный или белый текст — что контрастнее на данном фоне. */
    public static int contrast(int rgb) {
        double l = 0.299 * ((rgb >> 16) & 255) + 0.587 * ((rgb >> 8) & 255) + 0.114 * (rgb & 255);
        return l > 140 ? 0xFF111111 : 0xFFFFFFFF;
    }
}
