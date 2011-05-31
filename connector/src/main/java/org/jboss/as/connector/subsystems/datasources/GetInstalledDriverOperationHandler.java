/**
 *
 */
package org.jboss.as.connector.subsystems.datasources;

import org.jboss.as.connector.ConnectorServices;
import org.jboss.as.connector.registry.DriverRegistry;
import org.jboss.as.connector.registry.InstalledDriver;
import org.jboss.as.controller.NewOperationContext;
import org.jboss.as.controller.NewStepHandler;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.operations.validation.ParametersValidator;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;

import static org.jboss.as.connector.subsystems.datasources.Constants.DEPLOYMENT_NAME;
import static org.jboss.as.connector.subsystems.datasources.Constants.DRIVER_CLASS_NAME;
import static org.jboss.as.connector.subsystems.datasources.Constants.DRIVER_MAJOR_VERSION;
import static org.jboss.as.connector.subsystems.datasources.Constants.DRIVER_MINOR_VERSION;
import static org.jboss.as.connector.subsystems.datasources.Constants.DRIVER_MODULE_NAME;
import static org.jboss.as.connector.subsystems.datasources.Constants.DRIVER_NAME;
import static org.jboss.as.connector.subsystems.datasources.Constants.DRIVER_XA_DATASOURCE_CLASS_NAME;
import static org.jboss.as.connector.subsystems.datasources.Constants.JDBC_COMPLIANT;
import static org.jboss.as.connector.subsystems.datasources.Constants.MODULE_SLOT;

/**
 * Reads the "installed-drivers" attribute.
 * @author Brian Stansberry (c) 2011 Red Hat Inc.
 */
public class GetInstalledDriverOperationHandler implements NewStepHandler {

    public static final GetInstalledDriverOperationHandler INSTANCE = new GetInstalledDriverOperationHandler();

    private final ParametersValidator validator = new ParametersValidator();

    private GetInstalledDriverOperationHandler() {
        validator.registerValidator(DRIVER_NAME, new StringLengthValidator(1));
    }

    @Override
    public void execute(final NewOperationContext context, final ModelNode operation)
            throws OperationFailedException {

        validator.validate(operation);

        final String name = operation.require(DRIVER_NAME).asString();
        if (context.getType() == NewOperationContext.Type.SERVER) {
            context.addStep(new NewStepHandler() {

                @Override
                public void execute(final NewOperationContext context, final ModelNode operation) throws OperationFailedException {
                    ServiceController<?> sc = context.getServiceRegistry(false).getRequiredService(
                            ConnectorServices.JDBC_DRIVER_REGISTRY_SERVICE);
                    DriverRegistry driverRegistry = DriverRegistry.class.cast(sc.getValue());
                    ModelNode result = new ModelNode();
                    InstalledDriver driver = driverRegistry.getInstalledDriver(name);
                    ModelNode driverNode = new ModelNode();
                    driverNode.get(DRIVER_NAME).set(driver.getDriverName());
                    if (driver.isFromDeployment()) {
                        driverNode.get(DEPLOYMENT_NAME).set(driver.getDriverName());
                        driverNode.get(DRIVER_MODULE_NAME);
                        driverNode.get(MODULE_SLOT);
                        driverNode.get(DRIVER_XA_DATASOURCE_CLASS_NAME);
                    } else {
                        driverNode.get(DEPLOYMENT_NAME);
                        driverNode.get(DRIVER_MODULE_NAME).set(driver.getModuleName().getName());
                        driverNode.get(MODULE_SLOT).set(driver.getModuleName().getSlot());
                        driverNode.get(DRIVER_XA_DATASOURCE_CLASS_NAME).set(driver.getXaDataSourceClassName());

                    }
                    driverNode.get(DRIVER_CLASS_NAME).set(driver.getDriverClassName());
                    driverNode.get(DRIVER_MAJOR_VERSION).set(driver.getMajorVersion());
                    driverNode.get(DRIVER_MINOR_VERSION).set(driver.getMinorVersion());
                    driverNode.get(JDBC_COMPLIANT).set(driver.isJdbcCompliant());
                    result.add(driverNode);

                    context.getResult().set(result);
                    context.completeStep();
                }
            }, NewOperationContext.Stage.RUNTIME);
        }

        context.completeStep();
    }
}
