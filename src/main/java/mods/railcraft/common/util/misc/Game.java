/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2018
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.util.misc;

import mods.railcraft.api.core.ClientAccessException;
import mods.railcraft.common.core.Railcraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFormatMessage;
import org.apache.logging.log4j.message.SimpleMessage;
import org.jetbrains.annotations.Nullable;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public final class Game {
    public static final boolean OBFUSCATED;
    public static final boolean DEVELOPMENT_ENVIRONMENT;
    public static final boolean BUKKIT;
    /**
     * A logging level dedicated to debug reports. It is more prioritized than errors but less serious than fatal.
     */
    public static final Level DEBUG_REPORT = Level.forName("DEBUG_REPORT", 150);

    static {
        boolean worldFound = false;
        boolean worldObjFound = false;
        try {
            worldFound = Entity.class.getDeclaredField("world") != null;
        } catch (NoSuchFieldException | SecurityException ignored) {
        }
        try {
            //noinspection JavaReflectionMemberAccess
            worldObjFound = Entity.class.getDeclaredField("worldObj") != null;
        } catch (NoSuchFieldException | SecurityException ignored) {
        }
        OBFUSCATED = !worldFound && !worldObjFound;
        DEVELOPMENT_ENVIRONMENT = Railcraft.getVersion().matches(".*(alpha|beta).*") || !OBFUSCATED;
        boolean foundBukkit = false;
        try {
            foundBukkit = Class.forName("org.spigotmc.SpigotConfig") != null;
        } catch (ClassNotFoundException ignored) {
        }
        BUKKIT = foundBukkit;
        if (BUKKIT)
            log(Level.INFO, "Bukkit detected, disabling Tile Entity caching because Bukkit doesn't seem to invalid Tile Entities properly!");
    }

    public static boolean isHost(final World world) {
        return !world.isRemote;
    }

    public static boolean isClient(final World world) {
        return world.isRemote;
    }

    public static void notClient(final World world) {
        if (isClient(world)) throw new ClientAccessException();
    }

    public static void requiresServerThread() {
        MinecraftServer mcServer = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (!mcServer.isCallingFromMinecraftThread())
            throw new IllegalThreadStateException("Must call from server!");
    }

    public static MinecraftServer getServer() {
        return FMLCommonHandler.instance().getMinecraftServerInstance();
    }

    @SideOnly(Side.CLIENT)
    public static @Nullable WorldClient getWorld() {
        return FMLClientHandler.instance().getWorldClient();
    }

    @SuppressWarnings("SameReturnValue")
    public static boolean isObfuscated() {
        return OBFUSCATED;
    }

    private static Message getMessage(String msg, Object... args) {
        return new MessageFormatMessage(msg, args);
    }

    public static void log(Level level, @Nullable String msg, Object... args) {
        if (msg != null)
            log(level, getMessage(msg, args));
    }

    public static void log(Level level, Message msg) {
        LogManager.getLogger(Railcraft.MOD_ID).log(level, msg);
    }

    public static void logTrace(Level level, String msg, Object... args) {
        logTrace(level, getMessage(msg, args));
    }

    public static void logTrace(Level level, Message message) {
        Game.logTrace(level, 5, message);
    }

    public static void logTrace(Level level, int lines, String msg, Object... args) {
        log(level, getMessage(msg, args));
        logTrace(level, lines, 2, Thread.currentThread().getStackTrace());
    }

    public static void logTrace(Level level, int lines, Message message) {
        log(level, message);
        logTrace(level, lines, 2, Thread.currentThread().getStackTrace());
    }

    private static void logTrace(Level level, int lines, int skipLines, StackTraceElement[] stackTrace) {
        for (int i = skipLines; i < stackTrace.length && i < skipLines + lines; i++) {
            log(level, stackTrace[i].toString());
        }
    }

    public static void logThrowable(String msg, Throwable error, Object... args) {
        logThrowable(Level.ERROR, 3, error, msg, args);
    }

    public static void logThrowable(String msg, int lines, Throwable error, Object... args) {
        logThrowable(Level.ERROR, lines, error, msg, args);
    }

    public static void logThrowable(Level level, int lines, Throwable error, String msg, Object... args) {
        log(level, msg, args);
        log(level, new SimpleMessage(error.toString()));
        logTrace(level, lines, 0, error.getStackTrace());
    }

    public static void logDebug(String msg, Object... args) {
        if (!DEVELOPMENT_ENVIRONMENT)
            return;
        log(Level.DEBUG, msg, args);
    }

    public static void logErrorAPI(String mod, Throwable error, Class<?>... classFiles) {
        StringBuilder msg = new StringBuilder(mod);
        msg.append(" API error, please update your mods. Error: ").append(error);
        logThrowable(Level.ERROR, 2, error, msg.toString());

        for (Class<?> classFile : classFiles) {
            if (classFile != null) {
                msg = new StringBuilder(mod);
                msg.append(" API error: ").append(classFile.getSimpleName()).append(" is loaded from ").append(classFile.getProtectionDomain().getCodeSource().getLocation());
                log(Level.ERROR, msg.toString());
            }
        }
    }

    public static void logErrorFingerprint(String mod) {
        log(Level.FATAL, "{0} failed validation, terminating. Please re-download {0} from an official source.", mod);
    }

    private Game() {
    }
}
