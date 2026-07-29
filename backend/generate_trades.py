import csv
from datetime import date, timedelta

output = "src/main/resources/db/changelog/changes/data/trades.csv"

statuses = ["MATCHED", "UNMATCHED", "DISPUTED", "PENDING"]
assets = ["EQUITY", "BOND", "FX"]

months = [
    date(2026, 1, 1),
    date(2026, 2, 1),
    date(2026, 3, 1),
    date(2026, 4, 1)
]

with open(output, "w", newline="") as f:

    writer = csv.writer(f)

    writer.writerow([
        "id",
        "trade_ref",
        "instrument_id",
        "counterparty_id",
        "asset_class",
        "side",
        "quantity",
        "price",
        "trade_date",
        "status"
    ])

    trade_id = 1

    for month in months:
        for i in range(125):

            writer.writerow([
                trade_id,
                f"TRD{trade_id:06d}",
                (trade_id % 10) + 1,
                (trade_id % 10) + 1,
                assets[trade_id % 3],
                "BUY" if trade_id % 2 else "SELL",
                100 + trade_id,
                50 + trade_id,
                month + timedelta(days=i % 20),
                statuses[trade_id % 4]
            ])

            trade_id += 1

print("Created 500 trades")