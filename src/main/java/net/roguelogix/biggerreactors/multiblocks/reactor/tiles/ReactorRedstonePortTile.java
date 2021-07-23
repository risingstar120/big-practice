package net.roguelogix.biggerreactors.multiblocks.reactor.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fmllegacy.network.NetworkHooks;
import net.roguelogix.biggerreactors.multiblocks.reactor.blocks.ReactorRedstonePort;
import net.roguelogix.biggerreactors.multiblocks.reactor.containers.ReactorRedstonePortContainer;
import net.roguelogix.biggerreactors.multiblocks.reactor.state.ReactorActivity;
import net.roguelogix.biggerreactors.multiblocks.reactor.state.ReactorRedstonePortSelection;
import net.roguelogix.biggerreactors.multiblocks.reactor.state.ReactorRedstonePortState;
import net.roguelogix.biggerreactors.multiblocks.reactor.state.ReactorRedstonePortTriggers;
import net.roguelogix.phosphophyllite.gui.client.api.IHasUpdatableState;
import net.roguelogix.phosphophyllite.multiblock.generic.*;
import net.roguelogix.phosphophyllite.registry.RegisterTileEntity;
import net.roguelogix.phosphophyllite.registry.TileSupplier;
import net.roguelogix.phosphophyllite.util.BlockStates;
import org.apache.logging.log4j.core.jmx.Server;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@RegisterTileEntity(name = "reactor_redstone_port")
public class ReactorRedstonePortTile extends ReactorBaseTile implements MenuProvider, ITickableMultiblockTile, IHasUpdatableState<ReactorRedstonePortState>, IOnAssemblyTile, IOnDisassemblyTile {

    @RegisterTileEntity.Type
    public static BlockEntityType<?> TYPE;
    
    @RegisterTileEntity.Supplier
    public static final TileSupplier SUPPLIER = ReactorRedstonePortTile::new;
    
    public final ReactorRedstonePortState reactorRedstonePortState = new ReactorRedstonePortState(this);

    public ReactorRedstonePortTile(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    private boolean isEmitting;
    double mainVal = 0;
    double secondaryVal = 0;
    Direction powerOutputDirection = null;

    public boolean isEmitting(Direction side) {
        if (side.getOpposite() != powerOutputDirection) {
            return false;
        }
        return isEmitting;
    }

    private boolean isPowered = false;
    private boolean wasPowered = false;

    public void updatePowered() {
        if (powerOutputDirection == null) {
            return;
        }
        assert level != null;
        isPowered = level.hasSignal(worldPosition.relative(powerOutputDirection), powerOutputDirection);
    }

    private boolean isLit = false;

    @Override
    public void tick() {
        boolean shouldBeEmitting = false;
        boolean shouldLight = false;
        switch (reactorRedstonePortState.selectedTab) {
            case INPUT_ACTIVITY:
                shouldLight = isPowered;
                if (reactorRedstonePortState.triggerPS.toBool()) {
                    // signal
                    if (wasPowered != isPowered) {
                        controller.setActive(isPowered ? ReactorActivity.ACTIVE : ReactorActivity.INACTIVE);
                    }
                } else if (!wasPowered && isPowered) {
                    // not signal, so, pulse
                    controller.toggleActive();
                }
                break;
            case INPUT_CONTROL_ROD_INSERTION: {
                shouldLight = isPowered;
                if (reactorRedstonePortState.triggerPS.toBool()) {
                    if (wasPowered == isPowered) {
                        break;
                    }
                    if (isPowered) {
                        controller.setAllControlRodLevels(mainVal);
                    } else {
                        controller.setAllControlRodLevels(secondaryVal);
                    }
                } else {
                    if (!wasPowered && isPowered) {
                        switch (reactorRedstonePortState.triggerMode) {
                            case 0: {
                                controller.setAllControlRodLevels(controller.controlRodLevel(0) + mainVal);
                                break;
                            }
                            case 1: {
                                controller.setAllControlRodLevels(controller.controlRodLevel(0) - mainVal);
                                break;
                            }
                            case 2: {
                                controller.setAllControlRodLevels(mainVal);
                                break;
                            }
                            default:
                                break;
                        }
                    }
                }
            }
            break;
            case INPUT_EJECT_WASTE: {
                shouldLight = isPowered;
                if (!wasPowered && isPowered) {
                    controller.ejectWaste();
                }
                break;
            }
            case OUTPUT_FUEL_TEMP: {
                double fuelTemp = controller.simulation().fuelHeat();
                if ((fuelTemp < mainVal) == reactorRedstonePortState.triggerAB.toBool()) {
                    shouldBeEmitting = true;
                }
            }
            break;
            case OUTPUT_CASING_TEMP: {
                double casingTemperature = controller.simulation().caseHeat();
                if ((casingTemperature < mainVal) == reactorRedstonePortState.triggerAB.toBool()) {
                    shouldBeEmitting = true;
                }
            }
            break;
            case OUTPUT_FUEL_ENRICHMENT: {
                double fuelPercent = controller.simulation().fuelTank().fuel();
                fuelPercent /= controller.simulation().fuelTank().totalStored();
                fuelPercent *= 100;
                if ((fuelPercent < mainVal) == reactorRedstonePortState.triggerAB.toBool()) {
                    shouldBeEmitting = true;
                }
            }
            break;
            case OUTPUT_FUEL_AMOUNT: {
                double fuelAmount = controller.simulation().fuelTank().fuel();
                if ((fuelAmount < mainVal) == reactorRedstonePortState.triggerAB.toBool()) {
                    shouldBeEmitting = true;
                }
            }
            break;
            case OUTPUT_WASTE_AMOUNT: {
                double wasteAmount = controller.simulation().fuelTank().waste();
                if ((wasteAmount < mainVal) == reactorRedstonePortState.triggerAB.toBool()) {
                    shouldBeEmitting = true;
                }
            }
            break;
            case OUTPUT_ENERGY_AMOUNT: {
                double energyAmount = controller.simulation().battery().stored();
                energyAmount /= (double) controller.simulation().battery().capacity();
                energyAmount *= 100;
                if ((energyAmount < mainVal) == reactorRedstonePortState.triggerAB.toBool()) {
                    shouldBeEmitting = true;
                }
            }
            break;
        }
        shouldLight |= shouldBeEmitting;
        if (shouldBeEmitting != isEmitting || wasPowered != isPowered) {
            isEmitting = shouldBeEmitting;
            wasPowered = isPowered;
            assert level != null;
            BlockPos updatePos = worldPosition.relative(powerOutputDirection);
            level.blockUpdated(this.getBlockPos(), this.getBlockState().getBlock());
            level.blockUpdated(updatePos, level.getBlockState(updatePos).getBlock());
        }
        if (isLit != shouldLight) {
            isLit = shouldLight;
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(ReactorRedstonePort.IS_LIT_BOOLEAN_PROPERTY, isLit));
        }
        // TODO: 7/22/21 only mark changed when it actually changed 
        this.setChanged();
    }

