package com.gmail.artemis.the.gr8.playerstats.msg;

import com.gmail.artemis.the.gr8.playerstats.config.ConfigHandler;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;

import java.time.LocalDate;
import java.time.Month;

import static net.kyori.adventure.text.Component.*;


public class PrideMessageFactory extends MessageFactory {

    private static ConfigHandler config;

    public PrideMessageFactory(ConfigHandler c, LanguageKeyHandler l) {
        super(c, l);
        config = c;
    }

    @Override
    protected TextComponent getPrefixAsTitle(boolean isConsoleSender) {
        if (cancelRainbow(isConsoleSender)) {
            return super.getPrefixAsTitle(isConsoleSender);
        }
        else {
            String title = "<rainbow:16>____________    [PlayerStats]    ____________</rainbow>"; //12 underscores
            return text()
                    .append(MiniMessage.miniMessage().deserialize(title))
                    .build();
        }
    }

    @Override
    protected TextComponent pluginPrefix(boolean isConsoleSender) {
        if (cancelRainbow(isConsoleSender)) {
            return super.pluginPrefix(isConsoleSender);
        }
        return text()
                .append(MiniMessage.miniMessage()
                        .deserialize("<#fe3e3e>[</#fe3e3e>" +
                                "<#fe5640>P</#fe5640>" +
                                "<#f67824>l</#f67824>" +
                                "<#ee8a19>a</#ee8a19>" +
                                "<#e49b0f>y</#e49b0f>" +
                                "<#cbbd03>e</#cbbd03>" +
                                "<#bccb01>r</#bccb01>" +
                                "<#8aee08>S</#8aee08>" +
                                "<#45fe31>t</#45fe31>" +
                                "<#01c1a7>a</#01c1a7>" +
                                "<#0690d4>t</#0690d4>" +
                                "<#205bf3>s</#205bf3>" +
                                "<#6c15fa>] </#6c15fa>"))
                .build();
    }

    /** Don't use rainbow formatting if the rainbow Prefix is disabled,
     if festive formatting is disabled or it is not pride month,
     or the commandsender is a Bukkit or Spigot console.*/
    private boolean cancelRainbow(boolean isConsoleSender) {
        return !(config.useRainbowPrefix() || (config.useFestiveFormatting() && LocalDate.now().getMonth().equals(Month.JUNE))) ||
                (isConsoleSender && Bukkit.getName().equalsIgnoreCase("CraftBukkit"));
        // If a player uses the command after pride month, with rainbow enabled
        // not (true OR (true && false)) OR (false && false)
        //   not (true OR (false)) OR (false)
        //       not (true) OR (false)                       not (false) OR (false)
        // false OR (false)      not (true)                true OR false    not (false)
        //                false                                       true
    }
}
