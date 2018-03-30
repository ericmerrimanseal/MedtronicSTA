package com.seal.contracts.ws.client.seal.meta;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.seal.contracts.generator.csv.enums.seal.ReviewType;
import org.junit.Test;

import java.io.InputStream;

import static com.seal.contracts.generator.csv.enums.seal.ReviewType.SUPPORTING_DOCUMENT;
import static com.seal.contracts.generator.csv.enums.seal.ReviewType.UNKNOWN;
import static com.seal.contracts.generator.csv.service.Json.JSON;
import static org.junit.Assert.*;

/**
 * Created by jantonak on 06/09/17.
 */
public class MetaDataTest {

    @Test
    public void testFindItem_Exists() throws Exception {
        Item item = new Item("Title", Lists.newArrayList(new Value("Some title", "String")));
        SealContractsResponse.MetaData metaData = new SealContractsResponse.MetaData(Lists.newArrayList(item));
        Optional<Item> found = metaData.findItem("Title");
        assertTrue(found.isPresent());
    }

    @Test
    public void testFindItem_NotExists() throws Exception {
        SealContractsResponse.MetaData metaData = new SealContractsResponse.MetaData(Lists.newArrayList());
        Optional<Item> item = metaData.findItem("Title");
        assertFalse(item.isPresent());
    }

    @Test
    public void testGetDocumentType() throws Exception {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("data/seal/pull/metadata.json");
        SealContractsResponse response = (SealContractsResponse) JSON.toObject(stream, SealContractsResponse.class);
        assertEquals("C4", response.getMetaData().getDocumentType().get());
    }

    @Test
    public void testGetLegacyContractId() throws Exception {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("data/seal/pull/metadata.json");
        SealContractsResponse response = (SealContractsResponse) JSON.toObject(stream, SealContractsResponse.class);
        assertEquals("BP00214978", response.getMetaData().getLegacyContractId().get());
    }

    @Test
    public void testGetOwner() throws Exception {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("data/seal/pull/metadata.json");
        SealContractsResponse response = (SealContractsResponse) JSON.toObject(stream, SealContractsResponse.class);
        assertEquals("Tipping, Eliza", response.getMetaData().getOwner().get());
    }

    @Test
    public void testGetReviewType() throws Exception {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("data/seal/pull/metadata.json");
        SealContractsResponse response = (SealContractsResponse) JSON.toObject(stream, SealContractsResponse.class);
        assertEquals("Supporting Document", response.getMetaData().getReviewType().getLabel());
    }

    @Test
    public void testGetReviewComplete() throws Exception {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("data/seal/pull/metadata.json");
        SealContractsResponse response = (SealContractsResponse) JSON.toObject(stream, SealContractsResponse.class);
        assertTrue(response.getMetaData().getReviewComplete());
    }

    @Test
    public void testGetMigrationReady() throws Exception {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("data/seal/pull/metadata.json");
        SealContractsResponse response = (SealContractsResponse) JSON.toObject(stream, SealContractsResponse.class);
        assertFalse(response.getMetaData().getMigrationReady());
    }



    @Test
    public void testMigrationReadyNotExist() throws Exception {
        SealContractsResponse.MetaData metaData = new SealContractsResponse.MetaData(Lists.newArrayList());
        assertFalse(metaData.getMigrationReady());
    }

    @Test
    public void testMigrationReadyExistsWithYes() throws Exception {
        Item item = new Item("MigrationReady", Lists.newArrayList(new Value("Yes", "String")));
        SealContractsResponse.MetaData metaData = new SealContractsResponse.MetaData(Lists.newArrayList(item));
        assertTrue(metaData.getMigrationReady());
    }

    @Test
    public void testMigrationReadyExistsWithNotYes() throws Exception {
        Item item = new Item("MigrationReady", Lists.newArrayList(new Value("other than yes", "String")));
        SealContractsResponse.MetaData metaData = new SealContractsResponse.MetaData(Lists.newArrayList(item));
        assertFalse(metaData.getMigrationReady());
    }

    @Test
    public void testReviewCompleteNotExist() throws Exception {
        SealContractsResponse.MetaData metaData = new SealContractsResponse.MetaData(Lists.newArrayList());
        assertFalse(metaData.getReviewComplete());
    }

