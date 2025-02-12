package io.ticticboom.mods.mm.structure.transformers;

import io.ticticboom.mods.mm.setup.model.StructureModel;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.List;

public abstract class MMStructureTransform extends ForgeRegistryEntry<MMStructureTransform> {
    public abstract List<StructureModel.PlacedStructurePart> transform(List<StructureModel.PlacedStructurePart> original);
}
