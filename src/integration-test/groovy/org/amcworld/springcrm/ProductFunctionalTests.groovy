/*
 * ProductFunctionalTests.groovy
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

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.openqa.selenium.Alert
import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.Select


/**
 * The class {@code ProductFunctionalTests} represents a functional test
 * case for the product section of SpringCRM.
 *
 * @author  Daniel Ellermann
 * @version 1.4
 * @since   1.3
 */
@RunWith(JUnit4)
class ProductFunctionalTests extends SalesItemTestCase {

    //-- Instance variables ---------------------

    @Rule
    public TestName name = new TestName()


    //-- Public methods -------------------------

    @Before
    void login() {
        String methodName = name.methodName
        if (!methodName.startsWith('testCreate')) {
            if (methodName.endsWith('NoPricing')) {
                prepareProductWithoutPricing()
            } else {
                prepareProductWithPricing()
            }
        }

        open '/', 'de'
        driver.findElement(BY_USER_NAME).sendKeys 'mkampe'
        driver.findElement(BY_PASSWORD).sendKeys 'abc1234'
        driver.findElement(BY_LOGIN_BTN).click()

        open '/product/list'
    }

    @Test
    void testCreateProductSuccessNoPricing() {
        clickToolbarButton 0, getUrl('/product/create')
        checkTitles 'Produkt anlegen', 'Produkte', 'Neues Produkt'
        setInputValue 'name', 'Papier A4 80 g/m²'
        setInputValue 'category.id', '3000'
        setInputValue 'manufacturer', 'Papierwerk Brandenburg'
        setInputValue 'retailer', 'Druckereibedarf Kiel GmbH'
        setInputValue 'quantity', '1'
        setInputValue 'unit.id', '302'
        setInputValue 'unitPrice', '2,49'
        setInputValue 'weight', '200'
        setInputValue 'taxRate.id', '401'
        setInputValue 'purchasePrice', '2,19'
        setInputValue 'description', 'Packung zu 100 Blatt. Chlorfrei gebleicht.'
        assert 'Für diesen Artikel wurde noch keine Kalkulation durchgeführt.' == driver.findElement(By.cssSelector('.empty-list > p')).text
        WebElement link = driver.findElement(By.id('start-pricing'))
        assert link.displayed
        assert 'Kalkulation beginnen' == link.text
        submitForm getUrl('/product/show/')

        assert 'Produkt Papier A4 80 g/m² wurde angelegt.' == flashMessage
        assert 'Papier A4 80 g/m²' == driver.findElement(BY_SUBHEADER).text
        def dataSheet = driver.findElement(By.className('data-sheet'))
        def fieldSet = getFieldset(dataSheet, 1)
        def col = fieldSet.findElement(By.className('col-l'))
        assert 'P-10000' == getShowFieldText(col, 1)
        assert 'Papier A4 80 g/m²' == getShowFieldText(col, 2)
        assert 'Materialien' == getShowFieldText(col, 3)
        assert 'Papierwerk Brandenburg' == getShowFieldText(col, 4)
        assert 'Druckereibedarf Kiel GmbH' == getShowFieldText(col, 5)
        assert '1' == getShowFieldText(col, 6)
        assert 'Packung' == getShowFieldText(col, 7)
        assert '2,490 €' == getShowFieldText(col, 8)
        col = fieldSet.findElement(By.className('col-r'))
        assert '200' == getShowFieldText(col, 1)
        assert '7 %' == getShowFieldText(col, 2)
        assert '2,190 €' == getShowFieldText(col, 3)
        fieldSet = getFieldset(dataSheet, 2)
        assert 'Packung zu 100 Blatt. Chlorfrei gebleicht.' == getShowFieldText(fieldSet, 1)
        driver.quit()

        assert 1 == Product.count()
    }

