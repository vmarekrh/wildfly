/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.host.controller;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DOMAIN_MODEL;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.FAILURE_DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HOST;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.IGNORED_RESOURCES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.IGNORED_RESOURCE_TYPE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OPERATION_HEADERS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OUTCOME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RESULT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUCCESS;
import static org.jboss.as.host.controller.logging.HostControllerLogger.ROOT_LOGGER;

import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLHandshakeException;
import javax.security.sasl.SaslException;

import org.jboss.as.controller.Cancellable;
import org.jboss.as.controller.HashUtil;
import org.jboss.as.controller.ModelController;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ProxyController;
import org.jboss.as.controller.RunningMode;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.Operation;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.controller.client.OperationMessageHandler;
import org.jboss.as.controller.client.OperationResponse;
import org.jboss.as.controller.client.impl.ExistingChannelModelControllerClient;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.extension.ExtensionRegistry;
import org.jboss.as.controller.logging.ControllerLogger;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.controller.remote.ResponseAttachmentInputStreamSupport;
import org.jboss.as.controller.remote.TransactionalProtocolClient;
import org.jboss.as.controller.remote.TransactionalProtocolHandlers;
import org.jboss.as.controller.remote.TransactionalProtocolOperationHandler;
import org.jboss.as.domain.controller.DomainController;
import org.jboss.as.domain.controller.LocalHostControllerInfo;
import org.jboss.as.domain.controller.SlaveRegistrationException;
import org.jboss.as.domain.controller.operations.FetchMissingConfigurationHandler;
import org.jboss.as.domain.controller.operations.SyncDomainModelOperationHandler;
import org.jboss.as.domain.controller.operations.SyncServerGroupOperationHandler;
import org.jboss.as.domain.controller.operations.coordination.DomainControllerLockIdUtils;
import org.jboss.as.domain.controller.operations.deployment.SyncModelParameters;
import org.jboss.as.domain.management.SecurityRealm;
import org.jboss.as.host.controller.discovery.DiscoveryOption;
import org.jboss.as.host.controller.discovery.RemoteDomainControllerConnectionConfiguration;
import org.jboss.as.host.controller.ignored.IgnoredDomainResourceRegistry;
import org.jboss.as.host.controller.logging.HostControllerLogger;
import org.jboss.as.host.controller.mgmt.DomainControllerProtocol;
import org.jboss.as.host.controller.mgmt.DomainRemoteFileRequestAndHandler;
import org.jboss.as.host.controller.mgmt.HostControllerRegistrationHandler;
import org.jboss.as.host.controller.mgmt.HostInfo;
import org.jboss.as.protocol.ProtocolConnectionConfiguration;
import org.jboss.as.protocol.StreamUtils;
import org.jboss.as.protocol.mgmt.AbstractManagementRequest;
import org.jboss.as.protocol.mgmt.ActiveOperation;
import org.jboss.as.protocol.mgmt.FlushableDataOutput;
import org.jboss.as.protocol.mgmt.ManagementChannelHandler;
import org.jboss.as.protocol.mgmt.ManagementRequestContext;
import org.jboss.as.remoting.management.ManagementRemotingServices;
import org.jboss.as.repository.ContentReference;
import org.jboss.as.repository.ContentRepository;
import org.jboss.as.repository.HostFileRepository;
import org.jboss.as.repository.RemoteFileRequestAndHandler.CannotCreateLocalDirectoryException;
import org.jboss.as.repository.RemoteFileRequestAndHandler.DidNotReadEntireFileException;
import org.jboss.as.version.ProductConfig;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.jboss.remoting3.Connection;
import org.jboss.remoting3.Endpoint;
import org.jboss.remoting3.RemotingOptions;
import org.jboss.threads.AsyncFuture;
import org.jboss.threads.AsyncFutureTask;
import org.wildfly.security.manager.WildFlySecurityManager;
import org.xnio.OptionMap;
import org.xnio.Options;

/**
 * Establishes the connection from a slave {@link org.jboss.as.domain.controller.DomainController} to the master
 * {@link org.jboss.as.domain.controller.DomainController}
 *
 * @author Kabir Khan
 */
public class RemoteDomainConnectionService implements MasterDomainControllerClient, Service<MasterDomainControllerClient> {

    public static final String DOMAIN_CONNECTION_ID = "domain-connection-id";

