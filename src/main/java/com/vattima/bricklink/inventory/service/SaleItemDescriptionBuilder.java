package com.vattima.bricklink.inventory.service;

import com.vattima.bricklink.inventory.BricklinkInventoryException;
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
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class SaleItemDescriptionBuilder {
    private static final String PHOTO_URL_PART = "[<a href=\"%s\" target=\"_blank\">(%d) Photos</a>]";
    private static final String EXTENDED_DESCRIPTION_NOTE = "(See extended description)";
    private final AlbumManager albumManager;

    public String buildDescription(BricklinkInventory bricklinkInventory) {
        ConditionDecoder conditionDecoder = new ConditionDecoder();
        StringBuilder description = new StringBuilder();
        StringBuilder extendedDescription = new StringBuilder();

        // Album Manifest
        AlbumManifest albumManifest = albumManager.getAlbumManifest(bricklinkInventory.getUuid(), bricklinkInventory.getBlItemNo());

        // Box
        conditionDecoder.decode(bricklinkInventory.getBoxConditionId()).ifPresent(c -> {
            append(description, "Box: %s".formatted(c));
        });

        // Sealed
        if (bricklinkInventory.getSealed()) {
            append(description, "Sealed");
        }

        // Instructions
        conditionDecoder.decode(bricklinkInventory.getInstructionsConditionId()).ifPresent(c -> {
            append(description, "Instructions: %s".formatted(c));
        });

        if (hasShortUrl(albumManifest)) {
                appendWithNewLine(description, shortUrlLinkBuilder(albumManifest));
        } else {
            appendWithNewLine(description, "Contact me for photos!");
        }

        // Get all remarks from all photos
        Optional.ofNullable(
                StringUtils.trimToNull(albumManifest.getPhotos()
                                                    .stream()
                                                    .map(p -> Optional.ofNullable(p.getKeyword("cp")))
                                                    .flatMap(Optional::stream)
                                                    .collect(Collectors.joining(" "))))
                .ifPresent(extendedDescription::append);

        // Extra Description
        Optional.ofNullable(bricklinkInventory.getExtraDescription()).ifPresent(extendedDescription::append);

        // Bricklink only allows a Description length of 255. Put overflow into the Extra Description
        if ((description.length() + 1 + extendedDescription.length()) > 255) {
            if ((description.length() + 1 + EXTENDED_DESCRIPTION_NOTE.length()) > 255) {
                String fullDescription = description.toString() + " " + EXTENDED_DESCRIPTION_NOTE;
                throw new BricklinkInventoryException("Length [%d] of Description [%s] for bricklink item [%s]".formatted(description.length() + 1 + EXTENDED_DESCRIPTION_NOTE.length(), fullDescription, bricklinkInventory.getBlItemNo()));
            } else {
                description.append(" ").append(EXTENDED_DESCRIPTION_NOTE);
                bricklinkInventory.setExtendedDescription(extendedDescription.toString());
                log.info("Extended Description [{}]", extendedDescription.toString());
            }
        } else {
            description.append(" ").append(extendedDescription);
        }

        log.info("Description [{}]", description.toString());
        bricklinkInventory.setDescription(description.toString());
        return description.toString();
    }

    public boolean hasShortUrl(final AlbumManifest albumManifest) {
        return Optional.ofNullable(albumManifest).map(AlbumManifest::getShortUrl).isPresent();
    }

    public String shortUrlLinkBuilder(final AlbumManifest albumManifest) {
        return PHOTO_URL_PART.formatted(albumManifest.getShortUrl().toExternalForm(), albumManifest.getPhotos().size());
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
