package org.asutarisucu.GUI;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.BooleanHotkeyGuiWrapper;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.StringUtils;

import java.util.Collections;
import java.util.List;

import org.asutarisucu.Configs.Configs;
import org.asutarisucu.Configs.FeatureToggle;
import org.asutarisucu.Configs.Hotkeys;

import org.asutarisucu.Reference;

public class GuiConfigs extends GuiConfigsBase {
    public static ImmutableList<FeatureToggle> TWEAK_LIST = FeatureToggle.VALUES;

    private static ConfigGuiTab tab = ConfigGuiTab.GENERIC;

    public GuiConfigs() {
        super(10, 50, Reference.MOD_ID, null, Reference.MOD_ID + " %s", String.format("%s", Reference.VERSION));
    }

    @Override
    public void initGui() {
        super.initGui();
        this.clearOptions();

        int x = 10;
        int y = 26;

        for (ConfigGuiTab tab : ConfigGuiTab.values()) {
            x += this.createButton(x, y, -1, tab);
        }
    }

    private int createButton(int x, int y, int width, ConfigGuiTab tab) {
        ButtonGeneric button = new ButtonGeneric(x, y, width, 20, tab.getDisplayName());
        button.setEnabled(GuiConfigs.tab != tab);
        this.addButton(button, new ButtonListener(tab, this));
        return button.getWidth() + 2;
    }

    @Override
    protected int getConfigWidth() {
        ConfigGuiTab tab = GuiConfigs.tab;

        if (tab == ConfigGuiTab.GENERIC) {
            return 120;
        }
        return 260;
    }

    @Override
    protected boolean useKeybindSearch() {
        return GuiConfigs.tab == ConfigGuiTab.GENERIC_HOTKEYS;
    }

    @Override
    public List<ConfigOptionWrapper> getConfigs() {
        List<? extends IConfigBase> configs;
        ConfigGuiTab tab = GuiConfigs.tab;

        if (tab == ConfigGuiTab.GENERIC) {
            configs= Configs.Generic.OPTIONS;
        } else if (tab == ConfigGuiTab.GENERIC_HOTKEYS) {
            configs = Hotkeys.HOTKEY_LIST;
        } else if (tab==ConfigGuiTab.TWEAKS) {
            return ConfigOptionWrapper.createFor(TWEAK_LIST.stream().map(this::wrapConfig).toList());
        } else {
            return Collections.emptyList();
        }
        return ConfigOptionWrapper.createFor(configs);
    }

    protected BooleanHotkeyGuiWrapper wrapConfig(FeatureToggle config) {
        return new BooleanHotkeyGuiWrapper(config.getName(), config, config.getKeybind());
    }

    private record ButtonListener(ConfigGuiTab tab, GuiConfigs parent) implements IButtonActionListener {

        @Override
            public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
                GuiConfigs.tab = this.tab;
                this.parent.reCreateListWidget(); // apply the new config width
                this.parent.getListWidget().resetScrollbarPosition();
                this.parent.initGui();
            }
        }

    public enum ConfigGuiTab {
        GENERIC         ("Generic"),
        GENERIC_HOTKEYS ("Hotkeys"),
        TWEAKS          ("Tweaks");

        private final String translationKey;

        ConfigGuiTab(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getDisplayName() {
            return StringUtils.translate(this.translationKey);
        }
    }
}