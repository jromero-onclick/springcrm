/*
 * InstallationTest.groovy
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

import org.codehaus.groovy.grails.web.context.ServletContextHolder as SCH
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes as GA
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.Select


/**
 * The class {@code InstallationTest} represents ...
 *
 * @author	Daniel Ellermann
 * @version 1.3
 * @since   1.3
 */
class InstallationTest extends GeneralTestCase {

    //-- Constants ------------------------------

    protected static final String TITLE = 'Installation'
    protected static final List<String> URL_ACTIONS = [
        'index', 'install-base-data', 'client-data', 'create-admin', 'finish'
    ].asImmutable()


    //-- Instance variables ---------------------

    @Rule
    public TestName name = new TestName()

    InstallService installService


    //-- Public methods -------------------------

    @Before
    @Override
    void setUp() {
        super.setUp()
        def ctx = SCH.servletContext.getAttribute(GA.APPLICATION_CONTEXT)
        installService = ctx.getBean('installService')
        installService.disableInstaller()
    }

    @Test
    void testNavigationBar() {
        open('', 'de')
        assert getUrl(0) == driver.currentUrl
        for (int i = 0; i < URL_ACTIONS.size(); i++) {
            for (int j = 0; j < URL_ACTIONS.size(); j++) {
                driver.findElement(By.xpath("//ol[@id='install-progress']/li[${i + 1}]/a")).click()
                assert getUrl(i) == driver.currentUrl
                assert TITLE == driver.title
                driver.findElement(By.xpath("//ol[@id='install-progress']/li[${j + 1}]/a")).click()
                assert getUrl(j) == driver.currentUrl
                assert TITLE == driver.title
            }
        }
        driver.quit()

        assert 0 == SelValue.count()
        assert 0 == SeqNumber.count()
        assert 0 == Config.count()
    }

    @Test
    void testNavigationBack() {
        def link1Sel = By.xpath('//ul[@id="toolbar"]/li[1]/a')
        def link2Sel = By.xpath('//ul[@id="toolbar"]/li[2]/a')

        int n = URL_ACTIONS.size() - 1
        driver.get(getUrl(n) + '?lang=de')
        for (int i = n; i > 0; i--) {
            def link1 = driver.findElement(link1Sel)
            assert 'Zurück' == link1.text
            String label = driver.findElement(link2Sel).text
            if (i == n) {
                assert 'Fertigstellen' == label
            } else {
                assert 'Weiter' == label
            }
            link1.click()
            assert getUrl(i - 1) == driver.currentUrl
            assert TITLE == driver.title
        }
        driver.quit()

        assert 0 == SelValue.count()
        assert 0 == SeqNumber.count()
        assert 0 == Config.count()
    }

