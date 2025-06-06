package com.github.lipinskipawel.model;

import com.github.lipinskipawel.jooq.tables.records.UsersRecord;
import org.jooq.TableRecord;

import static com.github.lipinskipawel.model.Token.token;
import static com.github.lipinskipawel.model.Username.username;

final class UserMapper {

    static TableRecord toRecord(User user) {
        return new UsersRecord(
            user.id(),
            user.username().username(),
            user.token().token(),
            user.state().name(),
            user.createdDate(),
            user.updatedDate()
        );
    }

    static User fromRecord(UsersRecord record) {
        return User.Builder.createdUser()
            .id(record.getId())
            .username(username(record.getUsername()))
            .token(token(record.getToken()))
            .createdDate(record.getCreatedDate())
            .updatedDate(record.getUpdatedDate())
            .build();
    }
}
