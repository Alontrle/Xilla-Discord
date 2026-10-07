package net.xilla.discord;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.xilla.boot.Logger;
import net.xilla.boot.XillaApplication;
import net.xilla.boot.api.program.ProcessPriority;
import net.xilla.boot.api.program.XillaProcess;
import net.xilla.boot.storage.file.loader.JsonConfigLoader;
import net.xilla.boot.storage.setting.SettingsFile;
import net.xilla.discord.api.CommandStartup;
import net.xilla.discord.api.DiscordCommand;
import net.xilla.discord.api.DiscordListener;
import net.xilla.discord.api.ListenerStartup;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        new Main();
    }

    private JDA discord;
    private XillaApplication application;
    private List<DiscordListener> listeners = new ArrayList<>();
    private List<DiscordCommand> commands = new ArrayList<>();

    public Main() {
        application = new XillaApplication(getClass().getClassLoader());

        loadSettings();
        loadDiscord();

        application.getClassScanner().addScan((clazz) -> {
            Annotation[] annotations = clazz.getAnnotationsByType(ListenerStartup.class);
            if(annotations.length > 0){

                // Instantiate the class
                DiscordListener listener;
                try {
                    Constructor<?> constructor = clazz.getDeclaredConstructor(XillaApplication.class);
                    listener = (DiscordListener) constructor.newInstance(application);
                    listeners.add(listener);
                    Logger.info("Started listener " + listener.getClass().getSimpleName());
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException("Failed to instantiate DiscordListener: " + clazz.getName(), e);
                }
                return true;
            }
            return false;
        });

        application.getClassScanner().addScan((clazz) -> {
            Annotation[] annotations = clazz.getAnnotationsByType(CommandStartup.class);
            if(annotations.length > 0){

                // Instantiate the class
                DiscordCommand command;
                try {
                    Constructor<?> constructor = clazz.getDeclaredConstructor(XillaApplication.class);
                    command = (DiscordCommand) constructor.newInstance(application);
                    commands.add(command);
                    Logger.info("Started command " + command.getClass().getSimpleName());
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException("Failed to instantiate DiscordCommand: " + clazz.getName(), e);
                }
                return true;
            }
            return false;
        });

        application.initialize();
        application.start();
    }

    private void loadSettings() {
        SettingsFile settingsFile = new SettingsFile(application, "settings.json", new JsonConfigLoader("settings.json"));
        settingsFile.reload();
        settingsFile.setDefault("bot-id", "put-discord-id-here");
        settingsFile.setDefault("embed-color", "#d742f5");
        settingsFile.save();
        application.registerManager(settingsFile);
    }

    private void loadDiscord() {
        application.registerStartupProcess(new XillaProcess("Discord", ProcessPriority.COMPLETE) {
            @Override
            public void run() {
                try {
                    String botToken = application.getApi().getSetting("settings.json", "bot-id");

                    if(botToken.equalsIgnoreCase("put-discord-id-here")) {
                        Logger.error("Failed to start the discord bot! You must set the bot ID in the config.yml");
                        return;
                    }

                    List<ListenerAdapter> listenerAdapters = new ArrayList<>();
                    for(DiscordCommand command : commands) {
                        listenerAdapters.add(command.listener());
                    }
                    for(DiscordListener listener : listeners) {
                        listenerAdapters.add(listener.listener());
                    }
                    ListenerAdapter[] adapters  = new ListenerAdapter[listenerAdapters.size()];
                    for(int i = 0; i < adapters.length; i++){
                        adapters[i] = listenerAdapters.get(i);
                    }
                    discord = JDABuilder
                            .createDefault(botToken, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                            .setChunkingFilter(ChunkingFilter.ALL)
                            .setMemberCachePolicy(MemberCachePolicy.ALL)
                            .addEventListeners(adapters)
                            .setActivity(Activity.playing("Babysitting a bunch of babies!"))
                            .build();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    discord.awaitReady();

                    SlashCommandData[] adapters  = new SlashCommandData[commands.size()];
                    for(int i = 0; i < adapters.length; i++){
                        adapters[i] = commands.get(i).getCommandContext();
                    }
                    discord.updateCommands().addCommands(adapters).queue();

                    // After the bot is ready, you can perform tasks like logging its status
                    Logger.info("Bot is ready! Connected as: " + discord.getSelfUser().getAsTag());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}