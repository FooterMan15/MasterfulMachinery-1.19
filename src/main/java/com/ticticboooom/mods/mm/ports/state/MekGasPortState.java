package com.ticticboooom.mods.mm.ports.state;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ticticboooom.mods.mm.MM;
import com.ticticboooom.mods.mm.exception.InvalidProcessDefinitionException;
import com.ticticboooom.mods.mm.helper.RLUtils;
import com.ticticboooom.mods.mm.ports.storage.IPortStorage;
import com.ticticboooom.mods.mm.ports.storage.MekGasPortStorage;
import lombok.SneakyThrows;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.GasStack;
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

public class MekGasPortState extends PortState {

    public static final Codec<MekGasPortState> CODEC = RecordCodecBuilder.create(x -> x.group(
            Codec.STRING.fieldOf("gas").forGetter(z -> z.gas),
            Codec.LONG.fieldOf("amount").forGetter(z -> z.amount)
    ).apply(x, MekGasPortState::new));

    private final String gas;
    private final long amount;

    public MekGasPortState(String gas, long amount) {

        this.gas = gas;
        this.amount = amount;
    }

    @Override
    public void processRequirement(List<IPortStorage> storage) {
        long current = amount;
        for (IPortStorage st : storage) {
            if (st instanceof MekGasPortStorage) {
                MekGasPortStorage gasStorage = (MekGasPortStorage) st;
                GasStack extract = gasStorage.getInv().extractChemical(0, current, Action.EXECUTE);
                current -= extract.getAmount();
                if (current <= 0) {
                    return;
                }
            }
        }
    }

    @Override
    public boolean validateRequirement(List<IPortStorage> storage) {
        long current = amount;
        for (IPortStorage st : storage) {
            if (st instanceof MekGasPortStorage) {
                MekGasPortStorage gasStorage = (MekGasPortStorage) st;
                if (gasStorage.getInv().getStack().getType().getRegistryName().toString().equals(gas)) {
                    GasStack extract = gasStorage.getInv().extractChemical(0, current, Action.SIMULATE);
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
            if (st instanceof MekGasPortStorage) {
                MekGasPortStorage gasStorage = (MekGasPortStorage) st;
                    GasStack extract = gasStorage.getInv().insertChemical(new GasStack(Objects.requireNonNull(MekanismAPI.gasRegistry().getValue(RLUtils.toRL(gas))), current), Action.EXECUTE);
                    current -= extract.getAmount();
                if (current <= 0){
                    return;
                }
            }
        }
    }

    @Override
    public boolean validateResult(List<IPortStorage> storage) {
        long current = amount;
        for (IPortStorage st : storage) {
            if (st instanceof MekGasPortStorage) {
                MekGasPortStorage gasStorage = (MekGasPortStorage) st;
                GasStack extract = gasStorage.getInv().insertChemical(new GasStack(Objects.requireNonNull(MekanismAPI.gasRegistry().getValue(RLUtils.toRL(gas))), current), Action.SIMULATE);
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
        return new ResourceLocation(MM.ID, "mekanism_gas");
    }

    @SneakyThrows
    @Override
    public void validateDefinition() {
        if (!RLUtils.isRL(gas)){
            throw new InvalidProcessDefinitionException("Gas: " + gas + " is not a valid gas id (ResourceLocation)");
        }

        if (!MekanismAPI.gasRegistry().containsKey(RLUtils.toRL(gas))){
            throw new InvalidProcessDefinitionException("Gas: " + gas + " does not exist in the mekansim gas registry");
        }
    }

    @Override
    public void render(MatrixStack ms, int x, int y, int mouseX, int mouseY, IJeiHelpers helpers) {
        IDrawableStatic drawable = helpers.getGuiHelper().getSlotDrawable();
        drawable.draw(ms, x, y);
    }

    @Override
    public void setupRecipe(IRecipeLayout layout, Integer typeIndex, int x, int y, boolean input) {
        IGuiIngredientGroup<GasStack> gasGroup = layout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        gasGroup.init(typeIndex, input, x + 1, y + 1);
        gasGroup.set(typeIndex, new GasStack(MekanismAPI.gasRegistry().getValue(RLUtils.toRL(gas)), 1000));
    }

    @Override
    public void setIngredient(IIngredients in, boolean input) {
        if (input){
            in.setInput(MekanismJEI.TYPE_GAS, new GasStack(MekanismAPI.gasRegistry().getValue(RLUtils.toRL(gas)), 1000));
        } else {
            in.setOutput(MekanismJEI.TYPE_GAS, new GasStack(MekanismAPI.gasRegistry().getValue(RLUtils.toRL(gas)), 1000));
        }
    }
}
