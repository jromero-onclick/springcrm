/*
 * QuoteController.groovy
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


package org.amcworld.springcrm

import static org.springframework.http.HttpStatus.*

import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import grails.web.RequestParameter
import org.bson.types.ObjectId


/**
 * The class {@code QuoteController} contains actions which manage quotes.
 *
 * @author  Daniel Ellermann
 * @version 3.0
 */
@Secured(['ROLE_ADMIN', 'ROLE_QUOTE'])
class QuoteController {

    //-- Fields ---------------------------------

    InvoicingTransactionService invoicingTransactionService
    OrganizationService organizationService
    PersonService personService
    QuoteService quoteService
    SeqNumberService seqNumberService


    //-- Public methods -------------------------

    def copy(Quote quote) {
        respond new Quote(quote), view: 'create'
    }

    def create() {
        respond new Quote(params)
    }

    def delete(String id) {
        if (id == null) {
            notFound()
            return
        }

        Quote quote = quoteService.delete(new ObjectId(id))

        request.withFormat {
            //noinspection GroovyAssignabilityCheck
            form multipartForm {
                flash.message = message(
                    code: 'default.deleted.message',
                    args: [message(code: 'quote.label'), quote]
                ) as Object
                redirect action: 'index', method: 'GET'
            }
            '*' { render status: NO_CONTENT }
        }
    }

    def edit(String id) {
        respond id == null ? null : quoteService.get(new ObjectId(id))
    }

    def find() {
        String name = params.name
        Integer number
        try {
            number = name as Integer
        } catch (NumberFormatException ignored) {
            number = null
        }

        String organizationId = params.organization
        Organization organization = organizationId == null ? null
            : organizationService.get(new ObjectId(organizationId))

        //noinspection GroovyVariableNotAssigned
        respond quoteService.find(number, name, organization)
    }

    def index() {
        List<Quote> list
        int count
        if (params.search) {
            String searchFilter = "%${params.search}%".toString()
            list = quoteService.findAllBySubjectLike(searchFilter, params)
            count = quoteService.countBySubjectLike(searchFilter)
        } else {
            list = quoteService.list(params)
            count = quoteService.count()
        }

        respond list, model: [quoteCount: count]
    }

    def listEmbedded(@RequestParameter('organization') String organizationId,
                     @RequestParameter('person') String personId)
    {
        List<Quote> list = null
        Map model = null

        if (organizationId != null) {
            Organization organization =
                organizationService.get(new ObjectId(organizationId))
            if (organization != null) {
                list = quoteService.findAllByOrganization(organization, params)
                model = [
                    quoteCount: quoteService.countByOrganization(organization),
                    linkParams: [organization: organization.id.toString()]
                ]
            }
        } else if (personId != null) {
            Person person = personService.get(new ObjectId(personId))
            if (person != null) {
                list = quoteService.findAllByPerson(person, params)
                model = [
                    phoneCallCount: quoteService.countByPerson(person),
                    linkParams: [person: person.id.toString()]
                ]
            }
        }

        respond list, model: model
    }

    def print(String id, String template) {
        Quote quote = id == null ? null : quoteService.get(new ObjectId(id))
        if (quote == null) {
            notFound()
            return
        }

        boolean duplicate = !params.duplicate?.isBlank()
        byte [] pdf =
            invoicingTransactionService.print(quote, template, duplicate)

        StringBuilder buf = new StringBuilder()
        buf << (message(code: 'quote.label') as String)
        buf << ' ' << seqNumberService.getFullNumber(quote)
        if (duplicate) {
            buf << ' '
            buf << (message(code: 'invoicingTransaction.duplicate') as String)
        }
        buf << '.pdf'

        response.contentLength = pdf.length
        render(
            contentType: 'application/pdf',
            file: pdf,
            fileName: buf.toString()
        )
    }

    def save(Quote quote) {
        if (quote == null) {
            notFound()
            return
        }

        try {
            quote.beforeInsert()
            quoteService.save quote
        } catch (ValidationException ignored) {
            respond quote.errors, view: 'create'
            return
        }

        request.withFormat {
            //noinspection GroovyAssignabilityCheck
            form multipartForm {
                flash.message = message(
                    code: 'default.created.message',
                    args: [message(code: 'quote.label'), quote]
                ) as Object
                redirect quote
            }
            '*' { respond quote, [status: CREATED] }
        }
    }

    def show(String id) {
        respond id == null ? null : quoteService.get(new ObjectId(id))
    }

    def update(Quote quote) {
        if (quote == null) {
            notFound()
            return
        }

        try {
            quote.beforeUpdate()
            quoteService.save quote
        } catch (ValidationException ignored) {
            respond quote.errors, view: 'edit'
            return
        }

        request.withFormat {
            //noinspection GroovyAssignabilityCheck
            form multipartForm {
                flash.message = message(
                    code: 'default.updated.message',
                    args: [message(code: 'quote.label'), quote]
                ) as Object
                redirect quote
            }
            '*' { respond quote, [status: OK] }
        }
    }


    //-- Non-public methods ---------------------

    private void notFound() {
        request.withFormat {
            //noinspection GroovyAssignabilityCheck
            form multipartForm {
                flash.message = message(
                    code: 'default.not.found.message',
                    args: [message(code: 'quote.label'), params.id]
                ) as Object
                redirect action: 'index', method: 'GET'
            }
            '*' { render status: NOT_FOUND }
        }
    }
}
