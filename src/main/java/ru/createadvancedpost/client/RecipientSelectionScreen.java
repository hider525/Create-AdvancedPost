package ru.createadvancedpost.client;

import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import ru.createadvancedpost.network.CancelDeliveryPayload;
import ru.createadvancedpost.network.SubmitDeliveryPayload;

public final class RecipientSelectionScreen extends Screen {
    private static final int MAX_SUGGESTIONS = 5;

    private final BlockPos packagerPos;
    private final String address;
    private final List<String> recipients;
    private EditBox recipientInput;
    private boolean submitted;
    private boolean rejected;
    private boolean submitting;

    public RecipientSelectionScreen(BlockPos packagerPos, String address, List<String> recipients) {
        super(Component.translatable("screen.create_advancedpost.delivery.title"));
        this.packagerPos = packagerPos;
        this.address = address;
        this.recipients = List.copyOf(recipients);
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int centerY = height / 2;

        recipientInput = new EditBox(
                font,
                centerX - 120,
                centerY - 14,
                240,
                20,
                Component.translatable("screen.create_advancedpost.delivery.recipient")
        );
        recipientInput.setMaxLength(16);
        addRenderableWidget(recipientInput);
        setInitialFocus(recipientInput);

        addRenderableWidget(Button.builder(
                        Component.translatable("screen.create_advancedpost.delivery.send"),
                        button -> submit()
                )
                .bounds(centerX - 105, centerY + 106, 100, 20)
                .build());
        addRenderableWidget(Button.builder(
                        Component.translatable("screen.create_advancedpost.delivery.cancel"),
                        button -> onClose()
                )
                .bounds(centerX + 5, centerY + 106, 100, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xFF101010);
        int centerX = width / 2;
        int centerY = height / 2;
        graphics.fill(centerX - 135, centerY - 64, centerX + 135, centerY + 130, 0xFF272421);
        graphics.fill(centerX - 135, centerY - 64, centerX + 135, centerY - 62, 0xFFD99B35);
        graphics.renderOutline(centerX - 135, centerY - 64, 270, 194, 0xFF66543A);
        graphics.drawCenteredString(font, title, centerX, centerY - 48, 0xFFD99B35);
        graphics.drawString(
                font,
                Component.translatable("screen.create_advancedpost.delivery.recipient_prompt"),
                centerX - 120,
                centerY - 34,
                0xFFE6E0D5
        );
        if (rejected) {
            graphics.drawString(
                    font,
                    Component.translatable("screen.create_advancedpost.delivery.submission_rejected"),
                    centerX - 120,
                    centerY + 12,
                    0xFFE06B52
            );
        }
        for (Renderable renderable : renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
        }
        renderSuggestions(graphics, mouseX, mouseY);
    }

    private void renderSuggestions(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!recipientInput.isFocused()) {
            return;
        }

        List<String> suggestions = getSuggestions();
        int left = width / 2 - 120;
        int top = height / 2 + 7;
        for (int i = 0; i < suggestions.size(); i++) {
            int rowTop = top + i * 18;
            boolean hovered = mouseX >= left && mouseX < left + 240
                    && mouseY >= rowTop && mouseY < rowTop + 18;
            graphics.fill(left, rowTop, left + 240, rowTop + 18, hovered ? 0xFF54432B : 0xF0272421);
            graphics.renderOutline(left, rowTop, 240, 18, 0xFF66543A);
            graphics.drawString(font, suggestions.get(i), left + 5, rowTop + 5, 0xFFE6E0D5);
        }
    }

    private List<String> getSuggestions() {
        String query = recipientInput.getValue().trim().toLowerCase(Locale.ROOT);
        return recipients.stream()
                .filter(recipient -> query.isEmpty() || recipient.toLowerCase(Locale.ROOT).contains(query))
                .limit(MAX_SUGGESTIONS)
                .toList();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && recipientInput.isFocused()) {
            List<String> suggestions = getSuggestions();
            int left = width / 2 - 120;
            int top = height / 2 + 7;
            if (mouseX >= left && mouseX < left + 240 && mouseY >= top) {
                int row = (int) (mouseY - top) / 18;
                if (row < suggestions.size()) {
                    recipientInput.setValue(suggestions.get(row));
                    rejected = false;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            onClose();
            return true;
        }
        if (keyCode == 257 || keyCode == 335) {
            submit();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void submit() {
        if (submitting) {
            return;
        }
        submitting = true;
        rejected = false;
        PacketDistributor.sendToServer(new SubmitDeliveryPayload(
                packagerPos,
                address,
                recipientInput.getValue().trim()
        ));
    }

    public void handleSubmissionResult(boolean accepted) {
        submitting = false;
        if (accepted) {
            submitted = true;
            onClose();
            return;
        }
        rejected = true;
    }

    @Override
    public void onClose() {
        if (!submitted) {
            PacketDistributor.sendToServer(new CancelDeliveryPayload(packagerPos));
        }
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
