package net.xilla.discord.listener;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.xilla.boot.XillaApplication;
import net.xilla.discord.api.DiscordListener;
import net.xilla.discord.api.ListenerStartup;
import net.xilla.discord.data.XillaButton;

@ListenerStartup
public class ButtonListener extends DiscordListener {

    public ButtonListener(XillaApplication application) {
        super(application);
    }

    @Override
    public ListenerAdapter listener() {
        return new ListenerAdapter() {
            @Override
            public void onButtonInteraction(ButtonInteractionEvent event) {
                // Check which button was pressed using the custom ID
                XillaButton xillaButton = getApi().getObject(XillaButton.class, event.getComponentId());
                if (xillaButton != null) {
                    if(xillaButton.isOneTime()) {
                        if (xillaButton.isRanYet()) {
                            event.reply("You can only use this button once.").queue();
                            return;
                        }
                    }

                    xillaButton.onClick(event);

                    if(xillaButton.isOneTime())
                        xillaButton.setRanYet(true);
                }
            }
        };
    }
}
