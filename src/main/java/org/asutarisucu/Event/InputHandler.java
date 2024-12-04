package org.asutarisucu.Event;

import fi.dy.masa.malilib.hotkeys.*;

import org.asutarisucu.Configs.FeatureToggle;
import org.asutarisucu.Configs.Hotkeys;

import org.asutarisucu.AsutanTweaks;
import org.asutarisucu.Reference;

public class InputHandler implements IKeybindProvider, IKeyboardInputHandler, IMouseInputHandler {
    private static final InputHandler INSTANCE = new InputHandler();
    private InputHandler() {
        super();
    }
    public static InputHandler getInstance() {
        return INSTANCE;
    }
    @Override
    public void addKeysToMap(IKeybindManager manager) {
        for (FeatureToggle toggle : FeatureToggle.values()) {
            manager.addKeybindToMap(toggle.getKeybind());
        }
        for (IHotkey hotkey : Hotkeys.HOTKEY_LIST) {
            manager.addKeybindToMap(hotkey.getKeybind());
        }
    }
    @Override
    public void addHotkeys(IKeybindManager manager) {
        manager.addHotkeysForCategory(Reference.MOD_ID, "amatweaks.hotkeys.category.generic_hotkeys", Hotkeys.HOTKEY_LIST);
    }
}