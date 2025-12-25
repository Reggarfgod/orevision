package com.reggarf.mods.orevision.keybinds;

import com.reggarf.mods.orevision.screen.OreScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class Keybinds {

    //CATEGORY
    private static final String CATEGORY = "key.categories.orevision";

    public static KeyMapping TOGGLE;
    public static KeyMapping OPEN;

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent e) {

        TOGGLE = new KeyMapping("key.orevision.toggle", GLFW.GLFW_KEY_O, CATEGORY);
        e.register(TOGGLE);

        OPEN = new KeyMapping("key.orevision.open", GLFW.GLFW_KEY_I, CATEGORY);
        e.register(OPEN);
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post e) {
        if (OPEN.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new OreScreen(mc.screen));
        }
    }
}
