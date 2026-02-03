module com.cryptotrader {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires org.slf4j;

    opens com.cryptotrader to javafx.fxml;

    exports com.cryptotrader;
    exports com.cryptotrader.models;
}
