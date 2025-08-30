from fastapi import FastAPI, HTTPException, Query
from pydantic import BaseModel, Field
from typing import List
from stockdex import Ticker
import re

class Dividend(BaseModel):
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
    dividends: List[Dividend] = Field(default_factory=list)
    splits: List[Split] = Field(default_factory=list)

app = FastAPI(
    title="Stock Data API",
    description="An API to fetch dividend and split data for stock tickers.",
    version="1.0.0"
)

def clean_dividend_data(dividends_data: List[dict]) -> List[dict]:
    for dividend in dividends_data:
        raw_amount = dividend.get("Dividend amount (change)", "")
        match = re.search(r"(\d+\.\d+)\s+USD", raw_amount)
        if match:
            dividend["Dividend amount (change)"] = f"{match.group(1)} USD"
        else:
            dividend["Dividend amount (change)"] = raw_amount.split('\n').strip()
    return dividends_data

@app.get("/api/stock-data", response_model=StockDataResponse)
async def get_stock_data(ticker: str = Query(..., description="The stock ticker symbol (e.g., VOO)")):
    if not ticker:
        raise HTTPException(status_code=400, detail="Ticker symbol is required")
    try:
        stock_ticker = Ticker(ticker=ticker)

        # Dividends
        dividends_df = stock_ticker.digrin_dividend
        dividends_data = dividends_df.to_dict(orient="records") if dividends_df is not None else []
        cleaned_dividends = clean_dividend_data(dividends_data)

        # Splits (robust to missing/empty)
        splits_data = []
        try:
            splits_df = getattr(stock_ticker, "digrin_stock_splits", None)
            if splits_df is not None and not getattr(splits_df, "empty", True):
                splits_data = splits_df.to_dict(orient="records")
        except Exception:
            splits_data = []

        return {
            "ticker": ticker,
            "dividends": cleaned_dividends,
            "splits": splits_data
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"An internal error occurred: {e}")
