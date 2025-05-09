package com.github.lipinskipawel;

import com.github.lipinskipawel.model.UserRepository;
import com.github.lipinskipawel.register.AuthRegister;
import com.github.lipinskipawel.register.Register;
import com.github.lipinskipawel.register.TokenGenerator;

import javax.sql.DataSource;

public final class Dependencies {
    public final UserRepository userRepository;

    public final AuthRegister authRegister;

    public Dependencies(DataSource dataSource) {
        this.userRepository = new UserRepository(dataSource);
        this.authRegister = new AuthRegister(new Register(new TokenGenerator()), userRepository);
    }
}
