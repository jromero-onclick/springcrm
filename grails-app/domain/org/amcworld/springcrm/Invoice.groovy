/*
 * Invoice.groovy
 *
 * Copyright (c) 2011-2012, Daniel Ellermann
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


/**
 * The class {@code Invoice} represents an invoice.
 *
 * @author	Daniel Ellermann
 * @version 1.0
 */
class Invoice extends InvoicingTransaction {

    //-- Class variables ------------------------

    static constraints = {
		stage()
		dueDatePayment()
		paymentDate(nullable: true)
		paymentAmount(nullable: true, min: 0.0, scale: 2, widget: 'currency')
		paymentMethod(nullable: true)
		quote(nullable: true)
		salesOrder(nullable: true)
    }
    static belongsTo = [quote: Quote, salesOrder: SalesOrder]
    static hasMany = [creditMemos: CreditMemo, dunnings: Dunning]
	static mapping = {
		stage column: 'invoice_stage_id'
	}
	static searchable = true
    static transients = [
        'balance', 'balanceColor', 'closingBalance', 'paymentStateColor'
    ]


    //-- Instance variables ---------------------

	InvoiceStage stage
	Date dueDatePayment
	Date paymentDate
	BigDecimal paymentAmount
	PaymentMethod paymentMethod


    //-- Instance initializer -------------------

	{
		type = 'I'
	}


    //-- Constructors ---------------------------

	Invoice() {}

	Invoice(Quote q) {
		super(q)
		quote = q
	}

	Invoice(SalesOrder so) {
		super(so)
		salesOrder = so
	}

	Invoice(Invoice i) {
		super(i)
		quote = i.quote
		salesOrder = i.salesOrder
	}


    //-- Public methods -------------------------

    /**
     * Gets the balance of this invoice, that is the difference between the
     * payment amount and the invoice total sum.
     * <p>
     * Note that the invoice balance does not take any credit memos into
     * account.  Use method {@code getClosingBalance} for it.
     *
     * @return  the invoice balance
     * @since   1.0
     * @see     #getClosingBalance()
     */
    BigDecimal getBalance() {
        return (paymentAmount ?: 0) - (total ?: 0)
    }

    /**
     * Gets the closing balance of this invoice.  The closing balance is
     * calculated from the invoice balance plus the sum of the balances of
     * all credit memos associated to this invoice.  A negative balance
     * indicates a claim to the customer, a positive one indicates a credit of
     * the customer.
     *
     * @return  the closing balance
     * @since   1.0
     * @see     #getBalance()
     */
    BigDecimal getClosingBalance() {
        return balance + (creditMemos ? creditMemos*.balance.sum() : 0)
    }

    /**
     * Gets the name of a color indicating the status of the balance of this
     * invoice.  This property is usually use to compute CSS classes in the
     * views.
     *
     * @return  the indicator color
     * @since   1.0
     */
    String getBalanceColor() {
        String color = 'default'
        if (closingBalance < 0) {
            color = 'red'
        } else if (closingBalance > 0) {
            color = 'green'
        }
        return color
    }

    /**
     * Gets the name of a color indicating the payment state of this invoice.
     * This property is usually use to compute CSS classes in the views.
     *
     * @return  the indicator color
     * @since   1.0
     */
    String getPaymentStateColor() {
        String color = 'default'
        switch (stage?.id) {
        case 907:                       // cancelled
            color = (closingBalance >= 0) ? 'green' : colorIndicatorByDate()
            break
        case 906:                       // booked out
            color = 'black'
            break
        case 905:                       // cashing
            color = 'blue'
            break
        case 904:                       // dunned
            color = 'purple'
            break
        case 903:                       // paid
            color = (closingBalance >= 0) ? 'green' : colorIndicatorByDate()
            break
        case 902:                       // delivered
            color = colorIndicatorByDate()
            break
        }
        return color
    }


    //-- Non-public methods ---------------------

    /**
     * Returns the color indicator for the payment state depending on the
     * current date and its relation to the due date of payment.
     *
     * @return  the indicator color
     * @since   1.0
     */
    protected String colorIndicatorByDate() {
        String color = 'default'
        Date d = new Date()
        if (d >= dueDatePayment - 3) {
            if (d <= dueDatePayment) {
                color = 'yellow'
            } else if (d <= dueDatePayment + 3) {
                color = 'orange'
            } else {
                color = 'red'
            }
        }
        return color
    }
}
