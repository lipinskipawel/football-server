package com.github.lipinskipawel.routes;

import com.github.lipinskipawel.IntegrationSpec;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class RegisterResourceTest
    extends IntegrationSpec
    implements WithAssertions {

    @AfterEach
    public void afterEach() {
        truncateUsers();
    }

    @Test
    void register_username_when_username_not_taken() {
        var mark = authClient.register("mark");

        assertThat(mark).isPresent();
    }

    @Test
    void does_not_register_the_same_username_twice() {
        var mark = authClient.register("mark");
        var secondMark = authClient.register("mark");

        assertThat(mark).isPresent();
        assertThat(secondMark).isEmpty();
    }

    @Test
    void must_find_registered_username_by_token() {
        var mark = "mark";
        var markToken = authClient.register(mark);

        var username = authClient.findUsernameByToken(markToken.get());

        assertThat(username).satisfies(it -> {
            assertThat(it).isPresent();
            assertThat(it.get()).isEqualTo(mark);
        });
    }

    @Test
    void does_not_find_unregistered_username_by_token() {
        var notFound = authClient.findUsernameByToken("sdflsjdiflj88374");

        assertThat(notFound).isEmpty();
    }
}
