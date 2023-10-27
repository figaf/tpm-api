package com.figaf.integration.tpm.client.parser;

import com.figaf.integration.tpm.entity.AdministrativeData;
import com.figaf.integration.tpm.entity.BaseTpmObject;
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

        List<BaseTpmObject> parsedObjects = parseJsonResponse(jsonResponse);

        assertNotNull(parsedObjects);
        assertEquals(1, parsedObjects.size());

        BaseTpmObject obj = parsedObjects.get(0);
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

    private List<BaseTpmObject> parseJsonResponse(String jsonResponse) throws Exception {
        GenericTpmResponseParser<BaseTpmObject> parser = new GenericTpmResponseParser<>(BaseTpmObject::new);
        return parser.parseResponse(jsonResponse);
    }
}
