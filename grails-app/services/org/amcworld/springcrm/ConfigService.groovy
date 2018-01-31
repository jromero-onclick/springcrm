/*
 * ConfigService.groovy
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

import grails.gorm.services.Service
import groovy.transform.CompileStatic


/**
 * The interface {@code IConfigService} contains general methods to handle
 * configuration values in the underlying data store.
 *
 * @author  Daniel Ellermann
 * @version 3.0
 * @since   3.0
 */
interface IConfigService {

    //-- Public methods -------------------------

    /**
     * Deletes the configuration with the given ID.
     *
     * @param id    the given ID
     */
    void delete(String id)

    /**
     * Gets the configuration with the given ID.
     *
     * @param id    the given ID
     * @return      the configuration instance or {@code null} if no such
     *              configuration exists
     */
    Config get(String id)

    /**
     * Lists all configuration settings.
     *
     * @return  a list of all configuration settings
     */
    List<Config> list()

    /**
     * Saves the given configuration.
     *
     * @param config    the given configuration
     * @return          the saved configuration or {@code null} if an error
     *                  occurred
     */
    Config save(Config config)
}


/**
 * The class {@code ConfigService} contains additional methods to handle
 * configuration values in the underlying data store.
 *
 * @author  Daniel Ellermann
 * @version 3.0
 * @since   3.0
 */
@CompileStatic
@Service(Config)
abstract class ConfigService implements IConfigService {

    //-- Public methods -------------------------

    /**
     * Gets the configuration with the given ID as boolean value.
     *
     * @param id        the given ID
     * @param defValue  a default value which should be used if no such
     *                  configuration exists or is unset
     * @return          the boolean value or {@code null} if no such
     *                  configuration exists or is unset and no default value
     *                  has been specified
     */
    Boolean getBoolean(String id, Boolean defValue = null) {
        String value = get(id)?.value

        value == null ? defValue : Boolean.valueOf(value)
    }

    /**
     * Gets the configuration with the given ID as calendar value.
     *
     * @param id        the given ID
     * @param defValue  a default value which should be used if no such
     *                  configuration exists or is unset
     * @return          the calendar value or {@code null} if no such
     *                  configuration exists or is unset and no default value
     *                  has been specified
     */
    Calendar getCalendar(String id, Calendar defValue = null) {
        getDate(id)?.toCalendar() ?: defValue
    }

    /**
     * Gets the configuration with the given ID as date value.
     *
     * @param id        the given ID
     * @param defValue  a default value which should be used if no such
     *                  configuration exists or is unset
     * @return          the date value or {@code null} if no such configuration
     *                  exists or is unset and no default value has been
     *                  specified
     */
    Date getDate(String id, Date defValue = null) {
        String value = get(id)?.value

        value ? Date.parseToStringDate(value) : defValue
    }

    /**
     * Gets the configuration with the given ID as integer value.
     *
     * @param id        the given ID
     * @param defValue  a default value which should be used if no such
     *                  configuration exists or is unset
     * @return          the integer value or {@code null} if no such
     *                  configuration exists or is unset and no default value
     *                  has been specified
     */
    Integer getInteger(String id, Integer defValue = null) {
        Integer res = get(id)?.value?.asType(Integer)

        res == null ? defValue : res
    }

    /**
     * Gets the configuration with the given ID as long value.
     *
     * @param id        the given ID
     * @param defValue  a default value which should be used if no such
     *                  configuration exists or is unset
     * @return          the long value or {@code null} if no such configuration
     *                  exists or is unset and no default value
     *                  has been specified
     */
    Long getLong(String id, Long defValue = null) {
        Long res = get(id)?.value?.asType(Long)

        res == null ? defValue : res
    }

    /**
     * Gets the configuration with the given ID as string value.
     *
     * @param id        the given ID
     * @param defValue  a default value which should be used if no such
     *                  configuration exists or is unset
     * @return          the string value or {@code null} if no such
     *                  configuration exists or is unset and no default value
     *                  has been specified
     */
    String getString(String id, String defValue = null) {
        String res = get(id)?.toString()

        res == null ? defValue : res
    }

    /**
     * Stores the configuration with the given ID and value.
     *
     * @param id    the given ID
     * @param value the given value; may be {@code null}
     * @return      the saved configuration instance
     */
    Config store(String id, Object value) {
        Config config = new Config(value: value?.toString())
        config.id = id

        save config
    }
}
