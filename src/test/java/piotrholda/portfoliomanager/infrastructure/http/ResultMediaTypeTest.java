package piotrholda.portfoliomanager.infrastructure.http;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.HttpMediaTypeNotAcceptableException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResultMediaTypeTest {
    @ParameterizedTest
    @CsvSource(value = {
            "''|false",
            "*/*|false",
            "text/csv|false",
            "application/json|true",
            "application/*|true",
            "application/json, text/csv|false",
            "application/json;q=0.9, text/csv;q=0.1|true",
            "text/csv;q=0.9, application/json;q=0.1|false",
            "application/json;q=0, */*|false",
            "text/csv;q=0, */*|true",
            "application/json;q=0.5, */*;q=0.1|true",
            "application/json;q=0.1, */*;q=0.5|false"
    }, delimiter = '|')
    void selectsRepresentationAndPreservesCsvDefault(String accept, boolean json) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        if (!accept.isEmpty()) {
            headers.add(HttpHeaders.ACCEPT, accept);
        }
        assertEquals(json, ResultMediaType.isJson(headers));
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/xml", "*/*;q=0", "application/json;q=0, text/csv;q=0"})
    void rejectsUnacceptableRepresentations(String accept) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, accept);
        assertThrows(HttpMediaTypeNotAcceptableException.class, () -> ResultMediaType.isJson(headers));
    }
}
