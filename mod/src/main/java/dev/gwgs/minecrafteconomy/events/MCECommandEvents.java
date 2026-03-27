package dev.gwgs.minecrafteconomy.events;

import dev.gwgs.minecrafteconomy.Minecrafteconomy;
import dev.gwgs.minecrafteconomy.commands.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.server.command.ConfigCommand;

@EventBusSubscriber(modid = Minecrafteconomy.MODID)
public class MCECommandEvents {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        new MCE_MintCommand(event.getDispatcher());
        new MCE_UnmintCommand(event.getDispatcher());
        new MCE_DepositCommand(event.getDispatcher());
        new MCE_WithdrawCommand(event.getDispatcher());
        new MCE_Create_ChequeCommand(event.getDispatcher());
        new MCE_Create_InvoiceCommand(event.getDispatcher());
        new MCE_Cash_ChequeCommand(event.getDispatcher());
        new MCE_Pay_InvoiceCommand(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }
}
