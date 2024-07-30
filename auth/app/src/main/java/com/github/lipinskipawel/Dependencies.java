package com.github.lipinskipawel;

import com.github.lipinskipawel.register.AuthRegister;
import com.github.lipinskipawel.register.Register;
import com.github.lipinskipawel.register.TokenGenerator;

public final class Dependencies {

    public final AuthRegister authRegister;

    public Dependencies() {
        this.authRegister = new AuthRegister(new Register(new TokenGenerator()));
    }
}
