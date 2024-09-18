package com.github.lipinskipawel.db;

import com.github.lipinskipawel.jooq.tables.records.UsersRecord;
import org.jooq.TableRecord;

import static java.util.Optional.ofNullable;

final class UserMapper {

    static TableRecord toRecord(User user) {
        return new UsersRecord(
            user.id(),
            user.username(),
            user.token(),
            user.created(),
            user.terminated().orElse(null)
        );
    }

    static User fromRecord(UsersRecord record) {
        return User.Builder.userBuilder()
            .id(record.getId())
            .username(record.getUsername())
            .token(record.getToken())
            .created(record.getCreated())
            .terminated(ofNullable(record.getTerminated()))
            .build();
    }
}
