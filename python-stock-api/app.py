from fastapi import FastAPI, HTTPException, Query
from pydantic import BaseModel, Field
from typing import List, Optional
from stockdex import Ticker
import re

# --- Pydantic Models with Aliases ---

class Dividend(BaseModel):
    # Use Field(alias=...) to map the JSON key to a valid Python attribute name.
    ex_dividend_date: str = Field(alias="Ex-dividend date")
    payable_date: str = Field(alias="Payable date")
    dividend_amount_change: str = Field(alias="Dividend amount (change)")
    adjusted_price: str = Field(alias="Adjusted Price")
    close_price: str = Field(alias="Close Price")

class Split(BaseModel):
    date: str = Field(alias="Date")
    split_ratio: str = Field(alias="Split Ratio")

class StockDataResponse(BaseModel):
    ticker: str
    dividends: List[Dividend]
    splits: List[Split]

# --- FastAPI App ---
app = FastAPI(
    title="Stock Data API",
    description="An API to fetch dividend and split data for stock tickers.",
    version="1.0.0"
)


# --- Data Cleaning Function ---

def clean_dividend_data(dividends_data: List[dict]) -> List[dict]:
    """
    Cleans the 'Dividend amount (change)' field in each dividend record.
    """
    for dividend in dividends_data:
        raw_amount = dividend.get("Dividend amount (change)", "")
        # Use regex to find the first floating-point number (the dividend amount)
        match = re.search(r"(\d+\.\d+)\s+USD", raw_amount)
        if match:
            dividend["Dividend amount (change)"] = f"{match.group(1)} USD"
        else:
            # Fallback if the pattern doesn't match
            dividend["Dividend amount (change)"] = raw_amount.split('\n')[0].strip()
    return dividends_data


@app.get("/api/stock-data", response_model=StockDataResponse)
async def get_stock_data(ticker: str = Query(..., description="The stock ticker symbol (e.g., VOO)")):
    """
    Retrieves historical dividend and stock split data from Digrin.
    """
    if not ticker:
        raise HTTPException(status_code=400, detail="Ticker symbol is required")

    try:
        stock_ticker = Ticker(ticker=ticker)

        dividends_data = stock_ticker.digrin_dividend.to_dict(orient='records')
        splits_data = stock_ticker.digrin_stock_splits.to_dict(orient='records')

        # Clean the messy dividend amount field before returning
        cleaned_dividends = clean_dividend_data(dividends_data)

        return {
            "ticker": ticker,
            "dividends": cleaned_dividends,
            "splits": splits_data
        }

    except Exception as e:
        # It's good practice to log the actual exception
        # import logging
        # logging.exception("An error occurred while fetching stock data.")
        raise HTTPException(status_code=500, detail=f"An internal error occurred: {e}")
