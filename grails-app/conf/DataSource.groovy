/*
 * DataSource.groovy
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


/* General data source settings */
dataSource {
    dbCreate = 'none'   // updated by database-migration plugin
    pooled = true
    driverClassName = 'com.mysql.jdbc.Driver'
}

/* Cache settings */
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
}

/* environment specific settings */
environments {

    /* development environment */
    development {
        dataSource {
            url = 'jdbc:mysql://localhost/springcrm?autoreconnect=true'
            username = 'projects'
            password = 'haluni21'
        }
    }

    /* test environment */
    test {
        dataSource {
            driverClassName = 'org.h2.Driver'
            dbCreate = 'create'
            url = 'jdbc:h2:mem:testDb'
            username = 'sa'
            password = ''
        }
    }

    /* production (deployment) environment */
    production {
        dataSource {
            url = 'jdbc:mysql://localhost/springcrm'
            username = 'springcrm'
            password = 'springcrm'
            properties {
                validationQuery = 'select 1'
                testWhileIdle = true
                timeBetweenEvictionRunsMillis = 60000
            }
        }
    }

    /* live enviroment on the AMC World server */
    live {
        dataSource {
            url = 'jdbc:mysql://db.amc-world.home/springcrm?autoreconnect=true'
            username = 'projects'
            password = 'haluni21'
            properties {
                validationQuery = 'select 1'
                testWhileIdle = true
                timeBetweenEvictionRunsMillis = 60000
            }
        }
    }

    /* standalone environment for demonstration purposes */
    standalone {
        dataSource {
            driverClassName = 'org.h2.Driver'
            url = "jdbc:h2:file:${userHome}/.${appName}/database/springcrm"
            username = 'springcrm'
            password = ''
            properties {
                validationQuery = 'select 1'
                testWhileIdle = true
                timeBetweenEvictionRunsMillis = 60000
            }
        }
    }
}
