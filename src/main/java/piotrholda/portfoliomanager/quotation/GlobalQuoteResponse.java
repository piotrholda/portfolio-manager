package piotrholda.portfoliomanager.quotation;

import com.fasterxml.jackson.annotation.JsonProperty;

class GlobalQuoteResponse {
    @JsonProperty("Global Quote")
    private GlobalQuote globalQuote;

    // Getter and setter
    public GlobalQuote getGlobalQuote() { return globalQuote; }
    public void setGlobalQuote(GlobalQuote globalQuote) { this.globalQuote = globalQuote; }
}
