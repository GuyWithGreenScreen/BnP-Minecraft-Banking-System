package dev.gwgs.minecrafteconomy.items;

import com.mojang.serialization.Codec;
import dev.gwgs.minecrafteconomy.Minecrafteconomy;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public class MCEDataComponentTypes {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Minecrafteconomy.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> UNMINTED_ITEM = register("unminted_item",
            builder -> builder.persistent(ResourceLocation.CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> CURRENCY_ID = register("currency_id",
            builder -> builder.persistent(Codec.STRING));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> COIN_NAME = register("coin_name",
            builder -> builder.persistent(Codec.STRING));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> AMOUNT = register("amount",
            builder -> builder.persistent(Codec.INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CURRENCY_TYPE = register("currency_type",
            builder -> builder.persistent(Codec.INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> CHEQUE_INVOICE_ID = register("cheque_invoice_id",
            builder -> builder.persistent(Codec.STRING));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> CHEQUE_INVOICE_FROM_USER_ID = register("cheque_invoice_from_user_id",
            builder -> builder.persistent(Codec.STRING));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> CHEQUE_INVOICE_FROM_USER_NAME = register("cheque_invoice_from_user_name",
            builder -> builder.persistent(Codec.STRING));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> CHEQUE_INVOICE_TO_USER_ID = register("cheque_invoice_to_user_id",
            builder -> builder.persistent(Codec.STRING));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> CHEQUE_INVOICE_TO_USER_NAME = register("cheque_invoice_to_user_name",
            builder -> builder.persistent(Codec.STRING));


    private static <T>DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }


    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}
