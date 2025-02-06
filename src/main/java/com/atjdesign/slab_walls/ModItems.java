package com.atjdesign.slab_walls;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.joml.Vector3d;

import java.util.function.Function;

public class ModItems {

    public static final String[] woodTypes = new String[]
            {
                    "oak",
                    "dark_oak",
                    "spruce",
                    "birch",
                    "jungle",
                    "acacia",
                    "pale_oak",
                    "mangrove",
                    "cherry",
                    "crimson",
                    "warped",
                    "bamboo"
            };

    public static final String[] stoneTypesNoWall = new String[]
            {
                    "cut_red_sandstone",
                    "cut_sandstone",
                    "polished_andesite",
                    "polished_diorite",
                    "polished_granite",
                    "smooth_red_sandstone",
                    "smooth_sandstone",
                    "smooth_stone",
                    "stone"
            };

    public static final String[] stoneTypes = new String[]
            {
                    "cobblestone",
                    "mossy_cobblestone",
                    "stone",
                    "stone_brick",
                    "sandstone",
                    "smooth_sandstone",
                    "cut_sandstone",
                    "red_sandstone",
                    "smooth_red_sandstone",
                    "cut_red_sandstone",
                    "granite",
                    "polished_granite",
                    "diorite",
                    "polished_diorite",
                    "cobbled_deepslate",
                    "polished_deepslate",
                    "deepslate_brick",
                    "tuff",
                    "polished_tuff",
                    "tuff_brick",
                    "andesite",
                    "polished_andesite",
                    "smooth_stone"
            };

    public static void Initialize() {
        // Register all of the wood wall blocks...
        for (String logPanelType : woodTypes)
        {
            registerBlock(logPanelType + (logPanelType.equals("crimson") || logPanelType.equals("warped") ? "_stem" : logPanelType.equals("bamboo") ? "_block" : "_log") + "_slab", SlabBlock::new, AbstractBlock.Settings.create().strength(2.0f).sounds(BlockSoundGroup.WOOD));
            registerBlock(logPanelType + "_wall", WallBlock::new, AbstractBlock.Settings.create().strength(2.0f).sounds(BlockSoundGroup.WOOD));
            registerBlock(logPanelType + "_log_wall", WallBlock::new, AbstractBlock.Settings.create().strength(2.0f).sounds(BlockSoundGroup.WOOD));
            registerBlock(logPanelType + "_slim_wall", SlimWallBlock::new, AbstractBlock.Settings.create().strength(1.0f).sounds(BlockSoundGroup.WOOD));
            registerBlock(logPanelType + "_slim_log_wall", SlimWallBlock::new, AbstractBlock.Settings.create().strength(1.0f).sounds(BlockSoundGroup.WOOD));
            registerBlock(logPanelType + "_pillared_wall", WallBlock::new, AbstractBlock.Settings.create().strength(2.0f).sounds(BlockSoundGroup.WOOD));
            registerBlock(logPanelType + "_trim", TrimBlock::new, AbstractBlock.Settings.create().strength(2.0f).sounds(BlockSoundGroup.WOOD));
            registerBlock("pillar_" + logPanelType, PillarBlock::new, AbstractBlock.Settings.create().strength(2.0f).sounds(BlockSoundGroup.WOOD).nonOpaque());
            registerBlock("pillar_" + logPanelType + "_log", PillarBlock::new, AbstractBlock.Settings.create().strength(2.0f).sounds(BlockSoundGroup.WOOD).nonOpaque());
            registerBlock("strut_" + logPanelType, StrutBlock::new, AbstractBlock.Settings.create().strength(2.0f).sounds(BlockSoundGroup.WOOD).nonOpaque());
        }

        for (String newStoneWallType: stoneTypesNoWall)
        {
            registerBlock(newStoneWallType + "_wall", WallBlock::new, AbstractBlock.Settings.create().strength(2.0f).requiresTool().sounds(BlockSoundGroup.STONE));
        }

        for (String stonePanelType : stoneTypes)
        {
            registerBlock(stonePanelType + "_trim", TrimBlock::new, AbstractBlock.Settings.create().strength(2.0f).requiresTool().sounds(BlockSoundGroup.STONE));
            registerBlock("pillar_" + stonePanelType, PillarBlock::new, AbstractBlock.Settings.create().strength(2.0f).requiresTool().sounds(BlockSoundGroup.STONE).nonOpaque());
            registerBlock(stonePanelType + "_slim_wall", SlimWallBlock::new, AbstractBlock.Settings.create().strength(1.0f).requiresTool().sounds(BlockSoundGroup.STONE));
            registerBlock("strut_" + stonePanelType, StrutBlock::new, AbstractBlock.Settings.create().strength(2.0f).requiresTool().sounds(BlockSoundGroup.STONE));
        }
    }

