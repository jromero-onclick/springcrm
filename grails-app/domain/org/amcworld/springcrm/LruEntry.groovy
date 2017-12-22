/*
 * LruEntry.groovy
 *
 * Copyright (c) 2011-2017, Daniel Ellermann
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

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.bson.types.ObjectId


/**
 * The class {@code LruEntry} stores the data of an entry in the last recently
 * used (LRU) list.
 *
 * @author  Daniel Ellermann
 * @version 3.0
 */
@EqualsAndHashCode(includes = ['user', 'controller', 'itemId'])
@ToString(includes = ['user', 'controller', 'itemId'])
class LruEntry {

    //-- Class fields ---------------------------

    static constraints = {
        controller blank: false
        name nullable: true, blank: true
    }
    static mapping = {
        compoundIndex user: 1, controller: 1
        controller index: true
        user index: true
        version false
    }


    //-- Fields ---------------------------------

    /**
     * The controller where the item belongs to.
     */
    String controller

    /**
     * The ID of the LRU entry.
     */
    ObjectId id

    /**
     * The ID of the item.
     */
    ObjectId itemId

    /**
     * The textual representation of the LRU item, for example the title of the
     * item.
     */
    String name

    /**
     * The position of the LRU entry among others.
     */
    long pos

    /**
     * The user the LRU entry belongs to.
     */
    User user
}
