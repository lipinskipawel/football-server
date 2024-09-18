package com.github.lipinskipawel.db;

import javax.sql.DataSource;
import java.util.Optional;

import static com.github.lipinskipawel.db.UserMapper.toRecord;
import static com.github.lipinskipawel.jooq.Tables.USERS;

public final class UserRepository extends AbstractRepository {

    public UserRepository(DataSource dataSource) {
        super(dataSource);
    }

    public void truncate() {
        db.truncate(USERS).execute();
    }

    public int save(User user) {
        final var record = toRecord(user);
        return db.executeInsert(record);
    }

    public Optional<User> findByToken(String token) {
        return db.selectFrom(USERS)
            .where(USERS.TOKEN.eq(token)
                .and(USERS.TERMINATED.isNull()))
            .fetch()
            .map(UserMapper::fromRecord)
            .stream()
            .findAny();
    }
}
