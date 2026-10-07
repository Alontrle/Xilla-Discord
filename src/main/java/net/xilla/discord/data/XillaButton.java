package net.xilla.discord.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.xilla.boot.XillaApplication;
import net.xilla.boot.reflection.annotation.CacheManager;

@AllArgsConstructor
@CacheManager
@Getter
public abstract class XillaButton {

    private String id;
    private XillaApplication application;
    @Setter
    private boolean oneTime = false;
    @Setter
    private boolean ranYet = false;

    public XillaButton(XillaApplication application) {
        this.id = application.getApi().getManager(XillaButton.class).getUniqueID();
        application.getApi().setObject(XillaButton.class, this);
        this.application = application;
    }

    public abstract void onClick(ButtonInteractionEvent event);

}
