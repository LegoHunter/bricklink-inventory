package com.vattima.bricklink.inventory.service;

import com.vattima.lego.imaging.model.AlbumManifest;
import com.vattima.lego.imaging.service.AlbumManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bricklink.data.lego.dto.BricklinkInventory;
import org.apache.commons.lang3.StringUtils;
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
        conditionDecoder.decode(bricklinkInventory.getBoxConditionId()).ifPresent(c -> {
            append(description, String.format("Box: %s", c));
        });

        // Sealed
        if (bricklinkInventory.getSealed()) {
            append(description, "Sealed");
        }

        // Instructions
        conditionDecoder.decode(bricklinkInventory.getInstructionsConditionId()).ifPresent(c -> {
            append(description, String.format("Instructions: %s", c));
        });

        // Album Manifest
        AlbumManifest albumManifest = albumManager.getAlbumManifest(bricklinkInventory.getUuid(), bricklinkInventory.getBlItemNo());
        if (hasShortUrl(albumManifest)) {
                appendWithNewLine(description, shortUrlLinkBuilder(albumManifest));
        }

        log.info("Description [{}]", description.toString());
        bricklinkInventory.setDescription(description.toString());
        return description.toString();
    }

    public boolean hasShortUrl(final AlbumManifest albumManifest) {
        return Optional.ofNullable(albumManifest).map(AlbumManifest::getShortUrl).isPresent();
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
        private Map<Integer, String> conditionDescriptions = new HashMap<>();

        public ConditionDecoder() {
            conditionDescriptions.put(1, "Mint");
            conditionDescriptions.put(2, "Excellent");
            conditionDescriptions.put(3, "Very Good");
            conditionDescriptions.put(4, "Good");
            conditionDescriptions.put(5, "Poor");
            conditionDescriptions.put(6, "Not Applicable");
            conditionDescriptions.put(7, "Fair");
            conditionDescriptions.put(8, "Missing");
            conditionDescriptions.put(9, "Color Copy");
            conditionDescriptions.put(10, "Black & White Copy");
            conditionDescriptions.put(11, "Sealed");
        }

        public Optional<String> decode(Integer conditionId) {
            return Optional.ofNullable(conditionDescriptions.get(conditionId));
        }
    }
}
