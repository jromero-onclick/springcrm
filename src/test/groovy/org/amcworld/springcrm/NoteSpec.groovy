/*
 * NoteSpec.groovy
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

import grails.testing.gorm.DomainUnitTest
import org.bson.types.ObjectId
import spock.lang.Specification


class NoteSpec extends Specification implements DomainUnitTest<Note> {

	//-- Feature methods ---------------------------

    def 'Creating an empty note initializes the properties'() {
        when: 'I create an empty note'
        def n = new Note()

        then: 'the properties are initialized properly'
        0i == n.number
        null == n.title
        null == n.content
        null == n.organization
        null == n.person
        null == n.dateCreated
        null == n.lastUpdated
    }

    def 'Copy an empty instance using constructor'() {
        given: 'an empty note'
        def n1 = new Note()

        when: 'I copy the note using the constructor'
        def n2 = new Note(n1)

        then: 'the properties are set properly'
        0i == n2.number
		null == n2.title
        null == n2.content
        null == n2.organization
        null == n2.person
        null == n2.dateCreated
        null == n2.lastUpdated
    }

	def 'Copy a note using constructor'() {
		given: 'a note with various properties'
		def n1 = new Note(
			number: 10032i,
			title: 'note title',
			content: 'note content',
			dateCreated: new Date(),
			lastUpdated: new Date()
		)

		when: 'I copy the object'
		def n2 = new Note(n1)

		then: 'some properties are the same'
		n2.title == n1.title
		n2.content == n1.content

		and: 'some properties are unset'
		0i == n2.number
		null == n2.dateCreated
		null == n2.lastUpdated
	}

    def 'Equals is null-safe'() {
        given: 'a note'
        def n = new Note()

        expect:
        null != n
        n != null
        !n.equals(null)
    }

    def 'Instances of other types are always unequal'() {
        given: 'a note'
        def n = new Note()

        expect:
        n != 'foo'
        n != 45
        n != 45.3
        n != new Date()
    }

    def 'Not persisted instances are equal'() {
        given: 'three instances without ID'
        def n1 = new Note(title: 'Network configuration')
        def n2 = new Note(title: 'Network configuration')
        def n3 = new Note(title: 'Network configuration')

        expect: 'equals() is reflexive'
        n1 == n1
        n2 == n2
        n3 == n3

        and: 'all instances are equal and equals() is symmetric'
        n1 == n2
        n2 == n1
        n2 == n3
        n3 == n2

        and: 'equals() is transitive'
        n1 == n3
        n3 == n1
    }

    def 'Persisted instances are equal if they have the same ID'() {
        given: 'three instances with different properties but same IDs'
        def id = new ObjectId()
        def n1 = new Note(title: 'Network configuration')
        n1.id = id
        def n2 = new Note(title: 'Customer assessment')
        n2.id = id
        def n3 = new Note(title: 'Staff')
        n3.id = id

        expect: 'equals() is reflexive'
        n1 == n1
        n2 == n2
        n3 == n3

        and: 'all instances are equal and equals() is symmetric'
        n1 == n2
        n2 == n1
        n2 == n3
        n3 == n2

        and: 'equals() is transitive'
        n1 == n3
        n3 == n1
    }

    def 'Persisted instances are unequal if they have the different ID'() {
        given: 'three instances with same properties but different IDs'
		def n1 = new Note(title: 'Network configuration')
        n1.id = new ObjectId()
        def n2 = new Note(title: 'Network configuration')
        n2.id = new ObjectId()
        def n3 = new Note(title: 'Network configuration')
        n3.id = new ObjectId()

        expect: 'equals() is reflexive'
        n1 == n1
        n2 == n2
        n3 == n3

        and: 'all instances are unequal and equals() is symmetric'
        n1 != n2
        n2 != n1
        n2 != n3
        n3 != n2

        and: 'equals() is transitive'
        n1 != n3
        n3 != n1
    }

    def 'Can compute hash code of an empty instance'() {
        given: 'an empty instance'
        def note = new Note()

        expect:
        3937i == note.hashCode()
    }

    def 'Can compute hash code of a not persisted instance'() {
        given: 'an instance without ID'
        def note = new Note(title: 'Network configuration')

        expect:
        3937i == note.hashCode()
    }

    def 'Hash codes are consistent'() {
        given: 'an instance with ID'
        def note = new Note(title: 'Network configuration')
        note.id = new ObjectId()

        when: 'I compute the hash code'
        int h = note.hashCode()

        then: 'the hash code remains consistent'
        for (int j = 0; j < 500; j++) {
            note = new Note(title: 'Customer assessment')
            note.id = new ObjectId()
            h == note.hashCode()
        }
    }

    def 'Equal instances produce the same hash code'() {
        given: 'three instances with different properties but same IDs'
        def id = new ObjectId()
		def n1 = new Note(title: 'Network configuration')
        n1.id = id
        def n2 = new Note(title: 'Customer assessment')
        n2.id = id
        def n3 = new Note(title: 'Staff')
        n3.id = id

        expect:
        n1.hashCode() == n2.hashCode()
        n2.hashCode() == n3.hashCode()
    }

    def 'Different instances produce different hash codes'() {
        given: 'three instances with same properties but different IDs'
		def n1 = new Note(title: 'Network configuration')
        n1.id = new ObjectId()
        def n2 = new Note(title: 'Network configuration')
        n2.id = new ObjectId()
        def n3 = new Note(title: 'Network configuration')
        n3.id = new ObjectId()

        expect:
        n1.hashCode() != n2.hashCode()
        n2.hashCode() != n3.hashCode()
    }

    def 'Equals is null-safe'() {
        given: 'a note'
        def group = new RoleGroup()

        expect:
        null != group
        group != null
        //noinspection ChangeToOperator
        !group.equals(null)
    }

    def 'Instances of other types are always unequal'() {
        given: 'a note'
        def group = new RoleGroup()

        expect:
        group != 'foo'
        group != 45
        group != 45.3
        group != new Date()
    }

    def 'Instances are equal if they have the same name'() {
        given: 'three instances with the same name'
        def group1 = new RoleGroup(name: 'Group 1')
        group1.id = new ObjectId()
        def group2 = new RoleGroup(name: 'Group 1')
        group2.id = new ObjectId()
        def group3 = new RoleGroup(name: 'Group 1')
        group3.id = new ObjectId()

        expect: 'equals() is reflexive'
        group1 == group1
        group2 == group2
        group3 == group3

        and: 'all instances are equal and equals() is symmetric'
        group1 == group2
        group2 == group1
        group2 == group3
        group3 == group2

        and: 'equals() is transitive'
        group1 == group3
        group3 == group1
    }

    def 'Instances are unequal if they have the different names'() {
        given: 'three instances with different names'
        def id = new ObjectId()
        def group1 = new RoleGroup(name: 'Group 1')
        group1.id = id
        def group2 = new RoleGroup(name: 'Group 2')
        group2.id = id
        def group3 = new RoleGroup(name: 'Group 3')
        group3.id = id

        expect: 'equals() is reflexive'
        group1 == group1
        group2 == group2
        group3 == group3

        and: 'all instances are unequal and equals() is symmetric'
        group1 != group2
        group2 != group1
        group2 != group3
        group3 != group2

        and: 'equals() is transitive'
        group1 != group3
        group3 != group1
    }

    def 'Can compute hash code of an empty instance'() {
        given: 'an empty instance'
        def group = new RoleGroup()

        expect:
        3937i == group.hashCode()
    }

    def 'Hash codes are consistent'() {
        given: 'an instance with ID'
        def group = new RoleGroup(name: 'Administrators')

        when: 'the hash code is computed'
        int h = group.hashCode()

        then: 'the hash code remains consistent'
        for (int j = 0; j < 500; j++) {
            group = new RoleGroup(name: 'Administrators')
            h == group.hashCode()
        }
    }

    def 'Equal instances produce the same hash code'() {
        given: 'three instances with the same name'
        def group1 = new RoleGroup(name: 'Group 1')
        group1.id = new ObjectId()
        def group2 = new RoleGroup(name: 'Group 1')
        group2.id = new ObjectId()
        def group3 = new RoleGroup(name: 'Group 1')
        group3.id = new ObjectId()

        expect:
        group1.hashCode() == group2.hashCode()
        group2.hashCode() == group3.hashCode()
    }

    def 'Different instances produce different hash codes'() {
        given: 'three instances with different names'
        def id = new ObjectId()
        def group1 = new RoleGroup(name: 'Group 1')
        group1.id = id
        def group2 = new RoleGroup(name: 'Group 2')
        group2.id = id
        def group3 = new RoleGroup(name: 'Group 3')
        group3.id = id

        expect:
        group1.hashCode() != group2.hashCode()
        group2.hashCode() != group3.hashCode()
    }

    def 'Can convert to string'(String title) {
        given: 'an empty item'
        def note = new Note()

        when: 'I set the title'
        note.title = title

        then: 'I get a valid string representation'
        (title ?: '') == note.toString()

        where:
        title << [null, '', '   ', 'a', 'abc', '  foo  ', 'Network config']
    }

    def 'Title must not be blank and max 200 char long'(String t, boolean v) {
        given: 'a quite valid note'
        def n = new Note(content: 'My note')

        when: 'I set the title'
        n.title = t

        then: 'the instance is valid or not'
        v == n.validate()

        where:
        t       	|| v
        null    	|| false
        ''      	|| false
        'a'     	|| true
        'S'     	|| true
        'abc'   	|| true
        'a  x ' 	|| true
        ' name' 	|| true
		'a' * 200	|| true
		'a' * 201	|| false
    }

    def 'Content must not be null'(String c, boolean v) {
		given: 'a quite valid note'
        def n = new Note(title: 'Network configuration')

        when: 'I set the content'
        n.content = c

        then: 'the instance is valid or not'
        v == n.validate()

		where:
		c			|| v
		null		|| false
		''			|| true
		'a'     	|| true
        'S'     	|| true
        'abc'   	|| true
        'a  x ' 	|| true
        ' name' 	|| true
		'a' * 200	|| true
		'a' * 15000	|| true
    }
}
