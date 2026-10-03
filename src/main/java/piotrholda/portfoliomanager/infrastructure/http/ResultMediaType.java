package piotrholda.portfoliomanager.infrastructure.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeNotAcceptableException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** CSV remains the default when both representations have equal preference. */
public final class ResultMediaType {
    public static final String CSV = "text/csv";
    private static final MediaType CSV_TYPE = MediaType.valueOf(CSV);

    private ResultMediaType() {
    }

    public static boolean isJson(HttpHeaders headers) throws HttpMediaTypeNotAcceptableException {
        List<MediaType> accepted = new ArrayList<>(headers.getAccept());
        if (accepted.isEmpty()) {
            return false;
        }
        double csvQuality = quality(accepted, CSV_TYPE);
        double jsonQuality = quality(accepted, MediaType.APPLICATION_JSON);
        if (csvQuality <= 0 && jsonQuality <= 0) {
            throw new HttpMediaTypeNotAcceptableException(List.of(CSV_TYPE, MediaType.APPLICATION_JSON));
        }
        return jsonQuality > csvQuality;
    }

    private static double quality(List<MediaType> accepted, MediaType offered) {
        List<MediaType> matching = accepted.stream()
                .filter(type -> type.isCompatibleWith(offered))
                .collect(Collectors.toList());
        // A specific media range overrides a wildcard, including an explicit q=0 exclusion.
        MediaType.sortBySpecificity(matching);
        return matching.isEmpty() ? 0 : matching.get(0).getQualityValue();
    }
}