    private static final int CONNECTION_TIMEOUT_DEFAULT = 30000;
    private static final String CONNECTION_TIMEOUT_PROPERTY = "jboss.host.domain.connection.timeout";
    private static final int CONNECTION_TIMEOUT = getSystemProperty(CONNECTION_TIMEOUT_PROPERTY, CONNECTION_TIMEOUT_DEFAULT);

    //private static final ModelNode APPLY_EXTENSIONS = new ModelNode();
    private static final ModelNode APPLY_DOMAIN_MODEL = new ModelNode();
    private static final Operation GRAB_DOMAIN_RESOURCE;

    static {
//        APPLY_EXTENSIONS.get(OP).set(ApplyExtensionsHandler.OPERATION_NAME);
//        APPLY_EXTENSIONS.get(OPERATION_HEADERS, "execute-for-coordinator").set(true);
//        APPLY_EXTENSIONS.get(OP_ADDR).setEmptyList();
//        APPLY_EXTENSIONS.protect();

        APPLY_DOMAIN_MODEL.get(OP).set(ModelDescriptionConstants.APPLY_REMOTE_DOMAIN_MODEL);
        //FIXME this makes the op work after boot (i.e. slave connects to restarted master), but does not make the slave resync the servers
        APPLY_DOMAIN_MODEL.get(OPERATION_HEADERS, "execute-for-coordinator").set(true);
        APPLY_DOMAIN_MODEL.get(OP_ADDR).setEmptyList();
        APPLY_DOMAIN_MODEL.protect();

        ModelNode mn  = new ModelNode();
        mn.get(OP).set("grab-domain-resource"); // This is actually not used anywhere
        mn.get(OP_ADDR).setEmptyList();
        mn.protect();
        GRAB_DOMAIN_RESOURCE = OperationBuilder.create(mn).build();
    }

    private final ExtensionRegistry extensionRegistry;
    private final ModelController controller;
    private final ProductConfig productConfig;
    private final LocalHostControllerInfo localHostInfo;
    private final RemoteFileRepository remoteFileRepository;
    private final ContentRepository contentRepository;
    private final IgnoredDomainResourceRegistry ignoredDomainResourceRegistry;
    private final HostControllerRegistrationHandler.OperationExecutor operationExecutor;
    private final DomainController domainController;
    private final HostControllerEnvironment hostControllerEnvironment;
    private final RunningMode runningMode;
    private final File tempDir;
    private final Map<String, ProxyController> serverProxies;

    /** Used to invoke ModelController ops on the master */
    private volatile ModelControllerClient masterProxy;
    private volatile TransactionalProtocolClient txMasterProxy;

    private final FutureClient futureClient = new FutureClient();
    private final InjectedValue<Endpoint> endpointInjector = new InjectedValue<Endpoint>();
    private final InjectedValue<SecurityRealm> securityRealmInjector = new InjectedValue<SecurityRealm>();
    private final InjectedValue<ServerInventory> serverInventoryInjector = new InjectedValue<ServerInventory>();
    private final InjectedValue<ScheduledExecutorService> scheduledExecutorInjector = new InjectedValue<>();
    private final ExecutorService executor;
    private final AtomicBoolean domainModelComplete;

    private ManagementChannelHandler handler;
    private volatile ResponseAttachmentInputStreamSupport responseAttachmentSupport;
    private volatile RemoteDomainConnection connection;

    private RemoteDomainConnectionService(final ModelController controller,
                                          final ExtensionRegistry extensionRegistry,
                                          final LocalHostControllerInfo localHostControllerInfo,
                                          final RemoteFileRepository remoteFileRepository,
                                          final ContentRepository contentRepository,
                                          final IgnoredDomainResourceRegistry ignoredDomainResourceRegistry,
                                          final HostControllerRegistrationHandler.OperationExecutor operationExecutor,
                                          final DomainController domainController,
                                          final HostControllerEnvironment hostControllerEnvironment,
                                          final ExecutorService executor,
                                          final RunningMode runningMode,
                                          final Map<String, ProxyController> serverProxies,
                                          final AtomicBoolean domainModelComplete){
        this.controller = controller;
        this.extensionRegistry = extensionRegistry;
        this.productConfig = hostControllerEnvironment.getProductConfig();
        this.localHostInfo = localHostControllerInfo;
        this.remoteFileRepository = remoteFileRepository;
        this.contentRepository = contentRepository;
        remoteFileRepository.setRemoteFileRepositoryExecutor(remoteFileRepositoryExecutor);
        this.ignoredDomainResourceRegistry = ignoredDomainResourceRegistry;
        this.operationExecutor = operationExecutor;
        this.domainController = domainController;
        this.hostControllerEnvironment = hostControllerEnvironment;
        this.executor = executor;
        this.runningMode = runningMode;
        this.tempDir = hostControllerEnvironment.getDomainTempDir();
        this.serverProxies = serverProxies;
        this.domainModelComplete = domainModelComplete;
    }

