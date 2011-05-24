package org.amcworld.springcrm

import grails.test.*

class ServiceTests extends GrailsUnitTestCase {

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testConstruct() {
        Service sv = new Service(
            number:10000, name:"Installation of TYPO3", quantity:1.5,
            unitPrice:60.5
        )
        assertEquals 10000, sv.number
        assertEquals "Installation of TYPO3", sv.name
        assertNull sv.category
        assertEquals 1.5, sv.quantity
        assertNull sv.unit
        assertEquals 60.5, sv.unitPrice
        assertNull sv.taxClass
        assertEquals 0, sv.commission
        assertNull sv.salesStart
        assertNull sv.salesEnd
		assertNull sv.description
    }

    void testCategory() {
        Service sv = new Service()
        sv.category = new ServiceCategory(name:"Installation")
        assertEquals "Installation", sv.category.name
        assertEquals 0, sv.category.orderId
    }

    void testUnit() {
        Service sv = new Service()
        sv.unit = new Unit(name:"h")
        assertEquals "h", sv.unit.name
        assertEquals 0, sv.unit.orderId
    }

    void testTaxClass() {
        Service sv = new Service()
        sv.taxClass = new TaxClass(name:"19%")
        assertEquals "19%", sv.taxClass.name
        assertEquals 0, sv.taxClass.orderId
    }
	
	void testBlankConstraints() {
		mockForConstraintsTests Service
		def validationFields = ["name"]
		Service sv = new Service()
		assertFalse sv.validate(validationFields)
		assertEquals "nullable", sv.errors["name"]
		
		sv = new Service(name:"")
		assertFalse sv.validate(validationFields)
		assertEquals "blank", sv.errors["name"]
	}
	
	void testMinConstraints() {
		mockForConstraintsTests Service
		def validationFields = ["quantity", "unitPrice", "commission"]
		Service sv = new Service(quantity:-1, unitPrice:0, commission:-0.5)
		assertFalse sv.validate(validationFields)
		assertEquals "min", sv.errors["quantity"]
		assertEquals "min", sv.errors["unitPrice"]
		assertEquals "min", sv.errors["commission"]
		
		sv = new Service(quantity:0, unitPrice:0.01, commission:0)
		assertTrue sv.validate(validationFields)
	}
	
	void testFullNumber() {
        Service sv = new Service(number:10000, name:"Installation of TYPO3")
		assertEquals "SRV-10000", sv.fullNumber
	}
	
	void testToString() {
        Service sv = new Service(number:10000, name:"Installation of TYPO3")
		assertEquals "Installation of TYPO3", sv.toString()
		
		sv = new Service()
		assertEquals "", sv.toString()
	}
}
