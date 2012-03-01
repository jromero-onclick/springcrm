/*
 * QuoteController.groovy
 *
 * Copyright (c) 2012, AMC World Technologies GmbH
 * Fischerinsel 1, D-10179 Berlin, Deutschland
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of AMC World
 * Technologies GmbH ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with AMC World Technologies GmbH.
 */


package org.amcworld.springcrm

import grails.converters.XML
import org.springframework.dao.DataIntegrityViolationException


/**
 * The class {@code QuoteController} contains actions which manage quotes.
 *
 * @author	Daniel Ellermann
 * @version 0.9
 */
class QuoteController {

    //-- Class variables ------------------------

    static allowedMethods = [save: 'POST', update: 'POST', delete: 'GET']


    //-- Instance variables ---------------------

	def fopService
	def seqNumberService


    //-- Public methods -------------------------

    def index() {
        redirect(action: 'list', params: params)
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        return [quoteInstanceList: Quote.list(params), quoteInstanceTotal: Quote.count()]
    }

	def listEmbedded() {
		def l
		def count
		def linkParams
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
		if (params.organization) {
			def organizationInstance = Organization.get(params.organization)
			l = Quote.findAllByOrganization(organizationInstance, params)
			count = Quote.countByOrganization(organizationInstance)
			linkParams = [organization: organizationInstance.id]
		} else if (params.person) {
			def personInstance = Person.get(params.person)
			l = Quote.findAllByPerson(personInstance, params)
			count = Quote.countByPerson(personInstance)
			linkParams = [person: personInstance.id]
		}
		return [quoteInstanceList: l, quoteInstanceTotal: count, linkParams: linkParams]
	}

    def create() {
        def quoteInstance = new Quote()
        quoteInstance.properties = params
		Organization org = quoteInstance.organization
		if (org) {
			quoteInstance.billingAddrCountry = org.billingAddrCountry
			quoteInstance.billingAddrLocation = org.billingAddrLocation
			quoteInstance.billingAddrPoBox = org.billingAddrPoBox
			quoteInstance.billingAddrPostalCode = org.billingAddrPostalCode
			quoteInstance.billingAddrState = org.billingAddrState
			quoteInstance.billingAddrStreet = org.billingAddrStreet
			quoteInstance.shippingAddrCountry = org.shippingAddrCountry
			quoteInstance.shippingAddrLocation = org.shippingAddrLocation
			quoteInstance.shippingAddrPoBox = org.shippingAddrPoBox
			quoteInstance.shippingAddrPostalCode = org.shippingAddrPostalCode
			quoteInstance.shippingAddrState = org.shippingAddrState
			quoteInstance.shippingAddrStreet = org.shippingAddrStreet
		}
        return [quoteInstance: quoteInstance]
    }

	def copy() {
		def quoteInstance = Quote.get(params.id)
		if (!quoteInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'quote.label', default: 'Quote'), params.id])
            redirect(action: 'list')
            return
        }

		quoteInstance = new Quote(quoteInstance)
		render(view: 'create', model: [quoteInstance: quoteInstance])
	}

    def save() {
        def quoteInstance = new Quote(params)
        if (!quoteInstance.save(flush: true)) {
            log.debug(quoteInstance.errors)
            render(view: 'create', model: [quoteInstance: quoteInstance])
            return
        }
        params.id = quoteInstance.ident()

		quoteInstance.index()
        flash.message = message(code: 'default.created.message', args: [message(code: 'quote.label', default: 'Quote'), quoteInstance.toString()])
		if (params.returnUrl) {
			redirect(url: params.returnUrl)
		} else {
			redirect(action: 'show', id: quoteInstance.id)
		}
    }

    def show() {
        def quoteInstance = Quote.get(params.id)
        if (!quoteInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'quote.label', default: 'Quote'), params.id])
            redirect(action: 'list')
            return
        }

        return [quoteInstance: quoteInstance, printTemplates: fopService.templateNames]
    }

    def edit() {
        def quoteInstance = Quote.get(params.id)
        if (!quoteInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'quote.label', default: 'Quote'), params.id])
            redirect(action: 'list')
            return
        }

        return [quoteInstance: quoteInstance]
    }

    def update() {
        def quoteInstance = Quote.get(params.id)
        if (!quoteInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'quote.label', default: 'Quote'), params.id])
            redirect(action: 'list')
            return
        }

        if (params.version) {
            def version = params.version.toLong()
            if (quoteInstance.version > version) {
                quoteInstance.errors.rejectValue('version', 'default.optimistic.locking.failure', [message(code: 'quote.label', default: 'Quote')] as Object[], 'Another user has updated this Quote while you were editing')
                render(view: 'edit', model: [quoteInstance: quoteInstance])
                return
            }
        }
		if (params.autoNumber) {
			params.number = quoteInstance.number
		}

        /*
         * The original implementation which worked in Grails 2.0.0.
         */
        quoteInstance.properties = params