    static Future<MasterDomainControllerClient> install(final ServiceTarget serviceTarget,
                                                        final ModelController controller,
                                                        final ExtensionRegistry extensionRegistry,
                                                        final LocalHostControllerInfo localHostControllerInfo,
                                                        final String securityRealm,
                                                        final RemoteFileRepository remoteFileRepository,
                                                        final ContentRepository contentRepository,
                                                        final IgnoredDomainResourceRegistry ignoredDomainResourceRegistry,
                                                        final HostControllerRegistrationHandler.OperationExecutor operationExecutor,
                                                        final DomainController domainController,
                                                        final HostControllerEnvironment hostControllerEnvironment,
                                                        final ExecutorService executor,
                                                        final RunningMode currentRunningMode,
                                                        final Map<String, ProxyController> serverProxies,
                                                        final AtomicBoolean domainModelComplete) {
        RemoteDomainConnectionService service = new RemoteDomainConnectionService(controller, extensionRegistry, localHostControllerInfo,
                remoteFileRepository, contentRepository,
                ignoredDomainResourceRegistry, operationExecutor, domainController,
                hostControllerEnvironment, executor, currentRunningMode, serverProxies, domainModelComplete);
        ServiceBuilder<MasterDomainControllerClient> builder = serviceTarget.addService(MasterDomainControllerClient.SERVICE_NAME, service)
                .addDependency(ManagementRemotingServices.MANAGEMENT_ENDPOINT, Endpoint.class, service.endpointInjector)
                .addDependency(ServerInventoryService.SERVICE_NAME, ServerInventory.class, service.serverInventoryInjector)
                .addDependency(HostControllerService.HC_SCHEDULED_EXECUTOR_SERVICE_NAME, ScheduledExecutorService.class, service.scheduledExecutorInjector)
                .setInitialMode(ServiceController.Mode.ACTIVE);

        if (securityRealm != null) {
            SecurityRealm.ServiceUtil.addDependency(builder, service.securityRealmInjector, securityRealm, false);
        }

        builder.install();
        return service.futureClient;
    }

    /** {@inheritDoc} */
    public synchronized void register() throws IOException {
        boolean connected = false;
        List<DiscoveryOption> discoveryOptions = localHostInfo.getRemoteDomainControllerDiscoveryOptions();
        // Loop through discovery options
        for (Iterator<DiscoveryOption> i = discoveryOptions.iterator(); i.hasNext(); ) {
           DiscoveryOption discoveryOption = i.next();
           final long timeout = CONNECTION_TIMEOUT;
           final long endTime = System.currentTimeMillis() + timeout;
           int retries = 0;
           URI masterURI = null;
           try {
               // Determine the remote DC host and port to use
               List<RemoteDomainControllerConnectionConfiguration> remoteDcConfigs = discoveryOption.discover();
               while (!connected) {
                   IOException ex = null;
                   for (RemoteDomainControllerConnectionConfiguration remoteDcConfig : remoteDcConfigs) {
                       try {
                           masterURI = new URI(remoteDcConfig.getProtocol(), null, remoteDcConfig.getHost(), remoteDcConfig.getPort(), null, null, null);
                           connection.setUri(masterURI);
                           connection.connect();
                           connected = true;
                           break;
                       } catch (IOException e) {
                           // If the cause is one of the irrecoverable ones, unwrap and throw it on
                           rethrowIrrecoverableConnectionFailures(e);

                           HostControllerLogger.ROOT_LOGGER.cannotConnect(masterURI, e);
                           // if we're using isCachedDC, just try once then allow the poll to run in the background.
                           // this will allow us to start up without having to wait for retries to be exhausted.
                           if (hostControllerEnvironment.isUseCachedDc()) {
                               throw e;
                           }
                           // Something else; we can retry if time remains
                           ex = e;
                       }
                   }
                   if (ex != null) {
                       if (System.currentTimeMillis() > endTime) {
                           throw HostControllerLogger.ROOT_LOGGER.connectionToMasterTimeout(ex, retries, timeout);
                       }

                       try {
                           ReconnectPolicy.CONNECT.wait(retries);
                           retries++;
                       } catch (InterruptedException ie) {
                           Thread.currentThread().interrupt();
                           throw HostControllerLogger.ROOT_LOGGER.connectionToMasterInterrupted();
                       }
                   }
               }

               HostControllerLogger.ROOT_LOGGER.connectedToMaster(masterURI);
               setupHandler();
               break;

           } catch (Exception e) {
               boolean moreOptions = i.hasNext();
               logConnectionException(masterURI, discoveryOption, moreOptions, e);
               if (!moreOptions) {
                   throw HostControllerLogger.ROOT_LOGGER.discoveryOptionsFailureUnableToConnect(e);
               }
           }
        }
    }

