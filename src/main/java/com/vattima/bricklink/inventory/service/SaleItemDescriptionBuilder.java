package com.vattima.bricklink.inventory.service;

import com.vattima.lego.imaging.model.AlbumManifest;
import com.vattima.lego.imaging.service.AlbumManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bricklink.data.lego.dto.BricklinkInventory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class SaleItemDescriptionBuilder {
    private static final String PHOTO_URL_PART = "[<a href=\"%s\" target=\"_blank\">(%d) Photos</a>]";
    private final AlbumManager albumManager;

    public String buildDescription(BricklinkInventory bricklinkInventory) {
        ConditionDecoder conditionDecoder = new ConditionDecoder();
        StringBuilder description = new StringBuilder();

        // Extra Description
        Optional.ofNullable(bricklinkInventory.getExtraDescription()).ifPresent(description::append);

        // Box
        conditionDecoder.decode(bricklinkInventory.getBoxConditionCode()).ifPresent(c -> {
            append(description, String.format("Box: %s", c));
        });

        // Sealed
        if (bricklinkInventory.getSealed()) {
            append(description, "Sealed");
        }

        // Instructions
        conditionDecoder.decode(bricklinkInventory.getInstructionsConditionCode()).ifPresent(c -> {
            append(description, String.format("Instructions: %s", c));
        });

        // Album Manifest
        Optional<AlbumManifest> albumManifest = albumManager.getAlbumManifest(bricklinkInventory.getUuid());
        albumManifest.ifPresent(a -> {
            appendWithNewLine(description, shortUrlLinkBuilder(a));
        });

        log.info("Description [{}]", description.toString());
        return description.toString();
    }

    public String shortUrlLinkBuilder(final AlbumManifest albumManifest) {
        return String.format(PHOTO_URL_PART, albumManifest.getShortUrl().toExternalForm(), albumManifest.getPhotos().size());
    }

    private StringBuilder append(StringBuilder stringBuilder, String s) {
        if (stringBuilder.length() > 0) {
            stringBuilder.append(", ");
        }
        return stringBuilder.append(s);
    }

    private StringBuilder appendWithNewLine(StringBuilder stringBuilder, String s) {
        if (stringBuilder.length() > 0) {
            stringBuilder.append("&nbsp;</br>");
        }
        return stringBuilder.append(s);
    }

    static class ConditionDecoder {
        private Map<String, String> conditionDescriptions = new HashMap<>();

        public ConditionDecoder() {
            conditionDescriptions.put("M", "Mint");
            conditionDescriptions.put("E", "Excellent");
            conditionDescriptions.put("VG", "Very Good");
            conditionDescriptions.put("G", "Good");
            conditionDescriptions.put("P", "Poor");
            conditionDescriptions.put("NA", "Not Applicable");
            conditionDescriptions.put("F", "Fair");
            conditionDescriptions.put("MS", "Missing");
            conditionDescriptions.put("CC", "Color Copy");
            conditionDescriptions.put("BW", "Black & White Copy");
            conditionDescriptions.put("SL", "Sealed");
        }

        public Optional<String> decode(String conditionCode) {
            return Optional.ofNullable(conditionDescriptions.get(conditionCode));
        }
    }
}
