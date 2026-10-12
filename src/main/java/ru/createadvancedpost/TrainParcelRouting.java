package ru.createadvancedpost;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.datafixers.util.Pair;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorage;
import com.simibubi.create.content.equipment.clipboard.ClipboardBlock;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.contraptions.MountedStorageManager;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.depot.storage.DepotMountedStorage;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TrainParcelRouting {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainParcelRouting.class);
    private static final ResourceLocation DEPOT_ID = ResourceLocation.fromNamespaceAndPath("create", "depot");
    private static final ResourceLocation CLIPBOARD_ID = ResourceLocation.fromNamespaceAndPath("create", "clipboard");
    private static final ThreadLocal<Train> CURRENT_TRAIN = new ThreadLocal<>();

    private TrainParcelRouting() {
    }

    public static void begin(Train train) {
        if (train == null) {
            CURRENT_TRAIN.remove();
        } else {
            CURRENT_TRAIN.set(train);
        }
    }

    public static void end() {
        CURRENT_TRAIN.remove();
    }

    public static ItemStack insert(IItemHandler inventory, ItemStack stack, boolean simulate) {
        Train train = CURRENT_TRAIN.get();
        if (train == null || simulate) {
            return ItemHandlerHelper.insertItemStacked(inventory, stack, simulate);
        }

        for (Carriage carriage : train.carriages) {
            if (carriage.storage == null || carriage.storage.getAllItems() != inventory) {
                continue;
            }
            return insertIntoCarriage(carriage, stack);
        }

        return ItemHandlerHelper.insertItemStacked(inventory, stack, false);
    }

    private static ItemStack insertIntoCarriage(Carriage carriage, ItemStack stack) {
        CarriageContraptionEntity entity = carriage.anyAvailableEntity();
        if (entity == null || !(entity.getContraption() instanceof CarriageContraption contraption)) {
            return stack;
        }

        MountedStorageManager storageManager = carriage.storage;
        List<StorageTarget> filtered = new ArrayList<>();
        List<StorageTarget> unfiltered = new ArrayList<>();

        for (Map.Entry<BlockPos, MountedItemStorage> entry : storageManager.getAllItemStorages().entrySet()) {
            BlockPos storagePos = entry.getKey();
            StructureBlockInfo storageInfo = contraption.getBlocks().get(storagePos);
            MountedItemStorage storage = entry.getValue();
            if (storage instanceof DepotMountedStorage
                    || storageInfo == null
                    || BuiltInRegistries.BLOCK.getKey(storageInfo.state().getBlock()).equals(DEPOT_ID)) {
                continue;
            }

            Optional<List<String>> clipboardAddresses =
                    findAttachedClipboardAddresses(contraption, storagePos, entity.level());
            StorageTarget target = new StorageTarget(storage, clipboardAddresses.orElse(List.of()));
            if (clipboardAddresses.isPresent()) {
                filtered.add(target);
            } else {
                unfiltered.add(target);
            }
        }

        List<StorageTarget> destinations = filtered.stream()
                .filter(target -> target.addresses().stream().anyMatch(address -> PackageItem.matchAddress(stack, address)))
                .toList();
        if (destinations.isEmpty()) {
            destinations = unfiltered;
        }

        ItemStack remainder = stack;
        for (StorageTarget target : destinations) {
            remainder = ItemHandlerHelper.insertItemStacked(target.storage(), remainder, false);
            if (remainder.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        return remainder;
    }

    private static Optional<List<String>> findAttachedClipboardAddresses(
            CarriageContraption contraption, BlockPos storagePos, net.minecraft.world.level.Level level) {
        for (Direction direction : Direction.values()) {
            BlockPos clipboardPos = storagePos.relative(direction);
            StructureBlockInfo clipboardInfo = contraption.getBlocks().get(clipboardPos);
            if (clipboardInfo == null
                    || !BuiltInRegistries.BLOCK.getKey(clipboardInfo.state().getBlock()).equals(CLIPBOARD_ID)) {
                continue;
            }
            if (!clipboardPos.relative(getSupportDirection(clipboardInfo.state())).equals(storagePos)) {
                continue;
            }

            CompoundTag clipboardNbt = clipboardInfo.nbt();
            if (clipboardNbt == null || !clipboardNbt.contains("components", Tag.TAG_COMPOUND)) {
                return Optional.of(List.of());
            }

            DataComponentMap components = DataComponentMap.CODEC
                    .decode(level.registryAccess().createSerializationContext(NbtOps.INSTANCE),
                            clipboardNbt.getCompound("components"))
                    .resultOrPartial(error -> LOGGER.warn("Could not read clipboard address filter: {}", error))
                    .map(Pair::getFirst)
                    .orElse(DataComponentMap.EMPTY);

            List<String> addresses = new ArrayList<>();
            for (List<ClipboardEntry> page : ClipboardEntry.readAll(components)) {
                for (ClipboardEntry entry : page) {
                    if (entry.checked) {
                        continue;
                    }
                    String address = entry.text.getString().trim();
                    if (address.startsWith("#")) {
                        address = address.substring(1).trim();
                    }
                    if (!address.isEmpty()) {
                        addresses.add(address);
                    }
                }
            }
            return Optional.of(List.copyOf(addresses));
        }
        return Optional.empty();
    }

    private static Direction getSupportDirection(BlockState state) {
        AttachFace face = state.getValue(ClipboardBlock.FACE);
        return switch (face) {
            case CEILING -> Direction.UP;
            case FLOOR -> Direction.DOWN;
            case WALL -> state.getValue(HorizontalDirectionalBlock.FACING).getOpposite();
        };
    }

    private record StorageTarget(MountedItemStorage storage, List<String> addresses) {
    }
}
