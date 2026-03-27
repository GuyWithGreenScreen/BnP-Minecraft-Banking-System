package dev.gwgs.minecrafteconomy.commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.gwgs.minecrafteconomy.items.MCE_Items;
import dev.gwgs.minecrafteconomy.items.Resource_Token;
import dev.gwgs.minecrafteconomy.networking.MCENetwork;
import dev.gwgs.minecrafteconomy.networking.MCERequests;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.util.Objects;

public class MCE_MintCommand {
    public MCE_MintCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mint").executes(this::executeArgumentless).then(Commands.argument("amount", IntegerArgumentType.integer(1, 64)).executes(this::execute)));
    }

    private int executeArgumentless(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        Gson gson = new Gson();
        JsonObject request = gson.fromJson(MCERequests.MINT.requestTemplate, JsonObject.class);
        int count = player.getMainHandItem().getCount();
        ItemStack playerMainHand = player.getMainHandItem();
        Item pmhI = playerMainHand.getItem().asItem();
        if (!playerMainHand.isEmpty() && count <= playerMainHand.getCount() &&
                (pmhI != MCE_Items.CHEQUE.asItem() && pmhI != MCE_Items.COIN_TOKEN.asItem() && pmhI != MCE_Items.RESOURCE_TOKEN.asItem() && pmhI != MCE_Items.INVOICE.asItem())) {
            request.getAsJsonObject("dat").addProperty("amount", count);
            request.getAsJsonObject("dat").addProperty("item_id", playerMainHand.getItem().toString());
            request.getAsJsonObject("dat").addProperty("item_name", playerMainHand.getItem().getDefaultInstance().getHoverName().getString());
            JsonObject response = null;
            try {
                response = gson.fromJson(MCENetwork.request(request.toString()), JsonObject.class);
            } catch (IOException | InterruptedException e) {
                context.getSource().sendFailure(Component.literal("Error in Transaction, send to Mark or just read it ya goober: " + e.toString()));
                return 0;
            }
            if (response.get("response").getAsString().equals("accept") && response.get("error").getAsString().equals("None")) {
                player.addItem(
                        Resource_Token.returnSelf(playerMainHand.getItem(), response.getAsJsonObject("dat").get("resource_id").getAsString(), count));
                playerMainHand.shrink(count);

                JsonObject finalResponse = response;
                context.getSource().sendSuccess(() -> Component.literal(finalResponse.toString()), true);

                return 1;
            } else {
                context.getSource().sendFailure(Component.literal("Error in Transaction, send to Mark or just read it ya goober: " + response.toString()));
                return 0;
            }

        } else {
            context.getSource().sendFailure(Component.literal("Insufficent/Invalid Items for Transaction"));
            return 0;
        }
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        Gson gson = new Gson();
        JsonObject request = gson.fromJson(MCERequests.MINT.requestTemplate, JsonObject.class);
        int count = IntegerArgumentType.getInteger(context, "amount");
        ItemStack playerMainHand = player.getMainHandItem();
        Item pmhI = playerMainHand.getItem().asItem();
        if (!playerMainHand.isEmpty() && count <= playerMainHand.getCount() &&
                (pmhI != MCE_Items.CHEQUE.asItem() && pmhI != MCE_Items.COIN_TOKEN.asItem() && pmhI != MCE_Items.RESOURCE_TOKEN.asItem() && pmhI != MCE_Items.INVOICE.asItem())) {
            request.getAsJsonObject("dat").addProperty("amount", count);
            request.getAsJsonObject("dat").addProperty("item_id", playerMainHand.getItem().toString());
            request.getAsJsonObject("dat").addProperty("item_name", playerMainHand.getItem().getDefaultInstance().getHoverName().getString());
            JsonObject response = null;
            try {
                response = gson.fromJson(MCENetwork.request(request.toString()), JsonObject.class);
            } catch (IOException | InterruptedException e) {
                context.getSource().sendFailure(Component.literal("Error in Transaction, send to Mark or just read it ya goober: " + e.toString()));
                return 0;
            }
            if (response.get("response").getAsString().equals("accept") && response.get("error").getAsString().equals("None")) {
                playerMainHand.shrink(count);
                player.addItem(
                        Resource_Token.returnSelf(playerMainHand.getItem(), response.getAsJsonObject("dat").get("resource_id").getAsString(), count));


                JsonObject finalResponse = response;
                context.getSource().sendSuccess(() -> Component.literal(finalResponse.toString()), true);

                return 1;
            } else {
                context.getSource().sendFailure(Component.literal("Error in Transaction, send to Mark or just read it ya goober: " + response.toString()));
                return 0;
            }

        } else {
            context.getSource().sendFailure(Component.literal("Insufficent/Invalid Items for Transaction"));
            return 0;
        }
    }
}
