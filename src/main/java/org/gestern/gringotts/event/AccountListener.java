package org.gestern.gringotts.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.gestern.gringotts.Util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.gestern.gringotts.Configuration.CONF;
import static org.gestern.gringotts.event.VaultCreationEvent.Type;

/**
 * Listens for chest creation and destruction events.
 *
 * @author jast
 */
public class AccountListener implements Listener {

    private final Pattern vaultPattern = Pattern.compile(
            CONF.vaultPattern,
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    /**
     * Create an account chest by adding a sign marker over it.
     *
     * @param event Event data.
     */
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Component line0 = event.line(0);

        if (line0 == null) {
            return;
        }

        String line0String = PlainTextComponentSerializer.plainText().serialize(line0);

        Matcher match = vaultPattern.matcher(line0String);

        // consider only signs with proper formatting
        if (!match.matches()) {
            return;
        }

        String typeStr = match.group(1).toUpperCase();

        Type type;
        // default vault is player
        if (typeStr.isEmpty()) {
            type = Type.PLAYER;
        } else {
            try {
                type = Type.valueOf(typeStr);
            } catch (IllegalArgumentException notFound) {
                return;
            }
        }

        Optional<Sign> optionalSign = Util.getBlockStateAs(
                event.getBlock(),
                Sign.class
        );

        if (optionalSign.isPresent() && Util.chestBlock(optionalSign.get()) != null) {
            // we made it this far, throw the event to manage vault creation
            final VaultCreationEvent creation = new PlayerVaultCreationEvent(type, event);

            Bukkit.getServer().getPluginManager().callEvent(creation);
        }
    }
}
