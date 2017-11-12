/*
 The MIT License

 Copyright (c) 2013 Nitesh Patel http://niteshpatel.github.io/ministocks

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */

package nitezh.ministock.utils;

import org.junit.Test;

import java.text.ParseException;
import java.util.Locale;

import static org.junit.Assert.assertEquals;


public class NumberToolsTests {

    @Test
    public void trimWithNumberLessThan10AndScale1() throws ParseException {
        // Arrange
        String expected = "9.1";

        // Act
        String result = NumberTools.trim("9.1", Locale.US);

        // Assert
        assertEquals(expected, result);
    }

    @Test
    public void trimWithNumberLessThan10AndScale2() throws ParseException {
        // Arrange
        String expected = "8.99";

        // Act
        String result = NumberTools.trim("8.99", Locale.US);

        // Assert
        assertEquals(expected, result);
    }

    @Test
    public void trimWithNumberLessThan10AndScale4() throws ParseException {
        // Arrange
        String expected = "2.5900";

        // Act
        String result = NumberTools.trim("2.5900", Locale.US);

        // Assert
        assertEquals(expected, result);
    }

    @Test
    public void trimWithNumberLessThan10AndScale5() throws ParseException {
        // Arrange
        String expected = "9.3451";

        // Act
        String result = NumberTools.trim("9.34512", Locale.US);

        // Assert
        assertEquals(expected, result);
    }

    @Test
    public void trimWithNumberLessThan100AndScale1() throws ParseException {
        // Arrange
        String expected = "12.30";

        // Act
        String result = NumberTools.trim("12.3", Locale.US);

        // Assert
        assertEquals(expected, result);
    }

    @Test
    public void trimWithNumberLessThan100AndScale2() throws ParseException {
        // Arrange
        String expected = "21.23";

        // Act
        String result = NumberTools.trim("21.23", Locale.US);

        // Assert
        assertEquals(expected, result);
    }

    @Test
    public void trimWithNumberLessThan100AndScale4() throws ParseException {
        // Arrange
        String expected = "43.26";

        // Act
        String result = NumberTools.trim("43.2572", Locale.US);

        // Assert
        assertEquals(expected, result);
    }

    @Test
    public void trimWithNumberLessThan100AndScale5() throws ParseException {
        // Arrange
        String expected = "98.33";

        // Act
        String result = NumberTools.trim("98.33442", Locale.US);

        // Assert
        assertEquals(expected, result);
    }
}
