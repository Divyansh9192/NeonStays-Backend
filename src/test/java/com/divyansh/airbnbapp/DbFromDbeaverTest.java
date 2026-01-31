package com.divyansh.airbnbapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootTest
class DbFromDBeaverTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void shouldConnectUsingDBeaverConfig() throws Exception {
        try (Connection c = dataSource.getConnection()) {
            System.out.println("✅ Connected to DB via Spring (same as DBeaver)");
            System.out.println("DB URL = " + c.getMetaData().getURL());
            System.out.println("DB User = " + c.getMetaData().getUserName());
        }
    }
}
