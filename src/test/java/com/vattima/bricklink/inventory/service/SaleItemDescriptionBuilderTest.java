package com.vattima.bricklink.inventory.service;

import com.vattima.bricklink.inventory.BricklinkInventoryException;
import com.vattima.lego.imaging.config.LegoImagingProperties;
import com.vattima.lego.imaging.model.AlbumManifest;
import com.vattima.lego.imaging.model.PhotoMetaData;
import com.vattima.lego.imaging.service.AlbumManager;
import com.vattima.lego.imaging.service.flickr.AlbumManagerImpl;
import com.vattima.lego.imaging.service.flickr.ImageManagerImpl;
import com.vattima.lego.imaging.util.PathUtils;
import lombok.extern.slf4j.Slf4j;
import net.bricklink.data.lego.dao.BricklinkInventoryDao;
import net.bricklink.data.lego.dto.BricklinkInventory;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.vattima.bricklink.inventory.service.SaleItemDescriptionBuilder.ConditionDecoder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@Slf4j
public class SaleItemDescriptionBuilderTest {

    private String uuid;
    private String blItemNumber;
    private String shortUrl;
    private AlbumManager albumManager;
    private AlbumManifest albumManifest;
    private LegoImagingProperties legoImagingProperties;

    @Before
    public void setup() {
        uuid = "234851bc94cf3b875dfd91db73a76524";
        blItemNumber = "6658-1";
        shortUrl = "http://bit.ly/2EdZ3z6";
        albumManifest = albumManifest(uuid, blItemNumber, shortUrl, 5);
        albumManager = mockAlbumManager(uuid, blItemNumber, albumManifest);
        legoImagingProperties = new LegoImagingProperties();
        legoImagingProperties.setKeywordsKeyName("Keywords:");
    }

    @Test
    public void photosWithRemarks_buildsDescriptionWithExtraDescriptions() throws Exception {
        Path jpgPath = PathUtils.fromClasspath("actual-lego-photos-with-keywords-and-remarks/6603-1-c93848ddb00ec2d6f20cfafd0eea46ef");
        legoImagingProperties.setRootImagesFolder(jpgPath.toFile()
                                                         .getAbsolutePath());
        jpgPath.forEach(p -> {
            log.info("p=[{}]", p);
        });
        BricklinkInventory bricklinkInventory = new BricklinkInventory();
        bricklinkInventory.setBlItemNo("6603-1");
        bricklinkInventory.setUuid("c93848ddb00ec2d6f20cfafd0eea46ef");
        bricklinkInventory.setBoxConditionId(2);
        bricklinkInventory.setInstructionsConditionId(2);
        bricklinkInventory.setSealed(false);
        BricklinkInventoryDao bricklinkInventoryDao = mock(BricklinkInventoryDao.class);
        doReturn(bricklinkInventory).when(bricklinkInventoryDao)
                                    .getByUuid("c93848ddb00ec2d6f20cfafd0eea46ef");
        AlbumManager localAlbumManager = new AlbumManagerImpl(new ImageManagerImpl(), legoImagingProperties, bricklinkInventoryDao);
        localAlbumManager.addPhoto(new PhotoMetaData(jpgPath.resolve("DSC_0704.JPG")));
        localAlbumManager.addPhoto(new PhotoMetaData(jpgPath.resolve("DSC_0705.JPG")));
        localAlbumManager.addPhoto(new PhotoMetaData(jpgPath.resolve("DSC_0706.JPG")));
        localAlbumManager.addPhoto(new PhotoMetaData(jpgPath.resolve("DSC_0707.JPG")));
        localAlbumManager.addPhoto(new PhotoMetaData(jpgPath.resolve("DSC_0708.JPG")));

        SaleItemDescriptionBuilder saleItemDescriptionBuilder = new SaleItemDescriptionBuilder(localAlbumManager);
        String description = saleItemDescriptionBuilder.buildDescription(bricklinkInventory);
        assertThat(description).isNotEmpty()
                               .contains("This is caption #1.")
                               .contains("This is caption #2.")
                               .contains("This is caption #3.")
                               .contains("This is caption #4.")
                               .contains("This is caption #5.");

    }

