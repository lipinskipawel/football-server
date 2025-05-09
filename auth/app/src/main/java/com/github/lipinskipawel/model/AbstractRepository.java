package com.github.lipinskipawel.model;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import javax.sql.DataSource;

import static org.jooq.SQLDialect.POSTGRES;

abstract class AbstractRepository {
    protected final DSLContext db;

    AbstractRepository(DataSource dataSource) {
        db = DSL.using(dataSource, POSTGRES);
    }
}
