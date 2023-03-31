package fuzs.puzzleslib.api.client.event.v1;

import com.google.common.collect.Maps;
import fuzs.puzzleslib.api.event.v1.core.FabricEventFactory;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Map;
import java.util.Objects;

/**
 * Events originally found on Forge in the <code>net.minecraftforge.client.event</code> package.
 */
public final class FabricClientEvents {
    /**
     * Fires before the name tag of an entity is tried to be rendered, in addition to preventing the name tag from rendering, rendering can also be forced.
     */
    public static final Event<RenderNameTagCallback> RENDER_NAME_TAG = FabricEventFactory.createResult(RenderNameTagCallback.class);
    /**
     * Called when computing the field of view modifier on the client, mostly depending on {@link Attributes#MOVEMENT_SPEED},
     * but also changes for certain actions such as when drawing a bow.
     */
    public static final Event<ComputeFovModifierCallback> COMPUTE_FOV_MODIFIER = FabricEventFactory.createResult(ComputeFovModifierCallback.class);
    private static final Map<String, Event<RenderGuiElementEvents.Before>> BEFORE_RENDER_GUI_ELEMENT_EVENTS = Maps.newIdentityHashMap();
    private static final Map<String, Event<RenderGuiElementEvents.After>> AFTER_RENDER_GUI_ELEMENT_EVENTS = Maps.newIdentityHashMap();

    /**
     * Called before a gui element is rendered, allows for cancelling rendering.
     *
     * @param id id of the gui element, all vanilla ids can be found in {@link RenderGuiElementEvents}
     * @return the event instance
     */
    public static Event<RenderGuiElementEvents.Before> beforeRenderGuiElement(ResourceLocation id) {
        Objects.requireNonNull(id, "id is null");
        return BEFORE_RENDER_GUI_ELEMENT_EVENTS.computeIfAbsent(id.toString().intern(), $ -> FabricEventFactory.createResult(RenderGuiElementEvents.Before.class));
    }

    /**
     * Called after a gui element is rendered.
     *
     * @param id id of the gui element, all vanilla ids can be found in {@link RenderGuiElementEvents}
     * @return the event instance
     */
    public static Event<RenderGuiElementEvents.After> afterRenderGuiElement(ResourceLocation id) {
        Objects.requireNonNull(id, "id is null");
        return AFTER_RENDER_GUI_ELEMENT_EVENTS.computeIfAbsent(id.toString().intern(), $ -> FabricEventFactory.create(RenderGuiElementEvents.After.class));
    }
}