    @Test
    void testCreateProductSuccessPricing() {
        clickToolbarButton 0, getUrl('/product/create')
        checkTitles 'Produkt anlegen', 'Produkte', 'Neues Produkt'
        setInputValue 'name', 'Papier A4 80 g/m²'
        setInputValue 'category.id', '3000'
        setInputValue 'manufacturer', 'Papierwerk Brandenburg'
        setInputValue 'retailer', 'Druckereibedarf Kiel GmbH'
        setInputValue 'quantity', '1'
        setInputValue 'unit.id', '302'
        setInputValue 'unitPrice', '2,49'
        setInputValue 'weight', '200'
        setInputValue 'taxRate.id', '401'
        setInputValue 'description', 'Packung zu 100 Blatt. Chlorfrei gebleicht.'
        assert 'Für diesen Artikel wurde noch keine Kalkulation durchgeführt.' == driver.findElement(By.cssSelector('.empty-list > p')).text
        driver.findElement(By.id('start-pricing')).click()
        assert !getInput('quantity').displayed
        assert !getInput('unitPrice').displayed
        assert !getInput('unit.id').displayed
        setInputValue 'pricing.quantity', '10'
        setInputValue 'pricing.unit.id', '302'
        assert 1 == numStep1TableRows
        setStep1TableInputValue 0, 'quantity', '10'
        setStep1TableInputValue 0, 'unit', 'Packungen'
        setStep1TableInputValue 0, 'name', 'Materialeinkauf'
        setStep1TableInputValue 0, 'unitPrice', '2,19'
        assert '21,900' == getStep1TableRowTotal(0)
        assert '21,900' == step1Total
        assert '2,190' == step1UnitPrice
        assert '(10 Packung)' == driver.findElement(By.id('step1-total-price-quantity')).text
        assert '(1 Packung)' == driver.findElement(By.id('step1-unit-price-quantity')).text
        WebElement tr = getStep1TableRow(0)
        assert !tr.findElement(By.className('up-btn')).displayed
        assert !tr.findElement(By.className('down-btn')).displayed
        assert !tr.findElement(By.className('remove-btn')).displayed
        assert 2 == addNewStep1TableRow()
        setStep1TableInputValue 1, 'quantity', '0,25'
        setStep1TableInputValue 1, 'unit', 'Stunden'
        setStep1TableInputValue 1, 'name', 'Materialbeschaffung'
        setStep1TableInputValue 1, 'unitPrice', '40'
        assert '10,000' == getStep1TableRowTotal(1)
        assert '31,900' == step1Total
        assert '3,190' == step1UnitPrice
        tr = getStep1TableRow(0)
        assert !tr.findElement(By.className('up-btn')).displayed
        assert tr.findElement(By.className('down-btn')).displayed
        assert tr.findElement(By.className('remove-btn')).displayed
        tr = getStep1TableRow(1)
        assert tr.findElement(By.className('up-btn')).displayed
        assert !tr.findElement(By.className('down-btn')).displayed
        assert tr.findElement(By.className('remove-btn')).displayed
        assert 3 == addNewStep1TableRow()
        setStep1TableInputValue 2, 'type', 'sum'
        assert !getStep1TableInput(2, 'quantity').enabled
        assert !getStep1TableInput(2, 'unit').enabled
        assert !getStep1TableInput(2, 'name').enabled
        assert !getStep1TableInput(2, 'unitPrice').enabled
        assert '31,900' == getStep1TableRowTotal(2)
        assert '31,900' == step1Total
        assert '3,190' == step1UnitPrice
        tr = getStep1TableRow(0)
        assert !tr.findElement(By.className('up-btn')).displayed
        assert tr.findElement(By.className('down-btn')).displayed
        assert tr.findElement(By.className('remove-btn')).displayed
        tr = getStep1TableRow(1)
        assert tr.findElement(By.className('up-btn')).displayed
        assert tr.findElement(By.className('down-btn')).displayed
        assert tr.findElement(By.className('remove-btn')).displayed
        tr = getStep1TableRow(2)
        assert tr.findElement(By.className('up-btn')).displayed
        assert !tr.findElement(By.className('down-btn')).displayed
        assert tr.findElement(By.className('remove-btn')).displayed
        assert 4 == addNewStep1TableRow()
        setStep1TableInputValue 3, 'type', 'relativeToCurrentSum'
        setStep1TableInputValue 3, 'quantity', '1'
        setStep1TableInputValue 3, 'unit', 'Einheit'
        setStep1TableInputValue 3, 'name', 'Gewinn'
        setStep1TableInputValue 3, 'unitPercent', '5'
        assert '1,600' == getStep1TableRowTotal(3)
        assert '33,500' == step1Total
        assert '3,350' == step1UnitPrice
        assert 5 == addNewStep1TableRow()
        setStep1TableInputValue 4, 'type', 'relativeToLastSum'
        setStep1TableInputValue 4, 'quantity', '1'
        setStep1TableInputValue 4, 'unit', 'Einheit'
        setStep1TableInputValue 4, 'name', 'Risiko'
        setStep1TableInputValue 4, 'unitPercent', '5'
        assert '1,600' == getStep1TableRowTotal(4)
        assert '35,100' == step1Total
        assert '3,510' == step1UnitPrice
        moveRowDown 0
        assert '35,100' == step1Total
        assert '3,510' == step1UnitPrice
        moveRowUp 1
        assert '35,100' == step1Total
        assert '3,510' == step1UnitPrice
        moveRowUp 2
        assert '21,900' == getStep1TableRowTotal(1)
        assert '1,600' == getStep1TableRowTotal(3)
        assert '1,100' == getStep1TableRowTotal(4)
        assert '34,600' == step1Total
        assert '3,460' == step1UnitPrice
        moveRowDown 1
        assert '35,100' == step1Total
        assert '3,510' == step1UnitPrice
        assert '10' == getValue('step2-quantity')
        assert 'Packung' == getValue('step2-unit')
        assert '3,510' == getValue('step2-unit-price')
        assert '35,100' == getValue('step2-total-price')
        assert '10' == getValue('step2-total-quantity')
        assert 'Packung' == getValue('step2-total-unit')
        assert '3,510' == getValue('step2-total-unit-price')
        assert '35,100' == getValue('step2-total')
        setInputValue 'pricing.discountPercent', '1'
        assert '0,351' == getValue('step2-discount-percent-amount')
        setInputValue 'pricing.adjustment', '0,15'
        assert '10' == getValue('step2-total-quantity')
        assert 'Packung' == getValue('step2-total-unit')
        assert '3,490' == getValue('step2-total-unit-price')
        assert '34,899' == getValue('step2-total')
        assert '---' == getValue('step3-unit-price')
        assert '---' == getValue('step3-total-price')
        WebElement input = driver.findElement(By.id('step3-quantity'))
        input.sendKeys '10'
        def select = new Select(driver.findElement(By.id('step3-unit')))
        select.selectByValue '302'
        assert '3,490' == getValue('step3-unit-price')
        assert '34,899' == getValue('step3-total-price')
        input.clear()
        input.sendKeys '2'
        select.selectByValue '301'
        assert '17,450' == getValue('step3-unit-price')
        assert '34,899' == getValue('step3-total-price')
        submitForm getUrl('/product/show/')

        assert 'Produkt Papier A4 80 g/m² wurde angelegt.' == flashMessage
        assert 'Papier A4 80 g/m²' == driver.findElement(BY_SUBHEADER).text
        def dataSheet = driver.findElement(By.className('data-sheet'))
        def fieldSet = getFieldset(dataSheet, 1)
        def col = fieldSet.findElement(By.className('col-l'))
        assert 'P-10000' == getShowFieldText(col, 1)
        assert 'Papier A4 80 g/m²' == getShowFieldText(col, 2)
        assert 'Materialien' == getShowFieldText(col, 3)
        assert 'Papierwerk Brandenburg' == getShowFieldText(col, 4)
        assert 'Druckereibedarf Kiel GmbH' == getShowFieldText(col, 5)
        assert '2' == getShowFieldText(col, 6)
        assert 'Einheiten' == getShowFieldText(col, 7)
        assert '17,445 €' == getShowFieldText(col, 8)
        col = fieldSet.findElement(By.className('col-r'))
        assert '200' == getShowFieldText(col, 1)
        assert '7 %' == getShowFieldText(col, 2)
        fieldSet = getFieldset(dataSheet, 2)
        assert 'Packung zu 100 Blatt. Chlorfrei gebleicht.' == getShowFieldText(fieldSet, 1)
        fieldSet = getFieldset(dataSheet, 3)
        assert 'In der folgenden Tabelle berechne ich den Preis für 10 Packung:' == fieldSet.findElement(By.xpath('./p')).text
        WebElement tbody = fieldSet.findElement(By.tagName('tbody'))
        checkStaticRowValues tbody, 0, '1.', '10', 'Packungen', 'Materialeinkauf', 'absoluter Betrag', '', '0,00', '2,190 €', '21,900 €'
        checkStaticRowValues tbody, 1, '2.', '0,25', 'Stunden', 'Materialbeschaffung', 'absoluter Betrag', '', '0,00', '40,000 €', '10,000 €'
        checkStaticRowValues tbody, 2, '3.', '', 'Zwischensumme', '', '31,900 €'
        checkStaticRowValues tbody, 3, '4.', '1', 'Einheit', 'Gewinn', 'relativ zur akt. Summe', '', '5,00', '1,595 €', '1,595 €'
        checkStaticRowValues tbody, 4, '5.', '1', 'Einheit', 'Risiko', 'relativ zur letzt. Zw.-summe', '', '5,00', '1,595 €', '1,595 €'
        WebElement tfoot = fieldSet.findElement(By.tagName('tfoot'))
        checkStaticRowValues tfoot, 0, 'Gesamtpreis (10 Packung)', '', '35,090 €'
        checkStaticRowValues tfoot, 1, 'Kalkulierter Einzelpreis (1 Packung)', '', '3,509 €'
        fieldSet = getFieldset(dataSheet, 4)
        tbody = fieldSet.findElement(By.tagName('tbody'))
        checkStaticRowValues tbody, 0, 'Kalkulierter Gesamtwert', '10', 'Packung', '', 'zu je', '3,509 €', '35,090 €'
        checkStaticRowValues tbody, 1, 'Rabatt %', '', '', '1,00', '', '', '0,351 €'
        checkStaticRowValues tbody, 2, 'Preisanpassung +/-', '', '', '', '', '', '0,150 €'
        tfoot = fieldSet.findElement(By.tagName('tfoot'))
        checkStaticRowValues tfoot, 0, 'Verkaufspreis', '10', 'Packung', '', 'zu je', '3,489 €', '34,889 €'
        fieldSet = getFieldset(dataSheet, 5)
        tbody = fieldSet.findElement(By.tagName('tbody'))
        checkStaticRowValues tbody, 0, 'Der Artikel wird verkauft als', '2', 'Einheiten', 'zu je', '17,445 €', '34,889 €'
        driver.quit()

        assert 1 == Product.count()
    }

    @Test
    void testCreateProductErrorsNoPricing() {
        clickToolbarButton 0, getUrl('/product/create')
        checkTitles 'Produkt anlegen', 'Produkte', 'Neues Produkt'
        submitForm getUrl('/product/save?returnUrl=')

        assert checkErrorFields(['name'])
        cancelForm getUrl('/product/list')

        def emptyList = driver.findElement(By.className('empty-list'))
        assert 'Diese Liste enthält keine Einträge.' == emptyList.findElement(By.tagName('p')).text
        def link = emptyList.findElement(By.cssSelector('div.buttons > a.button'))
        assert 'Produkt anlegen' == link.text
        assert getUrl('/product/create') == link.getAttribute('href')
        driver.quit()

        assert 0 == Product.count()
    }

    @Test
    void testCreateProductErrorsPricing() {
        clickToolbarButton 0, getUrl('/product/create')
        checkTitles 'Produkt anlegen', 'Produkte', 'Neues Produkt'
        driver.findElement(By.id('start-pricing')).click()
        submitForm getUrl('/product/save?returnUrl=')

        assert checkErrorFields(['name', 'quantity'])
        WebElement fieldSet = driver.findElement(By.xpath('//fieldset[3]'))
        List<WebElement> errorMsgs = fieldSet.findElements(By.xpath('.//div[@id="step1-calculation-base"]//li[@class="error-msg"]'))
        assert 2 == errorMsgs.size()
        assert 'Muss größer als 0 sein.' == errorMsgs[0].text
        assert 'Feld darf nicht leer sein.' == errorMsgs[1].text
        errorMsgs = fieldSet.findElements(By.xpath(
            './div[1]/ul[@class="field-msgs"]/li[@class="error-msg"]'
        ))
        assert 2 == errorMsgs.size()
        assert 'Pos. 1, Bezeichnung: Feld darf nicht leer sein.' == errorMsgs[0].text
        assert 'Pos. 1, Einheit: Feld darf nicht leer sein.' == errorMsgs[1].text
        WebElement errorMsg = driver.findElement(By.xpath('//input[@id="step3-quantity"]/following-sibling::ul[@class="field-msgs"]/li[@class="error-msg"]'))
        assert 'Muss größer als 0 sein.' == errorMsg.text
        errorMsg = driver.findElement(By.xpath('//select[@id="step3-unit"]/following-sibling::ul[@class="field-msgs"]/li[@class="error-msg"]'))
        assert 'Feld darf nicht leer sein.' == errorMsg.text
        cancelForm getUrl('/product/list')

        def emptyList = driver.findElement(By.className('empty-list'))
        assert 'Diese Liste enthält keine Einträge.' == emptyList.findElement(By.tagName('p')).text
        def link = emptyList.findElement(By.cssSelector('div.buttons > a.button'))
        assert 'Produkt anlegen' == link.text
        assert getUrl('/product/create') == link.getAttribute('href')
        driver.quit()

        assert 0 == Product.count()
    }

