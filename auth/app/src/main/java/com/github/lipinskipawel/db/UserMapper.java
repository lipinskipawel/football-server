package com.github.lipinskipawel.db;

import com.github.lipinskipawel.jooq.tables.records.UsersRecord;
import org.jooq.TableRecord;

final class UserMapper {

    static TableRecord toRecord(User user) {
        return new UsersRecord(
            user.id(),
            user.username(),
            user.token(),
            user.state().name(),
            user.createdDate(),
            user.updatedDate()
        );
    }

    static User fromRecord(UsersRecord record) {
        return User.Builder.createdUser()
            .id(record.getId())
            .username(record.getUsername())
            .token(record.getToken())
            .createdDate(record.getCreatedDate())
            .updatedDate(record.getUpdatedDate())
            .build();
    }
}
