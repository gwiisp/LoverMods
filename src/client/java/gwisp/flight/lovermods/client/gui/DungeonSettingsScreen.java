package gwisp.flight.lovermods.client.gui;

import gwisp.flight.lovermods.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class DungeonSettingsScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;
    private final List<PlayerSlot> playerSlots = new ArrayList<>();

    public DungeonSettingsScreen(Screen parent, ModConfig config) {
        super(Text.literal("Dungeon Mods Settings"));
        this.parent = parent;
        this.config = config;

        List<String> existingPlayers = new ArrayList<>(config.getDungeonPartyMembers());
        if (existingPlayers.size() > 5) {
            existingPlayers = existingPlayers.subList(0, 5);
        }

        for (String name : existingPlayers) {
            playerSlots.add(new PlayerSlot(name));
        }
    }

    @Override
    protected void init() {
        super.init();

        this.clearChildren();

        int fieldWidth = 180;
        int centerX = this.width / 2;
        int startY = 70;
        int spacing = 30;

        for (int i = 0; i < playerSlots.size(); i++) {
            final int index = i;
            PlayerSlot slot = playerSlots.get(i);
            int y = startY + (i * spacing);

            TextFieldWidget textField = new TextFieldWidget(
                    this.textRenderer,
                    centerX - fieldWidth / 2,
                    y,
                    fieldWidth,
                    20,
                    Text.literal("Player name")
            );
            textField.setMaxLength(16);
            textField.setText(slot.playerName);
            textField.setPlaceholder(Text.literal("Enter player name"));
            textField.setChangedListener(text -> {
                slot.playerName = text;
            });
            slot.textField = textField;
            this.addDrawableChild(textField);

            ButtonWidget deleteButton = ButtonWidget.builder(
                    Text.literal("§cX"),
                    button -> {
                        removePlayerSlot(index);
                    }
            ).dimensions(centerX + fieldWidth / 2 + 5, y, 20, 20).build();
            this.addDrawableChild(deleteButton);
        }

        if (playerSlots.size() < 5) {
            int addButtonY = startY + (playerSlots.size() * spacing);
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("+ Add Player"),
                    button -> {
                        addPlayerSlot();
                    }
            ).dimensions(centerX - 90, addButtonY, 180, 20).build());
        }

        int bottomY = this.height - 30;

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Test Invites"),
                button -> {
                    savePlayerNames();
                    if (this.client != null && this.client.player != null) {
                        this.client.player.networkHandler.sendCommand("dunginv");
                    }
                }
        ).dimensions(centerX - 205, bottomY, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Save"),
                button -> {
                    savePlayerNames();
                    this.close();
                }
        ).dimensions(centerX - 100, bottomY, 95, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Back"),
                button -> {
                    this.close();
                }
        ).dimensions(centerX + 5, bottomY, 95, 20).build());
    }

    private void addPlayerSlot() {
        if (playerSlots.size() < 5) {
            playerSlots.add(new PlayerSlot(""));
            this.clearChildren();
            this.init(this.client, this.width, this.height);
        }
    }

    private void removePlayerSlot(int index) {
        if (index >= 0 && index < playerSlots.size()) {
            playerSlots.remove(index);
            this.clearChildren();
            this.init(this.client, this.width, this.height);
        }
    }

    private void savePlayerNames() {
        List<String> players = new ArrayList<>();
        for (PlayerSlot slot : playerSlots) {
            String name = slot.playerName.trim();
            if (!name.isEmpty()) {
                players.add(name);
            }
        }
        if (players.size() > 5) {
            players = players.subList(0, 5);
        }
        config.setDungeonPartyMembers(players);
        config.save();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§7Configure up to 5 players for /dunginv"),
                this.width / 2, 40, 0xAAAAAA);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§7Players: " + playerSlots.size() + "/5"),
                this.width / 2, 55, 0xAAAAAA);
    }

    @Override
    public void close() {
        savePlayerNames();
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    private static class PlayerSlot {
        String playerName;
        TextFieldWidget textField;

        PlayerSlot(String playerName) {
            this.playerName = playerName;
        }
    }
}