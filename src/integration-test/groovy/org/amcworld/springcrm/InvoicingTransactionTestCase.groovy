/*
 * InvoicingTransactionTestCase.groovy
 *
 * Copyright (c) 2011-2013, Daniel Ellermann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package org.amcworld.springcrm

import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait


/**
 * The class {@code InvoicingTransactionTestCase} represents a generic base
 * class for test cases concerning the invoicing transactions such as quotes,
 * invoices, dunnings etc.
 *
 * @author  Daniel Ellermann
 * @version 1.4
 * @since   1.3
 */
class InvoicingTransactionTestCase extends GeneralFunctionalTestCase {

    //-- Constants ------------------------------

    protected static final String [] CELL_NAMES = [
            'number', 'quantity', 'unit', 'name', 'description', 'unitPrice',
            'total', 'tax'
        ] as String[]


    //-- Non-public methods ---------------------

    /**
     * Adds a new row to the price table.
     *
     * @return  the current number of rows in the price table after adding the
     *          new row
     */
    protected int addNewPriceTableRow() {
        int numRows = numPriceTableRows
        driver.findElement(By.className('add-invoicing-item-btn')).click()

        def wait = new WebDriverWait(driver, 5)
        By by = By.xpath(
            "//table[${getXPathClassExpr('price-table')}]/tbody[1]/tr[${numRows + 1}]"
        )
        wait.until ExpectedConditions.visibilityOfElementLocated(by)
        numPriceTableRows
    }

    /**
     * Checks whether the given invoice has the values as defined in
     * {@code prepareInvoice}.
     *
     * @param i the invoice to check
     * @see     #prepareInvoice(Organization, Person, Quote, SalesOrder)
     */
    protected void checkInvoice(Invoice i) {
        checkInvoicingTransaction i
        assert 'R-10000-10000' == i.fullNumber
        assert new GregorianCalendar(2013, Calendar.APRIL, 1).time == i.docDate
        assert new GregorianCalendar(2013, Calendar.APRIL, 2).time == i.shippingDate
        assert '''für die durchgeführte Werbekampange **"Frühjahr 2013"** erlauben wir uns, Ihnen folgendes in Rechnung zu stellen.

Einzelheiten entnehmen Sie bitte dem beiliegenden Leistungsverzeichnis bzw. dem [Online-Verzeichnis](http://www.example.de/verzeichnis/).''' == i.headerText
        assert 'Die Ausführung und Abrechnung erfolgte _laut Pflichtenheft_.' == i.footerText
        assert '**Wichtig!** Beim Versand der Rechnung Leistungsverzeichnis nicht vergessen!' == i.notes
        assert 902L == i.stage.id
        assert new GregorianCalendar(2013, Calendar.APRIL, 16).time == i.dueDatePayment
        if (i.quote) {
            checkQuote i.quote
        }
        if (i.salesOrder) {
            checkSalesOrder i.salesOrder
        }
    }

