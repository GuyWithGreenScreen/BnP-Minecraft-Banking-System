package dev.gwgs.minecrafteconomy.commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.gwgs.minecrafteconomy.items.Coin_Token;
import dev.gwgs.minecrafteconomy.items.MCEDataComponentTypes;
import dev.gwgs.minecrafteconomy.items.Resource_Token;
import dev.gwgs.minecrafteconomy.networking.MCENetwork;
import dev.gwgs.minecrafteconomy.networking.MCERequests;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.IOException;

public class MCE_WithdrawCommand {
    public MCE_WithdrawCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("withdraw")
                .then(Commands.literal("coin")
                    .then(Commands.argument("coin_id", StringArgumentType.word())
                            .then(Commands.argument("amount", IntegerArgumentType.integer(1, 1024))
                                    .then(Commands.argument("user_id", StringArgumentType.word())
                                            .then(Commands.argument("password", StringArgumentType.string()).executes(this::executeCoin))))))
                .then(Commands.literal("resource")
                        .then(Commands.argument("resource_id", StringArgumentType.word())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 1024))
                                        .then(Commands.argument("user_id", StringArgumentType.word())
                                                .then(Commands.argument("password", StringArgumentType.string()).executes(this::executeResource)))))));
    }


    private int executeCoin(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        Gson gson = new Gson();
        JsonObject request = gson.fromJson(MCERequests.WITHDRAW.requestTemplate, JsonObject.class);
        int count = IntegerArgumentType.getInteger(context, "amount");
        String user_id = StringArgumentType.getString(context, "user_id").toUpperCase();
        String currency_id = StringArgumentType.getString(context, "coin_id").toUpperCase();
        String password = StringArgumentType.getString(context, "password");
        if (user_id.length() != 4) { context.getSource().sendFailure(Component.literal("Invalid User ID")); return 0;}
        if (currency_id.length() != 4) { context.getSource().sendFailure(Component.literal("Invalid Currency ID")); return 0;}
        request.getAsJsonObject("dat").addProperty("amount", count);
        request.getAsJsonObject("dat").addProperty("currency", 1);
        request.getAsJsonObject("dat").addProperty("currency_id", currency_id);
        request.getAsJsonObject("dat").addProperty("user_id", user_id);
        request.getAsJsonObject("dat").addProperty("password", password);
        JsonObject response = null;
        try {
            response = gson.fromJson(MCENetwork.request(request.toString()), JsonObject.class);
        } catch (IOException | InterruptedException e) {
            context.getSource().sendFailure(Component.literal("Error in Transaction, send to Mark or just read it ya goober: " + e.toString()));
            return 0;
        }
        if (response.get("response").getAsString().equals("accept") && response.get("error").getAsString().equals("None")) {

            player.addItem(Coin_Token.returnSelf(response.get("dat").getAsJsonObject().get("name").getAsString(), currency_id, count));

            JsonObject finalResponse = response;
            context.getSource().sendSuccess(() -> Component.literal(finalResponse.toString()), true);

            return 1;
        } else {
            context.getSource().sendFailure(Component.literal("Error in Transaction, send to Mark or just read it ya goober: " + response.toString()));
            return 0;
        }

    }

    private int executeResource(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        Gson gson = new Gson();
        JsonObject request = gson.fromJson(MCERequests.WITHDRAW.requestTemplate, JsonObject.class);
        int count = IntegerArgumentType.getInteger(context, "amount");
        String user_id = StringArgumentType.getString(context, "user_id").toUpperCase();
        String currency_id = StringArgumentType.getString(context, "resource_id").toUpperCase();
        String password = StringArgumentType.getString(context, "password");
        if (user_id.length() != 4) { context.getSource().sendFailure(Component.literal("Invalid User ID")); return 0;}
        if (currency_id.length() != 4) { context.getSource().sendFailure(Component.literal("Invalid Currency ID")); return 0;}
        request.getAsJsonObject("dat").addProperty("amount", count);
        request.getAsJsonObject("dat").addProperty("currency", 2);
        request.getAsJsonObject("dat").addProperty("currency_id", currency_id);
        request.getAsJsonObject("dat").addProperty("user_id", user_id);
        request.getAsJsonObject("dat").addProperty("password", password);
        JsonObject response = null;
        try {
            response = gson.fromJson(MCENetwork.request(request.toString()), JsonObject.class);
        } catch (IOException | InterruptedException e) {
            context.getSource().sendFailure(Component.literal("Error in Transaction, send to Mark or just read it ya goober: " + e.toString()));
            return 0;
        }
        if (response.get("response").getAsString().equals("accept") && response.get("error").getAsString().equals("None")) {


            Item parsedItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(response.get("dat").getAsJsonObject().get("name").getAsString()));
            if (parsedItem.asItem() == Items.AIR) {
                context.getSource().sendFailure(Component.literal("FATAL ERROR! RESOURCE TOKEN ITEM ID INVALID! PLEASE LET MARK KNOW!!!!"));
                return 0;
            }
            player.addItem(Resource_Token.returnSelf(parsedItem, currency_id, count));


            JsonObject finalResponse = response;
            context.getSource().sendSuccess(() -> Component.literal(finalResponse.toString()), true);

            return 1;
        } else {
            context.getSource().sendFailure(Component.literal("Error in Transaction, send to Mark or just read it ya goober: " + response.toString()));
            return 0;
        }

    }
}
