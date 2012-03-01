/*
 * InvoiceController.groovy
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
 * The class {@code InvoiceController} contains actions which manage invoices.
 *
 * @author	Daniel Ellermann
 * @version 0.9
 */
class InvoiceController {

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
        return [invoiceInstanceList: Invoice.list(params), invoiceInstanceTotal: Invoice.count()]
    }

	def listEmbedded() {
		def l
		def count
		def linkParams
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
		if (params.organization) {
			def organizationInstance = Organization.get(params.organization)
			l = Invoice.findAllByOrganization(organizationInstance, params)
			count = Invoice.countByOrganization(organizationInstance)
			linkParams = [organization: organizationInstance.id]
		} else if (params.person) {
			def personInstance = Person.get(params.person)
			l = Invoice.findAllByPerson(personInstance, params)
			count = Invoice.countByPerson(personInstance)
			linkParams = [person: personInstance.id]
		} else if (params.quote) {
			def quoteInstance = Quote.get(params.quote)
			l = Invoice.findAllByQuote(quoteInstance, params)
			count = Invoice.countByQuote(quoteInstance)
			linkParams = [quote: quoteInstance.id]
		} else if (params.salesOrder) {
			def salesOrderInstance = SalesOrder.get(params.salesOrder)
			l = Invoice.findAllBySalesOrder(salesOrderInstance, params)
			count = Invoice.countBySalesOrder(salesOrderInstance)
			linkParams = [salesOrder: salesOrderInstance.id]
		}
		return [invoiceInstanceList: l, invoiceInstanceTotal: count, linkParams: linkParams]
	}

    def create() {
        def invoiceInstance
		if (params.quote) {
			def quoteInstance = Quote.get(params.quote)
			invoiceInstance = new Invoice(quoteInstance)
		} else if (params.salesOrder) {
			def salesOrderInstance = SalesOrder.get(params.salesOrder)
			invoiceInstance = new Invoice(salesOrderInstance)
			invoiceInstance.quote = salesOrderInstance.quote
		} else {
			invoiceInstance = new Invoice()
			invoiceInstance.properties = params
		}
		Organization org = invoiceInstance.organization
		if (org) {
			invoiceInstance.billingAddrCountry = org.billingAddrCountry
			invoiceInstance.billingAddrLocation = org.billingAddrLocation
			invoiceInstance.billingAddrPoBox = org.billingAddrPoBox
			invoiceInstance.billingAddrPostalCode = org.billingAddrPostalCode
			invoiceInstance.billingAddrState = org.billingAddrState
			invoiceInstance.billingAddrStreet = org.billingAddrStreet
			invoiceInstance.shippingAddrCountry = org.shippingAddrCountry
			invoiceInstance.shippingAddrLocation = org.shippingAddrLocation
			invoiceInstance.shippingAddrPoBox = org.shippingAddrPoBox
			invoiceInstance.shippingAddrPostalCode = org.shippingAddrPostalCode
			invoiceInstance.shippingAddrState = org.shippingAddrState
			invoiceInstance.shippingAddrStreet = org.shippingAddrStreet
		}
        return [invoiceInstance: invoiceInstance]
    }

	def copy() {
		def invoiceInstance = Invoice.get(params.id)
		if (!invoiceInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'invoice.label', default: 'Invoice'), params.id])
            redirect(action: 'list')
            return
        }

        invoiceInstance = new Invoice(invoiceInstance)
		render(view: 'create', model: [invoiceInstance: invoiceInstance])
	}

    def save() {
        def invoiceInstance = new Invoice(params)
		if (!invoiceInstance.save(flush: true)) {
            log.debug(invoiceInstance.errors)
            render(view: 'create', model: [invoiceInstance: invoiceInstance])
            return
        }
        params.id = invoiceInstance.ident()

		invoiceInstance.index()
        flash.message = message(code: 'default.created.message', args: [message(code: 'invoice.label', default: 'Invoice'), invoiceInstance.toString()])
		if (params.returnUrl) {
			redirect(url: params.returnUrl)
		} else {
			redirect(action: 'show', id: invoiceInstance.id)
		}
    }

    def show() {
        def invoiceInstance = Invoice.get(params.id)
        if (!invoiceInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'invoice.label', default: 'Invoice'), params.id])
            redirect(action: 'list')
            return
        }

        return [invoiceInstance: invoiceInstance, printTemplates: fopService.templateNames]
    }

    def edit() {
        def invoiceInstance = Invoice.get(params.id)
        if (!invoiceInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'invoice.label', default: 'Invoice'), params.id])
            redirect(action: 'list')
            return
        }

		if (session.user.admin || invoiceInstance.stage.id < 902) {
			return [invoiceInstance: invoiceInstance]
		}
		redirect(action: 'list')
    }

    def editPayment() {
        def invoiceInstance = Invoice.get(params.id)
        if (!invoiceInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'invoice.label', default: 'Invoice'), params.id])
            redirect(action: 'list')
            return
        }

        return [invoiceInstance: invoiceInstance]
    }

    def update() {
        def invoiceInstance = Invoice.get(params.id)
        if (!invoiceInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'invoice.label', default: 'Invoice'), params.id])
            redirect(action: 'list')
            return
        }

        if (!session.user.admin && invoiceInstance.stage.id >= 902) {
            redirect(action: 'list')
            return
        }

        if (params.version) {
            def version = params.version.toLong()
            if (invoiceInstance.version > version) {
                invoiceInstance.errors.rejectValue('version', 'default.optimistic.locking.failure', [message(code: 'invoice.label', default: 'Invoice')] as Object[], "Another user has updated this Invoice while you were editing")
                render(view: 'edit', model: [invoiceInstance: invoiceInstance])
                return
            }
        }
		if (params.autoNumber) {
			params.number = invoiceInstance.number
		}

        /*
         * The original implementation which worked in Grails 2.0.0.
         */
        invoiceInstance.properties = params