    @Test
    void testShowProductNoPricing() {
        int id = clickListItem 0, 1, '/product/show'
        checkTitles 'Produkt anzeigen', 'Produkte', 'Papier A4 80 g/m²'
        def dataSheet = driver.findElement(By.className('data-sheet'))
        def fieldSet = getFieldset(dataSheet, 1)
        assert 'Allgemeine Informationen' == fieldSet.findElement(By.tagName('h3')).text
        def col = fieldSet.findElement(By.className('col-l'))
        assert 'P-10000' == getShowFieldText(col, 1)
        assert 'Papier A4 80 g/m²' == getShowFieldText(col, 2)
        assert 'Materialien' == getShowFieldText(col, 3)
        assert 'Papierwerk Brandenburg' == getShowFieldText(col, 4)
        assert 'Druckereibedarf Kiel GmbH' == getShowFieldText(col, 5)
        assert '1' == getShowFieldText(col, 6)
        assert 'Packung' == getShowFieldText(col, 7)
        assert '2,490 €' == getShowFieldText(col, 8)
        col = fieldSet.findElement(By.className('col-r'))
        assert '200' == getShowFieldText(col, 1)
        assert '7 %' == getShowFieldText(col, 2)
        assert '2,190 €' == getShowFieldText(col, 3)
        fieldSet = getFieldset(dataSheet, 2)
        assert 'Beschreibung' == fieldSet.findElement(By.tagName('h3')).text
        assert 'Packung zu 100 Blatt. Chlorfrei gebleicht.' == getShowFieldText(fieldSet, 1)

        assert driver.findElement(By.className('record-timestamps')).text.startsWith('Erstellt am ')

        checkDefaultShowToolbar 'product', id

        assert 0 == driver.findElements(By.xpath('//aside[@id="action-bar"]/ul')).size()
        driver.quit()

        assert 1 == Product.count()
    }

    @Test
    void testShowProductPricing() {
        int id = clickListItem 0, 1, '/product/show'
        checkTitles 'Produkt anzeigen', 'Produkte', 'Papier A4 80 g/m²'
        def dataSheet = driver.findElement(By.className('data-sheet'))
        def fieldSet = getFieldset(dataSheet, 1)
        assert 'Allgemeine Informationen' == fieldSet.findElement(By.tagName('h3')).text
        def col = fieldSet.findElement(By.className('col-l'))
        assert 'P-10000' == getShowFieldText(col, 1)
        assert 'Papier A4 80 g/m²' == getShowFieldText(col, 2)
        assert 'Materialien' == getShowFieldText(col, 3)
        assert 'Papierwerk Brandenburg' == getShowFieldText(col, 4)
        assert 'Druckereibedarf Kiel GmbH' == getShowFieldText(col, 5)
        assert '10' == getShowFieldText(col, 6)
        assert 'Packung' == getShowFieldText(col, 7)
        assert '3,489 €' == getShowFieldText(col, 8)
        col = fieldSet.findElement(By.className('col-r'))
        assert '200' == getShowFieldText(col, 1)
        assert '7 %' == getShowFieldText(col, 2)
        assert '2,190 €' == getShowFieldText(col, 3)
        fieldSet = getFieldset(dataSheet, 2)
        assert 'Beschreibung' == fieldSet.findElement(By.tagName('h3')).text
        assert 'Packung zu 100 Blatt. Chlorfrei gebleicht.' == getShowFieldText(fieldSet, 1)

        fieldSet = getFieldset(dataSheet, 3)
        assert 'In der folgenden Tabelle berechne ich den Preis für 10 Packung:' == fieldSet.findElement(By.xpath('./p')).text
        WebElement tbody = fieldSet.findElement(By.tagName('tbody'))
        checkStaticRowValues tbody, 0, '1.', '10', 'Packungen', 'Materialeinkauf', 'absoluter Betrag', '', '0,00', '2,190 €', '21,900 €'
        checkStaticRowValues tbody, 1, '2.', '0,25', 'Stunden', 'Materialbeschaffung', 'absoluter Betrag', '', '0,00', '40,000 €', '10,000 €'
        checkStaticRowValues tbody, 2, '3.', '', 'Zwischensumme', '', '31,900 €'
        checkStaticRowValues tbody, 3, '4.', '1', 'Einheit', 'Gewinn', 'relativ zur akt. Summe', '', '5,00', '1,595 €', '1,595 €'
        checkStaticRowValues tbody, 4, '5.', '1', 'Einheit', 'Risiko', 'relativ zur letzt. Zw.-summe', '', '5,00', '1,595 €', '1,595 €'
        WebElement tfoot = fieldSet.findElement(By.tagName('tfoot'))
        checkStaticRowValues tfoot, 0, 'Gesamtpreis (10 Packung)', '', '35,090 €'
        checkStaticRowValues tfoot, 1, 'Kalkulierter Einzelpreis (1 Packung)', '', '3,509 €'
        fieldSet = getFieldset(dataSheet, 4)
        tbody = fieldSet.findElement(By.tagName('tbody'))
        checkStaticRowValues tbody, 0, 'Kalkulierter Gesamtwert', '10', 'Packung', '', 'zu je', '3,509 €', '35,090 €'
        checkStaticRowValues tbody, 1, 'Rabatt %', '', '', '1,00', '', '', '0,351 €'
        checkStaticRowValues tbody, 2, 'Preisanpassung +/-', '', '', '', '', '', '0,150 €'
        tfoot = fieldSet.findElement(By.tagName('tfoot'))
        checkStaticRowValues tfoot, 0, 'Verkaufspreis', '10', 'Packung', '', 'zu je', '3,489 €', '34,889 €'
        fieldSet = getFieldset(dataSheet, 5)
        tbody = fieldSet.findElement(By.tagName('tbody'))
        checkStaticRowValues tbody, 0, 'Der Artikel wird verkauft als', '10', 'Packung', 'zu je', '3,489 €', '34,889 €'

        assert driver.findElement(By.className('record-timestamps')).text.startsWith('Erstellt am ')

        checkDefaultShowToolbar 'product', id

        assert 0 == driver.findElements(By.xpath('//aside[@id="action-bar"]/ul')).size()
        driver.quit()

        assert 1 == Product.count()
    }

