# CS441 HW3: Spark Monte Carlo Simulation
# Name:   Shyam Patel
# NetID:  spate54
# Date:   Nov 24, 2019

from alpha_vantage.timeseries import TimeSeries
from functools import reduce
import pandas as pd
import os
import time

# ticker symbols
symbols = ['GOOGL', 'AMZN', 'FB', 'AAPL', 'MSFT']

# call TimeSeries object with API key
ts = TimeSeries(key='YC4NTNF30CJGV05D', output_format='pandas')

# retrieve data for symbols
data_frames = []
for s in symbols:
    # get full daily time series data for symbol
    df, _ = ts.get_daily(symbol=s, outputsize='full')
    # drop all but close
    df = df.iloc[:, 3]
    # reverse data frame
    df = df.iloc[::-1]
    # compute % changes in close from previous recorded day
    df = df.pct_change()
    # drop oldest recorded day (no % change)
    df = df.iloc[1:]
    # reverse data frame
    df = df.iloc[::-1]
    # append % changes to collection of data frames
    data_frames.append(df)
    # wait 12s to ensure no more than 5 API calls per min
    time.sleep(12)

# merge data frames
df_merged = reduce(lambda x, y: pd.merge(x, y, on='date'), data_frames)
# rename merged data frame columns
df_merged.columns = symbols
# store data to change.csv
df_merged.to_csv(path_or_buf=os.path.join(os.getcwd(), 'change.csv'))
print(df_merged)
