/*
 * Copyright 2008-2012 by Emeric Vernat
 *
 *     This file is part of Java Melody.
 *
 * Java Melody is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Java Melody is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Java Melody.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.lex.perf.spring;

import org.lex.perf.api.factory.IndexFactory;
import org.lex.perf.api.factory.IndexType;
import org.lex.perf.jdbc.JdbcWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import javax.sql.DataSource;

/**
 * Spring {@link FactoryBean} for wrapping datasources with monitoring proxy.
 *
 * @author David J. M. Karlsen (davidkarlsen at gmail.com), Emeric Vernat
 * @see AbstractFactoryBean
 * @see JdbcWrapper
 */
public class SpringDataSourceFactoryBean extends AbstractFactoryBean {
    private final static Logger LOGGER = LoggerFactory.getLogger(SpringDataSourceFactoryBean.class);
    private String targetName;
    private String name;

    // exemple :
    //	<bean id="wrappedDataSource" class="net.bull.javamelody.SpringDataSourceFactoryBean">
    //		<property name="targetName" value="targetDataSource" />
    //	</bean>
    //
    //	<bean id="targetDataSource" ...
    //	</bean>

    /**
     * Name of target bean to instrument.
     * This should implement {@linkplain DataSource}.
     *
     * @param targetName name of bean, not null.
     */
    public void setTargetName(String targetName) {
        assert targetName != null;
        this.targetName = targetName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataSource createInstance() {
        if (targetName == null) {
            throw new IllegalStateException("targetName must not be null");
        }
        final DataSource dataSource = (DataSource) getBeanFactory().getBean(targetName,
                DataSource.class);
        JdbcWrapper.registerSpringDataSource(targetName, dataSource);
        JdbcWrapper jdbcWrapper = name == null ? JdbcWrapper.SINGLETON : new JdbcWrapper(IndexFactory.registerIndexSeries(name));
        final DataSource result = jdbcWrapper.createDataSourceProxy(targetName,
                dataSource);
        LOGGER.debug("Spring target datasource wrapped: " + targetName);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getObjectType() {
        return DataSource.class;
    }

    public void setName(String name) {
        this.name = name;
    }
}
