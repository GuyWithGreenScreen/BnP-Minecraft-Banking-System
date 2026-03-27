package dev.gwgs.minecrafteconomy.commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.gwgs.minecrafteconomy.items.MCEDataComponentTypes;
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

public class MCE_DepositCommand {
    public MCE_DepositCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("deposit").then(Commands.argument("user_id", StringArgumentType.word()).executes(this::executeAll)).then(Commands.argument("amount", IntegerArgumentType.integer(1, 64)).then(Commands.argument("user_id", StringArgumentType.word()).executes(this::execute))));
    }

    private int executeAll(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        Gson gson = new Gson();
        ItemStack playerMainHand = player.getMainHandItem();
        Item pmhI = playerMainHand.getItem().asItem();
        JsonObject request = gson.fromJson(MCERequests.DEPOSIT.requestTemplate, JsonObject.class);
        int count = playerMainHand.getCount();
        String user_id = StringArgumentType.getString(context, "user_id").toUpperCase();
        if (user_id.length() != 4) {
            context.getSource().sendFailure(Component.literal("Invalid User ID"));
            return 0;
        }
        if (pmhI == MCE_Items.RESOURCE_TOKEN.asItem() || pmhI == MCE_Items.COIN_TOKEN.asItem()) {
            if ((!playerMainHand.isEmpty() && count <= playerMainHand.getCount())) {
                int currency = 1;
                if (pmhI == MCE_Items.RESOURCE_TOKEN.asItem()) currency = 2;
                request.getAsJsonObject("dat").addProperty("amount", count);
                request.getAsJsonObject("dat").addProperty("currency", currency);
                request.getAsJsonObject("dat").addProperty("currency_id", playerMainHand.get(MCEDataComponentTypes.CURRENCY_ID));
                request.getAsJsonObject("dat").addProperty("user_id", user_id);
                JsonObject response = null;
                try {
                    response = gson.fromJson(MCENetwork.request(request.toString()), JsonObject.class);
                } catch (IOException | InterruptedException e) {
                    context.getSource().sendFailure(Component.literal("Error in Transaction, send to Mark or just read it ya goober: " + e.toString()));
                    return 0;
                }
                if (response.get("response").getAsString().equals("accept") && response.get("error").getAsString().equals("None")) {
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
        } else {
            context.getSource().sendFailure(Component.literal("Invalid Items for Transaction"));
            return 0;
        }
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        Gson gson = new Gson();
        JsonObject request = gson.fromJson(MCERequests.DEPOSIT.requestTemplate, JsonObject.class);
        int count = IntegerArgumentType.getInteger(context, "amount");
        String user_id = StringArgumentType.getString(context, "user_id").toUpperCase();
        if (user_id.length() != 4) {
            context.getSource().sendFailure(Component.literal("Invalid User ID"));
            return 0;
        }
        ItemStack playerMainHand = player.getMainHandItem();
        Item pmhI = playerMainHand.getItem().asItem();
        if (pmhI == MCE_Items.RESOURCE_TOKEN.asItem() || pmhI == MCE_Items.COIN_TOKEN.asItem()) {
            if ((!playerMainHand.isEmpty() && count <= playerMainHand.getCount())) {
                int currency = 1;
                if (pmhI == MCE_Items.RESOURCE_TOKEN.asItem()) currency = 2;
                request.getAsJsonObject("dat").addProperty("amount", count);
                request.getAsJsonObject("dat").addProperty("currency", currency);
                request.getAsJsonObject("dat").addProperty("currency_id", playerMainHand.get(MCEDataComponentTypes.CURRENCY_ID));
                request.getAsJsonObject("dat").addProperty("user_id", user_id);
                JsonObject response = null;
                try {
                    response = gson.fromJson(MCENetwork.request(request.toString()), JsonObject.class);
                } catch (IOException | InterruptedException e) {
                    context.getSource().sendFailure(Component.literal("Error in Transaction, send to Mark or just read it ya goober: " + e.toString()));
                    return 0;
                }
                if (response.get("response").getAsString().equals("accept") && response.get("error").getAsString().equals("None")) {
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
        } else {
            context.getSource().sendFailure(Component.literal("Invalid Items for Transaction"));
            return 0;
        }
    }
}