    @Test
    public void photosWithSomeRemarks_buildsDescriptionWithExtraDescriptions() throws Exception {
        Path jpgPath = PathUtils.fromClasspath("actual-lego-photos-with-keywords-and-some-remarks/6603-1-c93848ddb00ec2d6f20cfafd0eea46ef");
        legoImagingProperties.setRootImagesFolder(jpgPath.toFile()
                                                         .getAbsolutePath());
        jpgPath.forEach(p -> {
            log.info("p=[{}]", p);
        });
        BricklinkInventory bricklinkInventory = new BricklinkInventory();
        bricklinkInventory.setBlItemNo("6603-1");
        bricklinkInventory.setUuid("c93848ddb00ec2d6f20cfafd0eea46ef");
        bricklinkInventory.setBoxConditionId(2);
        bricklinkInventory.setInstructionsConditionId(2);
        bricklinkInventory.setSealed(false);
        BricklinkInventoryDao bricklinkInventoryDao = mock(BricklinkInventoryDao.class);
        doReturn(bricklinkInventory).when(bricklinkInventoryDao)
                                    .getByUuid("c93848ddb00ec2d6f20cfafd0eea46ef");
        AlbumManager localAlbumManager = new AlbumManagerImpl(new ImageManagerImpl(), legoImagingProperties, bricklinkInventoryDao);
        localAlbumManager.addPhoto(new PhotoMetaData(jpgPath.resolve("DSC_0704.JPG")));
        localAlbumManager.addPhoto(new PhotoMetaData(jpgPath.resolve("DSC_0705.JPG")));
        localAlbumManager.addPhoto(new PhotoMetaData(jpgPath.resolve("DSC_0706.JPG")));
        localAlbumManager.addPhoto(new PhotoMetaData(jpgPath.resolve("DSC_0707.JPG")));
        localAlbumManager.addPhoto(new PhotoMetaData(jpgPath.resolve("DSC_0708.JPG")));

        SaleItemDescriptionBuilder saleItemDescriptionBuilder = new SaleItemDescriptionBuilder(localAlbumManager);
        String description = saleItemDescriptionBuilder.buildDescription(bricklinkInventory);
        assertThat(description).isNotEmpty()
                               .contains("Box flap shows repairs made with heavy card stock glued underneath. All original pieces like new. Includes box, instructions, and all original pieces.");
    }

    @Test
    public void buildDescription_returnsNonNull() {
        albumManager = mockAlbumManager(uuid, blItemNumber, albumManifest);

        SaleItemDescriptionBuilder saleItemDescriptionBuilder = new SaleItemDescriptionBuilder(albumManager);

        BricklinkInventory bricklinkInventory = new BricklinkInventory();
        bricklinkInventory.setUuid(uuid);
        bricklinkInventory.setBlItemNo(blItemNumber);
        String description = saleItemDescriptionBuilder.buildDescription(bricklinkInventory);

        assertThat(description).isNotEmpty().contains(shortUrl).contains("(" + albumManifest.getPhotos().size() + ") Photos");
    }

    @Test
    public void buildDescription_withBoxConditionCode_containsBoxDescription() {
        SaleItemDescriptionBuilder saleItemDescriptionBuilder = new SaleItemDescriptionBuilder(albumManager);

        BricklinkInventory bricklinkInventory = new BricklinkInventory();
        bricklinkInventory.setUuid(uuid);
        bricklinkInventory.setBlItemNo(blItemNumber);
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
        bricklinkInventory.setBlItemNo(blItemNumber);
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
        bricklinkInventory.setBlItemNo(blItemNumber);
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
        bricklinkInventory.setBlItemNo(blItemNumber);
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
    public void buildDescription_withNoPhotos_containsMessageToContactMe() {
        final String uuid = "455a2f76a89dcf372ac56fced641b5e5";
        final String blItemNumber = "6604-1";
        final AlbumManifest albumManifest = albumManifest(uuid, blItemNumber);
        albumManager = mockAlbumManager(uuid, blItemNumber, albumManifest);

        SaleItemDescriptionBuilder saleItemDescriptionBuilder = new SaleItemDescriptionBuilder(albumManager);

        BricklinkInventory bricklinkInventory = new BricklinkInventory();
        bricklinkInventory.setUuid(uuid);
        bricklinkInventory.setBlItemNo(blItemNumber);
        bricklinkInventory.setInstructionsConditionId(1);
        String description = saleItemDescriptionBuilder.buildDescription(bricklinkInventory);
        assertThat(description).contains("Instructions: Mint").contains("Contact me for photos!").doesNotContain("[<a href=").doesNotContain(") Photos</a>]");
    }

    @Test
    public void buildDescription_withBoxAndInstructionsConditionCode_containsBoxAndInstructionsDescription() {
        SaleItemDescriptionBuilder saleItemDescriptionBuilder = new SaleItemDescriptionBuilder(albumManager);

        BricklinkInventory bricklinkInventory = new BricklinkInventory();
        bricklinkInventory.setUuid(uuid);
        bricklinkInventory.setBlItemNo(blItemNumber);
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

    private AlbumManifest albumManifest(String uuid, String blItemNumber, String shortUrl, int photos) {
        AlbumManifest albumManifest = new AlbumManifest();
        albumManifest.setUuid(uuid);
        albumManifest.setBlItemNumber(blItemNumber);
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

    private AlbumManifest albumManifest(String uuid, String blItemNumber) {
        AlbumManifest albumManifest = new AlbumManifest();
        albumManifest.setUuid(uuid);
        albumManifest.setBlItemNumber(blItemNumber);
        return albumManifest;
    }

    private AlbumManager mockAlbumManager(String uuid, String blItemNumber, AlbumManifest albumManifest) {
        AlbumManager albumManager = mock(AlbumManager.class);
        doReturn(albumManifest).when(albumManager)
                               .getAlbumManifest(eq(uuid), eq(blItemNumber));
        return albumManager;
    }
}