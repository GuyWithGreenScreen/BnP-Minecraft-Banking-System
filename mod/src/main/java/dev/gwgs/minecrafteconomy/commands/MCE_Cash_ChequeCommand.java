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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.IOException;

public class MCE_Cash_ChequeCommand {
    public MCE_Cash_ChequeCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cash_cheque")
                .then(Commands.argument("user_id", StringArgumentType.word()).then(Commands.argument("password", StringArgumentType.string()).executes(this::execute))));
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        Gson gson = new Gson();
        JsonObject request = gson.fromJson(MCERequests.CASH_CHEQUE.requestTemplate, JsonObject.class);
        String user_id = StringArgumentType.getString(context, "user_id").toUpperCase();
        String password = StringArgumentType.getString(context, "password");
        ItemStack playerMainHand = player.getMainHandItem();
        Item pmhI = playerMainHand.getItem().asItem();

        if (pmhI.asItem() != MCE_Items.CHEQUE.asItem()) {
            context.getSource().sendFailure(Component.literal("Main hand Item is not a cheque"));
            return 0;
        }

        if (user_id.length() != 4) { context.getSource().sendFailure(Component.literal("Invalid User ID")); return 0;}
        request.getAsJsonObject("dat").addProperty("cheque_id", playerMainHand.get(MCEDataComponentTypes.CHEQUE_INVOICE_ID));
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


            playerMainHand.shrink(1);

            JsonObject finalResponse = response;
            context.getSource().sendSuccess(() -> Component.literal(finalResponse.toString()), true);

            return 1;
        } else {
            context.getSource().sendFailure(Component.literal("Error in Transaction, send to Mark or just read it ya goober: " + response.toString()));
            return 0;
        }

    }
}
