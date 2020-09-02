package me.jaackson.speedrunners.game.manager.team;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public enum Teams {
    DEAD("dead", new StringTextComponent("Dead").mergeStyle(TextFormatting.GRAY), TextFormatting.GRAY),
    RUNNER("runner", new StringTextComponent("Runner").mergeStyle(TextFormatting.AQUA), TextFormatting.AQUA),
    HUNTER("hunter", new StringTextComponent("Hunter").mergeStyle(TextFormatting.RED), TextFormatting.RED);

    private final String id;
    private final ITextComponent name;
    private final TextFormatting color;

    Teams(String id, ITextComponent name, TextFormatting color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public ITextComponent getName() {
        return name;
    }

    public TextFormatting getColor() {
        return color;
    }
}