package net.xilla.discord.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.xilla.boot.XillaApplication;
import net.xilla.discord.api.CommandStartup;
import net.xilla.discord.api.DiscordCommand;

@CommandStartup
public class EmbedCommand extends DiscordCommand {

    public EmbedCommand(XillaApplication application) {
        super(application);
        setCommandContext(Commands.slash("createembed", "Create a new embed")
                .addOption(OptionType.CHANNEL, "channel", "The channel to send the embed to.", true)
                .addOption(OptionType.STRING, "title", "The title of the embed.", true)
                .addOption(OptionType.STRING, "description", "The description of the embed.", true)
                .addOption(OptionType.STRING, "color", "The color of the embed (hex, e.g. #FF0000).", false)
                .addOption(OptionType.STRING, "footer", "The footer text of the embed.", false)
                .addOption(OptionType.STRING, "image", "The image URL to display in the embed.", false)
                .addOption(OptionType.STRING, "thumbnail", "The thumbnail URL to display in the embed.", false)
        );
    }

    @Override
    public void runCommand(SlashCommandInteractionEvent event) {
        MessageChannel channel = event.getOption("channel").getAsChannel().asGuildMessageChannel();
        String title = event.getOption("title").getAsString();
        String description = event.getOption("description").getAsString();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(title);
        embed.setDescription(description);

        // Optional color (hex)
        if (event.getOption("color") != null) {
            String colorInput = event.getOption("color").getAsString().replace("#", "").trim();
            try {
                embed.setColor(Integer.parseInt(colorInput, 16));
            } catch (NumberFormatException e) {
                event.reply("That is not a valid hex color! Try something like `#5865F2`.")
                        .setEphemeral(true).queue();
                return;
            }
        }

        // Optional footer
        if (event.getOption("footer") != null) {
            embed.setFooter(event.getOption("footer").getAsString());
        }

        // Optional image
        if (event.getOption("image") != null) {
            embed.setImage(event.getOption("image").getAsString());
        }

        // Optional thumbnail
        if (event.getOption("thumbnail") != null) {
            embed.setThumbnail(event.getOption("thumbnail").getAsString());
        }

        channel.sendMessageEmbeds(embed.build()).queue(
                success -> event.reply("Embed sent to " + channel.getAsMention() + "!")
                        .setEphemeral(true).queue(),
                failure -> event.reply("Failed to send the embed: " + failure.getMessage())
                        .setEphemeral(true).queue()
        );
    }
}