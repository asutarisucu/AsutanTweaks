package org.asutarisucu;

import fi.dy.masa.malilib.event.InitializationHandler;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.filter.RegexFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsutanTweaks implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(Reference.MOD_ID);

    @Override
    public void onInitializeClient() {
        suppressSpuriousGlErrors();
        InitializationHandler.getInstance().registerInitializationHandler(new InitHandler());
        System.setProperty("java.awt.headless", "false");
    }

    // Suppress "OpenGL debug message ... in (null)" spam.
    // source=(null) means the driver reported no source function — real GPU errors always have one.
    // If genuine GL errors seem missing, disable this method and check logs.
    private static void suppressSpuriousGlErrors() {
        try {
            RegexFilter filter = RegexFilter.createFilter(
                    "OpenGL debug message.*in \\(null\\).*",
                    null, false,
                    RegexFilter.Result.DENY,
                    RegexFilter.Result.NEUTRAL);
            if (filter != null) {
                ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addFilter(filter);
            }
        } catch (Exception ignored) {
        }
    }
}