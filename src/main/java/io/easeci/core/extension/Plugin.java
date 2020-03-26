package io.easeci.core.extension;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.net.URL;
import java.nio.file.Path;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

/**
 * Representation of the plugin as POJO.
 * About naming (String name field):
 *   - use only alphanumeric characters in plugin's naming
 *   - the word separator used is a dash: '-'
 * About versioning (String version field):
 *   - versioning must to be compatible with semantic versioning
 *     ex. '1.11.0'
 * */

@Getter
@AllArgsConstructor
public class Plugin {
    private String name;
    private String version;
    private JarArchive jarArchive;

    public static Plugin of(String name, String version) {
        if (isNull(name)) {
            throw new RuntimeException("Cannot create Plugin.class instance, name of plugin is null");
        }
        if (isNull(version)) {
            throw new RuntimeException("Cannot create Plugin.class instance, version of plugin is null");
        }
        return new Plugin(name, version, JarArchive.empty());
    }

    public static Plugin of(Plugin plugin, JarArchive jarArchive) {
        if (isNull(plugin.name)) {
            throw new RuntimeException("Cannot create Plugin.class instance, name of plugin is null");
        }
        if (isNull(plugin.version)) {
            throw new RuntimeException("Cannot create Plugin.class instance, version of plugin is null");
        }
        return new Plugin(plugin.name, plugin.version, jarArchive);
    }

    @Override
    public String toString() {
        return "~ Plugin '".concat(name)
                .concat("' ver. ")
                .concat(version)
                .concat(", localised ")
                .concat((isNull(this.jarArchive) ? "" : this.jarArchive.toString()));
    }

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class JarArchive {
        private String fileName;
        private boolean isStoredLocally;
        private URL jarUrl;
        private Path jarPath;

        private JarArchive() {}

        public static JarArchive empty() {
            return new JarArchive();
        }

        @Override
        public String toString() {
            return "~ Jar file named: ".concat(fileName)
                    .concat(", localised here: ")
                    .concat(ofNullable(jarPath).orElse(Path.of("-")).toString())
                    .concat(", is exists: " + isStoredLocally);
        }
    }
}