    /** {@inheritDoc} */
    public synchronized void unregister() {
        StreamUtils.safeClose(connection);
    }

    @Override
    public synchronized Cancellable pollForConnect() {
        final Future<Connection> future = connection.reconnect();
        setupHandler();
        return new Cancellable() {
            @Override
            public boolean cancel() {
                return future.cancel(true);
            }
        };
    }

    /** {@inheritDoc} */
    public synchronized HostFileRepository getRemoteFileRepository() {
        return remoteFileRepository;
    }

    @Override
    public ModelNode execute(ModelNode operation) throws IOException {
        return execute(operation, OperationMessageHandler.logging);
    }

    @Override
    public ModelNode execute(Operation operation) throws IOException {
        return masterProxy.execute(operation, OperationMessageHandler.logging);
    }

    @Override
    public ModelNode execute(ModelNode operation, OperationMessageHandler messageHandler) throws IOException {
        return masterProxy.execute(operation, messageHandler);
    }

    @Override
    public ModelNode execute(Operation operation, OperationMessageHandler messageHandler) throws IOException {
        return masterProxy.execute(operation, messageHandler);
    }

    @Override
    public AsyncFuture<ModelNode> executeAsync(ModelNode operation, OperationMessageHandler messageHandler) {
        return masterProxy.executeAsync(operation, messageHandler);
    }

    @Override
    public AsyncFuture<ModelNode> executeAsync(Operation operation, OperationMessageHandler messageHandler) {
        return masterProxy.executeAsync(operation, messageHandler);
    }

    @Override
    public OperationResponse executeOperation(Operation operation, OperationMessageHandler messageHandler) throws IOException {
        return masterProxy.executeOperation(operation, messageHandler);
    }

    @Override
    public AsyncFuture<OperationResponse> executeOperationAsync(Operation operation, OperationMessageHandler messageHandler) {
        return masterProxy.executeOperationAsync(operation, messageHandler);
    }