    @Test
    void testListProductsNoPricing() {
        checkTitles 'Produkte', 'Produkte'
        def link = driver.findElement(By.xpath('//ul[@class="letter-bar"]/li[@class="available"]/a'))
        assert getUrl('/product/list?letter=P') == link.getAttribute('href')
        assert 'P' == link.text
        assert 1 == driver.findElements(By.xpath('//ul[@class="letter-bar"]/li[@class="available"]')).size()

        def tbody = driver.findElement(By.xpath('//table[@class="content-table"]/tbody'))
        assert 1 == tbody.findElements(By.tagName('tr')).size()
        def tr = tbody.findElement(By.xpath('tr[1]'))
        def td = tr.findElement(By.xpath('td[2]'))
        assert td.getAttribute('class').contains('id')
        link = td.findElement(By.tagName('a'))
        assert link.getAttribute('href').startsWith(getUrl('/product/show/'))
        assert 'P-10000' == link.text
        td = tr.findElement(By.xpath('td[3]'))
        assert td.getAttribute('class').contains('string')
        link = td.findElement(By.tagName('a'))
        assert link.getAttribute('href').startsWith(getUrl('/product/show/'))
        assert 'Papier A4 80 g/m²' == link.text
        td = tr.findElement(By.xpath('td[4]'))
        assert td.getAttribute('class').contains('string')
        assert 'Materialien' == td.text
        td = tr.findElement(By.xpath('td[5]'))
        assert td.getAttribute('class').contains('number')
        assert '1' == td.text
        td = tr.findElement(By.xpath('td[6]'))
        assert td.getAttribute('class').contains('string')
        assert 'Packung' == td.text
        td = tr.findElement(By.xpath('td[7]'))
        assert td.getAttribute('class').contains('currency')
        assert '2,490 €' == td.text
        td = tr.findElement(By.xpath('td[8]'))
        assert td.getAttribute('class').contains('action-buttons')
        link = td.findElement(By.xpath('a[1]'))
        assert link.getAttribute('href').startsWith(getUrl('/product/edit/'))
        assert link.getAttribute('class').contains('button')
        assert link.getAttribute('class').contains('green')
        assert 'Bearbeiten' == link.text
        link = td.findElement(By.xpath('a[2]'))
        assert link.getAttribute('href').startsWith(getUrl('/product/delete/'))
        assert link.getAttribute('class').contains('button')
        assert link.getAttribute('class').contains('red')
        assert link.getAttribute('class').contains('delete-btn')
        assert 'Löschen' == link.text
        link.click()
        driver.switchTo().alert().dismiss()
        assert getUrl('/product/list') == driver.currentUrl
        driver.quit()

        assert 1 == Product.count()
    }

    @Test
    void testListProductsPricing() {
        checkTitles 'Produkte', 'Produkte'
        def link = driver.findElement(By.xpath('//ul[@class="letter-bar"]/li[@class="available"]/a'))
        assert getUrl('/product/list?letter=P') == link.getAttribute('href')
        assert 'P' == link.text
        assert 1 == driver.findElements(By.xpath('//ul[@class="letter-bar"]/li[@class="available"]')).size()

        def tbody = driver.findElement(By.xpath('//table[@class="content-table"]/tbody'))
        assert 1 == tbody.findElements(By.tagName('tr')).size()
        def tr = tbody.findElement(By.xpath('tr[1]'))
        def td = tr.findElement(By.xpath('td[2]'))
        assert td.getAttribute('class').contains('id')
        link = td.findElement(By.tagName('a'))
        assert link.getAttribute('href').startsWith(getUrl('/product/show/'))
        assert 'P-10000' == link.text
        td = tr.findElement(By.xpath('td[3]'))
        assert td.getAttribute('class').contains('string')
        link = td.findElement(By.tagName('a'))
        assert link.getAttribute('href').startsWith(getUrl('/product/show/'))
        assert 'Papier A4 80 g/m²' == link.text
        td = tr.findElement(By.xpath('td[4]'))
        assert td.getAttribute('class').contains('string')
        assert 'Materialien' == td.text
        td = tr.findElement(By.xpath('td[5]'))
        assert td.getAttribute('class').contains('number')
        assert '10' == td.text
        td = tr.findElement(By.xpath('td[6]'))
        assert td.getAttribute('class').contains('string')
        assert 'Packung' == td.text
        td = tr.findElement(By.xpath('td[7]'))
        assert td.getAttribute('class').contains('currency')
        assert '3,489 €' == td.text
        td = tr.findElement(By.xpath('td[8]'))
        assert td.getAttribute('class').contains('action-buttons')
        link = td.findElement(By.xpath('a[1]'))
        assert link.getAttribute('href').startsWith(getUrl('/product/edit/'))
        assert link.getAttribute('class').contains('button')
        assert link.getAttribute('class').contains('green')
        assert 'Bearbeiten' == link.text
        link = td.findElement(By.xpath('a[2]'))
        assert link.getAttribute('href').startsWith(getUrl('/product/delete/'))
        assert link.getAttribute('class').contains('button')
        assert link.getAttribute('class').contains('red')
        assert link.getAttribute('class').contains('delete-btn')
        assert 'Löschen' == link.text
        link.click()
        driver.switchTo().alert().dismiss()
        assert getUrl('/product/list') == driver.currentUrl
        driver.quit()

        assert 1 == Product.count()
    }

    @Test
    void testEditProductSuccessNoPricing() {
        clickListActionButton 0, 0, getUrl('/product/edit/')
        checkTitles 'Produkt bearbeiten', 'Produkte', 'Papier A4 80 g/m²'
        def col = driver.findElement(By.xpath('//form[@id="product-form"]/fieldset[1]')).findElement(By.className('col-l'))
        assert getShowField(col, 1).text.startsWith('P-')
        assert '10000' == getInputValue('number')
        assert getInputValue('autoNumber')
        assert 'Papier A4 80 g/m²' == getInputValue('name')
        assert '3000' == getInputValue('category.id')
        assert 'Papierwerk Brandenburg' == getInputValue('manufacturer')
        assert 'Druckereibedarf Kiel GmbH' == getInputValue('retailer')
        assert '1' == getInputValue('quantity')
        assert '302' == getInputValue('unit.id')
        assert '2,490' == getInputValue('unitPrice')
        assert '200' == getInputValue('weight')
        assert '401' == getInputValue('taxRate.id')
        assert '2,190' == getInputValue('purchasePrice')
        assert 'Packung zu 100 Blatt. Chlorfrei gebleicht.' == getInputValue('description')
        assert 'Für diesen Artikel wurde noch keine Kalkulation durchgeführt.' == driver.findElement(By.cssSelector('.empty-list > p')).text
        WebElement link = driver.findElement(By.id('start-pricing'))
        assert link.displayed
        assert 'Kalkulation beginnen' == link.text

        setInputValue 'autoNumber', false
        setInputValue 'number', '10700'
        setInputValue 'name', 'Stempel'
        setInputValue 'manufacturer', 'Büromaterialien Herrmann Friesland'
        setInputValue 'retailer', 'Büro und mehr - Schneider'
        setInputValue 'unit.id', '300'
        setInputValue 'unitPrice', '8,99'
        setInputValue 'weight', '100'
        setInputValue 'taxRate.id', '400'
        setInputValue 'purchasePrice', '7,99'
        setInputValue 'description', 'Mit Firmenaufdruck nach Kundenvorgabe.'
        submitForm getUrl('/product/show/')

        assert 'Produkt Stempel wurde geändert.' == flashMessage
        assert 'Stempel' == driver.findElement(BY_SUBHEADER).text
        def dataSheet = driver.findElement(By.className('data-sheet'))
        def fieldSet = getFieldset(dataSheet, 1)
        col = fieldSet.findElement(By.className('col-l'))
        assert 'P-10700' == getShowFieldText(col, 1)
        assert 'Stempel' == getShowFieldText(col, 2)
        assert 'Materialien' == getShowFieldText(col, 3)
        assert 'Büromaterialien Herrmann Friesland' == getShowFieldText(col, 4)
        assert 'Büro und mehr - Schneider' == getShowFieldText(col, 5)
        assert '1' == getShowFieldText(col, 6)
        assert 'Stück' == getShowFieldText(col, 7)
        assert '8,990 €' == getShowFieldText(col, 8)
        col = fieldSet.findElement(By.className('col-r'))
        assert '100' == getShowFieldText(col, 1)
        assert '19 %' == getShowFieldText(col, 2)
        assert '7,990 €' == getShowFieldText(col, 3)
        fieldSet = getFieldset(dataSheet, 2)
        assert 'Mit Firmenaufdruck nach Kundenvorgabe.' == getShowFieldText(fieldSet, 1)
        driver.quit()

        assert 1 == Product.count()
    }

