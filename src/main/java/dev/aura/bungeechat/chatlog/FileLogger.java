package dev.aura.bungeechat.chatlog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import dev.aura.bungeechat.BungeeChat;
import dev.aura.bungeechat.api.placeholder.BungeeChatContext;
import dev.aura.bungeechat.api.placeholder.PlaceHolderManager;
import dev.aura.bungeechat.message.Format;
import dev.aura.bungeechat.module.BungeecordModuleManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileLogger implements ChatLogger, AutoCloseable {
    private static final BungeeChatContext context = new BungeeChatContext();
    private static final File pluginDir = BungeeChat.getInstance().getConfigFolder();

    private final String logFile = BungeecordModuleManager.CHAT_LOGGING_MODULE.getModuleSection().getString("logFile");
    private String oldFile = "";
    private File saveTo;
    private FileWriter fw;
    private PrintWriter pw;

    @Override
    public void log(BungeeChatContext context) {
        initLogFile();

        pw.println(Format.CHAT_LOGGING_FILE.get(context));
        pw.flush();
    }

    @Override
    public void close() throws Exception {
        fw.close();
        pw.close();
    }

    private void initLogFile() {
        String newFile = PlaceHolderManager.processMessage(logFile, context);

        if (oldFile.equals(newFile))
            return;

        try {
            saveTo = new File(pluginDir, newFile);
            Optional.ofNullable(saveTo.getParentFile()).ifPresent(File::mkdirs);

            if (!saveTo.exists()) {
                saveTo.createNewFile();
            }

            fw = new FileWriter(saveTo, true);
            pw = new PrintWriter(fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
