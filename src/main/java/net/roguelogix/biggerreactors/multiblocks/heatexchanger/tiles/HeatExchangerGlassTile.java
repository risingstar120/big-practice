package net.roguelogix.biggerreactors.multiblocks.heatexchanger.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.roguelogix.phosphophyllite.registry.RegisterTileEntity;
import net.roguelogix.phosphophyllite.registry.TileSupplier;

@RegisterTileEntity(name = "heat_exchanger_glass")
public class HeatExchangerGlassTile extends HeatExchangerBaseTile {
    
    @RegisterTileEntity.Type
    public static BlockEntityType<?> TYPE;
    
    @RegisterTileEntity.Supplier
    public static final TileSupplier SUPPLIER = HeatExchangerGlassTile::new;
    
    public HeatExchangerGlassTile(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }
}
