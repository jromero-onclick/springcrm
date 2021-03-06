/*
 * UserTagLib.groovy
 *
 * Copyright (c) 2011-2016, Daniel Ellermann
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
 * The class {@code UserTagLib} represents tags for login.
 *
 * @author  Daniel Ellermann
 * @version 2.0
 */
class UserTagLib {

    //-- Public methods -------------------------

    /**
     * Renders an area to display the currently logged in user.
     */
    def loginControl = {
        Credential credential = session.credential
        if (request.getSession(false) && credential) {
            out << '<small>' << credential.fullName << ' [' <<
                credential.userName << ']</small>'
            out << link(
                controller: 'user', action: 'logout',
                'class': 'btn btn-warning btn-xs',
                'role': 'button'
            ) {
                '<i class="fa fa-sign-out"></i> ' +
                    message(code: 'default.logout')
            }
        }
    }

    /**
     * Renders the value of a user setting.
     *
     * @attr key REQUIRED   the name of the user setting
     */
    def userSetting = { attrs, body ->
        def settings = session.credential?.settings
        if (settings) {
            out << settings[attrs.key]
        }
    }
}