    @Override
    public void fetchAndSyncMissingConfiguration(final OperationContext context, final Resource original) throws OperationFailedException {
        final TransactionalProtocolClient client = txMasterProxy;
        context.addStep(new OperationStepHandler() {
            @Override
            public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {

                // Create the operation to get the required configuration from the master
                final ModelNode fetchContentOp = new ModelNode();
                fetchContentOp.get(OP).set(FetchMissingConfigurationHandler.OPERATION_NAME);
                fetchContentOp.get(OP_ADDR).setEmptyList();
                // This is based on the configured server-configs and required server-groups and socket-bindings
                final PathElement hostElement = PathElement.pathElement(HOST, localHostInfo.getLocalHostName());
                final Resource hostModel = context.readResourceFromRoot(PathAddress.pathAddress(hostElement));
                // Add the information about which parts of configuration are required and ignored
                IgnoredNonAffectedServerGroupsUtil.addServerGroupsToModel(hostModel, fetchContentOp);
                final ModelNode ignoredModel = ignoredDomainResourceRegistry.getIgnoredResourcesAsModel();
                if (ignoredModel.hasDefined(IGNORED_RESOURCE_TYPE)) {
                    fetchContentOp.get(IGNORED_RESOURCES).set(ignoredModel.require(IGNORED_RESOURCE_TYPE));
                }

                // Attach the operation id, in case it got executed through the master
                final Integer domainControllerLock = context.getAttachment(DomainControllerLockIdUtils.DOMAIN_CONTROLLER_LOCK_ID_ATTACHMENT);
                if (domainControllerLock != null) {
                    fetchContentOp.get(OPERATION_HEADERS, DomainControllerLockIdUtils.DOMAIN_CONTROLLER_LOCK_ID).set(domainControllerLock);
                }

                // execute the operation blocking
                final TransactionalProtocolClient.PreparedOperation<TransactionalProtocolClient.Operation> preparedOperation;
                try {
                     preparedOperation = TransactionalProtocolHandlers.executeBlocking(fetchContentOp, client);
                } catch (IOException e) {
                    throw new OperationFailedException(e);
                } catch (InterruptedException e) {
                    throw ControllerLogger.ROOT_LOGGER.operationCancelledAsynchronously();
                }

                // Process the prepared result, note: this won't include outcome yet
                final ModelNode result = preparedOperation.getPreparedResult();
                if (preparedOperation.isFailed()) {
                    final ModelNode prepared = preparedOperation.getPreparedResult();
                    if (prepared.hasDefined(FAILURE_DESCRIPTION)) {
                        throw new OperationFailedException(prepared.get(FAILURE_DESCRIPTION).asString());
                    } else {
                        throw new OperationFailedException(prepared);
                    }
                } else if (result.get(FAILURE_DESCRIPTION).isDefined()) {
                    preparedOperation.rollback();
                    throw new OperationFailedException(result.get(FAILURE_DESCRIPTION).asString());
                }

                final ModelNode syncOperation = new ModelNode();
                syncOperation.get(OP).set("calculate-diff-and-sync");
                syncOperation.get(OP_ADDR).setEmptyList();
                syncOperation.get(DOMAIN_MODEL).set(result.get(RESULT));

                // Execute the handler to synchronize the model
                SyncModelParameters parameters =
                        new SyncModelParameters(domainController, ignoredDomainResourceRegistry,
                                hostControllerEnvironment, extensionRegistry, operationExecutor, false, serverProxies,
                                remoteFileRepository, contentRepository);
                final SyncServerGroupOperationHandler handler =
                        new SyncServerGroupOperationHandler(localHostInfo.getLocalHostName(), original, parameters);
                context.addStep(syncOperation, handler, OperationContext.Stage.MODEL, true);
                // Complete the remote tx on the master depending on the outcome
                // This cannot be executed as another step
                // If this is not called the lock on the master will not be released and result in a deadlock
                context.completeStep(new OperationContext.ResultHandler() {
                    @Override
                    public void handleResult(OperationContext.ResultAction resultAction, OperationContext context, ModelNode operation) {
                        if (resultAction == OperationContext.ResultAction.KEEP) {
                            preparedOperation.commit();
                        } else {
                            preparedOperation.rollback();
                        }
                    }
                });
            }
        }, OperationContext.Stage.MODEL, true);

    }

