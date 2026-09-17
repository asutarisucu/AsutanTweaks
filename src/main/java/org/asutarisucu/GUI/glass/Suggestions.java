package org.asutarisucu.GUI.glass;

import org.asutarisucu.Utiles.Block.BlockState;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

//#if MC < 260100
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
//#else
//$$ import net.minecraft.core.registries.BuiltInRegistries;
//$$ import net.minecraft.network.chat.Component;
//$$ import net.minecraft.resources.Identifier;
//$$ import net.minecraft.world.item.DyeColor;
//$$ import net.minecraft.world.item.Item;
//$$ import net.minecraft.world.item.ItemStack;
//$$ import net.minecraft.world.item.Items;
//$$ import net.minecraft.world.level.block.Block;
//$$ import net.minecraft.world.level.block.ShulkerBoxBlock;
//$$ import net.minecraft.world.level.block.state.properties.Property;
//#endif

/**
 * What the list editor offers while typing: items, blocks, dye colours or block
 * state names, searched by id and by the name shown in the game's language.
 */
public final class Suggestions {

    private Suggestions() {}

    public enum Kind { ITEM, BLOCK, DYE, STATE }

    /**
     * @param value  what is stored in the config
     * @param label  the name shown to the player
     * @param sub    a second line, such as the full id
     * @param icon   item to draw, or null
     * @param swatch RGB to draw instead of an icon, or -1
     */
    public record Entry(String value, String label, String sub, ItemStack icon, int swatch, String searchKey) {}

    private static final Map<Kind, List<Entry>> CACHE = new EnumMap<>(Kind.class);
    private static final Map<Kind, Map<String, Entry>> BY_VALUE = new EnumMap<>(Kind.class);
    private static String cacheKey;
    private static long checkedAt;
    private static boolean stacks;

    public static List<Entry> all(Kind kind) {
        refresh();
        return CACHE.computeIfAbsent(kind, Suggestions::build);
    }

    /**
     * Item stacks can only be made once a world has bound the item components,
     * so a list built on the title screen is rebuilt in a world to get its icons.
     */
    private static void refresh() {
        long now = System.currentTimeMillis();
        if (now - checkedAt < 1000) return;
        checkedAt = now;
        try {
            stacks = !new ItemStack(Items.STONE).isEmpty();
        } catch (RuntimeException e) {
            stacks = false;
        }
        String key = Lang.gameLanguage() + "|" + stacks;
        if (!key.equals(cacheKey)) {
            CACHE.clear();
            BY_VALUE.clear();
            cacheKey = key;
        }
    }

    /** The entry for a stored value, or a bare one when the value is unknown. */
    public static Entry describe(Kind kind, String value) {
        if (kind == null) return new Entry(value, value, "", null, -1, value);
        refresh();
        Map<String, Entry> map = BY_VALUE.computeIfAbsent(kind, k -> {
            Map<String, Entry> m = new HashMap<>();
            for (Entry e : all(k)) m.putIfAbsent(e.value().toLowerCase(Locale.ROOT), e);
            return m;
        });
        Entry e = map.get(value.toLowerCase(Locale.ROOT));
        return e != null ? e : new Entry(value, value, "", null, -1, value);
    }

    /** Best matches first: exact, then prefix, then anywhere; shorter names before longer. */
    public static List<Entry> search(Kind kind, String query, int limit) {
        String q = normalize(query);
        List<Entry> out = new ArrayList<>();
        if (q.isEmpty()) return out;
        String qId = q.replace(' ', '_');
        List<Object[]> scored = new ArrayList<>();
        for (Entry e : all(kind)) {
            String key = e.searchKey();
            int score;
            String value = e.value().toLowerCase(Locale.ROOT);
            String label = normalize(e.label());
            if (value.equals(qId) || label.equals(q)) score = 0;
            else if (value.startsWith(qId) || label.startsWith(q)) score = 1;
            else if (value.contains(qId) || key.contains(q) || key.contains(qId)) score = 2;
            else continue;
            scored.add(new Object[] { score, e });
        }
        scored.sort((a, b) -> {
            int c = Integer.compare((int) a[0], (int) b[0]);
            if (c != 0) return c;
            return Integer.compare(((Entry) a[1]).value().length(), ((Entry) b[1]).value().length());
        });
        for (int i = 0; i < scored.size() && i < limit; i++) out.add((Entry) scored.get(i)[1]);
        return out;
    }

