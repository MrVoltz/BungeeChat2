package dev.aura.bungeechat.message;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import dev.aura.bungeechat.account.Account;
import dev.aura.bungeechat.api.enums.ChannelType;
import dev.aura.bungeechat.api.enums.Permission;
import dev.aura.bungeechat.api.interfaces.BungeeChatAccount;
import dev.aura.bungeechat.api.placeholder.BungeeChatContext;
import dev.aura.bungeechat.filter.SwearWordsFilter;
import dev.aura.bungeechat.module.ModuleManager;
import dev.aura.bungeechat.permission.PermissionManager;
import dev.aura.bungeechat.placeholder.Context;
import dev.aura.bungeechat.placeholder.PlaceHolderUtil;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@UtilityClass
public class MessagesService {
    public static void sendPrivateMessage(CommandSender sender, ProxiedPlayer target, String message) {
        sendPrivateMessage(new Context(sender, target, message));
    }

    public static void sendPrivateMessage(BungeeChatContext context) {
        context.require(BungeeChatContext.HAS_SENDER, BungeeChatContext.HAS_TARGET, BungeeChatContext.HAS_MESSAGE);
    }

    public static void sendChannelMessage(CommandSender sender, ChannelType channel, String message) {
        sendChannelMessage(new Context(sender, message), channel);
    }

    public static void sendChannelMessage(BungeeChatContext context, ChannelType channel) {
        context.require(BungeeChatContext.HAS_PLAYER, BungeeChatContext.HAS_MESSAGE);

        switch (channel) {
        case GLOBAL:
            sendGlobalMessage(context);
            break;
        case LOCAL:
            // TODO
            break;
        case STAFF:
            sendStaffMessage(context);
            break;
        case HELP:
            sendHelpMessage(context);
            break;
        case GROUP:
            // TODO
            break;
        case NONE:
        default:
            // TODO
            break;
        }
    }

    public static void sendGlobalMessage(CommandSender sender, String message) {
        sendGlobalMessage(new Context(sender, message));
    }

    public static void sendGlobalMessage(BungeeChatContext context) {
        context.require(BungeeChatContext.HAS_PLAYER, BungeeChatContext.HAS_MESSAGE);

        String finalMessage = preProcessMessage(context, context.getPlayer(), "global");

        sendToMatchingPlayers(finalMessage);
    }

    public static void sendStaffMessage(CommandSender sender, String message) {
        sendStaffMessage(new Context(sender, message));
    }

    public static void sendStaffMessage(BungeeChatContext context) {
        context.require(BungeeChatContext.HAS_PLAYER, BungeeChatContext.HAS_MESSAGE);

        String finalMessage = preProcessMessage(context, context.getPlayer(), "staff");

        sendToMatchingPlayers(finalMessage,
                pp -> PermissionManager.hasPermission(pp, Permission.COMMAND_STAFFCHAT_VIEW));
    }

    public static void sendHelpMessage(CommandSender sender, String message) {
        sendHelpMessage(new Context(sender, message));
    }

    public static void sendHelpMessage(BungeeChatContext context) {
        context.require(BungeeChatContext.HAS_PLAYER, BungeeChatContext.HAS_MESSAGE);

        String finalMessage = preProcessMessage(context, context.getPlayer(), "helpop");

        sendToMatchingPlayers(finalMessage, pp -> PermissionManager.hasPermission(pp, Permission.COMMAND_HELPOP_VIEW));
    }

    public static String preProcessMessage(BungeeChatContext context, Optional<BungeeChatAccount> account,
            String format) {
        context.require(BungeeChatContext.HAS_MESSAGE);

        ProxiedPlayer player = Account.toProxiedPlayer(account.get());
        String message = context.getMessage().get();

        if (PermissionManager.hasPermission(player, Permission.USE_COLORED_CHAT)) {
            message = ChatColor.translateAlternateColorCodes('&', message);
        }

        // TODO: new filter logic
        if (ModuleManager.isModuleActive(ModuleManager.ANTI_SWEAR_MODULE)
                && !PermissionManager.hasPermission(player, Permission.BYPASS_ANTI_SWEAR)) {
            message = SwearWordsFilter.replaceSwearWords(message);
        }

        context.setMessage(message);

        return PlaceHolderUtil.getFullMessage(format, context);
    }

    @SafeVarargs
    @SuppressWarnings("deprecation")
    public static void sendToMatchingPlayers(String finalMessage, Predicate<? super ProxiedPlayer>... filters) {
        Stream<ProxiedPlayer> stream = ProxyServer.getInstance().getPlayers().stream();

        for (Predicate<? super ProxiedPlayer> filter : filters) {
            stream = stream.filter(filter);
        }

        stream.forEach(pp -> pp.sendMessage(finalMessage));
    }
}