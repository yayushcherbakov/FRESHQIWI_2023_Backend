package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;

/**
 * Этот класс представляет собой консольную утилиту для получения курсов валют
 * Центрального Банка Российской Федерации (ЦБ РФ) за определенную дату с использованием официального API ЦБ РФ.
 */
public class CurrencyRates {

    /**
     * Константа, которая содержит URL официального API ЦБ РФ для получения данных о курсах валют.
     */
    private static final String API_URL = "https://www.cbr.ru/scripts/XML_daily.asp";

    /**
     * Основной метод, который запускает утилиту. Принимает входные параметры из командной строки для задания
     * кода валюты и даты, за которую необходимо получить курс. Если параметры указаны некорректно,
     * выводит сообщение с правильным форматом использования.
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Использование: currency_rates --code=USD --date=2022-10-08");

            return;
        }

        var code = "";
        var dateStr = "";

        for (String arg : args) {
            if (arg.startsWith("--code=")) {
                code = arg.substring(7);
            } else if (arg.startsWith("--date=")) {
                dateStr = arg.substring(7);
            }
        }

        if (code.isEmpty() || dateStr.isEmpty()) {
            System.out.println("Неверные параметры. Использование: --code=USD --date=2022-10-08");
            return;
        }

        try {
            var date = getParsedDataFormat(dateStr);
            var xmlData = getCurrencyDataFromApi(date);

            var rate = parseCurrencyRate(xmlData, code);

            if (rate != null) {
                System.out.println(rate);
            } else {
                System.out.println("Курс валюты не найден для указанной даты и кода валюты.");
            }

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    /**
     * Преобразует строку с датой из формата "год-месяц-день" в формат "день/месяц/год",
     * чтобы использовать в запросе к API ЦБ РФ.
     */
    static String getParsedDataFormat(String dateStr) {
        var parts = dateStr.split("-");
        return parts[2] + '/' + parts[1] + '/' + parts[0];
    }

    /**
     * Отправляет HTTP GET-запрос к официальному API ЦБ РФ для получения данных о курсах валют за указанную дату.
     * Возвращает полученные данные в виде строки XML.
     */
    private static String getCurrencyDataFromApi(String date) throws IOException {
        var url = new URL(API_URL + "?date_req=" + date);
        var connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        var reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        var response = new StringBuilder();

        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        connection.disconnect();

        return response.toString();
    }

    /**
     * Разбирает полученные данные в формате XML и находит курс валюты с указанным кодом.
     * Возвращает строку с информацией о курсе валюты в формате "Код (Название): Значение курса".
     */
    static String parseCurrencyRate(String xmlData, String code) throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        var builder = factory.newDocumentBuilder();

        var doc = builder.parse(new org.xml.sax.InputSource(new java.io.StringReader(xmlData)));
        doc.getDocumentElement().normalize();

        var currencyList = doc.getElementsByTagName("Valute");

        for (int i = 0; i < currencyList.getLength(); i++) {
            var currency = (Element) currencyList.item(i);
            var currencyCode = currency.getElementsByTagName("CharCode").item(0).getTextContent();

            if (currencyCode.equalsIgnoreCase(code)) {
                var name = currency.getElementsByTagName("Name").item(0).getTextContent();
                var rate = currency.getElementsByTagName("Value").item(0).getTextContent();

                return currencyCode + " (" + name + "): " + formatCurrencyRate(rate);
            }
        }

        return null;
    }

    /**
     * Форматирует значение курса валюты в формате "###.####",
     * где "#" - цифра. Заменяет запятую на точку для корректного преобразования в число с плавающей точкой.
     */
    static String formatCurrencyRate(String rate) {
        var rateValue = Double.parseDouble(rate.replace(",", "."));
        var df = new DecimalFormat("#0.0000");

        return df.format(rateValue);
    }
}