    // Construction Item group...
    public static final RegistryKey<ItemGroup> CONSTRUCTION_ITEM_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(SlabWalls.MOD_ID, "item_group.construction"));
    public static ItemGroup CONSTRUCTION_ITEM_GROUP;

    public static boolean cigBuilt = false;

    private static Block registerBlock(String path, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        final Identifier identifier = Identifier.of(SlabWalls.MOD_ID, path);
        final RegistryKey<Block> regKey = RegistryKey.of(RegistryKeys.BLOCK, identifier);

        final Block block = register(
                factory.apply(settings.registryKey(regKey)),
                regKey,
                true
        );

        if (!cigBuilt)
        {
            cigBuilt = true;
            CONSTRUCTION_ITEM_GROUP =  FabricItemGroup.builder()
                    .icon(() -> new ItemStack(block.asItem()))
                    .displayName(Text.translatable("item_group.construction"))
                    .build();

            Registry.register(Registries.ITEM_GROUP, CONSTRUCTION_ITEM_GROUP_KEY, CONSTRUCTION_ITEM_GROUP);
        }

        ItemGroupEvents.modifyEntriesEvent(CONSTRUCTION_ITEM_GROUP_KEY).register(ig -> {
            ig.add(block.asItem());
        });

        return block;
    }

    public static Block register(Block block, RegistryKey<Block> blockKey, boolean shouldRegisterItem) {
        // Sometimes, you may not want to register an item for the block.
        // Eg: if it's a technical block like `minecraft:air` or `minecraft:end_gateway`
        if (shouldRegisterItem) {
            // Items need to be registered with a different type of registry key, but the ID
            // can be the same.
            RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, blockKey.getValue());

            BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(itemKey));
            Registry.register(Registries.ITEM, itemKey, blockItem);
        }

        return Registry.register(Registries.BLOCK, blockKey, block);
    }


    public static Box RotateBox(Direction.Axis axis, Box box, int rotations, Vector3d pivot)
    {
        while (rotations >= 4)
        {
            rotations -= 4;
        }
        while (rotations < 0)
        {
            rotations += 4;
        }

        if (rotations == 0)
        {
            return  box;
        }

        Box adjBox = new Box(box.minX - pivot.x, box.minY - pivot.y, box.minZ - pivot.z,
                box.maxX - pivot.x, box.maxY - pivot.y, box.maxZ - pivot.z);

        double omX = 0, oMX = 0, omY = 0, oMY = 0, omZ = 0, oMZ = 0;

        switch (axis)
        {
            // Up/Down
            case Y:
                omY = adjBox.minY + pivot.y;
                oMY = adjBox.maxY + pivot.y;

                // Remember -Z is North...
                if (rotations == 1)
                {
                    omZ = pivot.z + adjBox.minX;
                    oMZ = pivot.z + adjBox.maxX;

                    omX = pivot.x - adjBox.minZ;
                    oMX = pivot.x - adjBox.maxZ;
                }
                else if (rotations == 2)
                {
                    omZ = pivot.z - adjBox.minZ;
                    oMZ = pivot.z - adjBox.maxZ;

                    omX = pivot.x - adjBox.minX;
                    oMX = pivot.x - adjBox.maxX;
                }
                else
                {
                    omZ = pivot.z - adjBox.minX;
                    oMZ = pivot.z - adjBox.maxX;

                    omX = pivot.x + adjBox.minZ;
                    oMX = pivot.x + adjBox.maxZ;
                }

                break;

            // North/South
            case Z:

                omZ = adjBox.minZ + pivot.z;
                oMZ = adjBox.maxZ + pivot.z;

                if (rotations == 1)
                {
                    omX = pivot.x + adjBox.minY;
                    oMX = pivot.x + adjBox.maxY;

                    omY = pivot.y - adjBox.minX;
                    oMY = pivot.y - adjBox.maxX;
                }
                else if (rotations == 2)
                {
                    omX = pivot.x - adjBox.minX;
                    oMX = pivot.x - adjBox.maxX;

                    omY = pivot.y - adjBox.minY;
                    oMY = pivot.y - adjBox.maxY;
                }
                else
                {
                    omX = pivot.x - adjBox.minY;
                    oMX = pivot.x - adjBox.maxY;

                    omY = pivot.y + adjBox.minX;
                    oMY = pivot.y + adjBox.maxX;
                }

                break;

            // East/West
            case X:
                omX = adjBox.minX + pivot.x;
                oMX = adjBox.maxX + pivot.x;

                // Remember -Z is North...
                if (rotations == 1)
                {
                    omZ = pivot.z + adjBox.minY;
                    oMZ = pivot.z + adjBox.maxY;

                    omY = pivot.y - adjBox.minZ;
                    oMY = pivot.y - adjBox.maxZ;
                }
                else if (rotations == 2)
                {
                    omZ = pivot.z - adjBox.minZ;
                    oMZ = pivot.z - adjBox.maxZ;

                    omY = pivot.y - adjBox.minY;
                    oMY = pivot.y - adjBox.maxY;
                }
                else
                {
                    omZ = pivot.z - adjBox.minY;
                    oMZ = pivot.z - adjBox.maxY;

                    omY = pivot.y + adjBox.minZ;
                    oMY = pivot.y + adjBox.maxZ;
                }

                break;
        }

        return  new Box(omX < oMX ? omX : oMX, omY < oMY ? omY : oMY, omZ < oMZ ? omZ : oMZ,
                omX < oMX ? oMX : omX, omY < oMY ? oMY : omY, omZ < oMZ ? oMZ : omZ);
    }
}
