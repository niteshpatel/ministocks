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

package nitezh.ministock.mocks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nitezh.ministock.domain.Widget;
import nitezh.ministock.domain.WidgetRepository;


public class MockWidgetRepository implements WidgetRepository {

    private HashSet<String> widgetsStockSymbols;

    @Override
    public List<Integer> getIds() {
        return new ArrayList<>();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Set<String> getWidgetsStockSymbols() {
        if (this.widgetsStockSymbols != null) {
            return this.widgetsStockSymbols;
        }
        return new HashSet<>();
    }

    public void setWidgetsStockSymbols(HashSet<String> widgetsStockSymbols) {
        this.widgetsStockSymbols = widgetsStockSymbols;
    }

    @Override
    public void delWidget(int id) {
    }

    @Override
    public Widget getWidget(int id) {
        return new MockWidget();
    }

    @Override
    public Widget addWidget(int id, int size) {
        return this.getWidget(id);
    }

}
