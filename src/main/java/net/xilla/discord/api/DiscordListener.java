package net.xilla.discord.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.xilla.boot.XillaAPI;
import net.xilla.boot.XillaApplication;

@AllArgsConstructor
@Getter
public abstract class DiscordListener {

    private XillaApplication application;

    public abstract ListenerAdapter listener();

    public XillaAPI getApi() {
        return application.getApi();
    }

}
