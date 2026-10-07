package net.xilla.discord.api;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.xilla.boot.XillaAPI;
import net.xilla.boot.XillaApplication;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class DiscordCommand {

    private XillaApplication application;
    @Setter
    @Getter
    private SlashCommandData commandContext = null;// Used by Discord JDA
    @Setter
    @Getter
    private Permission permission = null;

    public DiscordCommand(XillaApplication application) {
        this.application = application;
    }

    public XillaAPI getApi() {
        return application.getApi();
    }

    public ListenerAdapter listener() {
        return new ListenerAdapter() {

            @Override
            public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
                if (event.getName().equalsIgnoreCase(commandContext.getName())) {

                    if(permission != null) {
                        if(!event.getMember().hasPermission(permission)) {
                            event.reply("You do not have access to this command!").setEphemeral(true).queue();
                        }
                    }

                    runCommand(event);
                }
            }

        };
    }

    public abstract void runCommand(SlashCommandInteractionEvent event);

}
