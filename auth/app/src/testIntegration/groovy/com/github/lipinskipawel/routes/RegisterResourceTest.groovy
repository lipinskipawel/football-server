package com.github.lipinskipawel.routes

import com.github.lipinskipawel.IntegrationSpec

class RegisterResourceTest extends IntegrationSpec {

    def setup() {
        truncateUsers()
    }

    def "register username when username is not taken"() {
        when:
        def mark = authClient.register("mark")

        then:
        mark.isPresent()
    }

    def "do not register the same username twice"() {
        given:
        def mark = authClient.register("mark")

        when:
        def secondMark = authClient.register("mark")

        then:
        mark.isPresent()
        secondMark.isEmpty()
    }

    def "must find registered username by token"() {
        given:
        def mark = "mark"
        def markToken = authClient.register(mark)

        when:
        def username = authClient.findUsernameByToken(markToken.get())

        then:
        username.with { it ->
            it.isPresent()
            it.get() == mark
        }
    }

    def "must find registered username by token"() {
        given:
        def mark = "mark"
        def markToken = authClient.register(mark)

        when:
        def username = authClient.findUsernameByToken(markToken.get())

        then:
        username.with { it ->
            it.isPresent()
            it.get() == mark
        }
    }
}
