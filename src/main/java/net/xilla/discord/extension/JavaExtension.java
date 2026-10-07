package net.xilla.discord.extension;

import lombok.Getter;

@Getter
public abstract class JavaExtension implements ExtensionBase {

    public abstract String getName();
    public abstract String getVersion();
    public abstract String getAuthor();

}
