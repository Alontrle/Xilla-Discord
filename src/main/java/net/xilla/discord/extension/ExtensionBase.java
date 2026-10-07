package net.xilla.discord.extension;

import net.dv8tion.jda.api.JDA;
import net.xilla.boot.api.program.ApplicationManager;

public interface ExtensionBase {

    public void startup(ApplicationManager application, JDA jda);

    public void shutdown();

}
