package com.seal.contracts.ws.client.seal.meta;

import com.seal.contracts.generator.csv.service.Json;
import org.junit.Test;

import java.io.InputStream;

import static com.seal.contracts.generator.csv.service.Json.JSON;
import static org.junit.Assert.*;

/**
 * Created by jantonak on 06/09/17.
 */
public class SealContractsResponseTest {

    @Test
    public void testGetId() throws Exception {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("data/seal/pull/metadata.json");
        SealContractsResponse response = (SealContractsResponse) JSON.toObject(stream, SealContractsResponse.class);
        assertEquals("f3496f549697562cec5a116db3c39ee3ffb9c40f", response.getId());
    }

    @Test
    public void testGetFileName() throws Exception {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("data/seal/pull/metadata.json");
        SealContractsResponse response = (SealContractsResponse) JSON.toObject(stream, SealContractsResponse.class);
        assertEquals("F-1963091094-20170118_BP_LTU_Extension Deed.pdf", response.getFileName());
    }

    @Test
    public void testGetMetaData() throws Exception {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("data/seal/pull/metadata.json");
        SealContractsResponse response = (SealContractsResponse) JSON.toObject(stream, SealContractsResponse.class);
        assertNotNull(response.getMetaData());
    }
}