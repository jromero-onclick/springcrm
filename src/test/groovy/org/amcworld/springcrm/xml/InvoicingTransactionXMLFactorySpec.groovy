/*
 * InvoicingTransactionXMLFactorySpec.groovy
 *
 * Copyright (c) 2011-2018, Daniel Ellermann
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


package org.amcworld.springcrm.xml

import com.naleid.grails.MarkdownService
import org.amcworld.springcrm.Invoice
import org.amcworld.springcrm.User
import spock.lang.Specification


//@TestMixin([GrailsUnitTestMixin, DomainClassUnitTestMixin])
//@Mock([Invoice, User])
class InvoicingTransactionXMLFactorySpec extends Specification {

    //-- Feature methods ------------------------

    def 'Create a new converter'() {
        given: 'a converter factory with a MarkdownService instance'
        def markdownService = new MarkdownService()
        def factory = new InvoicingTransactionXMLFactory(
            markdownService: markdownService
        )

        and: 'an invoice and a user'
        def invoice = new Invoice(subject: 'My invoice')
        def user = new User(username: 'jsmith')

        when: 'I create new converter'
        InvoicingTransactionXML conv = factory.newConverter(invoice, user)

        then: 'I get a valid converter'
        null != conv
        markdownService == conv.markdownService
        invoice == conv.data.transaction
        user == conv.data.user
    }
}