    @Test
    void testEditProductSuccessPricing() {
        clickListActionButton 0, 0, getUrl('/product/edit/')
        checkTitles 'Produkt bearbeiten', 'Produkte', 'Papier A4 80 g/m²'
        def col = driver.findElement(By.xpath('//form[@id="product-form"]/fieldset[1]')).findElement(By.className('col-l'))
        assert getShowField(col, 1).text.startsWith('P-')
        assert '10000' == getInputValue('number')
        assert getInputValue('autoNumber')
        assert 'Papier A4 80 g/m²' == getInputValue('name')
        assert '3000' == getInputValue('category.id')
        assert 'Papierwerk Brandenburg' == getInputValue('manufacturer')
        assert 'Druckereibedarf Kiel GmbH' == getInputValue('retailer')
        assert '200' == getInputValue('weight')
        assert '401' == getInputValue('taxRate.id')
        assert '2,190' == getInputValue('purchasePrice')
        assert 'Packung zu 100 Blatt. Chlorfrei gebleicht.' == getInputValue('description')
        assert '10' == getInputValue('pricing.quantity')
        assert '302' == getInputValue('pricing.unit.id')

        checkStep1RowValues 0, '10', 'Packungen', 'Materialeinkauf', 'absolute', null, null, '2,190', '21,900'
        checkStep1RowValues 1, '0,25', 'Stunden', 'Materialbeschaffung', 'absolute', null, null, '40,000', '10,000'
        checkStep1RowValues 2, null, null, null, 'sum', null, null, null, '31,900'
        checkStep1RowValues 3, '1', 'Einheit', 'Gewinn', 'relativeToCurrentSum', null, '5,00', '1,595', '1,595'
        checkStep1RowValues 4, '1', 'Einheit', 'Risiko', 'relativeToLastSum', null, '5,00', '1,595', '1,595'
        assert '35,090' == step1Total
        assert '3,509' == step1UnitPrice
        assert '10' == getValue('step2-quantity')
        assert 'Packung' == getValue('step2-unit')
        assert '3,509' == getValue('step2-unit-price')
        assert '35,090' == getValue('step2-total-price')
        assert '1,00' == getInputValue('pricing.discountPercent')
        assert '0,351' == getValue('step2-discount-percent-amount')
        assert '0,150' == getInputValue('pricing.adjustment')
        assert '10' == getValue('step2-total-quantity')
        assert 'Packung' == getValue('step2-total-unit')
        assert '3,489' == getValue('step2-total-unit-price')
        assert '34,889' == getValue('step2-total')
        assert '10' == driver.findElement(By.id('step3-quantity')).getAttribute('value')
        assert '302' == new Select(driver.findElement(By.id('step3-unit'))).firstSelectedOption.getAttribute('value')
        assert '3,489' == getValue('step3-unit-price')
        assert '34,889' == getValue('step3-total-price')

        setInputValue 'autoNumber', false
        setInputValue 'number', '10700'
        setInputValue 'name', 'Stempel'
        setInputValue 'manufacturer', 'Büromaterialien Herrmann Friesland'
        setInputValue 'retailer', 'Büro und mehr - Schneider'
        setInputValue 'weight', '100'
        setInputValue 'taxRate.id', '400'
        setInputValue 'purchasePrice', '7,99'
        setInputValue 'description', 'Mit Firmenaufdruck nach Kundenvorgabe.'

        setInputValue 'pricing.quantity', '20'
        setInputValue 'pricing.unit.id', '300'
        setStep1TableInputValue 0, 'quantity', '20'
        setStep1TableInputValue 0, 'unit', 'Stück'
        setStep1TableInputValue 0, 'unitPrice', '7,99'
        assert '159,800' == getStep1TableRowTotal(0)
        assert '(20 Stück)' == driver.findElement(By.id('step1-total-price-quantity')).text
        assert '(1 Stück)' == driver.findElement(By.id('step1-unit-price-quantity')).text

        assert 6 == addNewStep1TableRow()
        moveRowUp 5
        moveRowUp 4
        moveRowUp 3
        assert 'pricing.items[2].type' == getStep1TableCell(2, 'type').findElement(By.tagName('select')).getAttribute('name')
        setStep1TableInputValue 2, 'quantity', '1'
        setStep1TableInputValue 2, 'unit', 'Stunde'
        setStep1TableInputValue 2, 'name', 'Anpassungen'
        setStep1TableInputValue 2, 'unitPrice', '45,00'
        assert '45,000' == getStep1TableRowTotal(2)
        assert '214,800' == getStep1TableRowTotal(3)

        setStep1TableInputValue 4, 'type', 'relativeToLastSum'
        assert '10,740' == getStep1TableRowTotal(4)
        WebElement td = getStep1TableCell(5, 'relative-to-pos')
        WebElement span = td.findElement(By.tagName('span'))
        WebElement icon = span.findElement(By.tagName('i'))
        WebElement strong = span.findElement(By.tagName('strong'))
        assert !span.displayed

        setStep1TableInputValue 5, 'type', 'relativeToPos'
        assert span.displayed
        assert '' == strong.text
        icon.click()
        checkStep1NonSelectableFinderRows 5
        span.sendKeys Keys.ESCAPE
        for (int i = 0; i <= 5; i++) {
            assert '' == getStep1TableRow(i).getAttribute('class')
        }
        assert '' == strong.text

        setStep1TableReference 5, 1, 5
        getStep1TableRow(1).findElement(By.className('remove-btn')).click()
        assert 6 == numStep1TableRows
        assert '0,500' == getStep1TableRowTotal(5)
        moveRowDown 1
        assert 'pricing.items[1].type' == getStep1TableCell(1, 'type').findElement(By.tagName('select')).getAttribute('name')
        assert '3' == strong.text
        assert '2' == getStep1TableInput(5, 'relToPos').getAttribute('value')
        assert '0,500' == getStep1TableRowTotal(5)
        moveRowUp 2
        assert 'pricing.items[2].type' == getStep1TableCell(2, 'type').findElement(By.tagName('select')).getAttribute('name')
        assert '2' == strong.text
        assert '1' == getStep1TableInput(5, 'relToPos').getAttribute('value')
        assert '0,500' == getStep1TableRowTotal(5)

        setStep1TableReference 5, 3, 5
        assert '10,740' == getStep1TableRowTotal(5)
        assert '236,280' == getStep1Total()
        assert '11,814' == getStep1UnitPrice()
        moveRowUp 5
        moveRowUp 4
        Alert alert = driver.switchTo().alert()
        assert 'Zeile kann nicht verschoben werden, da "relativ zu Pos."-Einträge immer hinter der referenzierten Zeile stehen müssen.' == alert.text
        alert.accept()
        moveRowDown 4
        for (int i = 0; i <= 5; i++) {
            if (i == 3) {
                checkDisabledTypes i, 'relativeToPos'
            } else {
                checkDisabledTypes i
            }
        }

        assert 7 == addNewStep1TableRow()
        setStep1TableInputValue 6, 'quantity', '1'
        setStep1TableInputValue 6, 'unit', 'Einheit'
        setStep1TableInputValue 6, 'name', 'Test'
        setStep1TableInputValue 6, 'unitPrice', '40,000'
        checkDisabledTypes 6
        setStep1TableReference 5, 6, 5
        checkDisabledTypes 6, 'relativeToPos', 'relativeToLastSum', 'relativeToCurrentSum', 'sum'
        moveRowUp 6
        checkDisabledTypes 5, 'relativeToPos'
        moveRowDown 5
        setStep1TableReference 5, 3, 5
        setStep1TableInputValue 6, 'type', 'relativeToPos'
        setStep1TableReference 5, 3, 5, 6
        assert 6 == removeRow(6)

        setInputValue 'pricing.discountPercent', '2'
        assert '4,726' == getValue('step2-discount-percent-amount')
        setInputValue 'pricing.adjustment', '-1,564'
        assert '20' == getValue('step2-total-quantity')
        assert 'Stück' == getValue('step2-total-unit')
        assert '11,500' == getValue('step2-total-unit-price')
        assert '229,990' == getValue('step2-total')
        WebElement input = driver.findElement(By.id('step3-quantity'))
        input.clear()
        input.sendKeys '20'
        new Select(driver.findElement(By.id('step3-unit'))).selectByValue '300'
        assert '11,500' == getValue('step3-unit-price')
        assert '229,990' == getValue('step3-total-price')
        submitForm getUrl('/product/show/')

        assert 'Produkt Stempel wurde geändert.' == flashMessage
        assert 'Stempel' == driver.findElement(BY_SUBHEADER).text
        def dataSheet = driver.findElement(By.className('data-sheet'))
        def fieldSet = getFieldset(dataSheet, 1)
        col = fieldSet.findElement(By.className('col-l'))
        assert 'P-10700' == getShowFieldText(col, 1)
        assert 'Stempel' == getShowFieldText(col, 2)
        assert 'Materialien' == getShowFieldText(col, 3)
        assert 'Büromaterialien Herrmann Friesland' == getShowFieldText(col, 4)
        assert 'Büro und mehr - Schneider' == getShowFieldText(col, 5)
        assert '20' == getShowFieldText(col, 6)
        assert 'Stück' == getShowFieldText(col, 7)
        assert '11,500 €' == getShowFieldText(col, 8)
        col = fieldSet.findElement(By.className('col-r'))
        assert '100' == getShowFieldText(col, 1)
        assert '19 %' == getShowFieldText(col, 2)
        assert '7,990 €' == getShowFieldText(col, 3)
        fieldSet = getFieldset(dataSheet, 2)
        assert 'Mit Firmenaufdruck nach Kundenvorgabe.' == getShowFieldText(fieldSet, 1)

        fieldSet = getFieldset(dataSheet, 3)
        assert 'In der folgenden Tabelle berechne ich den Preis für 20 Stück:' == fieldSet.findElement(By.xpath('./p')).text
        WebElement tbody = fieldSet.findElement(By.tagName('tbody'))
        checkStaticRowValues tbody, 0, '1.', '20', 'Stück', 'Materialeinkauf', 'absoluter Betrag', '', '0,00', '7,990 €', '159,800 €'
        checkStaticRowValues tbody, 1, '2.', '0,25', 'Stunden', 'Materialbeschaffung', 'absoluter Betrag', '', '0,00', '40,000 €', '10,000 €'
        checkStaticRowValues tbody, 2, '3.', '1', 'Stunde', 'Anpassungen', 'absoluter Betrag', '', '0,00', '45,000 €', '45,000 €'
        checkStaticRowValues tbody, 3, '4.', '', 'Zwischensumme', '', '214,800 €'
        checkStaticRowValues tbody, 4, '5.', '1', 'Einheit', 'Gewinn', 'relativ zur letzt. Zw.-summe', '', '5,00', '10,740 €', '10,740 €'
        checkStaticRowValues tbody, 5, '6.', '1', 'Einheit', 'Risiko', 'relativ zu Pos.', '4', '5,00', '10,740 €', '10,740 €'
        WebElement tfoot = fieldSet.findElement(By.tagName('tfoot'))
        checkStaticRowValues tfoot, 0, 'Gesamtpreis (20 Stück)', '', '236,280 €'
        checkStaticRowValues tfoot, 1, 'Kalkulierter Einzelpreis (1 Stück)', '', '11,814 €'
        fieldSet = getFieldset(dataSheet, 4)
        tbody = fieldSet.findElement(By.tagName('tbody'))
        checkStaticRowValues tbody, 0, 'Kalkulierter Gesamtwert', '20', 'Stück', '', 'zu je', '11,814 €', '236,280 €'
        checkStaticRowValues tbody, 1, 'Rabatt %', '', '', '2,00', '', '', '4,726 €'
        checkStaticRowValues tbody, 2, 'Preisanpassung +/-', '', '', '', '', '', '-1,564 €'
        tfoot = fieldSet.findElement(By.tagName('tfoot'))
        checkStaticRowValues tfoot, 0, 'Verkaufspreis', '20', 'Stück', '', 'zu je', '11,500 €', '229,990 €'
        fieldSet = getFieldset(dataSheet, 5)
        tbody = fieldSet.findElement(By.tagName('tbody'))
        checkStaticRowValues tbody, 0, 'Der Artikel wird verkauft als', '20', 'Stück', 'zu je', '11,500 €', '229,990 €'

        driver.quit()

        assert 1 == Product.count()
    }

