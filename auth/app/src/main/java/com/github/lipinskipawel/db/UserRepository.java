package com.github.lipinskipawel.db;

import java.util.Optional;

import static com.github.lipinskipawel.db.UserMapper.toRecord;
import static com.github.lipinskipawel.jooq.Tables.USERS;

public final class UserRepository extends AbstractRepository {

    public int save(User user) {
        return toRecord(user).insert();
    }

    public Optional<User> findUser(String username) {
        return db.selectFrom(USERS)
            .where(USERS.USERNAME.eq(username)
                .and(USERS.TERMINATED.isNull()))
            .fetch()
            .map(UserMapper::fromRecord)
            .stream()
            .findAny();
    }
}
