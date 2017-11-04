package nitezh.ministock.utils;

import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.assertEquals;


public class UrlDataToolsTests {
    @Test
    public void testDataRetrievalFromIex() throws ParseException {
        // Arrange
        String url = "https://api.iextrading.com/1.0/stock/market/batch?symbols=aapl&types=quote";

        // Act
        @SuppressWarnings("ConstantConditions")
        String result = UrlDataTools.getUrlData(url).substring(0, 33);

        // Assert
        String expected = "{\"AAPL\":{\"quote\":{\"symbol\":\"AAPL\"";
        assertEquals(expected, result);
    }
}
