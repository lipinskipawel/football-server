package com.github.lipinskipawel.extension;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * This class must be used as a {@link org.junit.jupiter.api.extension.ExtendWith} extension provided on top of the
 * {@link Application} annotation.
 * This class is used to resolve test method parameter which is {@link AuthModuleFacade}.
 */
public final class AuthModuleResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == AuthModuleFacade.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return extensionContext
                .getStore(ExtensionContext.Namespace.create(FootballServerExtension.class))
                .get("register");
    }
}