    /**
     * Checks whether the given invoicing transaction has the values as defined
     * in {@code prepareQuote}, {@code prepareSalesOrder}, or
     * {@code prepareInvoice}.
     *
     * @param i the invoicing transaction to check
     * @see     #prepareQuote(Organization, Person)
     * @see     #prepareSalesOrder(Organization, Person, Quote)
     * @see     #prepareInvoice(Organization, Person, Quote, SalesOrder)
     */
    protected void checkInvoicingTransaction(InvoicingTransaction i) {
        assert 'Werbekampagne Frühjahr 2013' == i.subject
        assert 'Landschaftsbau Duvensee GbR' == i.organization.name
        assert 'Brackmann' == i.person.lastName
        assert 'Henry' == i.person.firstName
        assert 501L == i.carrier.id
        assert 'Dörpstraat 25' == i.billingAddr.street
        assert '23898' == i.billingAddr.postalCode
        assert 'Duvensee' == i.billingAddr.location
        assert 'Schleswig-Holstein' == i.billingAddr.state
        assert 'Deutschland' == i.billingAddr.country
        assert 'Dörpstraat 25' == i.shippingAddr.street
        assert '23898' == i.shippingAddr.postalCode
        assert 'Duvensee' == i.shippingAddr.location
        assert 'Schleswig-Holstein' == i.shippingAddr.state
        assert 'Deutschland' == i.shippingAddr.country
        List<InvoicingItem> items = i.items
        assert 3 == items.size()
        def item = items[0]
        assert 'S-10000' == item.number
        assert 1.0d == item.quantity
        assert 'Einheiten' == item.unit
        assert 'Konzeption und Planung' == item.name
        assert 'Konzeption der geplanten Werbekampagne' == item.description
        assert 440.0d == item.unitPrice
        assert 19.0d == item.tax
        item = items[1]
        assert 'S-10100' == item.number
        assert 1.0d == item.quantity
        assert 'Einheiten' == item.unit
        assert 'Mustervorschau' == item.name
        assert 'Anfertigung eines Musters _nach Kundenvorgaben_.' == item.description
        assert 450.0d == item.unitPrice
        assert 19.0d == item.tax
        item = items[2]
        assert 'P-10000' == item.number
        assert 2.0d == item.quantity
        assert 'Packung' == item.unit
        assert 'Papier A4 80 g/m²' == item.name
        assert 'Packung zu 100 Blatt. Chlorfrei gebleicht.' == item.description
        assert 2.49d == item.unitPrice
        assert 7.0d == item.tax
        def termsAndConditions = i.termsAndConditions
        assert 2 == termsAndConditions.size()
        assert (termsAndConditions*.id as Set) == ([700L, 701L] as Set)
    }

    /**
     * Checks the values of the input fields and the total value of the row
     * with the given index.
     *
     * @param rowIdx    the zero-based index of the row
     * @param values    the values to check in the order number, quantity,
     *                  unit, name, description, unitPrice, total, tax;
     *                  {@code null} values and optionally missing trailing
     *                  elements are not checked
     * @see             #CELL_NAMES
     */
    protected void checkRowValues(int rowIdx, String... values) {
        getPriceTableInput(rowIdx, 'name').click()
        String prefix = "items[${rowIdx}].".toString()
        for (int i = 0; i < values.length; i++) {
            String value = values[i]
            if (value != null) {
                String cellName = CELL_NAMES[i]
                if (cellName == 'total') {
                    assert value == getPriceTableRowTotal(rowIdx)
                } else {
                    assert value == getInputValue(prefix + cellName)
                }
            }
        }
    }

    /**
     * Checks whether the given quote has the values as defined in
     * {@code prepareQuote}.
     *
     * @param q the quote to check
     * @see     #prepareQuote(Organization, Person)
     */
    protected void checkQuote(Quote q) {
        checkInvoicingTransaction q
        assert 'A-10000-10000' == q.fullNumber
        assert new GregorianCalendar(2013, Calendar.FEBRUARY, 20).time == q.docDate
        assert new GregorianCalendar(2013, Calendar.FEBRUARY, 21).time == q.shippingDate
        assert '''für die geplante Werbekampange **"Frühjahr 2013"** möchten wir Ihnen gern folgendes Angebot unterbreiten.

Die Einzelheiten wurden im Meeting am 21.01.2013 festgelegt. Sie finden ein vollständiges Protokoll auf [unserer Webseite](http://www.example.de/protokoll/).''' == q.headerText
        assert 'Details zu den einzelnen Punkten finden Sie _im Pflichtenheft_.' == q.footerText
        assert 'Angebot unterliegt _möglicherweise_ weiteren Änderungen.' == q.notes
        assert 602L == q.stage.id
        assert new GregorianCalendar(2013, Calendar.MARCH, 20).time == q.validUntil
    }

    /**
     * Checks whether the given sales order has the values as defined in
     * {@code prepareSalesOrder}.
     *
     * @param so    the sales order to check
     * @see         #prepareSalesOrder(Organization, Person, Quote)
     */
    protected void checkSalesOrder(SalesOrder so) {
        checkInvoicingTransaction so
        assert 'B-10000-10000' == so.fullNumber
        assert new GregorianCalendar(2013, Calendar.MARCH, 4).time == so.docDate
        assert new GregorianCalendar(2013, Calendar.MARCH, 5).time == so.shippingDate
        assert 'vielen Dank für Ihren Auftrag zur Werbekampange **"Frühjahr 2013"**.' == so.headerText
        assert 'Die Umsetzung des Auftrags erfolgt **nach Pflichtenheft**.' == so.footerText
        assert 'Erste Teilergebnisse sollten vor dem *15.03.2013* vorliegen.' == so.notes
        assert 802L == so.stage.id
        assert new GregorianCalendar(2013, Calendar.MARCH, 28).time == so.dueDate
        if (so.quote) {
            checkQuote so.quote
        }
    }

