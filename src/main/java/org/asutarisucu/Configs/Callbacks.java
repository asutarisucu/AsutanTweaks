package org.asutarisucu.Configs;

import fi.dy.masa.malilib.hotkeys.KeyCallbackAdjustable;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.interfaces.IValueChangeCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import org.asutarisucu.GUI.GuiConfigs;

public class Callbacks {
    public static void init() {
        MinecraftClient mc = MinecraftClient.getInstance();
        IHotkeyCallback callbackGeneric = new KeyCallbackHotkeysGeneric();

        Hotkeys.OPEN_CONFIG_GUI.getKeybind().setCallback(callbackGeneric);
        FeatureToggle.LAST_USE_CANCEL.getKeybind().setCallback(KeyCallbackAdjustableFeature.createCallback(FeatureToggle.LAST_USE_CANCEL));
        FeatureToggle.AUTO_FILL_INVENTORY.getKeybind().setCallback(KeyCallbackAdjustableFeature.createCallback(FeatureToggle.AUTO_FILL_INVENTORY));

    }

    private static class KeyCallbackHotkeysGeneric implements IHotkeyCallback {

        public KeyCallbackHotkeysGeneric() {
        }

        @Override
        public boolean onKeyAction(KeyAction action, IKeybind key) {
            if (key == Hotkeys.OPEN_CONFIG_GUI.getKeybind()) {
                GuiBase.openGui(new GuiConfigs());
                return true;
            }
            return false;
        }
    }

    public static class FeatureCallbackHold implements IValueChangeCallback<IConfigBoolean> {
        private final KeyBinding keyBind;
        public FeatureCallbackHold(KeyBinding keyBind) {
            this.keyBind = keyBind;
        }

        @Override
        public void onValueChanged(IConfigBoolean config) {
            if (config.getBooleanValue()) {
                KeyBinding.setKeyPressed(InputUtil.fromTranslationKey(this.keyBind.getBoundKeyTranslationKey()), true);
                KeyBinding.onKeyPressed(InputUtil.fromTranslationKey(this.keyBind.getBoundKeyTranslationKey()));
            }
            else {
                KeyBinding.setKeyPressed(InputUtil.fromTranslationKey(this.keyBind.getBoundKeyTranslationKey()), false);
            }
        }
    }

    private record KeyCallbackAdjustableFeature(IConfigBoolean config) implements IHotkeyCallback {
        private static IHotkeyCallback createCallback(IConfigBoolean config) {
            return new KeyCallbackAdjustable(config, new KeyCallbackAdjustableFeature(config));
        }

        @Override
        public boolean onKeyAction(KeyAction action, IKeybind key) {
            this.config.toggleBooleanValue();

            boolean enabled = this.config.getBooleanValue();
            String strStatus = enabled ? "ON" : "OFF";
            String preGreen = GuiBase.TXT_GREEN;
            String preRed = GuiBase.TXT_RED;
            String rst = GuiBase.TXT_RST;
            String prettyName = this.config.getName();
            strStatus = (enabled ? preGreen : preRed) + strStatus + rst;

            InfoUtils.printActionbarMessage("Toggled %s %s", prettyName, strStatus);
            return true;
        }
    }
}