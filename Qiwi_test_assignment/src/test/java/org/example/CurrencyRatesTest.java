package org.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.ParseException;

public class CurrencyRatesTest {

    @Test
    void testGetParsedDataFormat() {
        String dateStr = "2022-10-08";
        String expectedFormattedDate = "08/10/2022";

        String formattedDate = CurrencyRates.getParsedDataFormat(dateStr);

        Assertions.assertEquals(expectedFormattedDate, formattedDate);
    }

    @Test
    void testParseCurrencyRate() throws Exception {
        String xmlData = "<ValCurs><Valute><CharCode>USD</CharCode><Name>Доллар США</Name><Value>61,2475</Value></Valute></ValCurs>";
        String code = "USD";
        String expectedRate = "USD (Доллар США): 61,2475";

        String rate = CurrencyRates.parseCurrencyRate(xmlData, code);

        Assertions.assertEquals(expectedRate, rate);
    }

    @Test
    void testParseCurrencyRateNotFound() throws Exception {
        String xmlData = "<ValCurs><Valute><CharCode>EUR</CharCode><Name>Евро</Name><Value>70,1234</Value></Valute></ValCurs>";
        String code = "USD";

        String rate = CurrencyRates.parseCurrencyRate(xmlData, code);

        Assertions.assertNull(rate);
    }

    @Test
    void testFormatCurrencyRate() throws ParseException {
        String rate = "61,2475";
        String expectedFormattedRate = "61,2475";

        String formattedRate = CurrencyRates.formatCurrencyRate(rate);

        Assertions.assertEquals(expectedFormattedRate, formattedRate);
    }
}