    @Test
    void testEditProductErrorsNoPricing() {
        clickListActionButton 0, 0, getUrl('/product/edit/')
        checkTitles 'Produkt bearbeiten', 'Produkte', 'Papier A4 80 g/m²'

        clearInput 'name'
        submitForm getUrl('/product/update')

        assert checkErrorFields(['name'])
        cancelForm getUrl('/product/list')

        driver.quit()

        assert 1 == Product.count()
    }

    @Test
    void testEditProductErrorsPricing() {
        clickListActionButton 0, 0, getUrl('/product/edit/')
        checkTitles 'Produkt bearbeiten', 'Produkte', 'Papier A4 80 g/m²'

        clearInput 'name'
        clearInput 'pricing.quantity'
        setInputValue 'pricing.unit.id', 'null'
        driver.findElement(By.id('step3-quantity')).clear()
        new Select(driver.findElement(By.id('step3-unit'))).selectByIndex(0)
        for (int i = 0; i < 4; i++) {
            removeRow 0
        }
        assert !getStep1TableRow(0).findElement(By.className('remove-btn')).displayed
        setStep1TableInputValue 0, 'unit', ''
        setStep1TableInputValue 0, 'name', ''
        submitForm getUrl('/product/update')

        assert checkErrorFields(['name', 'quantity'])
        WebElement fieldSet = driver.findElement(By.xpath('//fieldset[3]'))
        List<WebElement> errorMsgs = fieldSet.findElements(By.xpath('.//div[@id="step1-calculation-base"]//li[@class="error-msg"]'))
        assert 2 == errorMsgs.size()
        assert 'Muss größer als 0 sein.' == errorMsgs[0].text
        assert 'Feld darf nicht leer sein.' == errorMsgs[1].text
        errorMsgs = fieldSet.findElements(By.xpath(
            './div[1]/ul[@class="field-msgs"]/li[@class="error-msg"]'
        ))
        assert 2 == errorMsgs.size()
        assert 'Pos. 1, Bezeichnung: Feld darf nicht leer sein.' == errorMsgs[0].text
        assert 'Pos. 1, Einheit: Feld darf nicht leer sein.' == errorMsgs[1].text
        WebElement errorMsg = driver.findElement(By.xpath('//input[@id="step3-quantity"]/following-sibling::ul[@class="field-msgs"]/li[@class="error-msg"]'))
        assert 'Muss größer als 0 sein.' == errorMsg.text
        errorMsg = driver.findElement(By.xpath('//select[@id="step3-unit"]/following-sibling::ul[@class="field-msgs"]/li[@class="error-msg"]'))
        assert 'Feld darf nicht leer sein.' == errorMsg.text
        cancelForm getUrl('/product/list')

        driver.quit()

        assert 1 == Product.count()
    }

