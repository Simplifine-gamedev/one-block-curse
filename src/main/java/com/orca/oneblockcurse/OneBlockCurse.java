package com.orca.oneblockcurse;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class OneBlockCurse implements ModInitializer {
    public static final String MOD_ID = "one-block-curse";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // List of common blocks that can be cursed
    private static final List<Block> CURSABLE_BLOCKS = Arrays.asList(
        Blocks.STONE,
        Blocks.DIRT,
        Blocks.SAND,
        Blocks.GRAVEL,
        Blocks.OAK_LOG,
        Blocks.COBBLESTONE,
        Blocks.GRASS_BLOCK,
        Blocks.GRANITE,
        Blocks.DIORITE,
        Blocks.ANDESITE,
        Blocks.CLAY,
        Blocks.NETHERRACK
    );

    // Hostile mobs that can be spawned
    private static final List<EntityType<?>> HOSTILE_MOBS = Arrays.asList(
        EntityType.ZOMBIE,
        EntityType.SKELETON,
        EntityType.SPIDER,
        EntityType.CREEPER,
        EntityType.ENDERMAN
    );

    // Positive status effects for buffs (these are already RegistryEntry<StatusEffect>)
    private static final List<RegistryEntry<StatusEffect>> POSITIVE_EFFECTS = Arrays.asList(
        StatusEffects.SPEED,
        StatusEffects.STRENGTH,
        StatusEffects.JUMP_BOOST,
        StatusEffects.REGENERATION,
        StatusEffects.RESISTANCE,
        StatusEffects.HASTE,
        StatusEffects.NIGHT_VISION,
        StatusEffects.FIRE_RESISTANCE
    );

    @Override
    public void onInitialize() {
        LOGGER.info("One Block Curse mod initialized!");

        // Register block break event
        PlayerBlockBreakEvents.AFTER.register(this::onBlockBreak);

        // Register command
        CommandRegistrationCallback.EVENT.register(this::registerCommands);
    }

    private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher,
                                  CommandRegistryAccess registryAccess,
                                  CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("cursedblock")
            .requires(source -> source.hasPermissionLevel(0))
            .executes(context -> {
                ServerCommandSource source = context.getSource();
                ServerWorld world = source.getWorld();
                CurseData curseData = CurseData.getOrCreate(world.getServer());
                Block cursedBlock = curseData.getCursedBlock();

                if (cursedBlock != null) {
                    String blockName = Registries.BLOCK.getId(cursedBlock).toString();
                    source.sendFeedback(() -> Text.literal("The cursed block in this world is: " + blockName), false);
                } else {
                    source.sendFeedback(() -> Text.literal("No block has been cursed yet!"), false);
                }
                return 1;
            }));
    }

    private void onBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (world.isClient()) return;

        ServerWorld serverWorld = (ServerWorld) world;
        CurseData curseData = CurseData.getOrCreate(serverWorld.getServer());
        Block cursedBlock = curseData.getCursedBlock();

        if (cursedBlock == null) return;

        // Check if the broken block is the cursed block
        if (state.getBlock() == cursedBlock) {
            Random random = new Random();

            // 15% chance to trigger effect
            if (random.nextFloat() < 0.15f) {
                triggerRandomEffect(serverWorld, player, pos, random);
            }
        }
    }

    private void triggerRandomEffect(ServerWorld world, PlayerEntity player, BlockPos pos, Random random) {
        int effect = random.nextInt(5);

        switch (effect) {
            case 0 -> triggerExplosion(world, pos, player);
            case 1 -> spawnHostileMobs(world, pos, random);
            case 2 -> givePositiveBuff(player, random);
            case 3 -> dropBonusDiamonds(world, pos, random);
            case 4 -> reverseGravity(player);
        }
    }

    private void triggerExplosion(ServerWorld world, BlockPos pos, PlayerEntity player) {
        player.sendMessage(Text.literal("The curse triggers an explosion!"), true);
        world.createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
            2.0f, World.ExplosionSourceType.TNT);
        LOGGER.info("Curse triggered: Small explosion at {}", pos);
    }

    private void spawnHostileMobs(ServerWorld world, BlockPos pos, Random random) {
        EntityType<?> mobType = HOSTILE_MOBS.get(random.nextInt(HOSTILE_MOBS.size()));

        for (int i = 0; i < 3; i++) {
            double offsetX = random.nextDouble() * 4 - 2;
            double offsetZ = random.nextDouble() * 4 - 2;
            BlockPos spawnPos = pos.add((int) offsetX, 1, (int) offsetZ);

            mobType.spawn(world, spawnPos, SpawnReason.EVENT);
        }

        world.getPlayers().forEach(p -> {
            if (p.getBlockPos().isWithinDistance(pos, 16)) {
                p.sendMessage(Text.literal("The curse summons hostile mobs!"), true);
            }
        });
        LOGGER.info("Curse triggered: Spawned 3 {} at {}", mobType.getName().getString(), pos);
    }

    private void givePositiveBuff(PlayerEntity player, Random random) {
        RegistryEntry<StatusEffect> effect = POSITIVE_EFFECTS.get(random.nextInt(POSITIVE_EFFECTS.size()));

        player.addStatusEffect(new StatusEffectInstance(effect, 600, 1)); // 30 seconds, level 2
        String effectName = effect.value().getName().getString();
        player.sendMessage(Text.literal("The curse grants you " + effectName + "!"), true);
        LOGGER.info("Curse triggered: Gave {} buff to {}", effectName, player.getName().getString());
    }

    private void dropBonusDiamonds(ServerWorld world, BlockPos pos, Random random) {
        int count = random.nextInt(3) + 2; // 2-4 diamonds

        for (int i = 0; i < count; i++) {
            ItemStack diamonds = new ItemStack(Items.DIAMOND, 1);
            Block.dropStack(world, pos, diamonds);
        }

        world.getPlayers().forEach(p -> {
            if (p.getBlockPos().isWithinDistance(pos, 16)) {
                p.sendMessage(Text.literal("The curse rewards you with " + count + " diamonds!"), true);
            }
        });
        LOGGER.info("Curse triggered: Dropped {} diamonds at {}", count, pos);
    }

    private void reverseGravity(PlayerEntity player) {
        player.sendMessage(Text.literal("The curse reverses your gravity for 5 seconds!"), true);

        // Apply levitation effect for 5 seconds (simulates reverse gravity)
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 100, 2)); // 5 seconds, level 3

        // Also give slow falling to prevent fall damage when it ends
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 200, 0)); // 10 seconds

        LOGGER.info("Curse triggered: Reversed gravity for {}", player.getName().getString());
    }

    // Persistent state class to store the cursed block across sessions
    public static class CurseData extends PersistentState {
        private static final String DATA_NAME = MOD_ID + "_curse_data";
        private Block cursedBlock;

        public CurseData() {
            // Select random cursed block when first created
            Random random = new Random();
            this.cursedBlock = CURSABLE_BLOCKS.get(random.nextInt(CURSABLE_BLOCKS.size()));
            LOGGER.info("Selected cursed block for this world: {}", Registries.BLOCK.getId(cursedBlock));
        }

        public CurseData(Block cursedBlock) {
            this.cursedBlock = cursedBlock;
        }

        public Block getCursedBlock() {
            return cursedBlock;
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt, net.minecraft.registry.RegistryWrapper.WrapperLookup registries) {
            if (cursedBlock != null) {
                nbt.putString("cursedBlock", Registries.BLOCK.getId(cursedBlock).toString());
            }
            return nbt;
        }

        public static CurseData fromNbt(NbtCompound nbt, net.minecraft.registry.RegistryWrapper.WrapperLookup registries) {
            String blockId = nbt.getString("cursedBlock");
            Block block = Registries.BLOCK.get(Identifier.of(blockId));
            return new CurseData(block);
        }

        public static CurseData getOrCreate(MinecraftServer server) {
            ServerWorld world = server.getOverworld();
            PersistentStateManager stateManager = world.getPersistentStateManager();

            CurseData data = stateManager.getOrCreate(
                new PersistentState.Type<>(
                    CurseData::new,
                    CurseData::fromNbt,
                    null
                ),
                DATA_NAME
            );

            data.markDirty();
            return data;
        }
    }
}
