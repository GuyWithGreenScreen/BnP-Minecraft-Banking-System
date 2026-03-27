package dev.gwgs.minecrafteconomy.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class Resource_Token extends Item {

    private int tickAmount;

    public Resource_Token(Properties properties) {
        super(properties);
    }

    public static ItemStack returnSelf(Item unminted_Item, String ID, int count) {
        ItemStack returnItemStack = new ItemStack(MCE_Items.RESOURCE_TOKEN.asItem(), count);
        returnItemStack.set(MCEDataComponentTypes.UNMINTED_ITEM, BuiltInRegistries.ITEM.getKey(unminted_Item));
        returnItemStack.set(MCEDataComponentTypes.CURRENCY_ID, ID);
        return returnItemStack;
    }

    @Override
    public void onDestroyed(ItemEntity itemEntity, DamageSource damageSource) {
        System.out.println("BRUHHH");
        super.onDestroyed(itemEntity, damageSource);
    }

    @Override
    public boolean canBeHurtBy(ItemStack stack, DamageSource source) {
        return false;
    }


    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }



    @Override
    public int getEntityLifespan(ItemStack itemStack, Level level) {
        return 96000;
    }

    public static Item getUnmintedItem(ItemStack stack) {
        return BuiltInRegistries.ITEM.get(stack.get(MCEDataComponentTypes.UNMINTED_ITEM));
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("Minted ").append(getUnmintedItem(stack).getDefaultInstance().getHoverName()).withStyle(ChatFormatting.AQUA);
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, TooltipContext p_339594_, List<Component> p_41423_, TooltipFlag p_41424_) {
        p_41423_.add(Component.literal("Minted Item: ").withStyle(ChatFormatting.GRAY).append(Component.literal(getUnmintedItem(p_41421_).toString()).withStyle(ChatFormatting.LIGHT_PURPLE)));
        p_41423_.add(Component.literal("Resource ID: ").withStyle(ChatFormatting.GRAY).append(Component.literal(p_41421_.get(MCEDataComponentTypes.CURRENCY_ID).toString()).withStyle(ChatFormatting.DARK_GRAY)));
        super.appendHoverText(p_41421_, p_339594_, p_41423_, p_41424_);
    }
}
