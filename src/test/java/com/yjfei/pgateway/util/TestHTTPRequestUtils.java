package com.yjfei.pgateway.util;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import com.yjfei.pgateway.util.HTTPRequestUtils;

public  class TestHTTPRequestUtils {


        @Test
        public void detectsGzip() {
            assertTrue(HTTPRequestUtils.isGzipped("gzip"));
        }

        @Test
        public void detectsNonGzip() {
            assertFalse(HTTPRequestUtils.isGzipped("identity"));
        }

        @Test
        public void detectsGzipAmongOtherEncodings() {
            assertTrue(HTTPRequestUtils.isGzipped("gzip, deflate"));
        }

    }