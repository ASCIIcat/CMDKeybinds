package net.kyrptonaught.cmdkeybind.config;

import io.github.prospector.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.ClothConfigScreen;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kyrptonaught.cmdkeybind.CmdKeybindMod;
import net.kyrptonaught.cmdkeybind.MacroTypes.BaseMacro;
import net.kyrptonaught.cmdkeybind.config.clothconfig.ButtonEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;

import java.util.Optional;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public String getModId() {
        return CmdKeybindMod.MOD_ID;
    }

    @Override
    public Optional<Supplier<Screen>> getConfigScreen(Screen screen) {

        return Optional.of(() -> buildScreen(screen));
    }

    private static Screen buildScreen(Screen screen) {
        ConfigOptions options = CmdKeybindMod.getConfig();
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(screen).setTitle("Macros");

        builder.setSavingRunnable(() -> {
            CmdKeybindMod.config.saveAll();
            CmdKeybindMod.buildMacros();
        });
        ConfigCategory category = builder.getOrCreateCategory("key.cmdkeybind.config.category.main");
        ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
        category.addEntry(entryBuilder.startBooleanToggle("key.cmdkeybind.config.enabled", options.enabled).setDefaultValue(true).setSaveConsumer(val -> options.enabled = val).build());

        for (int i = 0; i < options.macros.size(); i++)
            category.addEntry(buildNewMacro(builder, entryBuilder, i).build());
        category.addEntry(new ButtonEntry("key.cmdkeybind.config.add", buttonEntry -> {
            CmdKeybindMod.addEmptyMacro();
            reloadScreen(builder);
        }));
        return builder.build();
    }

    private static SubCategoryBuilder buildNewMacro(ConfigBuilder builder, ConfigEntryBuilder entryBuilder, int macroNum) {
        ConfigOptions.ConfigMacro macro = CmdKeybindMod.getConfig().macros.get(macroNum);
        SubCategoryBuilder sub = entryBuilder.startSubCategory(macro.command).setTooltip(macro.keyName);
        sub.add(entryBuilder.startTextField("key.cmdkeybind.config.macro.command", macro.command).setDefaultValue("/").setSaveConsumer(cmd -> macro.command = cmd).build());
        sub.add(entryBuilder.startKeyCodeField("key.cmdkeybind.config.macro.key", InputUtil.fromName(macro.keyName)).setSaveConsumer(key -> macro.keyName = key.getName()).build());
        sub.add(entryBuilder.startKeyCodeField("key.cmdkeybind.config.macro.keymod", InputUtil.fromName(macro.keyModName)).setSaveConsumer(key -> macro.keyModName = key.getName()).setDefaultValue(InputUtil.UNKNOWN_KEYCODE).build());
        sub.add(entryBuilder.startEnumSelector("key.cmdkeybind.config.macrotype", BaseMacro.MacroType.class, macro.macroType).setSaveConsumer(val -> macro.macroType = val).build());
        sub.add(entryBuilder.startIntField("key.cmdkeybind.config.delay", macro.delay).setDefaultValue(0).setSaveConsumer(val -> macro.delay = val).build());
        sub.add(new ButtonEntry("key.cmdkeybind.config.remove", buttonEntry -> {
            CmdKeybindMod.getConfig().macros.remove(macroNum);
            reloadScreen(builder);
        }));
        return sub;
    }

    private static void reloadScreen(ConfigBuilder builder) {
        builder.getSavingRunnable().run();
        ((ClothConfigScreen) MinecraftClient.getInstance().currentScreen).saveAll(false);
        MinecraftClient.getInstance().openScreen(buildScreen(builder.getParentScreen()));
    }
}