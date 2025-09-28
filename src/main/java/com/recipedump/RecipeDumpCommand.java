package com.recipedump;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class RecipeDumpCommand {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("recipedump")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    return dumpRecipes(context.getSource());
                });

        event.getDispatcher().register(command);
    }

    private int dumpRecipes(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        RecipeManager recipeManager = server.getRecipeManager();

        JsonArray allRecipes = new JsonArray();

        Collection<Recipe<?>> recipes = recipeManager.getRecipes();

        for (Recipe<?> recipe : recipes) {
            try {
                JsonObject recipeJson = new JsonObject();

                recipeJson.addProperty("id", recipe.getId().toString());

                ResourceLocation recipeTypeKey = BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType());
                if (recipeTypeKey != null) {
                    recipeJson.addProperty("type", recipeTypeKey.toString());
                } else {
                    recipeJson.addProperty("type", "unknown");
                }

                recipeJson.addProperty("group", recipe.getGroup());

            if (recipe instanceof ShapedRecipe shapedRecipe) {
                recipeJson.addProperty("recipe_class", "shaped");
                recipeJson.addProperty("width", shapedRecipe.getWidth());
                recipeJson.addProperty("height", shapedRecipe.getHeight());

                JsonArray pattern = new JsonArray();
                for (int h = 0; h < shapedRecipe.getHeight(); h++) {
                    StringBuilder row = new StringBuilder();
                    for (int w = 0; w < shapedRecipe.getWidth(); w++) {
                        int index = w + h * shapedRecipe.getWidth();
                        if (index < shapedRecipe.getIngredients().size()) {
                            Ingredient ingredient = shapedRecipe.getIngredients().get(index);
                            if (ingredient.isEmpty()) {
                                row.append(" ");
                            } else {
                                row.append(Character.toString((char)('A' + index)));
                            }
                        }
                    }
                    pattern.add(row.toString());
                }
                recipeJson.add("pattern", pattern);

                JsonObject key = new JsonObject();
                int charIndex = 0;
                for (int i = 0; i < shapedRecipe.getIngredients().size(); i++) {
                    Ingredient ingredient = shapedRecipe.getIngredients().get(i);
                    if (!ingredient.isEmpty()) {
                        key.add(Character.toString((char)('A' + i)), serializeIngredient(ingredient));
                    }
                }
                recipeJson.add("key", key);

                recipeJson.add("result", serializeItemStack(shapedRecipe.getResultItem(server.registryAccess())));

            } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                recipeJson.addProperty("recipe_class", "shapeless");

                JsonArray ingredients = new JsonArray();
                for (Ingredient ingredient : shapelessRecipe.getIngredients()) {
                    ingredients.add(serializeIngredient(ingredient));
                }
                recipeJson.add("ingredients", ingredients);

                recipeJson.add("result", serializeItemStack(shapelessRecipe.getResultItem(server.registryAccess())));

            } else if (recipe instanceof SmeltingRecipe smeltingRecipe) {
                recipeJson.addProperty("recipe_class", "smelting");
                recipeJson.add("ingredient", serializeIngredient(smeltingRecipe.getIngredients().get(0)));
                recipeJson.add("result", serializeItemStack(smeltingRecipe.getResultItem(server.registryAccess())));
                recipeJson.addProperty("experience", smeltingRecipe.getExperience());
                recipeJson.addProperty("cookingTime", smeltingRecipe.getCookingTime());

            } else if (recipe instanceof BlastingRecipe blastingRecipe) {
                recipeJson.addProperty("recipe_class", "blasting");
                recipeJson.add("ingredient", serializeIngredient(blastingRecipe.getIngredients().get(0)));
                recipeJson.add("result", serializeItemStack(blastingRecipe.getResultItem(server.registryAccess())));
                recipeJson.addProperty("experience", blastingRecipe.getExperience());
                recipeJson.addProperty("cookingTime", blastingRecipe.getCookingTime());

            } else if (recipe instanceof SmokingRecipe smokingRecipe) {
                recipeJson.addProperty("recipe_class", "smoking");
                recipeJson.add("ingredient", serializeIngredient(smokingRecipe.getIngredients().get(0)));
                recipeJson.add("result", serializeItemStack(smokingRecipe.getResultItem(server.registryAccess())));
                recipeJson.addProperty("experience", smokingRecipe.getExperience());
                recipeJson.addProperty("cookingTime", smokingRecipe.getCookingTime());

            } else if (recipe instanceof CampfireCookingRecipe campfireRecipe) {
                recipeJson.addProperty("recipe_class", "campfire_cooking");
                recipeJson.add("ingredient", serializeIngredient(campfireRecipe.getIngredients().get(0)));
                recipeJson.add("result", serializeItemStack(campfireRecipe.getResultItem(server.registryAccess())));
                recipeJson.addProperty("experience", campfireRecipe.getExperience());
                recipeJson.addProperty("cookingTime", campfireRecipe.getCookingTime());

            } else if (recipe instanceof StonecutterRecipe stonecutterRecipe) {
                recipeJson.addProperty("recipe_class", "stonecutting");
                recipeJson.add("ingredient", serializeIngredient(stonecutterRecipe.getIngredients().get(0)));
                recipeJson.add("result", serializeItemStack(stonecutterRecipe.getResultItem(server.registryAccess())));

            } else if (recipe instanceof SmithingTransformRecipe smithingRecipe) {
                recipeJson.addProperty("recipe_class", "smithing_transform");
                if (!smithingRecipe.getIngredients().isEmpty() && smithingRecipe.getIngredients().size() >= 3) {
                    recipeJson.add("template", serializeIngredient(smithingRecipe.getIngredients().get(0)));
                    recipeJson.add("base", serializeIngredient(smithingRecipe.getIngredients().get(1)));
                    recipeJson.add("addition", serializeIngredient(smithingRecipe.getIngredients().get(2)));
                }
                recipeJson.add("result", serializeItemStack(smithingRecipe.getResultItem(server.registryAccess())));

            } else if (recipe instanceof SmithingTrimRecipe smithingTrimRecipe) {
                recipeJson.addProperty("recipe_class", "smithing_trim");
                if (!smithingTrimRecipe.getIngredients().isEmpty() && smithingTrimRecipe.getIngredients().size() >= 3) {
                    recipeJson.add("template", serializeIngredient(smithingTrimRecipe.getIngredients().get(0)));
                    recipeJson.add("base", serializeIngredient(smithingTrimRecipe.getIngredients().get(1)));
                    recipeJson.add("addition", serializeIngredient(smithingTrimRecipe.getIngredients().get(2)));
                }

            } else {
                recipeJson.addProperty("recipe_class", recipe.getClass().getSimpleName());

                if (!recipe.getIngredients().isEmpty()) {
                    JsonArray ingredients = new JsonArray();
                    for (Ingredient ingredient : recipe.getIngredients()) {
                        ingredients.add(serializeIngredient(ingredient));
                    }
                    recipeJson.add("ingredients", ingredients);
                }

                ItemStack result = recipe.getResultItem(server.registryAccess());
                if (!result.isEmpty()) {
                    recipeJson.add("result", serializeItemStack(result));
                }
            }

                allRecipes.add(recipeJson);
            } catch (Exception e) {
                LOGGER.warn("Failed to serialize recipe: {} - {}", recipe.getId(), e.getMessage());
            }
        }

        File outputFile = new File("recipes_dump.json");
        try (FileWriter writer = new FileWriter(outputFile)) {
            GSON.toJson(allRecipes, writer);
            source.sendSuccess(() -> Component.literal("Successfully dumped " + recipes.size() + " recipes to " + outputFile.getAbsolutePath()), true);
            LOGGER.info("Dumped {} recipes to {}", recipes.size(), outputFile.getAbsolutePath());
            return Command.SINGLE_SUCCESS;
        } catch (IOException e) {
            source.sendFailure(Component.literal("Failed to write recipes file: " + e.getMessage()));
            LOGGER.error("Failed to write recipes file", e);
            return 0;
        }
    }

    private JsonObject serializeIngredient(Ingredient ingredient) {
        JsonObject json = new JsonObject();
        JsonArray items = new JsonArray();

        for (ItemStack stack : ingredient.getItems()) {
            items.add(serializeItemStack(stack));
        }

        json.add("items", items);
        return json;
    }

    private JsonObject serializeItemStack(ItemStack stack) {
        JsonObject json = new JsonObject();

        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemKey != null) {
            json.addProperty("item", itemKey.toString());
        } else {
            json.addProperty("item", "unknown");
        }

        json.addProperty("count", stack.getCount());

        if (stack.hasTag()) {
            json.addProperty("nbt", stack.getTag().toString());
        }

        return json;
    }
}