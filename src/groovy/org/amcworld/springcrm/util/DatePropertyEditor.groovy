/*
 * DatePropertyEditor.groovy
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


package org.amcworld.springcrm.util

import java.beans.PropertyEditorSupport
import java.text.ParseException
import java.text.SimpleDateFormat
import org.springframework.context.i18n.LocaleContextHolder


/**
 * The class {@code DatePropertyEditor} represents a property editor which
 * converts dates to strings and vice versa.  The class understands localized
 * date/time formats.
 *
 * @author	Daniel Ellermann
 * @version 0.9
 */
class DatePropertyEditor extends PropertyEditorSupport {

	//-- Instance variables ---------------------

	String dateFormat
	String dateTimeFormat
	String timeFormat


	//-- Constructors ---------------------------

	DatePropertyEditor(messageSource) {
		dateFormat = messageSource.getMessage(
			'default.format.date', null, 'yyyy-MM-dd',
			LocaleContextHolder.locale
		)
		dateTimeFormat = messageSource.getMessage(
			'default.format.datetime', null, 'yyyy-MM-dd HH:mm',
			LocaleContextHolder.locale
		)
		timeFormat = messageSource.getMessage(
			'default.format.time', null, 'HH:mm', LocaleContextHolder.locale
		)
	}


	//-- Public methods -------------------------

	@Override
	String getAsText() {
		if (value == null) {
			return null
		} else {
			SimpleDateFormat format = new SimpleDateFormat(dateTimeFormat)
			return format.format(value)
		}
	}

	@Override
	void setAsText(String text) throws IllegalArgumentException {
		if (text == null) {
			value = null
		} else {
			value = (text.indexOf(' ') < 0) ? parseDate(text) : parseDateTime(text)
		}
	}


	//-- Non-public methods ---------------------

	protected Date parseDate(String text) throws IllegalArgumentException {
		String fmt
		if (text.isLong()) {
			fmt = (text.length() > 6) ? 'ddMMyyyy' : 'ddMMyy'
		} else {
			fmt = dateFormat
		}
		try {
			return new SimpleDateFormat(fmt).parse(text)
		} catch (ParseException) {
			throw new IllegalArgumentException()
		}
	}

	protected Date parseDateTime(String text) throws IllegalArgumentException {
		String fmt
		int pos = text.indexOf(' ')
		if (pos >= 0) {
			String s = text.substring(0, pos)
			if (s.isLong()) {
				fmt = (s.length() > 6) ? 'ddMMyyyy' : 'ddMMyy'
			} else {
				fmt = dateFormat
			}
			s = text.substring(pos + 1).trim()
			fmt += ' ' + (s.isLong() ? 'HHmm' : timeFormat)
		} else {
			fmt += dateTimeFormat
		}
		try {
			return new SimpleDateFormat(fmt).parse(text)
		} catch (ParseException) {
			throw new IllegalArgumentException()
		}
	}
}
