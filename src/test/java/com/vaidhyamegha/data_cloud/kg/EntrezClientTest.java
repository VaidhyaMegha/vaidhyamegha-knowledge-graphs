package com.vaidhyamegha.data_cloud.kg;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class EntrezClientTest extends TestCase {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    public void testTestGetPubMedIds() {
        ESearchResult r = EntrezClient.getPubMedIds("NCT01874691");
        System.out.println(r);
        System.out.println(Arrays.toString(r.getIdList().toArray()));
        assertEquals(r.getIdList().toArray().length, 9);
    }
}