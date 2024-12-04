package org.asutarisucu;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;

import org.asutarisucu.Configs.Callbacks;
import org.asutarisucu.Configs.Configs;
import org.asutarisucu.Event.InputHandler;
import org.asutarisucu.Event.LastUseCancel;

public class InitHandler implements IInitializationHandler {
    @Override
    public void registerModHandlers() {
        ConfigManager.getInstance().registerConfigHandler(Reference.MOD_ID,new Configs());
        InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.getInstance());
        InputEventHandler.getInputManager().registerKeyboardInputHandler(InputHandler.getInstance());
        InputEventHandler.getInputManager().registerMouseInputHandler(InputHandler.getInstance());
        Callbacks.init();
        Reference.LoadEvent();
    }
}
