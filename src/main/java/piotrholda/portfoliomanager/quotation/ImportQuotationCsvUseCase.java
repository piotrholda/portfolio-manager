package piotrholda.portfoliomanager.quotation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import piotrholda.portfoliomanager.Ticker;
import piotrholda.portfoliomanager.strategy.Quotation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
class ImportQuotationCsvUseCase implements ImportQuotationCsv {

    private final SaveQuotations saveQuotations;

    @Override
    public void importQuotations(Ticker ticker, MultipartFile file) {
        validateFile(file);
        log.info("Starting CSV quotation import for code={}, exchangeCode={}, currencyCode={}, filename={}",
                ticker.getCode(), ticker.getExchangeCode(), ticker.getCurrencyCode(), file.getOriginalFilename());
        Collection<Quotation> quotations = parseCsv(ticker, file);
        log.info("Parsed {} quotations from uploaded CSV for code={}, exchangeCode={}, currencyCode={}",
                quotations.size(), ticker.getCode(), ticker.getExchangeCode(), ticker.getCurrencyCode());
        saveQuotations.save(quotations);
        log.info("Finished CSV quotation import for code={}, exchangeCode={}, currencyCode={}",
                ticker.getCode(), ticker.getExchangeCode(), ticker.getCurrencyCode());
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("CSV file is required");
        }
    }

    private Collection<Quotation> parseCsv(Ticker ticker, MultipartFile file) {
        List<Quotation> quotations = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            validateHeader(header);

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                String[] columns = line.split(",");
                if (columns.length < 5) {
                    throw new RuntimeException("Invalid CSV row: " + line);
                }

                quotations.add(new Quotation(
                        ticker,
                        LocalDate.parse(columns[0].trim()),
                        new BigDecimal(columns[4].trim())
                ));
            }
        } catch (IOException e) {
            log.error("Failed to read uploaded CSV file", e);
            throw new RuntimeException("Failed to read uploaded CSV file", e);
        } catch (Exception e) {
            log.error("Failed to parse uploaded CSV file", e);
            throw new RuntimeException("Failed to parse uploaded CSV file", e);
        }

        return quotations;
    }

    private void validateHeader(String header) {
        if (header == null || header.isBlank()) {
            throw new RuntimeException("CSV file is empty");
        }

        String normalizedHeader = header.replace("\uFEFF", "").trim().toLowerCase();
        if (normalizedHeader.startsWith("date,") || normalizedHeader.startsWith("data,")) {
            return;
        }

        throw new RuntimeException("Unsupported CSV header: " + header);
    }
}
