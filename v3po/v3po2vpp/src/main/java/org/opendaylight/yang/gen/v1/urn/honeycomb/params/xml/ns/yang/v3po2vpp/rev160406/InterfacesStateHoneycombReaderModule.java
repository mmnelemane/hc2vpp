package org.opendaylight.yang.gen.v1.urn.honeycomb.params.xml.ns.yang.v3po2vpp.rev160406;


import static io.fd.honeycomb.v3po.translate.util.RWUtils.emptyAugReaderList;
import static io.fd.honeycomb.v3po.translate.util.RWUtils.emptyChildReaderList;
import static io.fd.honeycomb.v3po.translate.util.RWUtils.singletonAugReaderList;
import static io.fd.honeycomb.v3po.translate.util.RWUtils.singletonChildReaderList;

import io.fd.honeycomb.v3po.translate.impl.read.CompositeChildReader;
import io.fd.honeycomb.v3po.translate.impl.read.CompositeListReader;
import io.fd.honeycomb.v3po.translate.impl.read.CompositeRootReader;
import io.fd.honeycomb.v3po.translate.read.ChildReader;
import io.fd.honeycomb.v3po.translate.util.read.CloseableReader;
import io.fd.honeycomb.v3po.translate.util.read.ReflexiveRootReaderCustomizer;
import io.fd.honeycomb.v3po.translate.v3po.interfacesstate.InterfaceCustomizer;
import io.fd.honeycomb.v3po.translate.v3po.interfacesstate.VppInterfaceStateCustomizer;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfacesState;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfacesStateBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces.state.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces.state.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces.state.InterfaceKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.v3po.rev150105.VppInterfaceStateAugmentation;

public class InterfacesStateHoneycombReaderModule extends
    org.opendaylight.yang.gen.v1.urn.honeycomb.params.xml.ns.yang.v3po2vpp.rev160406.AbstractInterfacesStateHoneycombReaderModule {
    public InterfacesStateHoneycombReaderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier,
                                                org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public InterfacesStateHoneycombReaderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier,
                                                org.opendaylight.controller.config.api.DependencyResolver dependencyResolver,
                                                org.opendaylight.yang.gen.v1.urn.honeycomb.params.xml.ns.yang.v3po2vpp.rev160406.InterfacesStateHoneycombReaderModule oldModule,
                                                java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {

        final ChildReader<VppInterfaceStateAugmentation> vppInterfaceStateAugmentationChildReader =
            new CompositeChildReader<>(VppInterfaceStateAugmentation.class,
                new VppInterfaceStateCustomizer(getVppJvppDependency()));


        final CompositeListReader<Interface, InterfaceKey, InterfaceBuilder> interfaceReader =
            new CompositeListReader<>(Interface.class,
                emptyChildReaderList(),
                singletonAugReaderList(vppInterfaceStateAugmentationChildReader),
                new InterfaceCustomizer(getVppJvppDependency(), getInterfaceContextIfcStateDependency()));

        return new CloseableReader<>(new CompositeRootReader<>(
            InterfacesState.class,
            singletonChildReaderList(interfaceReader),
            emptyAugReaderList(),
            new ReflexiveRootReaderCustomizer<>(InterfacesStateBuilder.class)));
    }
}