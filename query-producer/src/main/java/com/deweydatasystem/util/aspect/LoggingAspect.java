package com.deweydatasystem.util.aspect;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.marker.Markers;
import com.deweydatasystem.utils.ExcludeFromJacocoGeneratedReport;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@ExcludeFromJacocoGeneratedReport
public class LoggingAspect {

    private static final String MESSAGE_JSON_KEY = "messageJson";

    @Getter
    @ToString
    @EqualsAndHashCode
    private static class MethodExecutionTime {

        private String className;

        private String methodName;

        private Long executionTime;

        private String errorMessage;

        private String classAndMethod;

        public MethodExecutionTime(
                String fullyQualifiedClassName,
                String methodName,
                Long executionTime
        ) {
            this.className = fullyQualifiedClassName;
            this.methodName = methodName;
            this.executionTime = executionTime;

            String[] classNameArray = fullyQualifiedClassName.split("\\.");
            if (classNameArray.length > 0) {
                String nonFullyQualifiedClassName = classNameArray[classNameArray.length - 1];
                this.classAndMethod = String.format("%s#%s", nonFullyQualifiedClassName, methodName);
            }
        }

        public MethodExecutionTime(
                String fullyQualifiedClassName,
                String methodName,
                Long executionTime,
                String errorMessage
        ) {
            this(fullyQualifiedClassName, methodName, executionTime);
            this.errorMessage = errorMessage;
        }

    }

    /**
     * An AOP Aspect that wraps around a {@link ProceedingJoinPoint} and calculates the {@link ProceedingJoinPoint}'s
     * execution time in milliseconds and logs the execution time along with metadata like the {@link ProceedingJoinPoint}'s
     * class name and method name.
     *
     * @param point {@link ProceedingJoinPoint}
     * @return The {@link Object} that the {@link ProceedingJoinPoint} returns after being executed.
     * @throws Throwable The {@link Throwable} that resulted from the {@link ProceedingJoinPoint}'s execution, if any.
     */
    @Around("@annotation(com.deweydatasystem.aspect.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();

        try {
            Object result = point.proceed();

            long executionTime = this.getExecutionTime(startTime);

            MethodExecutionTime methodExecutionTime = new MethodExecutionTime(
                    point.getSignature().getDeclaringTypeName(),
                    point.getSignature().getName(),
                    executionTime
            );

            log.info(
                    Markers.append(MESSAGE_JSON_KEY, methodExecutionTime),
                    methodExecutionTime.toString()
            );

            return result;
        } catch (Throwable throwable) {
            long executionTime = this.getExecutionTime(startTime);

            MethodExecutionTime methodExecutionTime = new MethodExecutionTime(
                    point.getSignature().getDeclaringTypeName(),
                    point.getSignature().getName(),
                    executionTime,
                    throwable.getMessage()
            );

            log.error(
                    Markers.append(MESSAGE_JSON_KEY, methodExecutionTime),
                    methodExecutionTime.toString(),
                    throwable
            );

            throw throwable;
        }
    }

    /**
     * A convenience method to calculate the difference in milliseconds between the start time and the current time.
     *
     * @param startTime The start time in milliseconds.
     * @return The milliseconds between the start time and now.
     */
    private long getExecutionTime(long startTime) {
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

}
