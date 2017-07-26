package com.elytradev.teckle.common.network.messages;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import com.elytradev.teckle.common.network.TeckleNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Objects;

@ReceivedOn(Side.CLIENT)
public class DebugReceiverMessage extends Message {

    public static boolean active;
    @MarshalledAs("string")
    public String message = "";

    public DebugReceiverMessage(NetworkContext ctx) {
        super(ctx);
    }

    public DebugReceiverMessage(String message) {
        this(TeckleNetworking.NETWORK);
        this.message = message;
    }

    @Override
    protected void handle(EntityPlayer receiver) {
        if (receiver != null && Objects.equals(receiver.getGameProfile().getName(), "darkevilmac") && active) {
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
        }
    }
}
