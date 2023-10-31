package com.figaf.integration.tpm.client.parser;

import com.figaf.integration.tpm.entity.AdministrativeData;
import com.figaf.integration.tpm.entity.TpmObjectMetadata;
import com.figaf.integration.tpm.enumtypes.TpmObjectType;
import com.figaf.integration.tpm.parser.GenericTpmResponseParser;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GenericTpmResponseParserTest {

    @Test
    void testParseResponse() throws Exception {

        String jsonResponse = "[{" +
            "\"id\": \"1\"," +
            "\"uniqueId\": \"UID1\"," +
            "\"displayName\": \"TestName\"," +
            "\"semanticVersion\": \"1.0.0\"," +
            "\"artifactStatus\": \"ACTIVE\"," +
            "\"administrativeData\": {" +
            "\"createdAt\": 1635292800000," +
            "\"createdBy\": \"user1\"," +
            "\"modifiedAt\": 1635292800000," +
            "\"modifiedBy\": \"user2\"" +
            "}" +
            "}]";

        List<TpmObjectMetadata> parsedObjects = parseJsonResponse(jsonResponse);

        assertNotNull(parsedObjects);
        assertEquals(1, parsedObjects.size());

        TpmObjectMetadata obj = parsedObjects.get(0);
        assertEquals("1", obj.getId());
        assertEquals("UID1", obj.getUniqueId());
        assertEquals("TestName", obj.getDisplayName());
        assertEquals("1.0.0", obj.getSemanticVersion());
        assertEquals("ACTIVE", obj.getArtifactStatus());

        AdministrativeData adminData = obj.getAdministrativeData();
        assertNotNull(adminData);
        assertEquals(new Date(1635292800000L), adminData.getCreatedAt());
        assertEquals(new Date(1635292800000L), adminData.getModifiedAt());
        assertEquals("user1", adminData.getCreatedBy());
        assertEquals("user2", adminData.getModifiedBy());
    }

    // it's enough to test only one child of BaseTpmObject because it provides the full coverage of setting BaseTpmObject fields
    private List<TpmObjectMetadata> parseJsonResponse(String jsonResponse) throws Exception {
        GenericTpmResponseParser parser = new GenericTpmResponseParser();
        return parser.parseResponse(jsonResponse, TpmObjectType.CLOUD_TRADING_PARTNER);
    }
}
