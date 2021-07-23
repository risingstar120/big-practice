package net.roguelogix.biggerreactors.machine.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluids;
import net.roguelogix.biggerreactors.BiggerReactors;
import net.roguelogix.biggerreactors.machine.containers.CyaniteReprocessorContainer;
import net.roguelogix.biggerreactors.machine.state.CyaniteReprocessorState;
import net.roguelogix.biggerreactors.client.CommonRender;
import net.roguelogix.phosphophyllite.gui.client.RenderHelper;
import net.roguelogix.phosphophyllite.gui.client.ScreenBase;
import net.roguelogix.phosphophyllite.gui.client.elements.Symbol;
import net.roguelogix.phosphophyllite.gui.client.elements.Tooltip;

import javax.annotation.Nonnull;

public class CyaniteReprocessorScreen extends ScreenBase<CyaniteReprocessorContainer> {

    private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(BiggerReactors.modid, "textures/screen/cyanite_reprocessor.png");

    private CyaniteReprocessorState cyaniteReprocessorState;

    public CyaniteReprocessorScreen(CyaniteReprocessorContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title, DEFAULT_TEXTURE, 176, 175);

        // Initialize reprocessor state.
        cyaniteReprocessorState = (CyaniteReprocessorState) this.getMenu().getGuiPacket();
    }

    /**
     * Initialize the screen.
     */
    @Override
    public void init() {
        super.init();

        // Set title to be drawn in the center.
        this.titleLabelX = (this.getWidth() / 2) - (this.getFont().width(this.getTitle()) / 2);

        // Initialize tooltips:
        this.initTooltips();

        // Initialize controls:

        // Initialize gauges:
        this.initGauges();

        // Initialize symbols:
        this.initSymbols();
    }

    /**
     * Initialize tooltips.
     */
    public void initTooltips() {
        // (Left) Internal battery:
        this.addElement(new Tooltip<>(this, 8, 6, 16, 16, new TranslatableComponent("screen.biggerreactors.cyanite_reprocessor.internal_battery.tooltip")));
    }

    /**
     * Initialize gauges.
     */
    public void initGauges() {
        // (Top) Internal battery:
        Symbol<CyaniteReprocessorContainer> internalBattery = new Symbol<>(this, 7, 25, 18, 64, 0, 152, TextComponent.EMPTY);
        internalBattery.onRender = (@Nonnull PoseStack mS, int mX, int mY) -> CommonRender.renderEnergyGauge(mS,
                internalBattery, cyaniteReprocessorState.energyStored, cyaniteReprocessorState.energyCapacity);
        this.addElement(internalBattery);

        // (Top) Water tank:
        Symbol<CyaniteReprocessorContainer> waterTank = new Symbol<>(this, 151, 25, 18, 64, 0, 152, TextComponent.EMPTY);
        waterTank.onRender = (@Nonnull PoseStack mS, int mX, int mY) -> CommonRender.renderFluidGauge(mS,
                waterTank, cyaniteReprocessorState.waterStored, cyaniteReprocessorState.waterCapacity,
                Fluids.WATER.getSource());
        this.addElement(waterTank);

        // (Center) Progress bar:
        Symbol<CyaniteReprocessorContainer> progressBar = new Symbol<>(this, 75, 40, 24, 18, 0, 175, null);
        progressBar.onRender = (@Nonnull PoseStack mS, int mX, int mY) -> CyaniteReprocessorScreen.renderProgressBar(mS,
                progressBar, cyaniteReprocessorState.workTime, cyaniteReprocessorState.workTimeTotal);
        this.addElement(progressBar);
    }

    /**
     * Initialize symbols.
     */
    public void initSymbols() {
        // (Right) Water tank symbol:
        Symbol<CyaniteReprocessorContainer> waterTankSymbol = new Symbol<>(this, 152, 6, 16, 16, 48, 175, new TranslatableComponent("screen.biggerreactors.cyanite_reprocessor.water_tank.tooltip"));
        waterTankSymbol.onRender = (@Nonnull PoseStack mS, int mX, int mY) -> RenderHelper.drawMaskedFluid(mS,
                waterTankSymbol.x, waterTankSymbol.y, this.getBlitOffset(),
                waterTankSymbol.width, waterTankSymbol.height,
                waterTankSymbol.u, waterTankSymbol.v, Fluids.WATER.getSource());
        this.addElement(waterTankSymbol);
    }

    /**
     * Render the progress bar.
     *
     * @param mStack        The current matrix stack.
     * @param symbol        The symbol to draw as.
     * @param workTime      The time the machine has been working.
     * @param workTimeTotal The total time needed for completion.
     */
    private static void renderProgressBar(@Nonnull PoseStack mStack, @Nonnull Symbol<CyaniteReprocessorContainer> symbol, int workTime, int workTimeTotal) {
        // If there's no progress, there's no need to draw.
        if ((workTime > 0) && (workTimeTotal > 0)) {
            // Calculate how much needs to be rendered.
            int renderSize = (int) ((symbol.width * workTime) / workTimeTotal);
            // Render progress.
            symbol.blit(mStack, renderSize, symbol.height, symbol.u + 24, symbol.v);
        }
    }
}
