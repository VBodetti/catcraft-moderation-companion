package net.catcraft.ccmc.report;

import com.mojang.blaze3d.platform.NativeImage;
import net.catcraft.ccmc.client.CcmcText;
import net.catcraft.ccmc.client.ClientFeedback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DiscordReportService {
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss", Locale.ROOT);
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
    private static final int CAPTURE_DELAY_TICKS = 2;
    private static PendingReport pending;

    private DiscordReportService() {}

    public static void queue(String player, String offense, String punishment) {
        pending = new PendingReport(safe(player), safe(offense), safe(punishment), LocalDateTime.now(), CAPTURE_DELAY_TICKS);
        local("[CCMC] Discord report queued; capturing evidence.");
    }

    public static void tick(Minecraft client) {
        PendingReport current = pending;
        if (current == null) return;
        if (current.ticksRemaining() > 0) {
            pending = current.withTicksRemaining(current.ticksRemaining() - 1);
            return;
        }
        pending = null;
        capture(client, current);
    }

    private static void capture(Minecraft client, PendingReport report) {
        if (client.gameRenderer == null || client.gameRenderer.mainRenderTarget() == null) {
            fallbackText(client, report, "screenshot unavailable");
            return;
        }
        Screenshot.takeScreenshot(client.gameRenderer.mainRenderTarget(), image -> saveAndCopy(client, report, image));
    }

    private static void saveAndCopy(Minecraft client, PendingReport report, NativeImage image) {
        String stamp = FILE_TIME.format(report.createdAt());
        String playerFile = sanitizeFilename(report.player());
        Path reportDir = FabricLoader.getInstance().getGameDir().resolve("screenshots").resolve("ccmc-reports");
        Path rawPath = reportDir.resolve(stamp + "_" + playerFile + "_evidence.png");
        Path reportPath = reportDir.resolve(stamp + "_" + playerFile + "_discord-report.png");

        try {
            Files.createDirectories(reportDir);
            image.writeToFile(rawPath);

            BufferedImage evidence = ImageIO.read(rawPath.toFile());
            if (evidence == null) throw new IOException("Unable to decode captured screenshot");

            BufferedImage reportImage = composeReportImage(evidence, report);
            if (!ImageIO.write(reportImage, "png", reportPath.toFile())) throw new IOException("No PNG writer available");

            String reportText = textReport(report, rawPath);
            try {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new ReportTransferable(reportImage, reportText), null);
                local("[CCMC] Discord report copied to clipboard. Paste it directly into Discord.");
                local("[CCMC] Raw evidence saved: " + rawPath.getFileName());
            } catch (RuntimeException clipboardFailure) {
                client.keyboardHandler.setClipboard(reportText);
                local("[CCMC] Image clipboard unavailable; report text copied instead.");
                local("[CCMC] Attach: " + reportPath.toAbsolutePath());
            }
        } catch (Exception error) {
            fallbackText(client, report, "screenshot save failed: " + error.getMessage());
        } finally {
            image.close();
        }
    }

    private static BufferedImage composeReportImage(BufferedImage evidence, PendingReport report) {
        int margin = Math.max(24, evidence.getWidth() / 45);
        int titleSize = Math.max(24, evidence.getWidth() / 44);
        int bodySize = Math.max(18, evidence.getWidth() / 62);
        int firstBodyLine = margin + titleSize + bodySize + 12;
        int spacing = bodySize + 10;
        int headerHeight = firstBodyLine + spacing * 3 + margin;

        BufferedImage output = new BufferedImage(evidence.getWidth(), headerHeight + evidence.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = output.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(new Color(24, 27, 32));
            g.fillRect(0, 0, output.getWidth(), headerHeight);

            g.setColor(Color.WHITE);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, titleSize));
            g.drawString("CatCraft Moderation Report", margin, margin + titleSize);

            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, bodySize));
            g.drawString("Player: " + report.player(), margin, firstBodyLine);
            g.drawString("Offense: " + report.offense(), margin, firstBodyLine + spacing);
            g.drawString("Punishment: " + report.punishment(), margin, firstBodyLine + spacing * 2);
            g.drawString("Evidence captured: " + DISPLAY_TIME.format(report.createdAt()), margin, firstBodyLine + spacing * 3);
            g.drawImage(evidence, 0, headerHeight, null);
        } finally {
            g.dispose();
        }
        return output;
    }

    private static String textReport(PendingReport report, Path rawPath) {
        return "Player: " + report.player() + "\n"
                + "Offense: " + report.offense() + "\n"
                + "Punishment Given: " + report.punishment() + "\n"
                + "Evidence: " + rawPath.toAbsolutePath();
    }

    private static void fallbackText(Minecraft client, PendingReport report, String reason) {
        client.keyboardHandler.setClipboard("Player: " + report.player() + "\n"
                + "Offense: " + report.offense() + "\n"
                + "Punishment Given: " + report.punishment() + "\n"
                + "Evidence: Screenshot unavailable");
        local("[CCMC] Discord report text copied; " + reason + ".");
    }

    private static String sanitizeFilename(String value) {
        String sanitized = value.replaceAll("[^A-Za-z0-9._-]", "_");
        return sanitized.isBlank() ? "player" : sanitized;
    }

    private static String safe(String value) {
        if (value == null || value.isBlank()) return "Unspecified";
        return value.strip();
    }

    private static void local(String message) {
        ClientFeedback.send(CcmcText.literal(message));
    }

    private record PendingReport(String player, String offense, String punishment, LocalDateTime createdAt, int ticksRemaining) {
        private PendingReport withTicksRemaining(int ticks) {
            return new PendingReport(player, offense, punishment, createdAt, ticks);
        }
    }

    private static final class ReportTransferable implements Transferable {
        private static final DataFlavor[] FLAVORS = {DataFlavor.imageFlavor, DataFlavor.stringFlavor};
        private final BufferedImage image;
        private final String text;

        private ReportTransferable(BufferedImage image, String text) {
            this.image = image;
            this.text = text;
        }

        @Override public DataFlavor[] getTransferDataFlavors() { return FLAVORS.clone(); }

        @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor) || DataFlavor.stringFlavor.equals(flavor);
        }

        @Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (DataFlavor.imageFlavor.equals(flavor)) return image;
            if (DataFlavor.stringFlavor.equals(flavor)) return text;
            throw new UnsupportedFlavorException(flavor);
        }
    }
}