    @Override
    @Nonnull
    public InteractionResult onBlockActivated(@Nonnull Player player, @Nonnull InteractionHand handIn) {
        assert level != null;
        if (level.getBlockState(worldPosition).getValue(MultiblockBlock.ASSEMBLED)) {
            if (!level.isClientSide) {
                NetworkHooks.openGui((ServerPlayer) player, this, this.getBlockPos());
            }
            return InteractionResult.SUCCESS;
        }
        return super.onBlockActivated(player, handIn);
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent(ReactorRedstonePort.INSTANCE.getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @Nonnull Inventory playerInventory, @Nonnull Player player) {
        return new ReactorRedstonePortContainer(windowId, this.worldPosition, player);
    }

    // Current changes/non-active. See reactorRedstonePortState to see what's actually being used for operations.
    private final ReactorRedstonePortState currentChanges = new ReactorRedstonePortState(this);

    public ReactorRedstonePortState getCurrentChanges() {
        return this.currentChanges;
    }

    @Nonnull
    @Override
    public ReactorRedstonePortState getState() {
        this.updateState();
        return this.reactorRedstonePortState;
    }

    @Override
    public void updateState() {
        // Update committed/active values.
    }

    public void applyChanges() {
        this.reactorRedstonePortState.selectedTab = this.currentChanges.selectedTab;
        this.reactorRedstonePortState.triggerPS = this.currentChanges.triggerPS;
        this.reactorRedstonePortState.triggerAB = this.currentChanges.triggerAB;
        this.reactorRedstonePortState.triggerMode = this.currentChanges.triggerMode;
        this.reactorRedstonePortState.textBufferA = this.currentChanges.textBufferA;
        this.reactorRedstonePortState.textBufferB = this.currentChanges.textBufferB;

        this.mainVal = (!this.reactorRedstonePortState.textBufferA.isEmpty()) ? Double.parseDouble(this.reactorRedstonePortState.textBufferA) : 0D;
        this.secondaryVal = (!this.reactorRedstonePortState.textBufferB.isEmpty()) ? Double.parseDouble(this.reactorRedstonePortState.textBufferB) : 0D;

        //if (!activeMainBuffer.isEmpty()) {
        //    mainVal = Double.parseDouble(activeMainBuffer);
        //} else {
        //    mainVal = 0;
        //}
        //if (!activeSecondBuffer.isEmpty()) {
        //    secondaryVal = Double.parseDouble(activeSecondBuffer);
        //} else {
        //    secondaryVal = 0;
        //}
    }

    public void revertChanges() {
        this.currentChanges.selectedTab = this.reactorRedstonePortState.selectedTab;
        this.currentChanges.triggerPS = this.reactorRedstonePortState.triggerPS;
        this.currentChanges.triggerAB = this.reactorRedstonePortState.triggerAB;
        this.currentChanges.triggerMode = this.reactorRedstonePortState.triggerMode;
        this.currentChanges.textBufferA = this.reactorRedstonePortState.textBufferA;
        this.currentChanges.textBufferB = this.reactorRedstonePortState.textBufferB;
    }

    @Override
    public void runRequest(String requestName, Object requestData) {
        // We save changes to an uncommitted changes temp state. When apply is pressed, then we send the run requests forward.
        switch (requestName) {
            case "setSelectedTab":
                this.currentChanges.selectedTab = ReactorRedstonePortSelection.fromInt((Integer) requestData);
                break;
            case "setTriggerPS":
                this.currentChanges.triggerPS = ReactorRedstonePortTriggers.fromBool((Boolean) requestData);
                break;
            case "setTriggerAB":
                this.currentChanges.triggerAB = ReactorRedstonePortTriggers.fromBool((Boolean) requestData);
                break;
            case "setTriggerMode":
                int triggerMode = (Integer) requestData;
                if (triggerMode >= 0 && triggerMode <= 2) {
                    this.currentChanges.triggerMode = triggerMode;
                } else {
                    this.currentChanges.triggerMode = 2;
                }
                break;
            case "setTextBufferA":
                this.currentChanges.textBufferA = (String) requestData;
                break;
            case "setTextBufferB":
                this.currentChanges.textBufferB = (String) requestData;
                break;
            case "revertChanges":
                System.out.println("No longer implemented!");
                //revertChanges();
                break;
            case "applyChanges":
                this.applyChanges();
                break;
            default:
                super.runRequest(requestName, requestData);
                break;
        }
    }

    @Override
    @Nonnull
    protected CompoundTag writeNBT() {
        CompoundTag compound = super.writeNBT();
        compound.putInt("settingId", reactorRedstonePortState.selectedTab.toInt());
        compound.putBoolean("triggerPulseOrSignal", reactorRedstonePortState.triggerPS.toBool());
        compound.putBoolean("triggerAboveOrBelow", reactorRedstonePortState.triggerAB.toBool());
        compound.putInt("mode", reactorRedstonePortState.triggerMode);
        compound.putString("mainBuffer", reactorRedstonePortState.textBufferA);
        compound.putString("secondBuffer", reactorRedstonePortState.textBufferB);
        compound.putBoolean("isPowered", isPowered);
        compound.putBoolean("isEmitting", isEmitting);
        return compound;
    }

    @Override
    protected void readNBT(@Nonnull CompoundTag compound) {
        super.readNBT(compound);
        if (compound.contains("settingId")) {
            reactorRedstonePortState.selectedTab = ReactorRedstonePortSelection.fromInt(compound.getInt("settingId"));
        }
        if (compound.contains("triggerPulseOrSignal")) {
            reactorRedstonePortState.triggerPS = ReactorRedstonePortTriggers.fromBool(compound.getBoolean("triggerPulseOrSignal"));
        }
        if (compound.contains("triggerAboveOrBelow")) {
            reactorRedstonePortState.triggerAB = ReactorRedstonePortTriggers.fromBool(compound.getBoolean("triggerAboveOrBelow"));
        }
        if (compound.contains("mode")) {
            reactorRedstonePortState.triggerMode = compound.getInt("mode");
        }
        if (compound.contains("mainBuffer")) {
            reactorRedstonePortState.textBufferA = compound.getString("mainBuffer");
        }
        if (compound.contains("secondBuffer")) {
            reactorRedstonePortState.textBufferB = compound.getString("secondBuffer");
        }
        if (compound.contains("isPowered")) {
            wasPowered = isPowered = compound.getBoolean("isPowered");
        }
        // Call reverted changes to align uncommitted settings to the active ones.
        revertChanges();
        applyChanges();
    }
    
    @Override
    public void onAssembly() {
        powerOutputDirection = getBlockState().getValue(BlockStates.FACING);
        updatePowered();
    }
    
    @Override
    public void onDisassembly() {
        powerOutputDirection = null;
        updatePowered();
    }
}
