package net.roguelogix.biggerreactors.blocks.materials;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.roguelogix.phosphophyllite.registry.RegisterBlock;

public class MaterialBlock extends Block {
    @RegisterBlock(name = "blutonium_block")
    public static final MaterialBlock BLUTONIUM = new MaterialBlock();
    @RegisterBlock(name = "cyanite_block")
    public static final MaterialBlock CYANITE = new MaterialBlock();
    @RegisterBlock(name = "graphite_block")
    public static final MaterialBlock GRAPHITE = new MaterialBlock();
    @RegisterBlock(name = "ludicrite_block")
    public static final MaterialBlock LUDICRITE = new MaterialBlock();
    @RegisterBlock(name = "uranium_block")
    public static final MaterialBlock URANIUM = new MaterialBlock();
    @RegisterBlock(name = "raw_uranium_block")
    public static final MaterialBlock RAW_URANIUM = new MaterialBlock();
    
    public MaterialBlock() {
        super(Properties.of()
                .sound(SoundType.STONE)
                .explosionResistance(3.0F)
                .destroyTime(2.0f)
                .requiresCorrectToolForDrops());
    }
}
