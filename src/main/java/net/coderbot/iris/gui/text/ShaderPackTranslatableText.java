package net.coderbot.iris.gui.text;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.BaseText;
import net.minecraft.text.MutableText;
import net.minecraft.text.ParsableText;
import org.jetbrains.annotations.Nullable;

public class ShaderPackTranslatableText extends BaseText implements ParsableText {
    @Override
    public BaseText copy() {
        return null;
    }

    @Override
    public MutableText parse(@Nullable ServerCommandSource source, @Nullable Entity sender, int depth) {
        return null;
    }
}