    @Test
    void testEditProductTransitionFromNoPricing() {
        clickListActionButton 0, 0, getUrl('/product/edit/')
        checkTitles 'Produkt bearbeiten', 'Produkte', 'Papier A4 80 g/m²'
        driver.findElement(By.id('start-pricing')).click()
        assert !getInput('quantity').displayed
        assert !getInput('unitPrice').displayed
        assert !getInput('unit.id').displayed
        setInputValue 'pricing.quantity', '10'
        setInputValue 'pricing.unit.id', '302'
        assert 1 == numStep1TableRows
        setStep1TableInputValue 0, 'quantity', '10'
        setStep1TableInputValue 0, 'unit', 'Packungen'
        setStep1TableInputValue 0, 'name', 'Materialeinkauf'
        setStep1TableInputValue 0, 'unitPrice', '2,190'
        assert '21,900' == getStep1TableRowTotal(0)
        assert '21,900' == step1Total
        assert '2,190' == step1UnitPrice
        assert '(10 Packung)' == driver.findElement(By.id('step1-total-price-quantity')).text
        assert '(1 Packung)' == driver.findElement(By.id('step1-unit-price-quantity')).text
        WebElement tr = getStep1TableRow(0)
        assert !tr.findElement(By.className('up-btn')).displayed
        assert !tr.findElement(By.className('down-btn')).displayed
        assert !tr.findElement(By.className('remove-btn')).displayed
        assert 2 == addNewStep1TableRow()
        setStep1TableInputValue 1, 'quantity', '0,25'
        setStep1TableInputValue 1, 'unit', 'Stunden'
        setStep1TableInputValue 1, 'name', 'Materialbeschaffung'
        setStep1TableInputValue 1, 'unitPrice', '40'
        assert '10,000' == getStep1TableRowTotal(1)
        assert '31,900' == step1Total
        assert '3,190' == step1UnitPrice
        tr = getStep1TableRow(0)
        assert !tr.findElement(By.className('up-btn')).displayed
        assert tr.findElement(By.className('down-btn')).displayed
        assert tr.findElement(By.className('remove-btn')).displayed
        tr = getStep1TableRow(1)
        assert tr.findElement(By.className('up-btn')).displayed
        assert !tr.findElement(By.className('down-btn')).displayed
        assert tr.findElement(By.className('remove-btn')).displayed
        assert 3 == addNewStep1TableRow()
        setStep1TableInputValue 2, 'type', 'sum'
        assert !getStep1TableInput(2, 'quantity').enabled
        assert !getStep1TableInput(2, 'unit').enabled
        assert !getStep1TableInput(2, 'name').enabled
        assert !getStep1TableInput(2, 'unitPrice').enabled
        assert '31,900' == getStep1TableRowTotal(2)
        assert '31,900' == step1Total
        assert '3,190' == step1UnitPrice
        tr = getStep1TableRow(0)
        assert !tr.findElement(By.className('up-btn')).displayed
        assert tr.findElement(By.className('down-btn')).displayed
        assert tr.findElement(By.className('remove-btn')).displayed
        tr = getStep1TableRow(1)
        assert tr.findElement(By.className('up-btn')).displayed
        assert tr.findElement(By.className('down-btn')).displayed
        assert tr.findElement(By.className('remove-btn')).displayed
        tr = getStep1TableRow(2)
        assert tr.findElement(By.className('up-btn')).displayed
        assert !tr.findElement(By.className('down-btn')).displayed
        assert tr.findElement(By.className('remove-btn')).displayed
        assert 4 == addNewStep1TableRow()
        setStep1TableInputValue 3, 'type', 'relativeToCurrentSum'
        setStep1TableInputValue 3, 'quantity', '1'
        setStep1TableInputValue 3, 'unit', 'Einheit'
        setStep1TableInputValue 3, 'name', 'Gewinn'
        setStep1TableInputValue 3, 'unitPercent', '5'
        assert '1,600' == getStep1TableRowTotal(3)
        assert '33,500' == step1Total
        assert '3,350' == step1UnitPrice
        assert 5 == addNewStep1TableRow()
        setStep1TableInputValue 4, 'type', 'relativeToLastSum'
        setStep1TableInputValue 4, 'quantity', '1'
        setStep1TableInputValue 4, 'unit', 'Einheit'
        setStep1TableInputValue 4, 'name', 'Risiko'
        setStep1TableInputValue 4, 'unitPercent', '5'
        assert '1,600' == getStep1TableRowTotal(4)
        assert '35,100' == step1Total
        assert '3,510' == step1UnitPrice
        assert '10' == getValue('step2-quantity')
        assert 'Packung' == getValue('step2-unit')
        assert '3,510' == getValue('step2-unit-price')
        assert '35,100' == getValue('step2-total-price')
        assert '10' == getValue('step2-total-quantity')
        assert 'Packung' == getValue('step2-total-unit')
        assert '3,510' == getValue('step2-total-unit-price')
        assert '35,100' == getValue('step2-total')
        setInputValue 'pricing.discountPercent', '1'
        assert '0,351' == getValue('step2-discount-percent-amount')
        setInputValue 'pricing.adjustment', '0,150'
        assert '10' == getValue('step2-total-quantity')
        assert 'Packung' == getValue('step2-total-unit')
        assert '3,490' == getValue('step2-total-unit-price')
        assert '34,899' == getValue('step2-total')
        assert '3,490' == getValue('step3-unit-price')
        assert '3,490' == getValue('step3-total-price')
        WebElement input = driver.findElement(By.id('step3-quantity'))
        input.clear()
        input.sendKeys '10'
        assert '3,490' == getValue('step3-unit-price')
        assert '34,899' == getValue('step3-total-price')
        input.clear()
        input.sendKeys '1'
        def select = new Select(driver.findElement(By.id('step3-unit')))
        select.selectByValue '301'
        assert '34,899' == getValue('step3-unit-price')
        assert '34,899' == getValue('step3-total-price')
        input.clear()
        input.sendKeys '2'
        assert '17,450' == getValue('step3-unit-price')
        assert '34,899' == getValue('step3-total-price')
        input.clear()
        input.sendKeys '10'
        select.selectByValue '302'
        assert '3,490' == getValue('step3-unit-price')
        assert '34,899' == getValue('step3-total-price')
        submitForm getUrl('/product/show/')

        assert 'Produkt Papier A4 80 g/m² wurde geändert.' == flashMessage
        assert 'Papier A4 80 g/m²' == driver.findElement(BY_SUBHEADER).text
        def dataSheet = driver.findElement(By.className('data-sheet'))
        def fieldSet = getFieldset(dataSheet, 1)
        def col = fieldSet.findElement(By.className('col-l'))
        assert 'P-10000' == getShowFieldText(col, 1)
        assert 'Papier A4 80 g/m²' == getShowFieldText(col, 2)
        assert 'Materialien' == getShowFieldText(col, 3)
        assert 'Papierwerk Brandenburg' == getShowFieldText(col, 4)
        assert 'Druckereibedarf Kiel GmbH' == getShowFieldText(col, 5)
        assert '10' == getShowFieldText(col, 6)
        assert 'Packung' == getShowFieldText(col, 7)
        assert '3,489 €' == getShowFieldText(col, 8)
        col = fieldSet.findElement(By.className('col-r'))
        assert '200' == getShowFieldText(col, 1)
        assert '7 %' == getShowFieldText(col, 2)
        fieldSet = getFieldset(dataSheet, 2)
        assert 'Packung zu 100 Blatt. Chlorfrei gebleicht.' == getShowFieldText(fieldSet, 1)
        fieldSet = getFieldset(dataSheet, 3)
        assert 'In der folgenden Tabelle berechne ich den Preis für 10 Packung:' == fieldSet.findElement(By.xpath('./p')).text
        WebElement tbody = fieldSet.findElement(By.tagName('tbody'))
        checkStaticRowValues tbody, 0, '1.', '10', 'Packungen', 'Materialeinkauf', 'absoluter Betrag', '', '0,00', '2,190 €', '21,900 €'
        checkStaticRowValues tbody, 1, '2.', '0,25', 'Stunden', 'Materialbeschaffung', 'absoluter Betrag', '', '0,00', '40,000 €', '10,000 €'
        checkStaticRowValues tbody, 2, '3.', '', 'Zwischensumme', '', '31,900 €'
        checkStaticRowValues tbody, 3, '4.', '1', 'Einheit', 'Gewinn', 'relativ zur akt. Summe', '', '5,00', '1,595 €', '1,595 €'
        checkStaticRowValues tbody, 4, '5.', '1', 'Einheit', 'Risiko', 'relativ zur letzt. Zw.-summe', '', '5,00', '1,595 €', '1,595 €'
        WebElement tfoot = fieldSet.findElement(By.tagName('tfoot'))
        checkStaticRowValues tfoot, 0, 'Gesamtpreis (10 Packung)', '', '35,090 €'
        checkStaticRowValues tfoot, 1, 'Kalkulierter Einzelpreis (1 Packung)', '', '3,509 €'
        fieldSet = getFieldset(dataSheet, 4)
        tbody = fieldSet.findElement(By.tagName('tbody'))
        checkStaticRowValues tbody, 0, 'Kalkulierter Gesamtwert', '10', 'Packung', '', 'zu je', '3,509 €', '35,090 €'
        checkStaticRowValues tbody, 1, 'Rabatt %', '', '', '1,00', '', '', '0,351 €'
        checkStaticRowValues tbody, 2, 'Preisanpassung +/-', '', '', '', '', '', '0,150 €'
        tfoot = fieldSet.findElement(By.tagName('tfoot'))
        checkStaticRowValues tfoot, 0, 'Verkaufspreis', '10', 'Packung', '', 'zu je', '3,489 €', '34,889 €'
        fieldSet = getFieldset(dataSheet, 5)
        tbody = fieldSet.findElement(By.tagName('tbody'))
        checkStaticRowValues tbody, 0, 'Der Artikel wird verkauft als', '10', 'Packung', 'zu je', '3,489 €', '34,889 €'
        driver.quit()

        assert 1 == Product.count()
    }

