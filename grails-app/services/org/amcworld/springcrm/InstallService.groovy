/*
 * InstallService.groovy
 *
 * Copyright (c) 2011-2013, Daniel Ellermann
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

import groovy.sql.Sql
import java.sql.Connection
import javax.servlet.ServletContext
import org.codehaus.groovy.grails.web.context.ServletContextHolder as SCH


/**
 * The class {@code InstallService} implements various methods to handle
 * installer issues, such as enabling or disabling the installer for security
 * reasons or obtaining the available base data packages.
 *
 * @author  Daniel Ellermann
 * @version 1.4
 */
class InstallService {

    //-- Constants ------------------------------

    /**
     * The directory containing the packages with base data which are loaded
     * into the database during installation.
     */
    protected static final String BASE_PACKAGE_DIR = '/WEB-INF/data/install'


    //-- Instance variables ---------------------

    def grailsApplication
    final ServletContext servletContext = SCH.servletContext


    //-- Public methods -------------------------

    /**
     * Applies all difference sets from the current version to the given
     * database version.
     *
     * @param connection    the SQL connection of the database where to apply
     *                      the difference sets
     * @param upToVersion   the number of the last difference set to include
     * @since               1.4
     */
    void applyAllDiffSets(Connection connection, int upToVersion) {
        ConfigHolder configHolder = ConfigHolder.instance
        String lang = (configHolder['baseDataLocale'] as String) ?: 'de-DE'
        int fromVersion = (configHolder['dbVersion'] as Integer) ?: 0i
        for (int i = fromVersion + 1; i <= upToVersion; i++) {
            applyDiffSet connection, i, lang
        }
        configHolder.setConfig 'dbVersion', upToVersion.toString()
    }

    /**
     * Applies the difference set with the given version and language.
     *
     * @param connection    the SQL connection of the database where to apply
     *                      the difference set
     * @param version       the given number of the difference set
     * @param lang          the given language of the difference set in the
     *                      form {@code lang-COUNTRY}; if {@code null} the
     *                      current base data language is used
     * @since               1.4
     */
    void applyDiffSet(Connection connection, int version, String lang = null) {
        if (!lang) {
            lang = (ConfigHolder.instance['baseDataLocale'] as String) ?: 'de-DE'
        }
        executeSqlFile connection, loadDiffSet(version, lang)
    }

    /**
     * Disables the installer by deleting the installer enable file
     * {@code ENABLE_INSTALLER} in the installer directory as specified in the
     * configuration file in key {@code springcrm.dir.installer}.
     */
    void disableInstaller() {
        enableFile.delete()
    }

    /**
     * Enables the installer by creating the installer enable file
     * {@code ENABLE_INSTALLER} in the installer directory as specified in the
     * configuration file in key {@code springcrm.dir.installer}.
     */
    void enableInstaller() {
        log.info enableFile
        enableFile.createNewFile()
    }

    /**
     * Gets a list of package keys containing base data for installation.  The
     * packages must be located in directory specified by
     * {@link #BASE_PACKAGE_DIR} and named {@code base-data-<key>.sql},
     * where {@code <key>} represents the package key.
     *
     * @return  the package keys as defined above
     */
    List<String> getBaseDataPackages() {
        def files = []
        servletContext.getResourcePaths(BASE_PACKAGE_DIR).each {
                def m = (it =~ /^.*\/base-data-([-\w]+)\.sql$/)
                if (m.matches()) {
                    files.add(m[0][1])
                }
            }
        files
    }

    /**
     * Installs the base data package with the given key in the database with
     * the stated connection.
     *
     * @param connection    the SQL connection where to install the base data
     *                      package
     * @param key           the given package key
     * @see                 #loadPackage(String)
     * @since               1.4
     */
    void installBaseDataPackage(Connection connection, String key) {
        executeSqlFile connection, loadBaseDataPackage(key)
    }

    /**
     * Checks whether or not the installer is enabled.  The installer is enable
     * if there is a file {@code ENABLE_INSTALLER} in the installer directory
     * as specified in the configuration file in key
     * {@code springcrm.dir.installer}.
     *
     * @return  {@code true} if the installer is enabled; {@code false}
     *          otherwise
     */
    boolean isInstallerDisabled() {
        !enableFile.exists()
    }

    /**
     * Loads the base data package with the given key.
     *
     * @param key   the given package key
     * @return      an input stream to read the package; {@code null} if no
     *              package with the given key exists
     */
    InputStream loadBaseDataPackage(String key) {
        servletContext.getResourceAsStream "${BASE_PACKAGE_DIR}/base-data-${key}.sql"
    }

    /**
     * Loads the difference set for the given version and language.  The method
     * first looks for a difference set with the given language.  If not found,
     * it falls back to a general difference set for the given version.
     *
     * @param version   the given version of the difference set
     * @param lang      the given language of the difference set in the form
     *                  {@code lang-COUNTRY}
     * @return          an input stream to the difference set; {@code null} if
     *                  no such difference set exists
     * @since           1.4
     */
    InputStream loadDiffSet(int version, String lang) {
        String name1 = "${BASE_PACKAGE_DIR}/db-diff-set-${version}-${lang}.sql"
        InputStream is = servletContext.getResourceAsStream(name1)
        if (is == null) {
            String name2 = "${BASE_PACKAGE_DIR}/db-diff-set-${version}.sql"
            is = servletContext.getResourceAsStream(name2)
            if (is == null && log.errorEnabled) {
                log.error "Can find neither diff set ${name1} nor ${name2}."
            }
        }
        is
    }


    //-- Non-public methods ---------------------

    /**
     * Executes all SQL commands in the given input stream.
     *
     * @param connection    the SQL connection where the SQL commands should be
     *                      executed
     * @param is            an input stream containing the SQL commands to
     *                      execute
     * @since               1.4
     */
    protected void executeSqlFile(Connection connection, InputStream is) {
        Sql sql = new Sql(connection)
        sql.withTransaction {
            is.newReader('utf-8').eachLine {
                if (!(it =~ /^\s*$/) && !(it =~ /^\s*--/)) {
                    sql.execute it
                }
            }
        }
    }

    /**
     * Gets the object representing the installer enable file.  This file is
     * stored in the installer directory as specified in the configuration
     * file in key {@code springcrm.dir.installer}.
     *
     * @return  the installer enable file
     */
    protected File getEnableFile() {
        def dir = new File(grailsApplication.config.springcrm.dir.installer)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        new File(dir, 'ENABLE_INSTALLER')
    }
}
