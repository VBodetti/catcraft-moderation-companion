package net.catcraft.ccmc.report;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.imageio.ImageIO;
import net.catcraft.ccmc.client.CcmcText;
import net.catcraft.ccmc.client.ClientFeedback;
import net.catcraft.ccmc.platform.PlatformBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;

public final class DiscordReportService {
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss", Locale.ROOT);
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
    private static final int CAPTURE_DELAY_TICKS = 2;
    private static PendingReport pending;

    private DiscordReportService() {
    }

    public static void queue(String player, String offense, String punishment) {
        pending = new PendingReport(DiscordReportService.safe(player), DiscordReportService.safe(offense), DiscordReportService.safe(punishment), LocalDateTime.now(), 2);
        DiscordReportService.local("[CCC] Discord report queued; capturing evidence.");
    }

    public static void tick(Minecraft client) {
        PendingReport current = pending;
        if (current == null) {
            return;
        }
        if (current.ticksRemaining() > 0) {
            pending = current.withTicksRemaining(current.ticksRemaining() - 1);
            return;
        }
        pending = null;
        DiscordReportService.capture(client, current);
    }

    private static void capture(Minecraft client, PendingReport report) {
        if (client.gameRenderer == null || client.gameRenderer.mainRenderTarget() == null) {
            DiscordReportService.fallbackText(client, report, "screenshot unavailable");
            return;
        }
        Screenshot.takeScreenshot((RenderTarget)client.gameRenderer.mainRenderTarget(), image -> DiscordReportService.saveAndCopy(client, report, image));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void saveAndCopy(Minecraft client, PendingReport report, NativeImage image) {
        String stamp = FILE_TIME.format(report.createdAt());
        String playerFile = DiscordReportService.sanitizeFilename(report.player());
        Path reportDir = PlatformBridge.gameDirectory().resolve("screenshots").resolve("ccmc-reports");
        Path rawPath = reportDir.resolve(stamp + "_" + playerFile + "_evidence.png");
        Path reportPath = reportDir.resolve(stamp + "_" + playerFile + "_discord-report.png");
        try {
            Files.createDirectories(reportDir, new FileAttribute[0]);
            image.writeToFile(rawPath);
            BufferedImage evidence = ImageIO.read(rawPath.toFile());
            if (evidence == null) {
                throw new IOException("Unable to decode captured screenshot");
            }
            BufferedImage reportImage = DiscordReportService.composeReportImage(evidence, report);
            if (!ImageIO.write((RenderedImage)reportImage, "png", reportPath.toFile())) {
                throw new IOException("No PNG writer available");
            }
            String reportText = DiscordReportService.textReport(report, rawPath);
            try {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new ReportTransferable(reportImage, reportText), null);
                DiscordReportService.local("[CCC] Discord report copied to clipboard. Paste it directly into Discord.");
                DiscordReportService.local("[CCC] Raw evidence saved: " + String.valueOf(rawPath.getFileName()));
            }
            catch (RuntimeException clipboardFailure) {
                client.keyboardHandler.setClipboard(reportText);
                DiscordReportService.local("[CCC] Image clipboard unavailable; report text copied instead.");
                DiscordReportService.local("[CCC] Attach: " + String.valueOf(reportPath.toAbsolutePath()));
            }
        }
        catch (Exception error) {
            DiscordReportService.fallbackText(client, report, "screenshot save failed: " + error.getMessage());
        }
        finally {
            image.close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static BufferedImage composeReportImage(BufferedImage evidence, PendingReport report) {
        int margin = Math.max(24, evidence.getWidth() / 45);
        int titleSize = Math.max(24, evidence.getWidth() / 44);
        int bodySize = Math.max(18, evidence.getWidth() / 62);
        int firstBodyLine = margin + titleSize + bodySize + 12;
        int spacing = bodySize + 10;
        int headerHeight = firstBodyLine + spacing * 3 + margin;
        BufferedImage output = new BufferedImage(evidence.getWidth(), headerHeight + evidence.getHeight(), 2);
        Graphics2D g = output.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(new Color(24, 27, 32));
            g.fillRect(0, 0, output.getWidth(), headerHeight);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", 1, titleSize));
            g.drawString("CatCraft Moderation Report", margin, margin + titleSize);
            g.setFont(new Font("SansSerif", 0, bodySize));
            g.drawString("Player: " + report.player(), margin, firstBodyLine);
            g.drawString("Offense: " + report.offense(), margin, firstBodyLine + spacing);
            g.drawString("Punishment: " + report.punishment(), margin, firstBodyLine + spacing * 2);
            g.drawString("Evidence captured: " + DISPLAY_TIME.format(report.createdAt()), margin, firstBodyLine + spacing * 3);
            g.drawImage((Image)evidence, 0, headerHeight, null);
        }
        finally {
            g.dispose();
        }
        return output;
    }

    private static String textReport(PendingReport report, Path rawPath) {
        return "Player: " + report.player() + "\nOffense: " + report.offense() + "\nPunishment Given: " + report.punishment() + "\nEvidence: " + String.valueOf(rawPath.toAbsolutePath());
    }

    private static void fallbackText(Minecraft client, PendingReport report, String reason) {
        client.keyboardHandler.setClipboard("Player: " + report.player() + "\nOffense: " + report.offense() + "\nPunishment Given: " + report.punishment() + "\nEvidence: Screenshot unavailable");
        DiscordReportService.local("[CCC] Discord report text copied; " + reason + ".");
    }

    private static String sanitizeFilename(String value) {
        String sanitized = value.replaceAll("[^A-Za-z0-9._-]", "_");
        return sanitized.isBlank() ? "player" : sanitized;
    }

    private static String safe(String value) {
        if (value == null || value.isBlank()) {
            return "Unspecified";
        }
        return value.strip();
    }

    private static void local(String message) {
        ClientFeedback.send((Component)CcmcText.literal(message));
    }

    private record PendingReport(String player, String offense, String punishment, LocalDateTime createdAt, int ticksRemaining) {
        private PendingReport withTicksRemaining(int ticks) {
            return new PendingReport(this.player, this.offense, this.punishment, this.createdAt, ticks);
        }
    }

    private static final class ReportTransferable
    implements Transferable {
        private static final DataFlavor[] FLAVORS = new DataFlavor[]{DataFlavor.imageFlavor, DataFlavor.stringFlavor};
        private final BufferedImage image;
        private final String text;

        private ReportTransferable(BufferedImage image, String text) {
            this.image = image;
            this.text = text;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return (DataFlavor[])FLAVORS.clone();
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor) || DataFlavor.stringFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (DataFlavor.imageFlavor.equals(flavor)) {
                return this.image;
            }
            if (DataFlavor.stringFlavor.equals(flavor)) {
                return this.text;
            }
            throw new UnsupportedFlavorException(flavor);
        }
    }
}
