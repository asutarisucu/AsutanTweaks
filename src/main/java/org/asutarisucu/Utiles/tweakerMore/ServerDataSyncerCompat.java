package org.asutarisucu.Utiles.tweakerMore;

import me.fallenbreath.tweakermore.impl.mod_tweaks.serverDataSyncer.ServerDataSyncer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * TweakerMore v3.33.2 renamed syncBlockEntity(BlockEntity) / syncEntity(Entity)
 * to syncBlockEntityToWorld / syncEntityToWorld. Looks up whichever name the
 * installed TweakerMore has, so one jar works with both.
 */
public class ServerDataSyncerCompat {
    private static final Method SYNC_BLOCK_ENTITY = find(BlockEntity.class, "syncBlockEntityToWorld", "syncBlockEntity");
    private static final Method SYNC_ENTITY = find(Entity.class, "syncEntityToWorld", "syncEntity");

    public static CompletableFuture<Void> syncBlockEntity(BlockEntity blockEntity) {
        return invoke(SYNC_BLOCK_ENTITY, blockEntity);
    }

    public static CompletableFuture<Void> syncEntity(Entity entity) {
        return invoke(SYNC_ENTITY, entity);
    }

    private static Method find(Class<?> paramType, String... names) {
        for (String name : names) {
            try {
                return ServerDataSyncer.class.getMethod(name, paramType);
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new IllegalStateException("ServerDataSyncer has none of the methods: " + String.join(", ", names));
    }

    @SuppressWarnings("unchecked")
    private static CompletableFuture<Void> invoke(Method method, Object arg) {
        try {
            return (CompletableFuture<Void>) method.invoke(ServerDataSyncer.getInstance(), arg);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
