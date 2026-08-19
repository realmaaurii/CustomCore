package com.customcore.plugin.script;

import com.customcore.plugin.CustomCorePlugin;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Verwaltet die JavaScript-Ebene des Plugins. Jedes Skript läuft in einem
 * eigenen, gesandboxten Context - kein Zugriff auf Java-Reflection,
 * Dateisystem oder Prozesse von außerhalb der explizit freigegebenen API.
 */
public class ScriptManager {

    private final CustomCorePlugin plugin;
    private File scriptFolder;
    private final Map<String, Context> activeContexts = new LinkedHashMap<>();
    private ScriptAPI api;

    public ScriptManager(CustomCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        String folderName = plugin.getConfig().getString("scripts.folder", "scripts");
        this.scriptFolder = new File(plugin.getDataFolder(), folderName);
        if (!scriptFolder.exists()) scriptFolder.mkdirs();
        this.api = new ScriptAPI(plugin);
    }

    public void loadAllScripts() {
        File[] files = scriptFolder.listFiles((dir, name) -> name.endsWith(".js"));
        if (files == null) return;
        for (File f : files) {
            loadScript(f.getName());
        }
    }

    public boolean loadScript(String fileName) {
        File file = new File(scriptFolder, fileName);
        if (!file.exists()) return false;

        unloadScript(fileName); // vorherige Instanz sauber beenden, falls vorhanden

        try {
            // Sandbox: kein Host-Klassenzugriff, kein IO, keine Prozesse
            Context context = Context.newBuilder("js")
                    .allowHostAccess(HostAccess.EXPLICIT)
                    .allowHostClassLookup(className -> false)
                    .allowIO(false)
                    .allowCreateProcess(false)
                    .allowCreateThread(false)
                    .option("js.ecmascript-version", "2022")
                    .build();

            // Nur explizit erlaubte API-Objekte werden dem Skript bereitgestellt
            context.getBindings("js").putMember("Server", api);

            String source = Files.readString(file.toPath());
            context.eval(Source.newBuilder("js", source, fileName).build());

            activeContexts.put(fileName, context);
            plugin.getLogger().info("Skript geladen: " + fileName);
            return true;
        } catch (IOException | PolyglotException e) {
            plugin.getLogger().warning("Fehler beim Laden von " + fileName + ": " + e.getMessage());
            return false;
        }
    }

    public boolean unloadScript(String fileName) {
        Context context = activeContexts.remove(fileName);
        if (context != null) {
            context.close(true);
            return true;
        }
        return false;
    }

    public void reloadAll() {
        for (String name : Map.copyOf(activeContexts).keySet()) {
            unloadScript(name);
        }
        loadAllScripts();
    }

    public Iterable<String> listLoaded() {
        return activeContexts.keySet();
    }

    public void shutdown() {
        for (Context c : activeContexts.values()) {
            c.close(true);
        }
        activeContexts.clear();
    }
}