    @Test
    void testWelcomePage() {
        def headerSel = By.cssSelector('#main-container-header > h2')
        def stepSel = By.cssSelector('#install-progress > .current > a')
        def link1Sel = By.xpath('//ul[@id="toolbar"]/li[1]/a')
        def link2Sel = By.xpath('//ul[@id="toolbar"]/li[2]/a')

        /* page "welcome" */
        int page = 0
        open('', 'de')
        assert getUrl(page) == driver.currentUrl
        assert TITLE == driver.title
        def h2 = driver.findElement(headerSel)
        assert 'Willkommen zur Installation von SpringCRM' == h2.text
        def step = driver.findElement(stepSel)
        assert 'Willkommen' == step.text
        driver.findElement(link1Sel).click()

        /* page "install base data" */
        page++
        assert getUrl(page) == driver.currentUrl
        assert TITLE == driver.title
        h2 = driver.findElement(headerSel)
        assert 'Basisdaten installieren' == h2.text
        step = driver.findElement(stepSel)
        assert 'Basisdaten installieren' == step.text
        assert 'Achtung! Dieser Vorgang überschreibt ggf. bestehende Daten. Dies betrifft die Daten von Auswahllisten und Systemeinstellungen.' == driver.findElement(By.className('warning')).text
        Select select = new Select(driver.findElement(By.name('package')))
        assert select.options.size() >= 3
        select.selectByValue('de-DE')
        driver.findElement(link2Sel).click()

        /* page "client data" */
        page++
        assert getUrl(page) == driver.currentUrl
        assert TITLE == driver.title
        h2 = driver.findElement(headerSel)
        assert 'Mandantendaten' == h2.text
        step = driver.findElement(stepSel)
        assert 'Mandantendaten' == step.text
        driver.findElement(link2Sel).click()
        assert checkErrorFields(['name', 'street', 'postalCode', 'location', 'phone', 'email'])
        driver.findElement(By.name('name')).sendKeys('Agentur Kampe')
        driver.findElement(By.name('street')).sendKeys('Hauptstraße 148')
        driver.findElement(By.name('postalCode')).sendKeys('23898')
        driver.findElement(By.name('location')).sendKeys('Labenz')
        driver.findElement(By.name('phone')).sendKeys('04536 45301-0')
        driver.findElement(By.name('fax')).sendKeys('04536 45301-90')
        driver.findElement(By.name('email')).sendKeys('info@kampe.example')
        driver.findElement(By.name('website')).sendKeys('http://www.kampe.example')
        driver.findElement(By.name('bankName')).sendKeys('Elbebank Hamburg')
        driver.findElement(By.name('bankCode')).sendKeys('120340560')
        driver.findElement(By.name('accountNumber')).sendKeys('45671234')
        driver.findElement(link2Sel).click()

        /* page "create admin" */
        page++
        assert getUrl(page) == driver.currentUrl
        assert TITLE == driver.title
        h2 = driver.findElement(headerSel)
        assert 'Administratorkonto anlegen' == h2.text
        step = driver.findElement(stepSel)
        assert 'Administratorkonto anlegen' == step.text
        driver.findElement(link2Sel).click()
        assert checkErrorFields(['password'])
        driver.findElement(By.name('password')).sendKeys('abc1234')
        driver.findElement(link2Sel).click()
        assert checkErrorFields(['password'])
        driver.findElement(By.name('password')).sendKeys('abc1234')
        driver.findElement(By.name('passwordRepeat')).sendKeys('abc1234')
        driver.findElement(link2Sel).click()
        assert checkErrorFields(['userName', 'firstName', 'lastName', 'email'])
        driver.findElement(By.name('userName')).sendKeys('mkampe')
        driver.findElement(By.name('password')).sendKeys('abc1234')
        driver.findElement(By.name('passwordRepeat')).sendKeys('abc1234')
        driver.findElement(By.name('firstName')).sendKeys('Marcus')
        driver.findElement(By.name('lastName')).sendKeys('Kampe')
        driver.findElement(By.name('phone')).sendKeys('04536 45301-10')
        driver.findElement(By.name('phoneHome')).sendKeys('04536 65530')
        driver.findElement(By.name('mobile')).sendKeys('0172 12034056')
        driver.findElement(By.name('fax')).sendKeys('04536 45301-90')
        driver.findElement(By.name('email')).sendKeys('m.kampe@kampe.example')
        driver.findElement(link2Sel).click()

        /* page "finish" */
        page++
        assert getUrl(page) == driver.currentUrl
        assert TITLE == driver.title
        h2 = driver.findElement(headerSel)
        assert 'Installation abgeschlossen' == h2.text
        step = driver.findElement(stepSel)
        assert 'Fertigstellen' == step.text
        driver.findElement(link2Sel).click()
        assert getUrl('/user/login') == driver.currentUrl

        driver.quit()

        assert installService.installerDisabled
        assert 1 < Salutation.count()
        assert 2 == TaxRate.count()
        def taxRate = TaxRate.get(400)
        assert '19 %' == taxRate.name
        assert 0.19d == taxRate.taxValue
        taxRate = TaxRate.get(401)
        assert '7 %' == taxRate.name
        assert 0.07d == taxRate.taxValue
        assert 11 == SeqNumber.count()
        assert 0 < Config.count()
        assert 0 < OrgType.count()
        assert 0 < Rating.count()
        assert 0 < Unit.count()
        assert 0 < Carrier.count()
        assert 0 < QuoteStage.count()
        assert 0 < TermsAndConditions.count()
        assert 0 < SalesOrderStage.count()
        assert 0 < InvoiceStage.count()
        assert 0 < Industry.count()
        assert 0 < ServiceCategory.count()
        assert 0 < PurchaseInvoiceStage.count()
        assert 0 < DunningStage.count()
        assert 0 < DunningLevel.count()
        assert 0 < PaymentMethod.count()
        assert 0 < CreditMemoStage.count()
        assert 0 < ProductCategory.count()
    }

    @Test
    void testExistingInstallationDisabled() {
        open('', 'de')
        assert getUrl('/user/login') == driver.currentUrl

        String urlExpected = getUrl('/user/login')
        for (int i = 0; i < URL_ACTIONS.size(); i++) {
            driver.get(getUrl(i))
            assert urlExpected == driver.currentUrl
        }
        driver.quit()
    }

    @Test
    void testExistingInstallationEnabled() {
        installService.enableInstaller()
        open('', 'de')
        assert getUrl('/user/login') == driver.currentUrl
        open('/install')
        assert getUrl('/install') == driver.currentUrl
        assert TITLE == driver.title
        assert 'Willkommen zur Installation von SpringCRM' == driver.findElement(By.cssSelector('#main-container-header > h2')).text
        assert 'Willkommen' == driver.findElement(By.cssSelector('#install-progress > .current > a')).text

        for (int i = 0; i < URL_ACTIONS.size(); i++) {
            String url = getUrl(i)
            driver.get(url)
            assert url == driver.currentUrl
        }
        driver.quit()
    }


    //-- Non-public methods ---------------------

    @Override
    protected Object getDatasets() {
        def res = []
        if (name.methodName.startsWith('testExisting')) {
            res << 'test-data/install-data.xml'
        } else {
            res << 'test-data/empty-install-data.xml'
        }
        return res
    }

    protected String getUrl(int i) {
        return getUrl('/install/' + URL_ACTIONS[i])
    }
}
