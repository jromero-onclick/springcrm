/*
 * DunningController.groovy
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
 * The class {@code DunningController} contains actions which manage dunnings.
 *
 * @author	Daniel Ellermann
 * @version 0.9
 */
class DunningController {

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
        return [dunningInstanceList: Dunning.list(params), dunningInstanceTotal: Dunning.count()]
    }

	def listEmbedded() {
		def l
		def count
		def linkParams
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
		if (params.organization) {
			def organizationInstance = Organization.get(params.organization)
			l = Dunning.findAllByOrganization(organizationInstance, params)
			count = Dunning.countByOrganization(organizationInstance)
			linkParams = [organization: organizationInstance.id]
		} else if (params.person) {
			def personInstance = Person.get(params.person)
			l = Dunning.findAllByPerson(personInstance, params)
			count = Dunning.countByPerson(personInstance)
			linkParams = [person: personInstance.id]
		} else if (params.invoice) {
			def invoiceInstance = Invoice.get(params.invoice)
			l = Dunning.findAllByInvoice(invoiceInstance, params)
			count = Dunning.countByInvoice(invoiceInstance)
			linkParams = [invoice: invoiceInstance.id]
		}
		return [dunningInstanceList: l, dunningInstanceTotal: count, linkParams: linkParams]
	}

    def create() {
        def dunningInstance
		if (params.invoice) {
			def invoiceInstance = Invoice.get(params.invoice)
			invoiceInstance.items?.clear()
			dunningInstance = new Dunning(invoiceInstance)
		} else {
			dunningInstance = new Dunning()
			dunningInstance.properties = params
		}

		Organization org = dunningInstance.organization
		if (org) {
			dunningInstance.billingAddrCountry = org.billingAddrCountry
			dunningInstance.billingAddrLocation = org.billingAddrLocation
			dunningInstance.billingAddrPoBox = org.billingAddrPoBox
			dunningInstance.billingAddrPostalCode = org.billingAddrPostalCode
			dunningInstance.billingAddrState = org.billingAddrState
			dunningInstance.billingAddrStreet = org.billingAddrStreet
			dunningInstance.shippingAddrCountry = org.shippingAddrCountry
			dunningInstance.shippingAddrLocation = org.shippingAddrLocation
			dunningInstance.shippingAddrPoBox = org.shippingAddrPoBox
			dunningInstance.shippingAddrPostalCode = org.shippingAddrPostalCode
			dunningInstance.shippingAddrState = org.shippingAddrState
			dunningInstance.shippingAddrStreet = org.shippingAddrStreet
		}

		ConfigHolder config = ConfigHolder.instance
		Integer serviceId = config['serviceIdDunningCharge'] as Integer
		if (serviceId) {
			def service = Service.get(serviceId)
			if (service) {
				dunningInstance.addToItems(serviceToItem(service))
			}
		}
		serviceId = config['serviceIdDefaultInterest'] as Integer
		if (serviceId) {
			def service = Service.get(serviceId)
			if (service) {
				dunningInstance.addToItems(serviceToItem(service))
			}
		}
        return [dunningInstance: dunningInstance]
    }

	def copy() {
		def dunningInstance = Dunning.get(params.id)
		if (!dunningInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'dunning.label', default: 'Dunning'), params.id])
            redirect(action: 'list')
            return
        }

        dunningInstance = new Dunning(dunningInstance)
		render(view: 'create', model: [dunningInstance: dunningInstance])
	}

    def save() {
        def dunningInstance = new Dunning(params)
        if (!dunningInstance.save(flush: true)) {
            log.debug(dunningInstance.errors)
            render(view: 'create', model: [dunningInstance: dunningInstance])
            return
        }
        params.id = dunningInstance.ident()

        dunningInstance.index()

		def invoiceInstance = dunningInstance.invoice
		invoiceInstance.stage = InvoiceStage.get(904)
		invoiceInstance.save(flush: true)
		invoiceInstance.reindex()

        flash.message = message(code: 'default.created.message', args: [message(code: 'dunning.label', default: 'Dunning'), dunningInstance.toString()])
		if (params.returnUrl) {
			redirect(url: params.returnUrl)
		} else {
			redirect(action: 'show', id: dunningInstance.id)
		}
    }

    def show() {
        def dunningInstance = Dunning.get(params.id)
        if (!dunningInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'dunning.label', default: 'Dunning'), params.id])
            redirect(action: 'list')
            return
        }

        return [dunningInstance: dunningInstance, printTemplates: fopService.templateNames]
    }

    def edit() {
        def dunningInstance = Dunning.get(params.id)
        if (!dunningInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'dunning.label', default: 'Dunning'), params.id])
            redirect(action: 'list')
            return
        }

		if (session.user.admin || dunningInstance.stage.id < 2202) {
			return [dunningInstance: dunningInstance]
		}
		redirect(action: 'list')
    }

    def editPayment() {
        def dunningInstance = Dunning.get(params.id)
        if (!dunningInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'dunning.label', default: 'Dunning'), params.id])
            redirect(action: 'list')
            return
        }

        return [dunningInstance: dunningInstance]
    }

    def update() {
        def dunningInstance = Dunning.get(params.id)
        if (!dunningInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'dunning.label', default: 'Dunning'), params.id])
            redirect(action: 'list')
            return
        }

        if (!session.user.admin && dunningInstance.stage.id >= 2202) {
            redirect(action: 'list')
            return
        }

        if (params.version) {
            def version = params.version.toLong()
            if (dunningInstance.version > version) {

                dunningInstance.errors.rejectValue('version', 'default.optimistic.locking.failure', [message(code: 'dunning.label', default: 'Dunning')] as Object[], "Another user has updated this Dunning while you were editing")
                render(view: 'edit', model: [dunningInstance: dunningInstance])
                return
            }
        }
		if (params.autoNumber) {
			params.number = dunningInstance.number
		}

        /*
         * The original implementation which worked in Grails 2.0.0.
         */
        dunningInstance.properties = params
