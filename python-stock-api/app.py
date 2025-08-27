from flask import Flask, jsonify, request
from stockdex import Ticker
import pandas as pd

app = Flask(__name__)

@app.route('/api/stock-data', methods=['GET'])
def get_stock_data():
    ticker_symbol = request.args.get('ticker')
    if not ticker_symbol:
        return jsonify({"error": "Ticker symbol is required"}), 400

    try:
        ticker = Ticker(ticker=ticker_symbol)

        # Convert DataFrames to dictionary records
        dividends = ticker.digrin_dividend.to_dict(orient='records')
        splits = ticker.digrin_stock_splits.to_dict(orient='records')

        return jsonify({
            "ticker": ticker_symbol,
            "dividends": dividends,
            "splits": splits
        })

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
