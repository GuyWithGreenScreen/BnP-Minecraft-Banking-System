package dev.gwgs.minecrafteconomy;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = Minecrafteconomy.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> API_KEY = BUILDER.comment("BnP BANK API KEY").define("APIKey", "XXXXXXXXXXXXXXXX");
    public static final ModConfigSpec.ConfigValue<String> API_IP = BUILDER.comment("BnP BANK API IP").define("API_IP", "X/api");

    static final ModConfigSpec SPEC = BUILDER.build();


    public static String apiKey;
    public static String apiIP;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getType() == ModConfig.Type.SERVER || true) {
            apiKey = API_KEY.get();
            apiIP = API_IP.get();
        }
    }
}
