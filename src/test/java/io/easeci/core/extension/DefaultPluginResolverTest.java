package io.easeci.core.extension;

import io.easeci.BaseWorkspaceContextTest;
import io.easeci.commons.DirUtils;
import io.easeci.commons.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static io.easeci.core.workspace.LocationUtils.getPluginsYmlLocation;
import static org.junit.jupiter.api.Assertions.*;

class DefaultPluginResolverTest extends BaseWorkspaceContextTest {

    @Test
    @DisplayName("Should correctly return set of Plugin.class object even when .jar not exists")
    void defaultPluginResolverTest() {
        PluginResolver pluginResolver = new DefaultPluginResolver();

        Path pluginYml = getPluginsYmlLocation();
        InfrastructureInit infrastructureInit = Mockito.mock(InfrastructureInit.class);
        Mockito.when(infrastructureInit.getPluginDirectories())
                .thenReturn(
                        List.of(
                                Path.of("/tmp/plugins"),
                                Path.of("/opt/plugins")
                        )
                );

        Set<Plugin> resolved = pluginResolver.resolve(pluginYml, infrastructureInit);
        Plugin plugin = (Plugin) resolved.toArray()[0];

        assertAll(() -> assertNotNull(resolved),
                () -> assertFalse(resolved.isEmpty()),
                () -> assertNotNull(plugin),
                () -> assertNotNull(plugin.getJarArchive()),
                () -> assertFalse(plugin.getJarArchive().isStoredLocally()),
                () -> assertNotNull(plugin.getJarArchive().getFileName()),
                () -> assertNull(plugin.getJarArchive().getJarPath()),
                () -> assertNull(plugin.getJarArchive().getJarUrl()));
    }

    @Test
    @DisplayName("Should correctly find one jar file with instance of plugin")
    void defaultPluginResolverJarExistsTest() throws IOException {
        PluginResolver pluginResolver = new DefaultPluginResolver();

//        create fake jar archive
        final String FILE_NAME = "/tmp/plugins/welcome-logo-0.0.1.jar";
        Path fakeDir = DirUtils.directoryCreate("/tmp/plugins");
        Path jarFilePath = FileUtils.fileSave(FILE_NAME, "", true);

        Path pluginYml = getPluginsYmlLocation();
        InfrastructureInit infrastructureInit = Mockito.mock(InfrastructureInit.class);
        Mockito.when(infrastructureInit.getPluginDirectories())
                .thenReturn(
                        List.of(
                                Path.of("/tmp/plugins"),
                                Path.of("/opt/plugins")
                        )
                );

        Set<Plugin> resolved = pluginResolver.resolve(pluginYml, infrastructureInit);
        Plugin plugin = (Plugin) resolved.toArray()[0];

        assertAll(() -> assertNotNull(resolved),
                () -> assertFalse(resolved.isEmpty()),
                () -> assertNotNull(plugin),
                () -> assertNotNull(plugin.getJarArchive()),
                () -> assertTrue(plugin.getJarArchive().isStoredLocally()),
                () -> assertNotNull(plugin.getJarArchive().getFileName()),
                () -> assertNotNull(plugin.getJarArchive().getJarPath()),
                () -> assertNotNull(plugin.getJarArchive().getJarUrl()),
                () -> assertTrue(FileUtils.isExist(plugin.getJarArchive().getJarPath().toString())),
                () -> assertTrue(FileUtils.isExist(plugin.getJarArchive().getJarUrl().getPath())),
                () -> assertEquals(jarFilePath, plugin.getJarArchive().getJarPath()));

        org.apache.commons.io.FileUtils.deleteDirectory(fakeDir.toFile());
    }

    @Test
    @DisplayName("Should correctly resolve Plugin object by name and version of plugin")
    void defaultPluginResolverLoadResolveSingle() throws IOException {
        PluginResolver pluginResolver = new DefaultPluginResolver();

        final String FILE_NAME = "/tmp/plugins/git-v1.0.jar";
        Path fakeDir = DirUtils.directoryCreate("/tmp/plugins");
        Path jarFilePath = FileUtils.fileSave(FILE_NAME, "", true);

        InfrastructureInit infrastructureInit = Mockito.mock(InfrastructureInit.class);
        Mockito.when(infrastructureInit.getPluginDirectories())
                .thenReturn(
                        List.of(
                                Path.of("/tmp/plugins"),
                                Path.of("/opt/plugins")
                        )
                );

        final String pluginName = "git";
        final String pluginVersion = "v1.0";

        Plugin plugin = pluginResolver.resolve(infrastructureInit, pluginName, pluginVersion);

        assertAll(() -> assertNotNull(plugin),
                () -> assertNotNull(plugin.getJarArchive()),
                () -> assertTrue(plugin.getJarArchive().isStoredLocally()),
                () -> assertNotNull(plugin.getJarArchive().getFileName()),
                () -> assertNotNull(plugin.getJarArchive().getJarPath()),
                () -> assertNotNull(plugin.getJarArchive().getJarUrl()),
                () -> assertTrue(FileUtils.isExist(plugin.getJarArchive().getJarPath().toString())),
                () -> assertTrue(FileUtils.isExist(plugin.getJarArchive().getJarUrl().getPath())),
                () -> assertNotNull(plugin.getJarArchive().getJarPath()),
                () -> assertEquals(jarFilePath, plugin.getJarArchive().getJarPath()));

        org.apache.commons.io.FileUtils.deleteDirectory(fakeDir.toFile());
    }
}
