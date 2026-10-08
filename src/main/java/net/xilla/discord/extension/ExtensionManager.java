package net.xilla.discord.extension;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.xilla.boot.Logger;
import net.xilla.boot.XillaApplication;
import net.xilla.boot.api.program.ProcessPriority;
import net.xilla.boot.api.program.XillaProcess;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ExtensionManager {

    @Getter
    private XillaApplication application;
    private List<JavaExtension> extensions = new ArrayList<>();

    public ExtensionManager(XillaApplication application) {
        this.application = application;

        application.getClassScanner().addScan((clazz) -> {
            Annotation[] annotations = clazz.getAnnotationsByType(XillaDiscordExtension.class);
            if(annotations.length > 0){

                // Instantiate the class
                JavaExtension extension;
                try {
                    Constructor<?> constructor = clazz.getDeclaredConstructor();
                    extension = (JavaExtension) constructor.newInstance();
                    extensions.add(extension);
                    Logger.info("Started extension " + extension.getName() + " (" + extension.getVersion() + ")");
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException("Failed to instantiate extension: " + clazz.getName(), e);
                }
                return true;
            }
            return false;
        });
    }

    public void load() {
        File folder = new File("extensions/");

        if (!folder.exists()) {
            Logger.info("Extensions folder not found, creating it: " + folder.getAbsolutePath());
            try {
                Files.createDirectories(folder.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        File[] jars = folder.listFiles();
        if (jars == null) {
            jars = new File[0];
        }

        for (File jar : jars) {
            if (!jar.isFile() || !jar.getName().endsWith(".jar")) {
                continue;
            }

            try {
                // 1. Create a dedicated classloader for this extension
                ClassLoader extensionClassLoader = createExtensionClassLoader(jar);
                application.getClassScanner().addClassLoader(extensionClassLoader);

                try {
                    Class.forName("co.anniecreates.discord.command.SendPointsCommand", false, extensionClassLoader);
                    System.out.println("loaded OK");
                } catch (Throwable t) {
                    t.printStackTrace();
                }

                Logger.info("Initialized extension " + jar.getName());
            } catch (Exception e) {
                Logger.warn("Failed to load extension from " + jar.getName() + ":");
                e.printStackTrace();
            }
        }
    }

    public void startup(JDA jda) {
        for(JavaExtension extension : extensions) {
            extension.startup(application, jda);
        }
    }

    /**
     * Creates a dedicated classloader for each extension JAR.
     * If Xilla provides a plugin loader utility, replace this with it.
     */
    private URLClassLoader createExtensionClassLoader(File jar) throws Exception {
        ClassLoader parent = getClass().getClassLoader();
        return new URLClassLoader(new URL[]{jar.toURL()}, parent);
     }
}