    /**
     * Folds width and case, and hiragana into katakana, so "だいや" finds "ダイヤモンド".
     */
    public static String normalize(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT).trim();
        StringBuilder sb = new StringBuilder(n.length());
        for (int i = 0; i < n.length(); i++) {
            char c = n.charAt(i);
            if (c >= 0x3041 && c <= 0x3096) c = (char) (c + 0x60);
            sb.append(c);
        }
        return sb.toString();
    }

    private static List<Entry> build(Kind kind) {
        List<Entry> list = new ArrayList<>();
        try {
            switch (kind) {
                case ITEM -> {
//#if MC < 260100
                    for (Item item : Registries.ITEM) {
                        if (item == Items.AIR) continue;
                        Identifier id = Registries.ITEM.getId(item);
//#else
//$$                 for (Item item : BuiltInRegistries.ITEM) {
//$$                     if (item == Items.AIR) continue;
//$$                     Identifier id = BuiltInRegistries.ITEM.getKey(item);
//#endif
                        ItemStack stack = stacks ? new ItemStack(item) : null;
                        String label = stack != null ? displayName(stack) : translated(item);
                        list.add(entry(id.getPath(), label, id.toString(), stack, -1));
                    }
                }
                case BLOCK -> {
//#if MC < 260100
                    for (Block block : Registries.BLOCK) {
                        Identifier id = Registries.BLOCK.getId(block);
//#else
//$$                 for (Block block : BuiltInRegistries.BLOCK) {
//$$                     Identifier id = BuiltInRegistries.BLOCK.getKey(block);
//#endif
                        if (id.getPath().equals("air")) continue;
                        Item item = block.asItem();
                        ItemStack stack = !stacks || item == Items.AIR ? null : new ItemStack(item);
                        list.add(entry(id.getPath(), block.getName().getString(), id.toString(), stack, -1));
                    }
                }
                case DYE -> {
                    // MC 26.x dropped the colour-to-block lookup, so find the boxes by walking the registry.
                    Map<DyeColor, ItemStack> boxes = new EnumMap<>(DyeColor.class);
//#if MC < 260100
                    for (Block block : Registries.BLOCK) {
//#else
//$$                 for (Block block : BuiltInRegistries.BLOCK) {
//#endif
                        if (stacks && block instanceof ShulkerBoxBlock box && box.getColor() != null) {
                            boxes.putIfAbsent(box.getColor(), new ItemStack(block));
                        }
                    }
                    for (DyeColor color : DyeColor.values()) {
                        String name = color.getName();
//#if MC < 260100
                        String label = Text.translatable("color.minecraft." + name).getString();
//#else
//$$                     String label = Component.translatable("color.minecraft." + name).getString();
//#endif
                        list.add(entry(name, label, name, boxes.get(color), color.getFireworkColor()));
                    }
                }
                case STATE -> {
                    for (String name : BlockState.names()) {
                        Property<?> p = BlockState.getProperty(name);
                        if (p == null) continue;
                        // Several properties share a name such as "facing", so the stored name leads.
                        list.add(entry(name, name, p.getName() + ": " + valuesOf(p), null, -1));
                    }
                }
            }
        } catch (Throwable t) {
            org.asutarisucu.AsutanTweaks.LOGGER.warn("[Suggestions] Failed to list {}", kind, t);
        }
        return list;
    }

    private static Entry entry(String value, String label, String sub, ItemStack icon, int swatch) {
        String key = normalize(value + " " + label + " " + sub);
        return new Entry(value, label, sub, icon, swatch, key);
    }

    private static String displayName(ItemStack stack) {
//#if MC < 260100
        return stack.getName().getString();
//#else
//$$ return stack.getHoverName().getString();
//#endif
    }

    private static String translated(Item item) {
//#if MC < 260100
        return Text.translatable(item.getTranslationKey()).getString();
//#else
//$$ return Component.translatable(item.getDescriptionId()).getString();
//#endif
    }

    private static <T extends Comparable<T>> String valuesOf(Property<T> p) {
        StringJoiner j = new StringJoiner(", ");
//#if MC < 260100
        Collection<T> values = p.getValues();
        for (T v : values) j.add(p.name(v));
//#else
//$$ Collection<T> values = p.getPossibleValues();
//$$ for (T v : values) j.add(p.getName(v));
//#endif
        return j.toString();
    }
}
