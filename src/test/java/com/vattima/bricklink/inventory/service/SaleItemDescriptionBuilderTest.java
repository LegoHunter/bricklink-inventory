package com.vattima.bricklink.inventory.service;

import com.vattima.bricklink.inventory.BricklinkInventoryException;
import com.vattima.lego.imaging.model.AlbumManifest;
import com.vattima.lego.imaging.model.PhotoMetaData;
import com.vattima.lego.imaging.service.AlbumManager;
import net.bricklink.data.lego.dto.BricklinkInventory;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.vattima.bricklink.inventory.service.SaleItemDescriptionBuilder.ConditionDecoder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class SaleItemDescriptionBuilderTest {

    private String uuid;
    private String shortUrl;
    private AlbumManager albumManager;
    private AlbumManifest albumManifest;

    @Before
    public void setup() {
        uuid = "234851bc94cf3b875dfd91db73a76524";
        shortUrl = "http://bit.ly/2EdZ3z6";
        albumManifest = albumManifest(shortUrl, 5);
        albumManager = mockAlbumManager(uuid, albumManifest);
    }

    @Test
    public void buildDescription_returnsNonNull() {
        albumManifest = albumManifest();
        albumManager = mockAlbumManager(uuid, albumManifest);

        SaleItemDescriptionBuilder saleItemDescriptionBuilder = new SaleItemDescriptionBuilder(albumManager);

        BricklinkInventory bricklinkInventory = new BricklinkInventory();
        bricklinkInventory.setUuid(uuid);
        String description = saleItemDescriptionBuilder.buildDescription(bricklinkInventory);

        assertThat(description).isEmpty();
    }

    @Test
    public void buildDescription_withBoxConditionCode_containsBoxDescription() {
        SaleItemDescriptionBuilder saleItemDescriptionBuilder = new SaleItemDescriptionBuilder(albumManager);

        BricklinkInventory bricklinkInventory = new BricklinkInventory();
        bricklinkInventory.setUuid(uuid);
        bricklinkInventory.setBoxConditionId(1);
        String description = saleItemDescriptionBuilder.buildDescription(bricklinkInventory);
        assertThat(description).contains("Box: Mint");

        bricklinkInventory.setBoxConditionId(2);
        description = saleItemDescriptionBuilder.buildDescription(bricklinkInventory);
        assertThat(description).contains("Box: Excellent");
    }

    @Test
    public void buildDescription_withSealedAndBoxConditionCode_containsBoxDescription() {
        SaleItemDescriptionBuilder saleItemDescriptionBuilder = new SaleItemDescriptionBuilder(albumManager);

        BricklinkInventory bricklinkInventory = new BricklinkInventory();
        bricklinkInventory.setUuid(uuid);
        bricklinkInventory.setSealed(true);
        bricklinkInventory.setBoxConditionId(2);
        String description = saleItemDescriptionBuilder.buildDescription(bricklinkInventory);
        assertThat(description).contains("Box: Excellent");
        assertThat(description).contains("Sealed");
    }

    @Test
    public void buildDescription_withExtraDescription() {
        SaleItemDescriptionBuilder saleItemDescriptionBuilder = new SaleItemDescriptionBuilder(albumManager);

        BricklinkInventory bricklinkInventory = new BricklinkInventory();
        bricklinkInventory.setUuid(uuid);
        bricklinkInventory.setExtraDescription("This is an awesome set!");
        bricklinkInventory.setSealed(true);
        bricklinkInventory.setBoxConditionId(2);
        bricklinkInventory.setInstructionsConditionId(1);
        String description = saleItemDescriptionBuilder.buildDescription(bricklinkInventory);
        assertThat(description).contains("This is an awesome set!");
        assertThat(description).contains("Box: Excellent");
        assertThat(description).contains("Sealed");
    }

    @Test
    public void buildDescription_withInstructionsConditionCode_containsInstructionsDescription() {
        SaleItemDescriptionBuilder saleItemDescriptionBuilder = new SaleItemDescriptionBuilder(albumManager);

        BricklinkInventory bricklinkInventory = new BricklinkInventory();
        bricklinkInventory.setUuid(uuid);
        bricklinkInventory.setInstructionsConditionId(1);
        String description = saleItemDescriptionBuilder.buildDescription(bricklinkInventory);
        assertThat(description).contains("Instructions: Mint");

        bricklinkInventory.setInstructionsConditionId(8);
        description = saleItemDescriptionBuilder.buildDescription(bricklinkInventory);
        assertThat(description).contains("Instructions: Missing");

        bricklinkInventory.setInstructionsConditionId(9);
        description = saleItemDescriptionBuilder.buildDescription(bricklinkInventory);
        assertThat(description).contains("Instructions: Color Copy");

        bricklinkInventory.setInstructionsConditionId(10);
        description = saleItemDescriptionBuilder.buildDescription(bricklinkInventory);
        assertThat(description).contains("Instructions: Black & White Copy");
    }

    @Test
    public void buildDescription_withBoxAndInstructionsConditionCode_containsBoxAndInstructionsDescription() {
        SaleItemDescriptionBuilder saleItemDescriptionBuilder = new SaleItemDescriptionBuilder(albumManager);

        BricklinkInventory bricklinkInventory = new BricklinkInventory();
        bricklinkInventory.setUuid(uuid);
        bricklinkInventory.setBoxConditionId(2);
        bricklinkInventory.setInstructionsConditionId(3);
        String description = saleItemDescriptionBuilder.buildDescription(bricklinkInventory);
        assertThat(description).contains("Box: Excellent");
        assertThat(description).contains("Instructions: Very Good");
    }

    @Test
    public void conditionDecoder_returnsCondition_forKnownValue() {
        ConditionDecoder conditionDecoder = new ConditionDecoder();
        Optional<String> condition = conditionDecoder.decode(1);
        assertThat(condition).isPresent().get().isEqualTo("Mint");
    }

    @Test
    public void conditionDecoder_returnsNotPresent_forUnknownValue() {
        ConditionDecoder conditionDecoder = new ConditionDecoder();
        Optional<String> condition = conditionDecoder.decode(99);
        assertThat(condition).isNotPresent();
    }

    private AlbumManifest albumManifest() {
        return null;
    }

    private AlbumManifest albumManifest(String shortUrl, int photos) {
        AlbumManifest albumManifest = new AlbumManifest();
        try {
            albumManifest.setShortUrl(new URL(shortUrl));
            List<PhotoMetaData> photosList = albumManifest.getPhotos();
            IntStream.range(0, photos)
                     .forEach(i -> photosList.add(new PhotoMetaData(Paths.get("./" + i + ".jpg"))));
            return albumManifest;
        } catch (MalformedURLException e) {
            throw new BricklinkInventoryException(e);
        }
    }

    private AlbumManager mockAlbumManager(String uuid, AlbumManifest albumManifest) {
        AlbumManager albumManager = mock(AlbumManager.class);
        doReturn(Optional.ofNullable(albumManifest)).when(albumManager)
                                                    .getAlbumManifest(eq(uuid));
        return albumManager;
    }
}