//        dunningInstance.items?.retainAll { it != null }

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
        dunningInstance.items?.clear()
        for (int i = 0; params."items[${i}]"; i++) {
            if (params."items[${i}]".id != 'null') {
                dunningInstance.addToItems(params."items[${i}]")
            }
        }

        if (!dunningInstance.save(flush: true)) {
            render(view: 'edit', model: [dunningInstance: dunningInstance])
            return
        }

		dunningInstance.reindex()

		def invoiceInstance = dunningInstance.invoice
		invoiceInstance.stage = InvoiceStage.get(904)
		invoiceInstance.save(flush: true)
		invoiceInstance.reindex()

		flash.message = message(code: 'default.updated.message', args: [message(code: 'dunning.label', default: 'Dunning'), dunningInstance.toString()])
		if (params.returnUrl) {
			redirect(url: params.returnUrl)
		} else {
			redirect(action: 'show', id: dunningInstance.id)
		}
    }

    def updatePayment() {
        def dunningInstance = Dunning.get(params.id)
        if (!dunningInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'dunning.label', default: 'Dunning'), params.id])
            redirect(action: 'list')
            return
        }

        if (params.version) {
            def version = params.version.toLong()
            if (dunningInstance.version > version) {

                dunningInstance.errors.rejectValue('version', 'default.optimistic.locking.failure', [message(code: 'dunning.label', default: 'Dunning')] as Object[], "Another user has updated this Dunning while you were editing")
                render(view: 'edit', model: [dunningInstance: dunningInstance])
                return
            }
        }

        dunningInstance.properties = params.findAll { it.key in ['stage.id', 'paymentDate', 'paymentAmount', 'paymentMethod.id'] }

        if (!dunningInstance.save(flush: true)) {
            render(view: 'edit', model: [dunningInstance: dunningInstance])
            return
        }

        dunningInstance.reindex()

        flash.message = message(code: 'default.updated.message', args: [message(code: 'dunning.label', default: 'Dunning'), dunningInstance.toString()])
        if (params.returnUrl) {
            redirect(url: params.returnUrl)
        } else {
            redirect(action: 'show', id: dunningInstance.id)
        }
    }

    def delete() {
        def dunningInstance = Dunning.get(params.id)
        if (dunningInstance && params.confirmed) {
			if (!session.user.admin && dunningInstance.stage.id >= 2202) {
				redirect(action: 'list')
                return
			}
            try {
                dunningInstance.delete(flush: true)
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'dunning.label', default: 'Dunning')])
				if (params.returnUrl) {
					redirect(url: params.returnUrl)
				} else {
					redirect(action: 'list')
				}
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'dunning.label', default: 'Dunning')])
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'dunning.label', default: 'Dunning'), params.id])
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

		def c = Dunning.createCriteria()
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
				for (d in list) {
					dunning id: d.id, name: d.fullName
				}
			}
		}
	}

	def print() {
        def dunningInstance = Dunning.get(params.id)
        if (!dunningInstance) {
            render(status: 404)
            return
        }

		def data = [
			transaction: dunningInstance,
			items: dunningInstance.items,
			organization: dunningInstance.organization,
			person: dunningInstance.person,
			invoice: dunningInstance.invoice,
			invoiceFullNumber: dunningInstance.invoice.fullNumber,
			user: session.user,
			fullNumber: dunningInstance.fullNumber,
			taxRates: dunningInstance.taxRateSums,
			values: [
		        subtotalNet: dunningInstance.subtotalNet,
				subtotalGross: dunningInstance.subtotalGross,
				discountPercentAmount: dunningInstance.discountPercentAmount,
				total: dunningInstance.total
			],
			watermark: params.duplicate ? 'duplicate' : '',
            client: Client.loadAsMap()
		]
		String xml = (data as XML).toString()
//        println xml

		GString fileName = "${message(code: 'dunning.label')} ${dunningInstance.fullNumber}"
		if (params.duplicate) {
			fileName += " (${message(code: 'invoicingTransaction.duplicate')})"
		}
		fileName += ".pdf"

        fopService.outputPdf(
            xml, 'dunning', params.template, response, fileName
        )
	}


    //-- Non-public methods ---------------------

	private InvoicingItem serviceToItem(Service s) {
		return new InvoicingItem(
			number: s.fullNumber, quantity: s.quantity, unit: s.unit.toString(),
			name: s.name, description: s.description, unitPrice: s.unitPrice,
			tax: s.taxRate.taxValue * 100
		)
	}
}