    @Override
    public void close() throws IOException {
        throw HostControllerLogger.ROOT_LOGGER.closeShouldBeManagedByService();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void start(StartContext context) throws StartException {
        final RemoteDomainConnection connection;
        final ManagementChannelHandler handler;
        try {

            ScheduledExecutorService scheduledExecutorService = scheduledExecutorInjector.getValue();
            this.responseAttachmentSupport = new ResponseAttachmentInputStreamSupport(scheduledExecutorService);

            final OptionMap options = OptionMap.builder()
                    .set(RemotingOptions.HEARTBEAT_INTERVAL, 15000)
                    .set(Options.READ_TIMEOUT, 45000)
                    .getMap();

            // Gather the required information to connect to the remote DC
            // The URI will be set later when looping through discovery options when registering with
            // or reconnecting to the remote DC.
            final ProtocolConnectionConfiguration configuration = ProtocolConnectionConfiguration.create(endpointInjector.getValue(), options);

            final SecurityRealm realm = securityRealmInjector.getOptionalValue();
            // Create the remote domain channel strategy
            connection = new RemoteDomainConnection(localHostInfo.getLocalHostName(), configuration, realm,
                    localHostInfo.getRemoteDomainControllerUsername(),
                    localHostInfo.getRemoteDomainControllerDiscoveryOptions(), executor, scheduledExecutorService,
                    new RemoteDomainConnection.HostRegistrationCallback() {
                /**
                 * Calculates the metadata required when connecting to the master {@link org.jboss.as.domain.controller.DomainController}
                 * This value does not require locking during use, as {@link org.jboss.as.host.controller.mgmt.HostInfo createLocalHostHostInfo()} uses
                 * '/domain-controller', which is read-only, and '/core-service=ignored-resources/ignored-resource-type' which has handlers on
                 * add, remove and write-attribute that will place the host into reload-required.
                 * @return
                 */
                @Override
                public ModelNode createLocalHostInfo() {
                    return HostInfo.createLocalHostHostInfo(localHostInfo, productConfig, ignoredDomainResourceRegistry, ReadRootResourceHandler.grabDomainResource(operationExecutor).getChildren(HOST).iterator().next());
                }

                @Override
                public ModelNode resolveSubsystemVersions(ModelNode extensions) {
                    return resolveSubsystems(extensions.asList());
                }

                @Override
                public boolean applyDomainModel(final List<ModelNode> bootOperations) {
                    // Apply the model..
                    final HostInfo info = HostInfo.fromModelNode(createLocalHostInfo());
                    return applyRemoteDomainModel(bootOperations, info);
                }

                @Override
                public void registrationComplete(ManagementChannelHandler handler) {
                    RemoteDomainConnectionService.this.domainModelComplete.set(true);
                }
            }, runningMode);
            // Setup the management channel handler
            handler = connection.getChannelHandler();
            handler.getAttachments().attach(ManagementChannelHandler.TEMP_DIR, tempDir);
        } catch (Exception e) {
            throw new StartException(e);
        } finally {
            futureClient.setClient(this);
        }
        this.connection = connection;
        this.handler = handler;
    }

    /**
     * Resolve the subsystem versions.
     *
     * @param extensions the extensions to install
     * @return the subsystem versions
     */
    private ModelNode resolveSubsystems(final List<ModelNode> extensions) {

        HostControllerLogger.ROOT_LOGGER.debug("Applying extensions provided by master");
        final ModelNode result = operationExecutor.installSlaveExtensions(extensions);
        if (!SUCCESS.equals(result.get(OUTCOME).asString())) {
            throw HostControllerLogger.ROOT_LOGGER.failedToAddExtensions(result.get(FAILURE_DESCRIPTION));
        }
        final ModelNode subsystems = new ModelNode();
        for (final ModelNode extension : extensions) {
            extensionRegistry.recordSubsystemVersions(extension.asString(), subsystems);
        }
        return subsystems;
    }

    /**
     * Apply the remote domain model to the local host controller.
     *
     * @param bootOperations the result of the remote read-domain-model op
     * @return {@code true} if the model was applied successfully, {@code false} otherwise
     */
    private boolean applyRemoteDomainModel(final List<ModelNode> bootOperations, final HostInfo hostInfo) {
        try {
            HostControllerLogger.ROOT_LOGGER.debug("Applying domain level boot operations provided by master");
            SyncModelParameters parameters =
                    new SyncModelParameters(domainController, ignoredDomainResourceRegistry,
                            hostControllerEnvironment, extensionRegistry, operationExecutor, true, serverProxies, remoteFileRepository, contentRepository);
            final SyncDomainModelOperationHandler handler =
                    new SyncDomainModelOperationHandler(hostInfo, parameters);
            final ModelNode operation = APPLY_DOMAIN_MODEL.clone();
            operation.get(DOMAIN_MODEL).set(bootOperations);

            final ModelNode result = operationExecutor.execute(OperationBuilder.create(operation).build(), OperationMessageHandler.DISCARD, ModelController.OperationTransactionControl.COMMIT, handler);

            final String outcome = result.get(OUTCOME).asString();
            final boolean success = SUCCESS.equals(outcome);
            if (!success) {
                ModelNode failureDesc = result.hasDefined(FAILURE_DESCRIPTION) ? result.get(FAILURE_DESCRIPTION) : new ModelNode();
                HostControllerLogger.ROOT_LOGGER.failedToApplyDomainConfig(outcome, failureDesc);
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            HostControllerLogger.ROOT_LOGGER.failedToApplyDomainConfig(e);
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void stop(final StopContext context) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    StreamUtils.safeClose(connection);
                    responseAttachmentSupport.shutdown();
                } finally {
                    context.complete();
                }
            }
        };
        try {
            executor.execute(r);
        } catch (RejectedExecutionException e) {
            r.run();
        } finally {
            context.asynchronous();
        }
    }