    /**
     * Checks the values of the fields of the row with the given index in the
     * show view.
     *
     * @param rowIdx    the zero-based index of the row
     * @param values    the values to check in the order number, quantity,
     *                  unit, name and description, unitPrice, total, tax;
     *                  {@code null} values and optionally missing trailing
     *                  elements are not checked
     */
    protected void checkStaticRowValues(int rowIdx, String... values) {
        WebElement row = getPriceTableRow(rowIdx)
        assert "${rowIdx + 1}." == row.findElement(By.xpath('./td[1]')).text
        for (int i = 0; i < values.length; i++) {
            String value = values[i]
            if (value != null) {
                assert value == row.findElement(By.xpath("./td[${i + 2}]")).text
            }
        }
    }

    /**
     * Checks the given values against the values of the "still unpaid" link.
     *
     * @param modifiedClosingBalance    the expected modified closing balance
     * @param stillUnpaid               the expected text of the "still unpaid"
     *                                  link
     * @param statusClass               the expected status class such as
     *                                  {@code still-unpaid-unpaid},
     *                                  {@code still-unpaid-too-much}, and
     *                                  {@code still-unpaid-paid}
     */
    protected void checkStillUnpaid(String modifiedClosingBalance,
                                    String stillUnpaid, String statusClass)
    {
        WebElement link = getStillUnpaid()
        assert modifiedClosingBalance == link.getAttribute('data-modified-closing-balance')
        assert stillUnpaid == link.findElement(By.tagName('output')).text.trim()
        assert statusClass == link.getAttribute('class')
    }

    /**
     * Checks the tax rates in the price table.  The method checks the number
     * of tax rates, their order and values.
     *
     * @param taxRates  a list of tuple lists representing the tax rates to
     *                  check; each tuple contains the tax rate as first
     *                  element and the tax value as second element
     */
    protected void checkTaxRates(List<List<String>> taxRates) {
        List<WebElement> taxRateRows = driver.findElements(By.className('tax-rate-sum'))
        int n = taxRates.size()
        assert n == taxRateRows.size()

        By labelBy = By.xpath('./td[@class="label"]/label')
        By priceBy = By.className('total-price')
        for (int i = 0; i < n; i++) {
            List<String> item = taxRates[i]
            WebElement row = taxRateRows[i]
            assert "${item[0]} % MwSt." == row.findElement(labelBy).text
            assert "${item[1]} €" == row.findElement(priceBy).text
        }
    }

    @Override
    protected Object getDatasets() {
        ['test-data/install-data.xml', 'test-data/sales-items.xml']
    }

    protected String getDiscountPercentAmount() {
        driver.findElement(By.id('discount-from-percent')).text
    }

    /**
     * Gets the number of rows in the price table.
     *
     * @return  the number of rows
     */
    protected int getNumPriceTableRows() {
        priceTable.findElements(By.xpath("./tbody[1]/tr")).size()
    }

    /**
     * Gets the web element representing the price table.
     *
     * @return  the price table web element
     */
    protected WebElement getPriceTable() {
        driver.findElement By.className('price-table')
    }

    /**
     * Gets the web element representing a cell in the price table.
     *
     * @param rowIdx    the zero-based index of the row
     * @param cellName  the class name of the cell
     * @return          the web element representing the cell
     */
    protected WebElement getPriceTableCell(int rowIdx, String cellName) {
        getPriceTableRow(rowIdx).findElement By.className(cellName)
    }

    /**
     * Gets the input field with the given name in the stated row in the price
     * table.
     *
     * @param rowIdx    the zero-based index of the table row
     * @param name      the name of the field such as "quantity", "name" etc.
     */
    protected WebElement getPriceTableInput(int rowIdx, String name) {
        getInput "items[${rowIdx}].${name}"
    }

