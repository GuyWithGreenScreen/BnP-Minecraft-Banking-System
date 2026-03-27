package dev.gwgs.minecrafteconomy.items;


import dev.gwgs.minecrafteconomy.Minecrafteconomy;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.checkerframework.checker.units.qual.C;

public class MCE_Items {
    public static DeferredRegister.Items DEFERRED_REGISTER = DeferredRegister.createItems(Minecrafteconomy.MODID);

    public static DeferredItem<Item> RESOURCE_TOKEN = DEFERRED_REGISTER.register("resource_token", () -> new Resource_Token(new Item.Properties()));
    public static DeferredItem<Item> COIN_TOKEN = DEFERRED_REGISTER.register("coin_token", () -> new Coin_Token(new Item.Properties()));
    public static DeferredItem<Item> CHEQUE = DEFERRED_REGISTER.register("cheque", () -> new Cheque(new Item.Properties()));
    public static DeferredItem<Item> INVOICE = DEFERRED_REGISTER.register("invoice", () -> new Invoice(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        DEFERRED_REGISTER.register(eventBus);
    }
}
