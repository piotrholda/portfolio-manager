package piotrholda.portfoliomanager.quotation.http;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
class ImportQuotationCsvRequest {

    private String code;
    private String exchangeCode = "NYSE";
    private String currencyCode;
    private MultipartFile file;

    ImportQuotationRequest toImportQuotationRequest() {
        ImportQuotationRequest request = new ImportQuotationRequest();
        request.setCode(code);
        request.setExchangeCode(exchangeCode);
        request.setCurrencyCode(currencyCode);
        return request;
    }
}
