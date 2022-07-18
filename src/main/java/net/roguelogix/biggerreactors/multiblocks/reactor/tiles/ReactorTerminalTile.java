package net.roguelogix.biggerreactors.multiblocks.reactor.tiles;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.roguelogix.biggerreactors.multiblocks.reactor.blocks.ReactorTerminal;
import net.roguelogix.biggerreactors.multiblocks.reactor.containers.ReactorTerminalContainer;
import net.roguelogix.biggerreactors.multiblocks.reactor.state.ReactorState;
import net.roguelogix.phosphophyllite.client.gui.api.IHasUpdatableState;
import net.roguelogix.phosphophyllite.registry.RegisterTile;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ReactorTerminalTile extends ReactorBaseTile implements MenuProvider, IHasUpdatableState<ReactorState> {
    
    @RegisterTile("reactor_terminal")
    public static final BlockEntityType.BlockEntitySupplier<ReactorTerminalTile> SUPPLIER = new RegisterTile.Producer<>(ReactorTerminalTile::new);
    
    public ReactorTerminalTile(BlockEntityType<?> TYPE, BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }
    
    public final ReactorState reactorState = new ReactorState(this);
    
    @Override
    public ReactorState getState() {
        this.updateState();
        return this.reactorState;
    }
    
    @Override
    public void updateState() {
        if (nullableController() != null) {
            controller().updateReactorState(reactorState);
        }
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable(ReactorTerminal.INSTANCE.getDescriptionId());
    }
    
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new ReactorTerminalContainer(windowId, this.worldPosition, player);
    }
}
