package fuzs.puzzleslib.fabric.mixin.client;

import fuzs.puzzleslib.fabric.api.client.event.v1.FabricClientEvents;
import fuzs.puzzleslib.api.client.event.v1.RenderGuiElementEvents;
import fuzs.puzzleslib.api.event.v1.data.DefaultedInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(value = Gui.class, priority = 500)
abstract class GuiFabricMixin {
    @Shadow
    @Final
    private static ResourceLocation PUMPKIN_BLUR_LOCATION;
    @Shadow
    @Final
    private static ResourceLocation POWDER_SNOW_OUTLINE_LOCATION;
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private int screenWidth;
    @Shadow
    private int screenHeight;
    @Unique
    private float puzzleslib$partialTick;
    @Unique
    private boolean puzzleslib$interruptSpyglassOverlay;
    @Unique
    private boolean puzzleslib$interruptTextureOverlay;

    @Inject(method = "render", at = @At("HEAD"))
    public void render$0(GuiGraphics guiGraphics, float partialTick, CallbackInfo callback) {
        this.puzzleslib$partialTick = partialTick;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getDeltaFrameTime()F", shift = At.Shift.AFTER))
    public void render$1(GuiGraphics guiGraphics, float partialTick, CallbackInfo callback) {
        this.puzzleslib$interruptSpyglassOverlay = FabricClientEvents.beforeRenderGuiElement(RenderGuiElementEvents.SPYGLASS.id()).invoker().onBeforeRenderGuiElement(this.minecraft, guiGraphics, this.puzzleslib$partialTick, this.screenWidth, this.screenHeight).isInterrupt();
        this.puzzleslib$interruptTextureOverlay = FabricClientEvents.beforeRenderGuiElement(RenderGuiElementEvents.HELMET.id()).invoker().onBeforeRenderGuiElement(this.minecraft, guiGraphics, this.puzzleslib$partialTick, this.screenWidth, this.screenHeight).isInterrupt();
    }

    @Inject(method = "renderSpyglassOverlay", at = @At("HEAD"), cancellable = true)
    private void renderSpyglassOverlay$0(GuiGraphics guiGraphics, float f, CallbackInfo callback) {
        if (this.puzzleslib$interruptSpyglassOverlay) callback.cancel();
        this.puzzleslib$interruptSpyglassOverlay = false;
    }

    @Inject(method = "renderSpyglassOverlay", at = @At("TAIL"))
    private void renderSpyglassOverlay$1(GuiGraphics guiGraphics, float f, CallbackInfo callback) {
        FabricClientEvents.afterRenderGuiElement(RenderGuiElementEvents.SPYGLASS.id()).invoker().onAfterRenderGuiElement(this.minecraft, guiGraphics, this.puzzleslib$partialTick, this.screenWidth, this.screenHeight);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getTicksFrozen()I"))
    public void render$2(GuiGraphics guiGraphics, float partialTick, CallbackInfo callback) {
        this.puzzleslib$interruptTextureOverlay = FabricClientEvents.beforeRenderGuiElement(RenderGuiElementEvents.FROSTBITE.id()).invoker().onBeforeRenderGuiElement(this.minecraft, guiGraphics, this.puzzleslib$partialTick, this.screenWidth, this.screenHeight).isInterrupt();
    }

    @Inject(method = "renderTextureOverlay", at = @At("HEAD"), cancellable = true)
    private void renderTextureOverlay$0(GuiGraphics guiGraphics, ResourceLocation resourceLocation, float f, CallbackInfo callback) {
        if (this.puzzleslib$interruptTextureOverlay) callback.cancel();
        this.puzzleslib$interruptTextureOverlay = false;
    }

    @Inject(method = "renderTextureOverlay", at = @At("TAIL"))
    private void renderTextureOverlay$1(GuiGraphics guiGraphics, ResourceLocation resourceLocation, float f, CallbackInfo callback) {
        if (Objects.equals(resourceLocation, PUMPKIN_BLUR_LOCATION)) {
            FabricClientEvents.afterRenderGuiElement(RenderGuiElementEvents.HELMET.id()).invoker().onAfterRenderGuiElement(this.minecraft, guiGraphics, this.puzzleslib$partialTick, this.screenWidth, this.screenHeight);
        } else if (Objects.equals(resourceLocation, POWDER_SNOW_OUTLINE_LOCATION)) {
            FabricClientEvents.afterRenderGuiElement(RenderGuiElementEvents.FROSTBITE.id()).invoker().onAfterRenderGuiElement(this.minecraft, guiGraphics, this.puzzleslib$partialTick, this.screenWidth, this.screenHeight);
        }
    }

    @Inject(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;oSpinningEffectIntensity:F"))
    public void render$3(GuiGraphics guiGraphics, float partialTick, CallbackInfo callback) {
        this.puzzleslib$interruptTextureOverlay = FabricClientEvents.beforeRenderGuiElement(RenderGuiElementEvents.PORTAL.id()).invoker().onBeforeRenderGuiElement(this.minecraft, guiGraphics, this.puzzleslib$partialTick, this.screenWidth, this.screenHeight).isInterrupt();
    }

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    private void renderPortalOverlay$0(GuiGraphics guiGraphics, float f, CallbackInfo callback) {
        if (this.puzzleslib$interruptTextureOverlay) callback.cancel();
        this.puzzleslib$interruptTextureOverlay = false;
    }

    @Inject(method = "renderPortalOverlay", at = @At("TAIL"))
    private void renderPortalOverlay$1(GuiGraphics guiGraphics, float f, CallbackInfo callback) {
        FabricClientEvents.afterRenderGuiElement(RenderGuiElementEvents.PORTAL.id()).invoker().onAfterRenderGuiElement(this.minecraft, guiGraphics, this.puzzleslib$partialTick, this.screenWidth, this.screenHeight);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;render(Lnet/minecraft/client/gui/GuiGraphics;III)V"))
    public void render$4(GuiGraphics guiGraphics, float partialTick, CallbackInfo callback) {
        guiGraphics.pose().pushPose();
        DefaultedInt posX = DefaultedInt.fromValue(0);
        DefaultedInt posY = DefaultedInt.fromValue(this.screenHeight - 48);
        FabricClientEvents.CUSTOMIZE_CHAT_PANEL.invoker().onRenderChatPanel(this.minecraft.getWindow(), guiGraphics, partialTick, posX, posY);
        if (posX.getAsOptionalInt().isPresent() || posY.getAsOptionalInt().isPresent()) {
            guiGraphics.pose().translate(posX.getAsInt(), posY.getAsInt() - (this.screenHeight - 48), 0.0);
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;render(Lnet/minecraft/client/gui/GuiGraphics;III)V", shift = At.Shift.AFTER))
    public void render$5(GuiGraphics guiGraphics, float partialTick, CallbackInfo callback) {
        guiGraphics.pose().popPose();
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void renderHotbar$0(float partialTick, GuiGraphics guiGraphics, CallbackInfo callback) {
        if (FabricClientEvents.beforeRenderGuiElement(RenderGuiElementEvents.HOTBAR.id()).invoker().onBeforeRenderGuiElement(this.minecraft, guiGraphics, this.puzzleslib$partialTick, this.screenWidth, this.screenHeight).isInterrupt()) {
            callback.cancel();
        }
    }

    @Inject(method = "renderHotbar", at = @At("TAIL"))
    private void renderHotbar$1(float partialTick, GuiGraphics guiGraphics, CallbackInfo callback) {
        FabricClientEvents.afterRenderGuiElement(RenderGuiElementEvents.HOTBAR.id()).invoker().onAfterRenderGuiElement(this.minecraft, guiGraphics, this.puzzleslib$partialTick, this.screenWidth, this.screenHeight);
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void renderCrosshair$0(GuiGraphics guiGraphics, CallbackInfo callback) {
        if (FabricClientEvents.beforeRenderGuiElement(RenderGuiElementEvents.CROSSHAIR.id()).invoker().onBeforeRenderGuiElement(this.minecraft, guiGraphics, this.puzzleslib$partialTick, this.screenWidth, this.screenHeight).isInterrupt()) {
            callback.cancel();
        }
    }

    @Inject(method = "renderCrosshair", at = @At("TAIL"))
    private void renderCrosshair$1(GuiGraphics guiGraphics, CallbackInfo callback) {
        FabricClientEvents.afterRenderGuiElement(RenderGuiElementEvents.CROSSHAIR.id()).invoker().onAfterRenderGuiElement(this.minecraft, guiGraphics, this.puzzleslib$partialTick, this.screenWidth, this.screenHeight);
    }

    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    protected void renderEffects$0(GuiGraphics guiGraphics, CallbackInfo callback) {
        if (FabricClientEvents.beforeRenderGuiElement(RenderGuiElementEvents.POTION_ICONS.id()).invoker().onBeforeRenderGuiElement(this.minecraft, guiGraphics, this.puzzleslib$partialTick, this.screenWidth, this.screenHeight).isInterrupt()) {
            callback.cancel();
        }
    }

    @Inject(method = "renderEffects", at = @At("TAIL"))
    protected void renderEffects$1(GuiGraphics guiGraphics, CallbackInfo callback) {
        FabricClientEvents.afterRenderGuiElement(RenderGuiElementEvents.POTION_ICONS.id()).invoker().onAfterRenderGuiElement(this.minecraft, guiGraphics, this.puzzleslib$partialTick, this.screenWidth, this.screenHeight);
    }

    @Inject(method = "renderExperienceBar", at = @At(value = "HEAD"))
    public void renderExperienceBar$0(GuiGraphics guiGraphics, int xPos, CallbackInfo callback) {
        if (FabricClientEvents.beforeRenderGuiElement(RenderGuiElementEvents.EXPERIENCE_BAR.id()).invoker().onBeforeRenderGuiElement(this.minecraft, guiGraphics, this.puzzleslib$partialTick, this.screenWidth, this.screenHeight).isInterrupt()) {
            callback.cancel();
        }
    }

    @Inject(method = "renderExperienceBar", at = @At(value = "TAIL"))
    public void renderExperienceBar$1(GuiGraphics guiGraphics, int xPos, CallbackInfo callback) {
        FabricClientEvents.afterRenderGuiElement(RenderGuiElementEvents.EXPERIENCE_BAR.id()).invoker().onAfterRenderGuiElement(this.minecraft, guiGraphics, this.puzzleslib$partialTick, this.screenWidth, this.screenHeight);
    }

    @Inject(method = "renderSelectedItemName", at = @At("HEAD"), cancellable = true)
    public void renderSelectedItemName$0(GuiGraphics guiGraphics, CallbackInfo callback) {
        if (FabricClientEvents.beforeRenderGuiElement(RenderGuiElementEvents.ITEM_NAME.id()).invoker().onBeforeRenderGuiElement(this.minecraft, guiGraphics, this.puzzleslib$partialTick, this.screenWidth, this.screenHeight).isInterrupt()) {
            callback.cancel();
        }
    }

    @Inject(method = "renderSelectedItemName", at = @At("TAIL"))
    public void renderSelectedItemName$1(GuiGraphics guiGraphics, CallbackInfo callback) {
        FabricClientEvents.afterRenderGuiElement(RenderGuiElementEvents.ITEM_NAME.id()).invoker().onAfterRenderGuiElement(this.minecraft, guiGraphics, this.puzzleslib$partialTick, this.screenWidth, this.screenHeight);
    }
}