//        invoiceInstance.items?.retainAll { it != null }

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
        invoiceInstance.items?.clear()
        for (int i = 0; params."items[${i}]"; i++) {
            if (params."items[${i}]".id != 'null') {
                invoiceInstance.addToItems(params."items[${i}]")
            }
        }

        if (!invoiceInstance.save(flush: true)) {
            log.debug(invoiceInstance.errors)
            render(view: 'edit', model: [invoiceInstance: invoiceInstance])
            return
        }

		invoiceInstance.reindex()

        flash.message = message(code: 'default.updated.message', args: [message(code: 'invoice.label', default: 'Invoice'), invoiceInstance.toString()])
		if (params.returnUrl) {
			redirect(url: params.returnUrl)
		} else {
			redirect(action: 'show', id: invoiceInstance.id)
		}
    }

    def updatePayment() {
        def invoiceInstance = Invoice.get(params.id)
        if (!invoiceInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'invoice.label', default: 'Invoice'), params.id])
            redirect(action: 'list')
            return
        }

        if (params.version) {
            def version = params.version.toLong()
            if (invoiceInstance.version > version) {
                invoiceInstance.errors.rejectValue('version', 'default.optimistic.locking.failure', [message(code: 'invoice.label', default: 'Invoice')] as Object[], "Another user has updated this Invoice while you were editing")
                render(view: 'edit', model: [invoiceInstance: invoiceInstance])
                return
            }
        }

        invoiceInstance.properties = params.findAll { it.key in ['stage.id', 'paymentDate', 'paymentAmount', 'paymentMethod.id'] }

        if (!invoiceInstance.save(flush: true)) {
            log.debug(invoiceInstance.errors)
            render(view: 'edit', model: [invoiceInstance: invoiceInstance])
            return
        }

        invoiceInstance.reindex()

        flash.message = message(code: 'default.updated.message', args: [message(code: 'invoice.label', default: 'Invoice'), invoiceInstance.toString()])
        if (params.returnUrl) {
            redirect(url: params.returnUrl)
        } else {
            redirect(action: 'show', id: invoiceInstance.id)
        }
    }

    def delete() {
        def invoiceInstance = Invoice.get(params.id)
        if (invoiceInstance && params.confirmed) {
			if (!session.user.admin && invoiceInstance.stage.id >= 902) {
				redirect(action: 'list')
                return
			}
            try {
                invoiceInstance.delete(flush: true)
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'invoice.label', default: 'Invoice')])
				if (params.returnUrl) {
					redirect(url: params.returnUrl)
				} else {
					redirect(action: 'list')
				}
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'invoice.label', default: 'Invoice')])
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'invoice.label', default: 'Invoice'), params.id])
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

		def c = Invoice.createCriteria()
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

		render(contentType: 'text/json') {
			array {
				for (i in list) {
					invoice id: i.id, name: i.fullName
				}
			}
		}
	}

	def print() {
        def invoiceInstance = Invoice.get(params.id)
        if (!invoiceInstance) {
            render(status: 404)
            return
        }

		def data = [
			transaction: invoiceInstance,
			items: invoiceInstance.items,
			organization: invoiceInstance.organization,
			person: invoiceInstance.person,
			user: session.user,
			fullNumber: invoiceInstance.fullNumber,
			taxRates: invoiceInstance.taxRateSums,
			values: [
		        subtotalNet: invoiceInstance.subtotalNet,
				subtotalGross: invoiceInstance.subtotalGross,
				discountPercentAmount: invoiceInstance.discountPercentAmount,
				total: invoiceInstance.total
			],
			watermark: params.duplicate ? 'duplicate' : '',
            client: Client.loadAsMap()
		]
		String xml = (data as XML).toString()
//		println xml

		GString fileName =
            "${message(code: 'invoice.label')} ${invoiceInstance.fullNumber}"
		if (params.duplicate) {
			fileName += " (${message(code: 'invoicingTransaction.duplicate')})"
		}
		fileName += ".pdf"

        fopService.outputPdf(
            xml, 'invoice', params.template, response, fileName
        )
	}

	def listUnpaidBills() {
		InvoiceStage stage = InvoiceStage.get(902)
		def c = Invoice.createCriteria()
		def invoiceInstanceList = c.list {
			eq('stage', stage)
			and {
				le('dueDatePayment', new Date())
			}
			order('docDate', 'desc')
		}
		return [invoiceInstanceList: invoiceInstanceList]
	}
}
