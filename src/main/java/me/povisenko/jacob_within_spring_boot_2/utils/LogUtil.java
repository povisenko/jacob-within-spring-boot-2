package me.povisenko.jacob_within_spring_boot_2.utils;

import org.slf4j.MDC;
import reactor.core.publisher.Signal;

import java.util.function.Consumer;

public abstract class LogUtil {

    /**
     * Helper wrapper which you can use combining with Flux/Mono#doOnEach method which allows you firstly log a message by a given log method
     *
     * @param logMethod  function which will be executed for logging
     * @param logMessage the log message
     * @return Consumer of a signal
     */
    public static <T> Consumer<Signal<T>> logNext(Consumer<String> logMethod, String logMessage) {

        return signal -> {
            initMdc(signal);

            if (!signal.isOnNext()) return;

            logMethod.accept(logMessage);
        };
    }

    /**
     * Helper wrapper which you can use combining with Flux/Mono#doOnEach method which allows you to don't worry about trace variable
     *
     * @param logMethod logging function which will be executed if signal represents an onNext event
     * @return Consumer of a signal
     */
    public static <T> Consumer<Signal<T>> logNext(Consumer<T> logMethod) {

        return signal -> {
            initMdc(signal);

            if (!signal.isOnNext()) return;

            logMethod.accept(signal.get());
        };
    }

    /**
     * Helper wrapper which you can use combining with Flux/Mono#doOnEach method which allows you to don't worry about trace variable
     *
     * @param clazz   exception class which will be mapped
     * @param onError function which will be executed if signal represents an onError event
     * @param <E>     exception type
     * @return Consumer of a signal
     */
    public static <E extends Throwable> Consumer<Signal> doOnError(Class<E> clazz, Consumer<Throwable> onError) {

        return signal -> {
            initMdc(signal);

            if (!signal.isOnError()) return;

            if (clazz.isInstance(signal.getThrowable())) onError.accept(signal.getThrowable());

        };
    }

    private static void initMdc(Signal signal) {

        final String trace = signal.getContext()
                                   .getOrDefault("trace", "NO_TRACE");
        MDC.putCloseable("trace", trace);

    }
}