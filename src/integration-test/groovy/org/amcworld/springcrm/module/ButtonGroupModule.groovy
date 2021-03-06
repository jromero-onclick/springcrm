/*
 * ButtonGroupModule.groovy
 *
 * Copyright (c) 2011-2014, Daniel Ellermann
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


package org.amcworld.springcrm.module


class ButtonGroupModule extends geb.Module {

    //-- Class variables ------------------------

    static content = {
        button { module ButtonModule, $('.button', 0) }
        dropdownButton { module ButtonModule, $('.dropdown') }
        dropdownMenu { $('ul.dropdown-menu') }
        dropdownMenuItems { dropdownMenu.find('li') }
        dropdownMenuLinks { module LinkModule, dropdownMenuItems[it].find('a') }
    }


    //-- Public methods -------------------------

    void clickDefButton() {
        this.button.click()
    }

    void selectItem(int idx, String expectedLabel = null) {
        this.dropdownButton.click()
        def link = this.dropdownMenuLinks(idx)
        if (expectedLabel) {
            assert expectedLabel == link.text()
        }
        link.click()
    }
}
