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
        for (String logPanelType : woodTypes) {
            registerBlock(logPanelType + "_wall", WallBlock::new, AbstractBlock.Settings.create().strength(2.0f).sounds(BlockSoundGroup.WOOD));
            registerBlock(logPanelType + "_log_wall", WallBlock::new, AbstractBlock.Settings.create().strength(2.0f).sounds(BlockSoundGroup.WOOD));
            registerBlock(logPanelType + "_pillared_wall", WallBlock::new, AbstractBlock.Settings.create().strength(2.0f).sounds(BlockSoundGroup.WOOD));
        }

        // Register the Wood & Stone 'trim' blocks...
        for (String woodPanelType : woodTypes) {
            registerBlock(woodPanelType + "_trim", TrimBlock::new, AbstractBlock.Settings.create().strength(2.0f).sounds(BlockSoundGroup.WOOD));
        }

        for (String stonePanelType : stoneTypes) {
            registerBlock(stonePanelType + "_trim", TrimBlock::new, AbstractBlock.Settings.create().strength(4.0f).requiresTool().sounds(BlockSoundGroup.STONE));
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
}