    @Test
    public void testReviewCompleteExistsWithYes() throws Exception {
        Item item = new Item("ReviewComplete", Lists.newArrayList(new Value("Yes", "String")));
        SealContractsResponse.MetaData metaData = new SealContractsResponse.MetaData(Lists.newArrayList(item));
        assertTrue(metaData.getReviewComplete());
    }

    @Test
    public void testReviewCompleteExistsNotYes() throws Exception {
        Item item = new Item("ReviewComplete", Lists.newArrayList(new Value("other than yes", "String")));
        SealContractsResponse.MetaData metaData = new SealContractsResponse.MetaData(Lists.newArrayList(item));
        assertFalse(metaData.getReviewComplete());
    }

    @Test
    public void testReviewTypeNotExist() throws Exception {
        SealContractsResponse.MetaData metaData = new SealContractsResponse.MetaData(Lists.newArrayList());
        assertEquals(UNKNOWN, metaData.getReviewType());
    }

    @Test
    public void testReviewTypeExistsWithPrimary() throws Exception {
        Item item = new Item("ReviewType", Lists.newArrayList(new Value("Primary Review Contract Document", "String")));
        SealContractsResponse.MetaData metaData = new SealContractsResponse.MetaData(Lists.newArrayList(item));
        assertEquals(ReviewType.PRIMARY_DOCUMENT, metaData.getReviewType());
    }

    @Test
    public void testReviewTypeExistsEmpty() throws Exception {
        Item item = new Item("ReviewType", Lists.newArrayList(new Value("", "String")));
        SealContractsResponse.MetaData metaData = new SealContractsResponse.MetaData(Lists.newArrayList(item));
        assertEquals(ReviewType.UNKNOWN, metaData.getReviewType());
    }

    @Test
    public void testReviewCompleteExistsWithSupporting() throws Exception {
        Item item = new Item("ReviewType", Lists.newArrayList(new Value("Supporting Document", "String")));
        SealContractsResponse.MetaData metaData = new SealContractsResponse.MetaData(Lists.newArrayList(item));
        assertEquals(SUPPORTING_DOCUMENT, metaData.getReviewType());
    }

    @Test
    public void testReviewCompleteExistsWithUnknown() throws Exception {
        Item item = new Item("ReviewType", Lists.newArrayList(new Value("unknown other review type", "String")));
        SealContractsResponse.MetaData metaData = new SealContractsResponse.MetaData(Lists.newArrayList(item));
        assertEquals(UNKNOWN, metaData.getReviewType());
    }

    @Test
    public void testDocumentTypeNotExists() throws Exception {
        SealContractsResponse.MetaData metaData = new SealContractsResponse.MetaData(Lists.newArrayList());
        assertFalse(metaData.getDocumentType().isPresent());
    }

    @Test
    public void testDocumentTypeExists() throws Exception {
        Item item = new Item("DocumentType", Lists.newArrayList(new Value("C4", "String")));
        SealContractsResponse.MetaData metaData = new SealContractsResponse.MetaData(Lists.newArrayList(item));
        assertTrue(metaData.getDocumentType().isPresent());
    }

    @Test
    public void testLegacyContractIdNotExists() throws Exception {
        SealContractsResponse.MetaData metaData = new SealContractsResponse.MetaData(Lists.newArrayList());
        assertFalse(metaData.getLegacyContractId().isPresent());
    }

    @Test
    public void testLegacyContractIdExists() throws Exception {
        Item item = new Item("BPContractID", Lists.newArrayList(new Value("BPXXXXX", "String")));
        SealContractsResponse.MetaData metaData = new SealContractsResponse.MetaData(Lists.newArrayList(item));
        assertTrue(metaData.getLegacyContractId().isPresent());
    }

    @Test
    public void testOwnerNotExists() throws Exception {
        SealContractsResponse.MetaData metaData = new SealContractsResponse.MetaData(Lists.newArrayList());
        assertFalse(metaData.getOwner().isPresent());
    }

    @Test
    public void testOwnerExists() throws Exception {
        Item item = new Item("ContractOwner", Lists.newArrayList(new Value("Owner1", "String")));
        SealContractsResponse.MetaData metaData = new SealContractsResponse.MetaData(Lists.newArrayList(item));
        assertTrue(metaData.getOwner().isPresent());
    }
}