    /** {@inheritDoc} */
    @Override
    public synchronized MasterDomainControllerClient getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    /**
     * Analyzes a failure thrown connecting to the master for causes that indicate
     * some problem not likely to be resolved by immediately retrying. If found,
     * throws an exception highlighting the underlying cause. If the cause is not
     * one of the ones understood by this method, the method returns normally.
     *
     * @throws org.jboss.as.domain.controller.SlaveRegistrationException if the remote HC rejected the request
     * @throws IllegalStateException for other failures understood by this method
     */
    static void rethrowIrrecoverableConnectionFailures(IOException e) throws SlaveRegistrationException {
        Throwable cause = e;
        while ((cause = cause.getCause()) != null) {
            if (cause instanceof SaslException) {
                throw HostControllerLogger.ROOT_LOGGER.authenticationFailureUnableToConnect(cause);
            } else if (cause instanceof SSLHandshakeException) {
                throw HostControllerLogger.ROOT_LOGGER.sslFailureUnableToConnect(cause);
            } else if (cause instanceof SlaveRegistrationException) {
                throw (SlaveRegistrationException) cause;
            }
        }
    }

    /**
     * Handles logging tasks related to a failure to connect to a remote HC.
     * @param uri the URI at which the connection attempt was made. Can be {@code null} indicating a failure to discover the HC
     * @param discoveryOption the {@code DiscoveryOption} used to determine {@code uri}
     * @param moreOptions {@code true} if there are more untried discovery options
     * @param e the exception
     */
    static void logConnectionException(URI uri, DiscoveryOption discoveryOption, boolean moreOptions, Exception e) {
        if (uri == null) {
            HostControllerLogger.ROOT_LOGGER.failedDiscoveringMaster(discoveryOption, e);
        } else {
            HostControllerLogger.ROOT_LOGGER.cannotConnect(uri, e);
        }
        if (!moreOptions) {
            // All discovery options have been exhausted
            HostControllerLogger.ROOT_LOGGER.noDiscoveryOptionsLeft();
        }
    }

    private class GetFileRequest extends AbstractManagementRequest<File, Void> {
        private final byte rootId;
        private final String filePath;
        private final HostFileRepository localFileRepository;

        private GetFileRequest(final byte rootId, final String filePath, final HostFileRepository localFileRepository) {
            this.rootId = rootId;
            this.filePath = filePath;
            this.localFileRepository = localFileRepository;
        }

        @Override
        public byte getOperationType() {
            return DomainControllerProtocol.GET_FILE_REQUEST;
        }

        @Override
        protected void sendRequest(ActiveOperation.ResultHandler<File> resultHandler, ManagementRequestContext<Void> context, FlushableDataOutput output) throws IOException {
            output.write(DomainControllerProtocol.PARAM_HOST_ID);
            output.writeUTF(localHostInfo.getLocalHostName());
            DomainRemoteFileRequestAndHandler.INSTANCE.sendRequest(output, rootId, filePath);
        }

        @Override
        public void handleRequest(DataInput input, ActiveOperation.ResultHandler<File> resultHandler, ManagementRequestContext<Void> context) throws IOException {
            final File localPath;
            switch (rootId) {
                case DomainControllerProtocol.PARAM_ROOT_ID_FILE: {
                    localPath = localFileRepository.getFile(filePath);
                    break;
                }
                case DomainControllerProtocol.PARAM_ROOT_ID_CONFIGURATION: {
                    localPath = localFileRepository.getConfigurationFile(filePath);
                    break;
                }
                case DomainControllerProtocol.PARAM_ROOT_ID_DEPLOYMENT: {
                    byte[] hash = HashUtil.hexStringToByteArray(filePath);
                    localPath = localFileRepository.getDeploymentRoot(new ContentReference(filePath, hash));
                    break;
                }
                default: {
                    localPath = null;
                }
            }
            try {
                DomainRemoteFileRequestAndHandler.INSTANCE.handleResponse(input, localPath, ROOT_LOGGER, resultHandler, context);
            } catch (CannotCreateLocalDirectoryException e) {
                throw HostControllerLogger.ROOT_LOGGER.cannotCreateLocalDirectory(e.getDir());
            } catch (DidNotReadEntireFileException e) {
                throw HostControllerLogger.ROOT_LOGGER.didNotReadEntireFile(e.getMissing());
            }
        }
    }