//        quoteInstance.items?.retainAll { it != null }

        /*
         * XXX  This code is necessary because the default implementation
         *      in Grails does not work.  The above lines worked in Grails
         *      2.0.0.  Now, either data binding or saving does not work
         *      correctly if items were deleted and gaps in the indices
         *      occurred (e. g. 0, 1, null, null, 4) or the items were
         *      re-ordered.  Then I observed cluttering in saved data
         *      columns.
         *      The following lines do not make me happy but they work.
         *      In future, this problem hopefully will be fixed in Grails
         *      so we can remove these lines.
         */
        quoteInstance.items?.clear()
        for (int i = 0; params."items[${i}]"; i++) {
            if (params."items[${i}]".id != 'null') {
                quoteInstance.addToItems(params."items[${i}]")
            }
        }

        if (!quoteInstance.save(flush: true)) {
            log.debug(quoteInstance.errors)
            render(view: 'edit', model: [quoteInstance: quoteInstance])
            return
        }

		quoteInstance.reindex()
        flash.message = message(code: 'default.updated.message', args: [message(code: 'quote.label', default: 'Quote'), quoteInstance.toString()])
		if (params.returnUrl) {
			redirect(url: params.returnUrl)
		} else {
			redirect(action: 'show', id: quoteInstance.id)
		}
    }

    def delete() {
        def quoteInstance = Quote.get(params.id)
        if (quoteInstance && params.confirmed) {
            try {
                quoteInstance.delete(flush: true)
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'quote.label', default: 'Quote')])
				if (params.returnUrl) {
					redirect(url: params.returnUrl)
				} else {
					redirect(action: 'list')
				}
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'quote.label', default: 'Quote')])
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'quote.label', default: 'Quote'), params.id])
			if (params.returnUrl) {
				redirect(url: params.returnUrl)
			} else {
				redirect(action: 'list')
			}
        }
    }

	def find() {
		Integer number = null
		try {
			number = params.name as Integer
		} catch (NumberFormatException) { /* ignored */ }
		def organization = params.organization ? Organization.get(params.organization) : null

		def c = Quote.createCriteria()
		def list = c.list {
			or {
				eq('number', number)
				ilike('subject', "%${params.name}%")
			}
			if (organization) {
				and {
					eq('organization', organization)
				}
			}
			order('number', 'desc')
		}

		render(contentType: "text/json") {
			array {
				for (q in list) {
					quote id: q.id, name: q.fullName
				}
			}
		}
	}

	def print() {
        def quoteInstance = Quote.get(params.id)
        if (!quoteInstance) {
            render(status: 404)
            return
        }

		def data = [
			transaction: quoteInstance, items: quoteInstance.items,
			organization: quoteInstance.organization,
			person: quoteInstance.person,
			user: session.user,
			fullNumber: quoteInstance.fullNumber,
			taxRates: quoteInstance.taxRateSums,
			values: [
		        subtotalNet: quoteInstance.subtotalNet,
				subtotalGross: quoteInstance.subtotalGross,
				discountPercentAmount: quoteInstance.discountPercentAmount,
				total: quoteInstance.total
			],
			watermark: params.duplicate ? 'duplicate' : '',
            client: Client.loadAsMap()
		]
		String xml = (data as XML).toString()
//		println xml

		GString fileName = "${message(code: 'quote.label')} ${quoteInstance.fullNumber}"
		if (params.duplicate) {
			fileName += " (${message(code: 'invoicingTransaction.duplicate')})"
		}
		fileName += ".pdf"

        fopService.outputPdf(xml, 'quote', params.template, response, fileName)
	}
}
