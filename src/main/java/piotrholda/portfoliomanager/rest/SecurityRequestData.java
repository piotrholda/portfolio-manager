package piotrholda.portfoliomanager.rest;

import lombok.Data;
import piotrholda.portfoliomanager.SecurityType;
import piotrholda.portfoliomanager.Ticker;

@Data
public class SecurityRequestData {
    private SecurityType type;
    private String name;
    private Ticker googleTicker;
}
