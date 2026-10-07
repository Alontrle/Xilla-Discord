package net.xilla.discord.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.xilla.boot.XillaApplication;
import net.xilla.boot.reflection.annotation.JsonFolderManager;

@Getter
@NoArgsConstructor
@JsonFolderManager(folderName = "users/", autoSave = true, autoSaveTime = 60)
public class UserData {

    private String id;

    public  UserData(String id) {
        this.id = id;
    }

}
