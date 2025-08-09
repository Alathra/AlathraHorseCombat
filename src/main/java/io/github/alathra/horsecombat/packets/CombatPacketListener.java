package io.github.alathra.horsecombat.packets;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAnimation;
import io.github.alathra.horsecombat.listener.HorseCombatListener;

public class CombatPacketListener implements PacketListener {

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        // Only handle animation packets
        if (event.getPacketType() != PacketType.Play.Client.ANIMATION) {
            return;
        }

        // Wrap the packet so we can read/modify
        WrapperPlayClientAnimation animation = new WrapperPlayClientAnimation(event);

        // Cancel/modify only if this player is in your tagged list
        //if (HorseCombatListener.getOffhandTaggedEntities().containsKey(event.getUser().getUUID())) {
            animation.setHand(InteractionHand.OFF_HAND);
        //}
    }

}