    static class RemoteFileRepository implements HostFileRepository {
        private final HostFileRepository localFileRepository;
        private volatile RemoteFileRepositoryExecutor remoteFileRepositoryExecutor;

        RemoteFileRepository(final HostFileRepository localFileRepository) {
            this.localFileRepository = localFileRepository;
        }

        @Override
        public final File getFile(String relativePath) {
            return getFile(relativePath, DomainControllerProtocol.PARAM_ROOT_ID_FILE);
        }

        @Override
        public final File getConfigurationFile(String relativePath) {
            return getFile(relativePath, DomainControllerProtocol.PARAM_ROOT_ID_CONFIGURATION);
        }

        @Override
        public final File[] getDeploymentFiles(ContentReference reference) {
            final File root = getDeploymentRoot(reference);
            return root.listFiles();
        }

        @Override
        public File getDeploymentRoot(ContentReference reference) {
            File file = localFileRepository.getDeploymentRoot(reference);
            if(! file.exists()) {
                return getFile(reference.getHexHash(), DomainControllerProtocol.PARAM_ROOT_ID_DEPLOYMENT);
            }
            return file;
        }

        private File getFile(final String relativePath, final byte repoId) {
            return remoteFileRepositoryExecutor.getFile(relativePath, repoId, localFileRepository);
        }

        void setRemoteFileRepositoryExecutor(RemoteFileRepositoryExecutor remoteFileRepositoryExecutor) {
            this.remoteFileRepositoryExecutor = remoteFileRepositoryExecutor;
        }

        @Override
        public void deleteDeployment(ContentReference reference) {
            localFileRepository.deleteDeployment(reference);
        }
    }

    interface RemoteFileRepositoryExecutor {
        File getFile(final String relativePath, final byte repoId, HostFileRepository localFileRepository);
    }

    private final RemoteFileRepositoryExecutor remoteFileRepositoryExecutor = new RemoteFileRepositoryExecutor() {
        public File getFile(final String relativePath, final byte repoId, HostFileRepository localFileRepository) {
            if(connection.isConnected()) {
                try {
                    return handler.executeRequest(new GetFileRequest(repoId, relativePath, localFileRepository), null).getResult().get();
                } catch (Exception e) {
                    throw HostControllerLogger.ROOT_LOGGER.failedToGetFileFromRemoteRepository(e);
                }
            } else {
                return localFileRepository.getFile(relativePath);
            }
        }
    };

    private void setupHandler() {
        // Setup the transaction protocol handler
        handler.addHandlerFactory(new TransactionalProtocolOperationHandler(controller, handler, responseAttachmentSupport));
        // Use the existing channel strategy
        masterProxy = ExistingChannelModelControllerClient.createAndAdd(handler);
        txMasterProxy = TransactionalProtocolHandlers.createClient(handler);
    }

    private class FutureClient extends AsyncFutureTask<MasterDomainControllerClient>{

        protected FutureClient() {
            super(null);
        }

        private void setClient(MasterDomainControllerClient client) {
            super.setResult(client);
        }
    }

    private static int getSystemProperty(final String name, final int defaultValue) {
        final String value = WildFlySecurityManager.getPropertyPrivileged(name, null);
        try {
            return value == null ? defaultValue : Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private static class ReadRootResourceHandler implements OperationStepHandler {
        private Resource resource;

        static Resource grabDomainResource(HostControllerRegistrationHandler.OperationExecutor executor) {
            ReadRootResourceHandler handler = new ReadRootResourceHandler();
            executor.execute(GRAB_DOMAIN_RESOURCE, OperationMessageHandler.DISCARD, ModelController.OperationTransactionControl.COMMIT, handler);
            return handler.resource;
        }

        @Override
        public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
            resource = context.readResourceFromRoot(PathAddress.EMPTY_ADDRESS);
        }
    }

}