    /**
     * Gets the web element representing a row in the price table.
     *
     * @param rowIdx    the zero-based index of the row
     * @return          the web element representing the row
     */
    protected WebElement getPriceTableRow(int rowIdx) {
        priceTable.findElement By.xpath("./tbody[1]/tr[${rowIdx + 1}]")
    }

    /**
     * Gets the total of a row in the price table.
     *
     * @param rowIdx    the zero-based index of the row
     * @return          the total value
     */
    protected String getPriceTableRowTotal(int rowIdx) {
        getPriceTableInput(rowIdx, 'name').click()
        getPriceTableCell(rowIdx, 'total-price').
            findElement(By.tagName('output')).
            text
    }

    protected WebElement getStillUnpaid() {
        driver.findElement By.id('still-unpaid')
    }

    protected String getSubtotalGross() {
        driver.findElement(By.id('subtotal-gross')).text
    }

    protected String getSubtotalNet() {
        driver.findElement(By.id('subtotal-net')).text
    }

    protected String getTotal() {
        driver.findElement(By.id('total-price')).text
    }

    /**
     * Moves the row with the given index one step downwards.
     *
     * @param rowIdx    the zero-based index of the table row
     */
    protected void moveRowDown(int rowIdx) {
        getPriceTableRow(rowIdx).findElement(By.className('down-btn')).click()
    }

    /**
     * Moves the row with the given index one step upwards.
     *
     * @param rowIdx    the zero-based index of the table row
     */
    protected void moveRowUp(int rowIdx) {
        getPriceTableRow(rowIdx).findElement(By.className('up-btn')).click()
    }

    /**
     * Opens a sales item selector dialog by clicking the corresponding link in
     * the table row with the given index.
     *
     * @param rowIdx    the zero-based index of the table row
     * @param type      the type of selector which is to open; may be either
     *                  {@code products} or {@code services}
     * @return          the web element representing the dialog
     */
    protected WebElement openSelector(int rowIdx, String type) {
        getPriceTableRow(rowIdx).
            findElement(By.className("select-btn-${type}")).
            click()
        def by = By.id("inventory-selector-${type}")
        def wait = new WebDriverWait(driver, 5)
        wait.until ExpectedConditions.visibilityOfElementLocated(by)
    }

    /**
     * Opens a sales item selector dialog by clicking the corresponding link in
     * the table row with the given index.  After opening the dialog the dialog
     * is closed again.
     *
     * @param rowIdx    the zero-based index of the table row
     * @param type      the type of selector which is to open; may be either
     *                  {@code products} or {@code services}
     */
    protected void openSelectorAndAbort(int rowIdx, String type) {
        WebElement dialog = openSelector rowIdx, type
        dialog.findElement(By.xpath('./preceding-sibling::div'))
            .findElement(By.className('ui-dialog-titlebar-close'))
            .click()
    }

    /**
     * Opens a sales item selector dialog by clicking the corresponding link in
     * the table row with the given index.  After opening the dialog the stated
     * link is clicked.
     *
     * @param rowIdx        the zero-based index of the table row
     * @param type          the type of selector which is to open; may be
     *                      either {@code products} or {@code services}
     * @param selectLink    the label of the link which is to click in the
     *                      selector dialog
     */
    protected void openSelectorAndSelect(int rowIdx, String type,
                                         String selectLink)
    {
        WebElement dialog = openSelector rowIdx, type
        dialog.findElement(By.linkText(selectLink)).click()
    }

    /**
     * Removes the row with the given index.
     *
     * @param rowIdx    the zero-based index of the table row
     * @return          the current number of rows in the price table after
     *                  removing the row
     */
    protected int removeRow(int rowIdx) {
        getPriceTableRow(rowIdx).findElement(By.className('action-buttons'))
            .findElement(By.className('remove-btn'))
            .click()
        numPriceTableRows
    }

    /**
     * Sets the value of the input field with the given name in the stated row
     * in the price table.
     *
     * @param rowIdx    the zero-based index of the table row
     * @param name      the name of the field such as "quantity", "name" etc.
     * @param value     the value to set
     */
    protected void setPriceTableInputValue(int rowIdx, String name,
                                           String value)
    {
        setInputValue "items[${rowIdx}].${name}", value
    }
}
