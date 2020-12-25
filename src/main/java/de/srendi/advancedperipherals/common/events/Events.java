package de.srendi.advancedperipherals.common.events;

import de.srendi.advancedperipherals.AdvancedPeripherals;
import de.srendi.advancedperipherals.common.addons.computercraft.CCEventManager;
import de.srendi.advancedperipherals.common.blocks.tileentity.ChatBoxTileEntity;
import de.srendi.advancedperipherals.common.blocks.tileentity.TileEntityList;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = AdvancedPeripherals.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Events {

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        Minecraft.getInstance().player.sendMessage(new StringTextComponent(TileEntityList.get().getBlockPositions() + ""), UUID.randomUUID());
        for(TileEntity tileEntity : TileEntityList.get().getTileEntitys(event.getPlayer().getServerWorld())) {
            if(tileEntity instanceof ChatBoxTileEntity) {
                CCEventManager.getInstance().sendEvent(tileEntity, "chatEvent", event.getUsername(), event.getMessage());
            }
        }
    }
}