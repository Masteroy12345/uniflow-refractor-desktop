package cm.uniflow.desktop.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CampusFirestoreServiceTest {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    void emitsOneEqualityFilterForStudentOwnedQueries() throws Exception {
        JsonNode query = CampusFirestoreService.structuredQuery("academic_grades",
                List.of(CampusFirestoreService.Filter.eq("studentUid", "uid-123")), 500);
        assertEquals("academic_grades", query.at("/structuredQuery/from/0/collectionId").asText());
        assertEquals("studentUid", query.at("/structuredQuery/where/fieldFilter/field/fieldPath").asText());
        assertEquals("uid-123", query.at("/structuredQuery/where/fieldFilter/value/stringValue").asText());
        assertEquals(100, query.at("/structuredQuery/limit").asInt());
        assertTrue(query.toString().contains("studentUid"));
    }

    @Test
    void emitsCompositeCampusAndPublicationFilters() throws Exception {
        JsonNode query = CampusFirestoreService.structuredQuery("academic_courses", List.of(
                CampusFirestoreService.Filter.eq("universityId", "uni-42"),
                CampusFirestoreService.Filter.eq("isPublished", true)), 50);
        JsonNode filters = query.at("/structuredQuery/where/compositeFilter/filters");
        assertEquals("AND", query.at("/structuredQuery/where/compositeFilter/op").asText());
        assertEquals("universityId", filters.get(0).at("/fieldFilter/field/fieldPath").asText());
        assertEquals("uni-42", filters.get(0).at("/fieldFilter/value/stringValue").asText());
        assertEquals("isPublished", filters.get(1).at("/fieldFilter/field/fieldPath").asText());
        assertTrue(filters.get(1).at("/fieldFilter/value/booleanValue").asBoolean());
        assertFalse(query.toString().contains("documents.list"));
    }

    @Test
    void refusesAnUnfilteredQuery() {
        assertThrows(IllegalArgumentException.class,
                () -> CampusFirestoreService.structuredQuery("academic_courses", List.of(), 20));
    }
}
