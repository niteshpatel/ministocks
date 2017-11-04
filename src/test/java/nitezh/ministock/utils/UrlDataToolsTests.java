package nitezh.ministock.utils;

import org.junit.Assume;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;


public class UrlDataToolsTests {
    @Test
    public void testDataRetrievalFromIex() throws IOException {
        // Skipif
        Assume.assumeTrue(System.getenv("TRAVIS_CI") == null);

        // Arrange
        String url = "https://api.iextrading.com/1.0/stock/market/batch?symbols=aapl&types=quote";

        // Act
        String result = UrlDataTools.urlToString(url).substring(0, 33);

        // Assert
        String expected = "{\"AAPL\":{\"quote\":{\"symbol\":\"AAPL\"";
        assertEquals(expected, result);
    }
}
