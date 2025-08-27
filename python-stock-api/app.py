from fastapi import FastAPI, HTTPException, Query
from stockdex import Ticker
import pandas as pd

app = FastAPI(
    title="Stock Data API",
    description="An API to fetch dividend and split data for stock tickers.",
    version="1.0.0"
)

@app.get("/api/stock-data")
async def get_stock_data(ticker: str = Query(..., description="The stock ticker symbol (e.g., AAPL)")):
    """
    Retrieves historical dividend and stock split data from Digrin.
    """
    if not ticker:
        raise HTTPException(status_code=400, detail="Ticker symbol is required")

    try:
        stock_ticker = Ticker(ticker=ticker)
        dividends = stock_ticker.digrin_dividend.to_dict(orient='records')
        splits = stock_ticker.digrin_stock_splits.to_dict(orient='records')

        return {
            "ticker": ticker,
            "dividends": dividends,
            "splits": splits
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
