package dev.gwgs.minecrafteconomy.commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.gwgs.minecrafteconomy.items.Cheque;
import dev.gwgs.minecrafteconomy.items.Invoice;
import dev.gwgs.minecrafteconomy.networking.MCENetwork;
import dev.gwgs.minecrafteconomy.networking.MCERequests;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;

public class MCE_Create_InvoiceCommand {
    public MCE_Create_InvoiceCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("create_invoice")
                .then(Commands.literal("coin")
                        .then(Commands.argument("coin_id", StringArgumentType.word())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 128000))
                                        .then(Commands.argument("your_user_id", StringArgumentType.word())
                                                .then(Commands.argument("your_password", StringArgumentType.string())
                                                        .then(Commands.argument("issued_to_user_id", StringArgumentType.word()).executes(this::executeCoin)))))))
                .then(Commands.literal("resource")
                        .then(Commands.argument("resource_id", StringArgumentType.word())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 128000))
                                        .then(Commands.argument("your_user_id", StringArgumentType.word())
                                                .then(Commands.argument("your_password", StringArgumentType.string())
                                                        .then(Commands.argument("issued_to_user_id", StringArgumentType.word()).executes(this::executeResource))))))));

    }

    private int executeCoin(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        Gson gson = new Gson();
        JsonObject request = gson.fromJson(MCERequests.CREATE_INVOICE.requestTemplate, JsonObject.class);
        int count = IntegerArgumentType.getInteger(context, "amount");
        String from_user_id = StringArgumentType.getString(context, "your_user_id").toUpperCase();
        String from_user_password = StringArgumentType.getString(context, "your_password");
        String to_user_id = StringArgumentType.getString(context, "issued_to_user_id").toUpperCase();
        String currency_id = StringArgumentType.getString(context, "coin_id").toUpperCase();
        if (from_user_id.length() != 4 || to_user_id.length() != 4) { context.getSource().sendFailure(Component.literal("Invalid User ID(s)")); return 0;}
        if (currency_id.length() != 4) { context.getSource().sendFailure(Component.literal("Invalid Coin ID")); return 0;}
        request.getAsJsonObject("dat").addProperty("currency", 1);
        request.getAsJsonObject("dat").addProperty("currency_id", currency_id);
        request.getAsJsonObject("dat").addProperty("amount", count);
        request.getAsJsonObject("dat").addProperty("from_user_id", from_user_id);
        request.getAsJsonObject("dat").addProperty("from_user_password", from_user_password);
        request.getAsJsonObject("dat").addProperty("recipient_user_id", to_user_id);
        JsonObject response;
        try {
            response = gson.fromJson(MCENetwork.request(request.toString()), JsonObject.class);
        } catch (IOException | InterruptedException e) {
            context.getSource().sendFailure(Component.literal("Error in Transaction, send to Mark or just read it ya goober: " + e.toString()));
            return 0;
        }
        if (response.get("response").getAsString().equals("accept") && response.get("error").getAsString().equals("None")) {

            player.addItem(Invoice.returnSelf(response.get("dat").getAsJsonObject().get("id").getAsString(),
                    1,
                    response.get("dat").getAsJsonObject().get("currency_name").getAsString(),
                    currency_id,
                    from_user_id,
                    to_user_id,
                    response.get("dat").getAsJsonObject().get("from_user_name").getAsString(),
                    response.get("dat").getAsJsonObject().get("to_user_name").getAsString(),
                    count));

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
        JsonObject request = gson.fromJson(MCERequests.CREATE_INVOICE.requestTemplate, JsonObject.class);
        int count = IntegerArgumentType.getInteger(context, "amount");
        String from_user_id = StringArgumentType.getString(context, "your_user_id").toUpperCase();
        String from_user_password = StringArgumentType.getString(context, "your_password");
        String to_user_id = StringArgumentType.getString(context, "issued_to_user_id").toUpperCase();
        String currency_id = StringArgumentType.getString(context, "resource_id").toUpperCase();
        if (from_user_id.length() != 4 || to_user_id.length() != 4) { context.getSource().sendFailure(Component.literal("Invalid User ID(s)")); return 0;}
        if (currency_id.length() != 4) { context.getSource().sendFailure(Component.literal("Invalid Resource ID")); return 0;}
        request.getAsJsonObject("dat").addProperty("currency", 2);
        request.getAsJsonObject("dat").addProperty("currency_id", currency_id);
        request.getAsJsonObject("dat").addProperty("amount", count);
        request.getAsJsonObject("dat").addProperty("from_user_id", from_user_id);
        request.getAsJsonObject("dat").addProperty("from_user_password", from_user_password);
        request.getAsJsonObject("dat").addProperty("recipient_user_id", to_user_id);
        JsonObject response;
        try {
            response = gson.fromJson(MCENetwork.request(request.toString()), JsonObject.class);
        } catch (IOException | InterruptedException e) {
            context.getSource().sendFailure(Component.literal("Error in Transaction, send to Mark or just read it ya goober: " + e.toString()));
            return 0;
        }
        if (response.get("response").getAsString().equals("accept") && response.get("error").getAsString().equals("None")) {

            player.addItem(Invoice.returnSelf(response.get("dat").getAsJsonObject().get("id").getAsString(),
                    2,
                    response.get("dat").getAsJsonObject().get("currency_name").getAsString(),
                    currency_id,
                    from_user_id,
                    to_user_id,
                    response.get("dat").getAsJsonObject().get("from_user_name").getAsString(),
                    response.get("dat").getAsJsonObject().get("to_user_name").getAsString(),
                    count));

            JsonObject finalResponse = response;
            context.getSource().sendSuccess(() -> Component.literal(finalResponse.toString()), true);

            return 1;
        } else {
            context.getSource().sendFailure(Component.literal("Error in Transaction, send to Mark or just read it ya goober: " + response.toString()));
            return 0;
        }
    }
}
