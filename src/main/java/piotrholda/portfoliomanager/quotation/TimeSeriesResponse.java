package piotrholda.portfoliomanager.quotation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

// Main response wrapper
class TimeSeriesResponse {
    @JsonProperty("Meta Data")
    private MetaData metaData;

    @JsonProperty("Time Series (Daily)")
    private Map<String, DailyQuote> dailyTimeSeries;

    // Getters and setters
    public MetaData getMetaData() { return metaData; }
    public void setMetaData(MetaData metaData) { this.metaData = metaData; }

    public Map<String, DailyQuote> getDailyTimeSeries() { return dailyTimeSeries; }
    public void setDailyTimeSeries(Map<String, DailyQuote> dailyTimeSeries) {
        this.dailyTimeSeries = dailyTimeSeries;
    }
}



// Daily quote data
 class DailyQuote {
    @JsonProperty("1. open")
    private String open;

    @JsonProperty("2. high")
    private String high;

    @JsonProperty("3. low")
    private String low;

    @JsonProperty("4. close")
    private String close;

    @JsonProperty("5. volume")
    private String volume;

    // Getters and setters
    public String getOpen() { return open; }
    public void setOpen(String open) { this.open = open; }

    public String getHigh() { return high; }
    public void setHigh(String high) { this.high = high; }

    public String getLow() { return low; }
    public void setLow(String low) { this.low = low; }

    public String getClose() { return close; }
    public void setClose(String close) { this.close = close; }

    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }
}

