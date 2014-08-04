//
// i18n-source.js
//
// Copyright (c) 2011-2014, Daniel Ellermann
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
//
// Key definitions for i18n retrieval.
//
// The file defines the keys of localized messages which are used in JavaScript
// files.  The localized messages are obtained from the message resources of
// the server (files messages*.properties).  From them a file named "i18n.js"
// is generated in the same directory.  The translation is done by class
// "org.amcworld.springcrm.I18nResourceMapper".
//
// Lines starting with to slashes (//) and empty lines are ignored.  The file
// consists of an anonymous JavaScript array ([]) containing the message keys.
// The opening bracket must be the first character (excluding whitespaces) in
// the line.  The keys must be enclosed in apostrophes or quotation marks.
// Dots in keys are translated by underlines.  Keys in the form
// "my.key{sub.key1,sub.key2,sub.key3}" are translated to a JavaScript object
// as follows:
//     my_key {
//         sub_key1: "submsg1",
//         sub_key2: "submsg2",
//         sub_key3: "submsg3",
//     }
[
    'calendarEvent.button.text.gotoDate',
    'calendarEvent.button.text.list',
    'calendarEvent.reminder.unit.day',
    'calendarEvent.reminder.unit.days',
    'calendarEvent.reminder.unit.hour',
    'calendarEvent.reminder.unit.hours',
    'calendarEvent.reminder.unit.minute',
    'calendarEvent.reminder.unit.minutes',
    'calendarEvent.reminder.unit.week',
    'calendarEvent.reminder.unit.weeks',
    'default.btn.remove',
    'default.button.cancel.label',
    'default.button.ok.label',
    'default.button.send.label',
    'default.copyAddressWarning.billingAddr',
    'default.copyAddressWarning.mailingAddr',
    'default.copyAddressWarning.otherAddr',
    'default.copyAddressWarning.shippingAddr',
    'default.delete.confirm.msg',
    'default.format.date',
    'default.format.time',
    'document.back.label',
    'document.path.label',
    'document.path.root',
    'help.markdown.title',
    'invoicingTransaction.addr.fromOrgBillingAddr',
    'invoicingTransaction.addr.fromOrgShippingAddr',
    'invoicingTransaction.billingAddr.clear',
    'invoicingTransaction.billingAddr.copy',
    'invoicingTransaction.changeState.label',
    'invoicingTransaction.shippingAddr.clear',
    'invoicingTransaction.shippingAddr.copy',
    'invoicingTransaction.taxRate.label',
    'organization.billingAddr.clear',
    'organization.billingAddr.copy',
    'organization.shippingAddr.clear',
    'organization.shippingAddr.copy',
    'person.addr.fromOrgBillingAddr',
    'person.addr.fromOrgShippingAddr',
    'person.mailingAddr.clear',
    'person.mailingAddr.copy',
    'person.otherAddr.clear',
    'person.otherAddr.copy',
    'salesItem.pricing.error.notMovable.refBeforeReferee',
    'salesItem.pricing.error.notRemovable',
    'salesItem.pricing.removePricing.confirm',
    'ticket.changeStage.assign.confirm',
    'ticket.changeStage.closed.confirm',
    'ticket.takeOn.confirm',
    'user.settings.googleAuth.revoke.confirm.disconnect'
];