    @Test
    void testEditProductTransitionFromPricing() {
        clickListActionButton 0, 0, getUrl('/product/edit/')
        checkTitles 'Produkt bearbeiten', 'Produkte', 'Papier A4 80 g/m²'
        driver.findElement(By.id('remove-pricing')).click()
        driver.switchTo().alert().dismiss()
        assert !driver.findElement(By.id('start-pricing')).displayed
        assert !getInput('quantity').displayed
        assert !getInput('unitPrice').displayed
        assert !getInput('unit.id').displayed
        assert step1Table.displayed

        driver.findElement(By.id('remove-pricing')).click()
        driver.switchTo().alert().accept()
        assert driver.findElement(By.id('start-pricing')).displayed
        WebElement input = getInput('quantity')
        assert input.displayed
        assert '10' == input.getAttribute('value')
        input = getInput('unit.id')
        assert input.displayed
        assert '302' == new Select(input).firstSelectedOption.getAttribute('value')
        input = getInput('unitPrice')
        assert input.displayed
        assert '3,489' == input.getAttribute('value')
        submitForm getUrl('/product/show/')

        assert 'Produkt Papier A4 80 g/m² wurde geändert.' == flashMessage
        assert 'Papier A4 80 g/m²' == driver.findElement(BY_SUBHEADER).text
        def dataSheet = driver.findElement(By.className('data-sheet'))
        def fieldSet = getFieldset(dataSheet, 1)
        def col = fieldSet.findElement(By.className('col-l'))
        assert 'P-10000' == getShowFieldText(col, 1)
        assert 'Papier A4 80 g/m²' == getShowFieldText(col, 2)
        assert 'Materialien' == getShowFieldText(col, 3)
        assert 'Papierwerk Brandenburg' == getShowFieldText(col, 4)
        assert 'Druckereibedarf Kiel GmbH' == getShowFieldText(col, 5)
        assert '10' == getShowFieldText(col, 6)
        assert 'Packung' == getShowFieldText(col, 7)
        assert '3,489 €' == getShowFieldText(col, 8)
        col = fieldSet.findElement(By.className('col-r'))
        assert '200' == getShowFieldText(col, 1)
        assert '7 %' == getShowFieldText(col, 2)
        assert '2,190 €' == getShowFieldText(col, 3)
        fieldSet = getFieldset(dataSheet, 2)
        assert 'Packung zu 100 Blatt. Chlorfrei gebleicht.' == getShowFieldText(fieldSet, 1)
        driver.quit()

        assert 1 == Product.count()
    }

    @Test
    void testEditProductNoActionTransitionFromPricing() {
        clickListActionButton 0, 0, getUrl('/product/edit/')
        checkTitles 'Produkt bearbeiten', 'Produkte', 'Papier A4 80 g/m²'
        driver.findElement(By.id('remove-pricing')).click()
        driver.switchTo().alert().accept()
        assert driver.findElement(By.id('start-pricing')).displayed
        WebElement input = getInput('quantity')
        assert input.displayed
        assert '10' == input.getAttribute('value')
        input = getInput('unit.id')
        assert input.displayed
        assert '302' == new Select(input).firstSelectedOption.getAttribute('value')
        input = getInput('unitPrice')
        assert input.displayed
        assert '3,489' == input.getAttribute('value')
        cancelForm getUrl('/product/list')

        def tbody = driver.findElement(By.xpath('//table[@class="content-table"]/tbody'))
        assert 1 == tbody.findElements(By.tagName('tr')).size()
        def tr = tbody.findElement(By.xpath('tr[1]'))
        assert '3,489 €' == tr.findElement(By.xpath('td[7]')).text
        tr.findElement(By.xpath('td[8]/a[1]')).click()
        assert 5 == numStep1TableRows
        assert driver.findElement(By.id('remove-pricing')).displayed
        driver.quit()

        assert 1 == Product.count()
        assert 1 == SalesItemPricing.count()
        assert 5 == SalesItemPricingItem.count()
    }

    @Test
    void testDeleteProductActionNoPricing() {
        driver.findElement(By.xpath('//table[@class="content-table"]/tbody/tr/td[@class="action-buttons"]/a[2]')).click()
        driver.switchTo().alert().accept()
        assert driver.currentUrl.startsWith(getUrl('/product/list'))
        assert 'Produkt wurde gelöscht.' == flashMessage
        def emptyList = driver.findElement(By.className('empty-list'))
        assert 'Diese Liste enthält keine Einträge.' == emptyList.findElement(By.tagName('p')).text
        driver.quit()

        assert 0 == Product.count()
        assert 0 == SalesItemPricing.count()
        assert 0 == SalesItemPricingItem.count()
    }

    @Test
    void testDeleteProductActionPricing() {
        driver.findElement(By.xpath('//table[@class="content-table"]/tbody/tr/td[@class="action-buttons"]/a[2]')).click()
        driver.switchTo().alert().accept()
        assert driver.currentUrl.startsWith(getUrl('/product/list'))
        assert 'Produkt wurde gelöscht.' == flashMessage
        def emptyList = driver.findElement(By.className('empty-list'))
        assert 'Diese Liste enthält keine Einträge.' == emptyList.findElement(By.tagName('p')).text
        driver.quit()

        assert 0 == Product.count()
        assert 0 == SalesItemPricing.count()
        assert 0 == SalesItemPricingItem.count()
    }

    @Test
    void testDeleteProductNoActionNoPricing() {
        driver.findElement(By.xpath('//table[@class="content-table"]/tbody/tr/td[@class="action-buttons"]/a[2]')).click()
        driver.switchTo().alert().dismiss()
        assert getUrl('/product/list') == driver.currentUrl
        driver.quit()

        assert 1 == Product.count()
        assert 0 == SalesItemPricing.count()
        assert 0 == SalesItemPricingItem.count()
    }

    @Test
    void testDeleteProductNoActionPricing() {
        driver.findElement(By.xpath('//table[@class="content-table"]/tbody/tr/td[@class="action-buttons"]/a[2]')).click()
        driver.switchTo().alert().dismiss()
        assert getUrl('/product/list') == driver.currentUrl
        driver.quit()

        assert 1 == Product.count()
        assert 1 == SalesItemPricing.count()
        assert 5 == SalesItemPricingItem.count()
    }


    //-- Non-public methods ---------------------

    protected Product prepareProductWithoutPricing() {
        def product = new Product(
            name: 'Papier A4 80 g/m²',
            quantity: 1.0d,
            unit: Unit.get(302),
            unitPrice: 2.49d,
            taxRate: TaxRate.get(401),
            purchasePrice: 2.19d,
            description: 'Packung zu 100 Blatt. Chlorfrei gebleicht.',
            category: ProductCategory.get(3000),
            manufacturer: 'Papierwerk Brandenburg',
            retailer: 'Druckereibedarf Kiel GmbH',
            weight: 200.0d
        )
        product.save flush: true
        product
    }

    protected Product prepareProductWithPricing() {
        def pricing = new SalesItemPricing(
            quantity: 10.0d,
            unit: Unit.get(302),
            discountPercent: 1.0d,
            adjustment: 0.15d
        )
        pricing.addToItems(new SalesItemPricingItem(
                quantity: 10.0d,
                unit: 'Packungen',
                name: 'Materialeinkauf',
                type: PricingItemType.absolute,
                unitPrice: 2.19d
            )).addToItems(new SalesItemPricingItem(
                quantity: 0.25d,
                unit: 'Stunden',
                name: 'Materialbeschaffung',
                type: PricingItemType.absolute,
                unitPrice: 40.0d
            )).addToItems(new SalesItemPricingItem(
                type: PricingItemType.sum,
            )).addToItems(new SalesItemPricingItem(
                quantity: 1.0d,
                unit: 'Einheit',
                name: 'Gewinn',
                type: PricingItemType.relativeToCurrentSum,
                unitPercent: 5.0d
            )).addToItems(new SalesItemPricingItem(
                quantity: 1.0d,
                unit: 'Einheit',
                name: 'Risiko',
                type: PricingItemType.relativeToLastSum,
                unitPercent: 5.0d
            ))
        pricing.save()
        def product = new Product(
            name: 'Papier A4 80 g/m²',
            quantity: 10.0d,
            unit: Unit.get(302),
            taxRate: TaxRate.get(401),
            purchasePrice: 2.19d,
            description: 'Packung zu 100 Blatt. Chlorfrei gebleicht.',
            pricing: pricing,
            category: ProductCategory.get(3000),
            manufacturer: 'Papierwerk Brandenburg',
            retailer: 'Druckereibedarf Kiel GmbH',
            weight: 200.0d
        )
        product.save flush: true
        product
    }
}
