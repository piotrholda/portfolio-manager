package piotrholda.portfoliomanager.quotation;

import org.springframework.web.multipart.MultipartFile;
import piotrholda.portfoliomanager.Ticker;

public interface ImportQuotationCsv {

    void importQuotations(Ticker ticker, MultipartFile file);
}
