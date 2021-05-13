package com.ticticboooom.mods.mm.ports.state;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ticticboooom.mods.mm.MM;
import com.ticticboooom.mods.mm.exception.InvalidProcessDefinitionException;
import com.ticticboooom.mods.mm.helper.RLUtils;
import com.ticticboooom.mods.mm.ports.storage.IPortStorage;
import com.ticticboooom.mods.mm.ports.storage.MekSlurryPortStorage;
import lombok.SneakyThrows;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.inventory.AutomationType;
import mekanism.client.jei.MekanismJEI;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Objects;

public class MekSlurryPortState extends PortState {

    public static final Codec<MekSlurryPortState> CODEC = RecordCodecBuilder.create(x -> x.group(
            Codec.STRING.fieldOf("slurry").forGetter(z -> z.slurry),
            Codec.LONG.fieldOf("amount").forGetter(z -> z.amount)
    ).apply(x, MekSlurryPortState::new));

    private final String slurry;
    private final long amount;

    public MekSlurryPortState(String gas, long amount) {

        this.slurry = gas;
        this.amount = amount;
    }

    @Override
    public void processRequirement(List<IPortStorage> storage) {
        long current = amount;
        for (IPortStorage st : storage) {
            if (st instanceof MekSlurryPortStorage) {
                MekSlurryPortStorage gasStorage = (MekSlurryPortStorage) st;
                if (gasStorage.getInv().getStack().getType().getRegistryName().toString().equals(slurry)) {
                    SlurryStack extract = gasStorage.getInv().extractChemical(0, current, Action.EXECUTE);
                    current -= extract.getAmount();
                }
                if (current <= 0){
                    return;
                }
            }
        }
    }

    @Override
    public boolean validateRequirement(List<IPortStorage> storage) {
        long current = amount;
        for (IPortStorage st : storage) {
            if (st instanceof MekSlurryPortStorage) {
                MekSlurryPortStorage gasStorage = (MekSlurryPortStorage) st;
                if (gasStorage.getInv().getStack().getType().getRegistryName().toString().equals(slurry)) {
                    SlurryStack extract = gasStorage.getInv().extractChemical(0, current, Action.SIMULATE);
                    current -= extract.getAmount();
                }
                if (current <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void processResult(List<IPortStorage> storage) {
        long current = amount;
        for (IPortStorage st : storage) {
            if (st instanceof MekSlurryPortStorage) {
                MekSlurryPortStorage gasStorage = (MekSlurryPortStorage) st;
                SlurryStack extract = gasStorage.getInv().insertChemical(new SlurryStack(Objects.requireNonNull(MekanismAPI.slurryRegistry().getValue(RLUtils.toRL(slurry))), current), Action.EXECUTE);
                current -= extract.getAmount();
                if (current <= 0) {
                    return;
                }
            }
        }
    }

    @Override
    public boolean validateResult(List<IPortStorage> storage) {
        long current = amount;
        for (IPortStorage st : storage) {
            if (st instanceof MekSlurryPortStorage) {
                MekSlurryPortStorage gasStorage = (MekSlurryPortStorage) st;
                SlurryStack extract = gasStorage.getInv().insertChemical(new SlurryStack(Objects.requireNonNull(MekanismAPI.slurryRegistry().getValue(RLUtils.toRL(slurry))), current), Action.SIMULATE);
                current -= extract.getAmount();
                if (current <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ResourceLocation getName() {
        return new ResourceLocation(MM.ID, "mekanism_slurry");
    }

    @SneakyThrows
    @Override
    public void validateDefinition() {
        if (!RLUtils.isRL(slurry)){
            throw new InvalidProcessDefinitionException("Slurry: " + slurry + " is not a valid slurry id (ResourceLocation)");
        }

        if (!MekanismAPI.slurryRegistry().containsKey(RLUtils.toRL(slurry))){
            throw new InvalidProcessDefinitionException("Slurry: " + slurry + " does not exist in the mekansim slurry registry");
        }
    }

    @Override
    public void render(MatrixStack ms, int x, int y, int mouseX, int mouseY, IJeiHelpers helpers) {
        IDrawableStatic drawable = helpers.getGuiHelper().getSlotDrawable();
        drawable.draw(ms, x, y);
    }

    @Override
    public void setupRecipe(IRecipeLayout layout, Integer typeIndex, int x, int y, boolean input) {
        IGuiIngredientGroup<SlurryStack> gasGroup = layout.getIngredientsGroup(MekanismJEI.TYPE_SLURRY);
        gasGroup.init(typeIndex, input, x + 1,  y+ 1);
        gasGroup.set(typeIndex, new SlurryStack(MekanismAPI.slurryRegistry().getValue(RLUtils.toRL(slurry)), 1000));
    }


    @Override
    public void setIngredient(IIngredients in, boolean input) {
        if (input) {
            in.setInput(MekanismJEI.TYPE_SLURRY, new SlurryStack(MekanismAPI.slurryRegistry().getValue(RLUtils.toRL(slurry)), 1000));
        } else {
            in.setOutput(MekanismJEI.TYPE_SLURRY, new SlurryStack(MekanismAPI.slurryRegistry().getValue(RLUtils.toRL(slurry)), 1000));
        }
    }
}
