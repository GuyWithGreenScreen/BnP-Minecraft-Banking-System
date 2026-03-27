package dev.gwgs.minecrafteconomy.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class Invoice extends Item {
    public Invoice(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("Invoice").withStyle(ChatFormatting.RED);
    }

    public static ItemStack returnSelf(String ID, int currency_type, String currency_name, String currency_id, String from_user_ID, String to_user_ID, String from_user_name, String to_user_name, int amount) {
        ItemStack returnItemStack = new ItemStack(MCE_Items.INVOICE.asItem(), 1);
        returnItemStack.set(MCEDataComponentTypes.CURRENCY_ID, currency_id);
        returnItemStack.set(MCEDataComponentTypes.CURRENCY_TYPE, currency_type);
        returnItemStack.set(MCEDataComponentTypes.COIN_NAME, currency_name);
        returnItemStack.set(MCEDataComponentTypes.CHEQUE_INVOICE_ID, ID);
        returnItemStack.set(MCEDataComponentTypes.AMOUNT, amount);
        returnItemStack.set(MCEDataComponentTypes.CHEQUE_INVOICE_FROM_USER_ID, from_user_ID);
        returnItemStack.set(MCEDataComponentTypes.CHEQUE_INVOICE_FROM_USER_NAME, from_user_name);
        returnItemStack.set(MCEDataComponentTypes.CHEQUE_INVOICE_TO_USER_ID, to_user_ID);
        returnItemStack.set(MCEDataComponentTypes.CHEQUE_INVOICE_TO_USER_NAME, to_user_name);
        return returnItemStack;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
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

    @Override
    public void appendHoverText(ItemStack p_41421_, TooltipContext p_339594_, List<Component> p_41423_, TooltipFlag p_41424_) {
        if (p_41421_.get(MCEDataComponentTypes.CURRENCY_TYPE) == 1) {
            p_41423_.add(Component.literal("AMOUNT DUE: ").withStyle(ChatFormatting.GRAY).append(Component.literal(p_41421_.get(MCEDataComponentTypes.AMOUNT) + " of").withStyle(ChatFormatting.WHITE)).append(Component.literal(" $" + p_41421_.get(MCEDataComponentTypes.COIN_NAME)).withStyle(ChatFormatting.GOLD).append(Component.literal(" ID:(" + p_41421_.get(MCEDataComponentTypes.CURRENCY_ID) + ")").withStyle(ChatFormatting.DARK_GRAY))));
        } else {
            p_41423_.add(Component.literal("AMOUNT DUE: ").withStyle(ChatFormatting.GRAY).append(Component.literal(p_41421_.get(MCEDataComponentTypes.AMOUNT) + " of").withStyle(ChatFormatting.WHITE)).append(Component.literal(" " + p_41421_.get(MCEDataComponentTypes.COIN_NAME)).withStyle(ChatFormatting.AQUA).append(Component.literal(" ID:(" + p_41421_.get(MCEDataComponentTypes.CURRENCY_ID) + ")").withStyle(ChatFormatting.DARK_GRAY))));
        }
        p_41423_.add(Component.literal("ISSUED BY: ").withStyle(ChatFormatting.GRAY).append(Component.literal(p_41421_.get(MCEDataComponentTypes.CHEQUE_INVOICE_FROM_USER_NAME)).withStyle(ChatFormatting.WHITE)).append(Component.literal(" ID:(" + p_41421_.get(MCEDataComponentTypes.CHEQUE_INVOICE_FROM_USER_ID) + ")").withStyle(ChatFormatting.DARK_GRAY)));
        p_41423_.add(Component.literal("TO: ").withStyle(ChatFormatting.GRAY).append(Component.literal(p_41421_.get(MCEDataComponentTypes.CHEQUE_INVOICE_TO_USER_NAME)).withStyle(ChatFormatting.WHITE)).append(Component.literal(" ID:(" + p_41421_.get(MCEDataComponentTypes.CHEQUE_INVOICE_TO_USER_ID) + ")").withStyle(ChatFormatting.DARK_GRAY)));
        p_41423_.add(Component.literal("INVOICE ID: " + p_41421_.get(MCEDataComponentTypes.CHEQUE_INVOICE_ID)).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(p_41421_, p_339594_, p_41423_, p_41424_);
    }
}
