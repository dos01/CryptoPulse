# CryptoTrader

CryptoTrader is a modern desktop application built with JavaFX for real-time cryptocurrency portfolio management and market analysis. It provides users with live tracking, technical insights, and automated signal generation to assist in trading decisions.

## 🚀 Features

- **Real-Time Portfolio Tracking**: View your holdings, current prices, and profit/loss in real-time.
- **Advanced Analytics**: Technical indicators including RSI (Relative Strength Index) and SMA (Simple Moving Average) to gauge market trends.
- **Trading Signals**: Integrated prediction service that provides BUY/SELL/HOLD signals based on automated technical analysis.
- **Trade History Log**: Keep track of all your past transactions with detailed logs.
- **Interactive Charts**: Visualize price movements and trends directly within the application.
- **Local Database**: Secure and fast data storage using SQLite.

## 🛠️ Tech Stack

- **Lanuage**: Java 17
- **UI Framework**: JavaFX
- **Build Tool**: Maven
- **Database**: SQLite
- **JSON Processing**: Jackson

## 📦 Getting Started

### Prerequisites

- Java Development Kit (JDK) 17 or higher
- Apache Maven

### Installation & Running

1. **Clone the repository**:
   ```bash
   git clone https://github.com/USER/CryptoTrader.git
   cd CryptoTrader
   ```

2. **Build the project**:
   ```bash
   mvn clean install
   ```

3. **Run the application**:
   ```bash
   mvn javafx:run
   ```

## 📈 Technical Analysis Strategy

The application uses a combined RSI and SMA crossover strategy:
- **BUY Signal**: Triggered when RSI < 30 (oversold) and the current price is above the 20-day SMA, or when RSI is extremely oversold.
- **SELL Signal**: Triggered when RSI > 70 (overbought) and the current price is below the 20-day SMA, or when RSI is extremely overbought.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
