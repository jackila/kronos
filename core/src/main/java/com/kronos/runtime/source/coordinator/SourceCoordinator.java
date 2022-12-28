package com.kronos.runtime.source.coordinator;

import com.kronos.api.connector.source.ReaderInfo;
import com.kronos.api.connector.source.SourceEvent;
import com.kronos.api.connector.source.SplitEnumerator;
import com.kronos.jobgraph.physic.operator.source.Source;
import com.kronos.runtime.operators.coordination.OperatorEvent;
import com.kronos.runtime.source.even.ReaderRegistrationEvent;
import com.kronos.runtime.source.even.RequestSplitEvent;
import com.kronos.runtime.source.even.SourceEventWrapper;
import com.kronos.utils.ThrowingRunnable;
import org.kronos.utils.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * implement for source
 * provides an event loop style thread model
 * maintains SplitEnumeratorContext and share with the enumerator
 *
 * set up by the source operator and calls corresponding method of the SplitEnumerator to take actions
 *
 * @Author: jackila
 * @Date: 11:23 2022-10-15
 */
public class SourceCoordinator implements OperatorCoordinator{
    private static final Logger LOG = LoggerFactory.getLogger(SourceCoordinator.class);

    /** The name of the operator this SourceCoordinator is associated with. */
    private final String operatorName;
    /** A single-thread executor to handle all the changes to the coordinator. */
    private final ExecutorService coordinatorExecutor;
    /** The Source that is associated with this SourceCoordinator. */
    private final Source source;

    /** The context containing the states of the coordinator. */
    private final SourceCoordinatorContext context;
    /**
     * The split enumerator created from the associated Source. This one is created either during
     * resetting the coordinator to a checkpoint, or when the coordinator is started.
     */
    private SplitEnumerator enumerator;
    /** A flag marking whether the coordinator has started. */
    private boolean started;

    public SourceCoordinator(
            String operatorName,
            ExecutorService coordinatorExecutor,
            Source source,
            SourceCoordinatorContext context) {
        this.operatorName = operatorName;
        this.coordinatorExecutor = coordinatorExecutor;
        this.source = source;
        this.context = context;
    }

    @Override
    public void start() throws Exception {
        LOG.info("Starting split enumerator for source {}.", operatorName);
        // we mark this as started first, so that we can later distinguish the cases where
        // 'start()' wasn't called and where 'start()' failed.
        started = true;

        // there are two ways the coordinator can get created:
        //  (1) Source.restoreEnumerator(), in which case the 'resetToCheckpoint()' method creates
        // it
        //  (2) Source.createEnumerator, in which case it has not been created, yet, and we create
        // it here
        if (enumerator == null) {
            enumerator = source.createEnumerator(context);
        }
        runInEventLoop(() -> enumerator.start(), "starting the SplitEnumerator.");
    }

    @Override
    public void close() throws Exception {
        LOG.info("Closing SourceCoordinator for source {}.", operatorName);
        try {
            if (started) {
                context.close();
                if (enumerator != null) {
                    enumerator.close();
                }
            }
        } finally {
            coordinatorExecutor.shutdownNow();
            // We do not expect this to actually block for long. At this point, there should
            // be very few task running in the executor, if any.
            coordinatorExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        }
        LOG.info("Source coordinator for source {} closed.", operatorName);
    }

    @Override
    public void handleEventFromOperator(int subtask,
                                        OperatorEvent event) throws Exception {

        runInEventLoop(
                () -> {
                    if (event instanceof RequestSplitEvent) {
                        LOG.info(
                                "Source {} received split request from parallel task {}",
                                operatorName,
                                subtask);
                        enumerator.handleSplitRequest(
                                subtask);
                    } else if (event instanceof SourceEventWrapper) {
                        final SourceEvent sourceEvent =
                                ((SourceEventWrapper) event).getSourceEvent();
                        LOG.debug(
                                "Source {} received custom event from parallel task {}: {}",
                                operatorName,
                                subtask,
                                sourceEvent);
                        enumerator.handleSourceEvent(subtask, sourceEvent);
                    } else if (event instanceof ReaderRegistrationEvent) {
                        final ReaderRegistrationEvent registrationEvent =
                                (ReaderRegistrationEvent) event;
                        LOG.info(
                                "Source {} registering reader for parallel task {} @ {}",
                                operatorName,
                                subtask
                                );
                        handleReaderRegistrationEvent(registrationEvent);
                    } else {
                        throw new RuntimeException("Unrecognized Operator Event: " + event);
                    }
                },
                "handling operator event %s from subtask %d",
                event,
                subtask);
    }

    private void runInEventLoop(
            final ThrowingRunnable<Throwable> action,
            final String actionName,
            final Object... actionNameFormatParameters) {

        ensureStarted();

        // we may end up here even for a non-started enumerator, in case the instantiation
        // failed, and we get the 'subtaskFailed()' notification during the failover.
        // we need to ignore those.
        if (enumerator == null) {
            return;
        }

        coordinatorExecutor.execute(
                () -> {
                    try {
                        action.run();
                    } catch (Throwable t) {
                        // if we have a JVM critical error, promote it immediately, there is a good
                        // chance the
                        // logging or job failing will not succeed any more
                        ExceptionUtils.rethrowIfFatalErrorOrOOM(t);

                        final String actionString =
                                String.format(actionName, actionNameFormatParameters);
                        LOG.error(
                                "Uncaught exception in the SplitEnumerator for Source {} while {}. Triggering job failover.",
                                operatorName,
                                actionString,
                                t);
                        //context.failJob(t);
                    }
                });
    }

    @Override
    public void subtaskReady(int subtask,
                             SubtaskGateway gateway) {

        context.subtaskReady(gateway);
    }

    private void ensureStarted() {
        if (!started) {
            throw new IllegalStateException("The coordinator has not started yet.");
        }
    }

    private void handleReaderRegistrationEvent(ReaderRegistrationEvent event) {
        context.registerSourceReader(new ReaderInfo(event.subtaskId()));
        enumerator.addReader(event.subtaskId());
    }

}
